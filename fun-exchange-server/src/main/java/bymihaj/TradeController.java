package bymihaj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;

// TODO change bank, add reservation and etc
// TODO create Map<Double, List<LimitOrderResponse>> as class
// TODO think about dual operation interface (sell/buy)
// TODO check in real concurrency way with emulation of long execution
public class TradeController {

	static Logger log = LoggerFactory.getLogger(TradeController.class);
	
    protected AtomicLong counter = new AtomicLong(1);
    protected Map<LimitOrderResponse, User> orderOfUser;
    protected Map<Double, List<LimitOrderResponse>> sellPool;
    protected Map<Double, List<LimitOrderResponse>> buyPool;
    
    public TradeController() {
    	orderOfUser = new ConcurrentHashMap<>();
        sellPool = new ConcurrentHashMap<>();
        buyPool = new ConcurrentHashMap<>();
    }
    
    public void onMarketOrder(User user, MarketOrderRequest moReq) {
        Map<Double, List<LimitOrderResponse>> pool;
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
            user.send(new RejectOrderResponse(rejectText));
        } else {
        	if(OrderSide.BUY.equals(order.getSide())) {
                user.descrease(order.getInstrument().getSecondary(), order.getAmount());
        	} else {
        		user.descrease(order.getInstrument().getPrimary(), order.getAmount());
        	}
        	
        	marketExecution(order, pool, user);
        	user.send(order);
        	user.send(new AssetsResponse(user.getBank().getProperties()));
        }
    }
    
    public void onLimitOrder(User user, LimitOrderRequest loReq) {
        // TODO
        
        // validation
        
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
        
        
        // fill as market order on sliced market
        Map<Double, List<LimitOrderResponse>> pool;
        if(resp.getSide().equals(OrderSide.BUY)) {
        	pool = sellPool.entrySet().stream()
        			.filter( p -> p.getKey().doubleValue() <= resp.getPrice())
        			.collect(Collectors.toMap( k -> k.getKey(), v -> v.getValue()));
        } else {
        	pool = buyPool.entrySet().stream()
        			.filter( p -> p.getKey().doubleValue() >= resp.getPrice())
        			.collect(Collectors.toMap( k -> k.getKey(), v -> v.getValue()));;
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
            orderOfUser.get(lor).descrease(lor.getInstrument().getSecondary(), lor.getAmount());
    	} else {
    		pool = sellPool;
    		orderOfUser.get(lor).descrease(lor.getInstrument().getPrimary(), lor.getAmount());
    	}
    	
    	if(!pool.containsKey(lor.getPrice())) {
    		pool.put(lor.getPrice(), new ArrayList<>());
        }
        pool.get(lor.getPrice()).add(lor);
    }
    
    protected void marketExecution(MarketOrderResponse order, Map<Double, List<LimitOrderResponse>> market, User initUser) {
    	List<Double> prices = new ArrayList<>(market.keySet());
    	Collections.sort(prices);
    	if(OrderSide.SELL.equals(order.getSide())) {
    		Collections.reverse(prices);
    	}
    	
    	for(Double priceLevel : prices) {
    		List<LimitOrderResponse> toRemove = new ArrayList<>();
    		for(LimitOrderResponse liq : market.get(priceLevel)) {
    			BigDecimal filled = new BigDecimal(Math.min(order.getRequiredAmount(), liq.getRequiredAmount()));
    			order.setFilledAmount(order.getFilledAmount() + filled.doubleValue());
    			
    			log.info("Match amount {} for #{} {} and #{} {}", filled.toPlainString(), order.getId(), order.getSide(), liq.getId(), liq.getSide());
    			
    			liq.setFilledAmount(liq.getFilledAmount() + filled.doubleValue());
    			User liqudityPrivider = orderOfUser.get(liq);
    			if(OrderSide.BUY.equals(liq.getSide()) ) {
    				initUser.increase(liq.getInstrument().getSecondary(), filled.doubleValue());
    				liqudityPrivider.increase(liq.getInstrument().getPrimary(), filled.doubleValue());
    			} else {
    				initUser.increase(liq.getInstrument().getPrimary(), filled.doubleValue());
    				liqudityPrivider.increase(liq.getInstrument().getSecondary(), filled.doubleValue());
    			}
    			liqudityPrivider.send(liq);
    			liqudityPrivider.send(new AssetsResponse(liqudityPrivider.getBank().getProperties()));
    			
    			if(liq.getRequiredAmount() == 0.0) {
    				toRemove.add(liq);
    			}
    			
    			if(order.getRequiredAmount() == 0.0) {
    				break;
    			}
    		}
    		
    		market.get(priceLevel).removeAll(toRemove);
    		for(LimitOrderResponse liq : toRemove) {
    			orderOfUser.get(liq).removeOrder(new Long(liq.getId()));
    		}
    		
    		if(order.getRequiredAmount() == 0.0) {
				break;
			}
    		
    		
    	}
    }
}
