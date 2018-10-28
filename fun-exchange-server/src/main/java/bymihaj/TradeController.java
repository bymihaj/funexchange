package bymihaj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.data.order.CancelOrderRequest;
import bymihaj.data.order.CancelOrderResponse;
import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;
import bymihaj.data.order.RejectOrderType;

// TODO change bank, add reservation and etc
// TODO create Map<Double, List<LimitOrderResponse>> as class
// TODO think about dual operation interface (sell/buy)
// TODO check in real concurrency way with emulation of long execution
public class TradeController {

	static Logger log = LoggerFactory.getLogger(TradeController.class);
	
	public final static int LEVEL_SIZE = 20;
	
    protected AtomicLong counter = new AtomicLong(1);
    protected Map<LimitOrderResponse, User> orderOfUser;
    protected ConcurrentSkipListMap<Double, List<LimitOrderResponse>> sellPool;
    protected ConcurrentSkipListMap<Double, List<LimitOrderResponse>> buyPool;
    protected LoginController loginController;
    
    public TradeController(LoginController loginController) {
    	this.loginController = loginController;
        orderOfUser = new ConcurrentHashMap<>();
        sellPool = new ConcurrentSkipListMap<>();
        buyPool = new ConcurrentSkipListMap<>();
    }
    
    public void onMarketOrder(User user, MarketOrderRequest moReq) {
        
        // TODO remove as no sense for market order
        if(!hasAsset(user, moReq)) {
            return;
        }
        
        SortedMap<Double, List<LimitOrderResponse>> pool;
        if(moReq.getSide().equals(OrderSide.BUY)) {
        	pool = sellPool;
        } else {
        	pool = buyPool;
        }
        
        
        
        MarketOrderResponse order = new MarketOrderResponse();
        order.setInstrument(moReq.getInstrument());
        order.setAmount(moReq.getAmount());
        order.setSide(moReq.getSide());
        order.setId(counter.getAndIncrement());
        if(pool.isEmpty()) {
        	String rejectText = "No liqudity for " + moReq.getSide() + " " + moReq.getAmount() + "on market";
        	RejectOrderResponse reject = new RejectOrderResponse(rejectText);
        	reject.setRejectType(RejectOrderType.NO_LIQUIDITY);
            user.send(reject);
        } else {
        	marketExecution(order, pool, user);
        	
        	if(OrderSide.BUY.equals(order.getSide())) {
        	    BigDecimal dec = BigDecimal.valueOf(order.getFilledAmount()).multiply(BigDecimal.valueOf( order.getFilledPrice()));
                user.descrease(order.getInstrument().getSecondary(), dec.doubleValue());
            } else {
                user.descrease(order.getInstrument().getPrimary(), order.getFilledAmount());
            }
        	
        	user.send(order);
        	user.send(new AssetsResponse(user.getBank().getProperties()));
        	
        	broadcastOrderBook();
        }
    }
    
    public void onLimitOrder(User user, LimitOrderRequest loReq) {
        // TODO
        
        // validation
        if(!hasAsset(user, loReq)) {
            return;
        }
        
        // generate id
        // save by 
        //1.id-order ( update or cancel order by id)
        //2.user-order ( get status of orders for user ) 
        // ==> order collection on user side
        
        //3.liquidity-list<order> (matching) !important
        //4.order-user ( sending result to user) 
        //  !important 
        //  ? replace by link 
        
        
        //5. try to partial fill be place to pool
        
        LimitOrderResponse resp = new LimitOrderResponse();
        resp.setInstrument(loReq.getInstrument());
        resp.setSide(loReq.getSide());
        resp.setPrice(loReq.getPrice());
        resp.setAmount(loReq.getAmount());
        resp.setFilledAmount(0);
        resp.setId(counter.getAndIncrement());
        
        
        user.addOrder(resp.getId(), resp);
        orderOfUser.put(resp, user);
        user.send(resp);
        
        // reserve assets
        if(OrderSide.BUY.equals(resp.getSide())) {
            orderOfUser.get(resp).descrease(resp.getInstrument().getSecondary(), resp.getRequiredAmount() * resp.getPrice());
        } else {
            orderOfUser.get(resp).descrease(resp.getInstrument().getPrimary(), resp.getRequiredAmount());
        }
        user.send(new AssetsResponse(user.getBank().getProperties()));
        
        // fill as market order on sliced market
        SortedMap<Double, List<LimitOrderResponse>> pool;
        if(resp.getSide().equals(OrderSide.BUY)) {
            pool = sellPool.subMap(Double.MIN_VALUE, true, resp.getPrice(), true);
        } else {
            pool = buyPool.subMap(resp.getPrice(), Double.MAX_VALUE);
        }
        if(!pool.isEmpty()) {
        	marketExecution(resp, pool, user);
        }
        
        // send result of partial fill on market
        if(resp.getFilledAmount() > 0.0) {
        	user.send(resp);
        }
        
        if(resp.getRequiredAmount() > 0) {
        	addToPool(resp);
        }
        
        user.send(new AssetsResponse(user.getBank().getProperties()));
        
        broadcastOrderBook();
    }
    
    public void onOrderStatusRequest(User user, OrderStatusRequest statRequest) {
    	OrderStatusResponse resp = new OrderStatusResponse();
    	resp.getOrders().addAll(user.getAllOrders());
    	user.send(resp);
    }
    
    // TODO check in junit rounding price!!!!
    protected void addToPool(LimitOrderResponse lor) {
    	Map<Double, List<LimitOrderResponse>> pool;
    	if(OrderSide.BUY.equals(lor.getSide())) {
            pool = buyPool;
        } else {
    		pool = sellPool;
    	}
    	
    	if(!pool.containsKey(lor.getPrice())) {
    		pool.put(lor.getPrice(), new ArrayList<>());
        }
        pool.get(lor.getPrice()).add(lor);
    }
    
    protected void marketExecution(MarketOrderResponse order, SortedMap<Double, List<LimitOrderResponse>> market, User initUser) {
    	List<Double> prices = new ArrayList<>(market.keySet());
    	Collections.sort(prices);
    	if(OrderSide.SELL.equals(order.getSide())) {
    		Collections.reverse(prices);
    	}
    	
    	for(Double priceLevel : prices) {
    		List<LimitOrderResponse> toRemove = new ArrayList<>();
    		for(LimitOrderResponse liq : market.get(priceLevel)) {
    			BigDecimal filled = BigDecimal.valueOf(order.getRequiredAmount()).min(BigDecimal.valueOf(liq.getRequiredAmount()));
    		    order.setFilledAmount(BigDecimal.valueOf(order.getFilledAmount()).add(filled).doubleValue());
    			order.setFilledPrice(priceLevel);
    			
    			log.info("Match amount {} for #{} {} and #{} {}", filled.toPlainString(), order.getId(), order.getSide(), liq.getId(), liq.getSide());
    			
    			liq.setFilledAmount(BigDecimal.valueOf(liq.getFilledAmount()).add(filled).doubleValue());
    			liq.setFilledPrice(priceLevel);
    			User liqudityPrivider = orderOfUser.get(liq);
    			if(OrderSide.BUY.equals(liq.getSide()) ) {
    			    BigDecimal initInc = BigDecimal.valueOf(filled.doubleValue()).multiply(BigDecimal.valueOf(priceLevel.doubleValue()));
    				initUser.increase(liq.getInstrument().getSecondary(), initInc.doubleValue());
    				liqudityPrivider.increase(liq.getInstrument().getPrimary(), filled.doubleValue() );
    			} else {
    				initUser.increase(liq.getInstrument().getPrimary(), filled.doubleValue());
    				BigDecimal liqInc = BigDecimal.valueOf(filled.doubleValue()).multiply(BigDecimal.valueOf(priceLevel.doubleValue()));
    				liqudityPrivider.increase(liq.getInstrument().getSecondary(), liqInc.doubleValue());
    			}
    			liqudityPrivider.send(liq);
    			liqudityPrivider.send(new AssetsResponse(liqudityPrivider.getBank().getProperties()));
    			
    			TradeHistory history = new TradeHistory();
    			history.setDateTime(new Date().toString());
    			history.setAmount(filled.doubleValue());
    			history.setPrice(priceLevel.doubleValue());
    			history.setSide(order.getSide());
    			for(User u : loginController.getAllLoginedUser()) {
    			    u.send(history);
    			}
    			
    			if(liq.getRequiredAmount() == 0.0) {
    				toRemove.add(liq);
    			}
    			
    			if(order.getRequiredAmount() == 0.0) {
    				break;
    			}
    		}
    		
    		market.get(priceLevel).removeAll(toRemove);
    		if(market.get(priceLevel).isEmpty()) {
    		    market.remove(priceLevel);
    		}
    		
    		for(LimitOrderResponse liq : toRemove) {
    			orderOfUser.get(liq).removeOrder(new Long(liq.getId()));
    		}
    		
    		if(order.getRequiredAmount() == 0.0) {
				break;
			}
    		
    		
    	}
    }
    
    protected boolean hasAsset(User user, MarketOrderRequest order) {
        Instrument instrument = order.getInstrument();
        Map<Symbol,Property> properties = user.getBank().getProperties();
        if(OrderSide.BUY.equals(order.getSide())) {
            if(properties.get(instrument.getSecondary()).getAmount().doubleValue() < order.getAmount()) {
                String text = "No asset "+instrument.getSecondary();
                RejectOrderResponse reject = new RejectOrderResponse(text);
                reject.setRejectType(RejectOrderType.NO_ASSET);
                user.send(reject);
                return false;
            }
        } else {
            if(properties.get(instrument.getPrimary()).getAmount().doubleValue() < order.getAmount()) {
                String text = "No asset "+instrument.getPrimary();
                RejectOrderResponse reject = new RejectOrderResponse(text);
                reject.setRejectType(RejectOrderType.NO_ASSET);
                user.send(reject);
                return false;
            }
        }
        return true;
    }
    
    public void onCancelOrderRequest(User user, CancelOrderRequest cancel) {
        Optional<LimitOrderResponse> optionalOrder =user.getAllOrders().stream().filter( p -> p.getId() == cancel.getId()).findFirst();
        if(!optionalOrder.isPresent()) {
            RejectOrderResponse reject = new RejectOrderResponse("Order #"+cancel.getId()+" not found");
            reject.setRejectType(RejectOrderType.NO_ID);
            user.send(reject);
            return;
        } else {
            LimitOrderResponse order = optionalOrder.get();
            user.removeOrder(order.getId());
            orderOfUser.remove(order);
            double priceLevel = order.getPrice();
            if(OrderSide.BUY.equals(order.getSide())) {
                buyPool.get(priceLevel).remove(order);
                if(buyPool.get(priceLevel).isEmpty()) {
                    buyPool.remove(priceLevel);
                }
            } else {
                sellPool.get(order.getPrice()).remove(order);
                if(sellPool.get(priceLevel).isEmpty()) {
                    sellPool.remove(priceLevel);
                }
            }
            
            CancelOrderResponse response = new CancelOrderResponse();
            response.setId(cancel.getId());
            user.send(response);
            
            OrderStatusResponse resp = new OrderStatusResponse();
            resp.getOrders().addAll(user.getAllOrders());
            user.send(resp);
            
            
            // undo assets reservation
            if(OrderSide.BUY.equals(order.getSide())) {
                user.increase(order.getInstrument().getSecondary(), order.getRequiredAmount() * order.getPrice());
            } else {
                user.increase(order.getInstrument().getPrimary(), order.getRequiredAmount());
            }
            user.send(new AssetsResponse(user.getBank().getProperties()));
            
            broadcastOrderBook();
        }
        
    }
    
    protected OrderBook packOrderBook() {
        OrderBook book = new OrderBook();
        
        if(!buyPool.isEmpty()) {
            List<Double> buyeLevels = new ArrayList<>(buyPool.keySet());
            Collections.sort(buyeLevels);
            Collections.reverse(buyeLevels);
            buyeLevels = buyeLevels.subList(0, Math.min(LEVEL_SIZE, buyeLevels.size()));
            for(Double level : buyeLevels) {
                double amount = 0;
                for(LimitOrderResponse order : buyPool.get(level)) {
                    amount = amount + order.getRequiredAmount();
                }
                book.getBuyLevels().put(level, amount);
            }
        }
        
        if(!sellPool.isEmpty()) {
            List<Double> sellLevels = new ArrayList<>(sellPool.keySet());
            Collections.sort(sellLevels);
            
            sellLevels = sellLevels.subList(0, Math.min(LEVEL_SIZE, sellLevels.size()));
            for(Double level : sellLevels) {
                double amount = 0;
                for(LimitOrderResponse order : sellPool.get(level)) {
                    amount = amount + order.getRequiredAmount();
                }
                book.getSellLevels().put(level, amount);
            }
        }
        
        return book;
    }
    
    
    public void broadcastOrderBook() {
        OrderBook book = packOrderBook();
        for(User user : loginController.getAllLoginedUser()) {
            user.send(book);
        }
    }
    
    public void onOrderBookRequest(User user, OrderBookRequest request) {
        OrderBook book = packOrderBook();
        user.send(book);
    }
}
