package bymihaj;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.java_websocket.server.WebSocketServer;
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
import bymihaj.data.order.Trade;
import bymihaj.jvm.GsonParser;

public class TradeController {

    static Logger log = LoggerFactory.getLogger(TradeController.class);

    public final static int LEVEL_SIZE = 20;

    protected AtomicLong counter = new AtomicLong(1);
    protected AtomicLong tidCounter = new AtomicLong(1);
    protected Map<LimitOrderResponse, User> orderOfUser;
    protected ConcurrentSkipListMap<Double, List<LimitOrderResponse>> sellPool;
    protected ConcurrentSkipListMap<Double, List<LimitOrderResponse>> buyPool;
    protected LoginController loginController;
    protected MessageResolver resolver;
    protected ThreadPoolExecutor executor;
    
    public TradeController(LoginController loginController) {
        this.loginController = loginController;
        orderOfUser = new ConcurrentHashMap<>();
        sellPool = new ConcurrentSkipListMap<>();
        buyPool = new ConcurrentSkipListMap<>();
        resolver = new MessageResolver(new GsonParser());
        
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue);
    }

    public void onMarketOrder(User user, MarketOrderRequest moReq) {

        RejectOrderResponse reject = null;
        
        reject = checkAndRoundAmount(moReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        reject = checkInstrument(moReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        reject = checkSide(moReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        
        log.info("Queue size: {}", executor.getQueue().size());
        executor.submit(() -> {
            safeMarket(user, moReq);
        });
        
    }
    
    protected void safeMarket(User user, MarketOrderRequest request) {
        
        if (!hasAsset(user, request)) {
            return;
        }
        
        SortedMap<Double, List<LimitOrderResponse>> pool;
        if (request.getSide().equals(OrderSide.BUY)) {
            pool = sellPool;
        } else {
            pool = buyPool;
        }

        MarketOrderResponse order = new MarketOrderResponse();
        order.setInstrument(request.getInstrument());
        order.setAmount(request.getAmount());
        order.setSide(request.getSide());
        order.setId(counter.getAndIncrement());
        if (pool.isEmpty()) {
            String rejectText = "No liqudity for " + request.getSide() + " " + request.getAmount() + "on market";
            RejectOrderResponse reject = new RejectOrderResponse(rejectText);
            reject.setRejectType(RejectOrderType.NO_LIQUIDITY);
            user.send(reject);
        } else {
            marketExecution(order, pool, user);
            user.send(order);
            user.sendAssests();
            broadcastOrderBook();
        }
    }

    public void onLimitOrder(User user, LimitOrderRequest loReq) {
        RejectOrderResponse reject = null;
        
        reject = checkAndRoundAmount(loReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        reject = checkAndRoundPrice(loReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        reject = checkInstrument(loReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        reject = checkSide(loReq);
        if(reject != null) {
            user.send(reject);
            return;
        }
        
        log.info("Queue size: {}", executor.getQueue().size());
        executor.submit(() -> {
            safeLimit(user, loReq);
        });
        
    }
    
    protected void safeLimit(User user, LimitOrderRequest loReq) {
        
        // validation
        if (!hasAsset(user, loReq)) {
            return;
        }

        LimitOrderResponse resp = new LimitOrderResponse();
        resp.setInstrument(loReq.getInstrument());
        resp.setSide(loReq.getSide());
        resp.setPrice(loReq.getPrice());
        resp.setAmount(loReq.getAmount());
        resp.setId(counter.getAndIncrement());

        orderOfUser.put(resp, user);
        user.send(resp);

        // fill as market order on sliced market
        SortedMap<Double, List<LimitOrderResponse>> pool;
        if (resp.getSide().equals(OrderSide.BUY)) {
            pool = sellPool.subMap(Double.MIN_VALUE, true, resp.getPrice(), true);
        } else {
            pool = buyPool.subMap(resp.getPrice(), Double.MAX_VALUE);
        }
        if (!pool.isEmpty()) {
            marketExecution(resp, pool, user);
        }

        // send result of partial fill on market
        if (resp.getFilledAmount() > 0.0) {
            user.send(resp);
        }

        if (resp.getRequiredAmount() > 0) {

            double maxPossible = 0;
            if (OrderSide.BUY == resp.getSide()) {
                maxPossible = user.getFreeAsset(Symbol.MON).doubleValue() / resp.getPrice();
            } else {
                maxPossible = user.getFreeAsset(Symbol.STK).doubleValue();
            }

            if (resp.getRequiredAmount() > maxPossible) {
                RejectOrderResponse reject = new RejectOrderResponse("No assets");
                reject.setRejectType(RejectOrderType.NO_ASSET);
                user.send(reject);
            } else {
                user.addOrder(resp.getId(), resp);
                addToPool(resp);
            }
        }

        user.sendAssests();

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
        if (OrderSide.BUY.equals(lor.getSide())) {
            pool = buyPool;
        } else {
            pool = sellPool;
        }

        if (!pool.containsKey(lor.getPrice())) {
            pool.put(lor.getPrice(), new ArrayList<>());
        }
        pool.get(lor.getPrice()).add(lor);
    }

    protected void marketExecution(MarketOrderResponse order, SortedMap<Double, List<LimitOrderResponse>> market,
            User initUser) {
        List<Double> prices = new ArrayList<>(market.keySet());
        Collections.sort(prices);
        if (OrderSide.SELL.equals(order.getSide())) {
            Collections.reverse(prices);
        }

        for (Double priceLevel : prices) {
            List<LimitOrderResponse> toRemove = new ArrayList<>();
            for (LimitOrderResponse liq : market.get(priceLevel)) {
                BigDecimal filled = BigDecimal.valueOf(order.getRequiredAmount())
                        .min(BigDecimal.valueOf(liq.getRequiredAmount()));

                User liqudityPrivider = orderOfUser.get(liq);
                if (OrderSide.BUY.equals(liq.getSide())) {
                    BigDecimal readyToSell = initUser.getFreeAsset(order.getInstrument().getPrimary());
                    filled = filled.min(readyToSell);

                    BigDecimal initInc = BigDecimal.valueOf(filled.doubleValue())
                            .multiply(BigDecimal.valueOf(priceLevel.doubleValue()));
                    initInc = initInc.setScale(Symbol.MON.getCoin().scale(), RoundingMode.HALF_DOWN);
                    initUser.increase(liq.getInstrument().getSecondary(), initInc.doubleValue());
                    liqudityPrivider.descrease(liq.getInstrument().getSecondary(), initInc.doubleValue());
                    liqudityPrivider.increase(liq.getInstrument().getPrimary(), filled.doubleValue());
                    initUser.descrease(liq.getInstrument().getPrimary(), filled.doubleValue());

                } else {
                    BigDecimal readyToBuy = initUser.getFreeAsset(order.getInstrument().getSecondary())
                            .divide(BigDecimal.valueOf(priceLevel), MathContext.DECIMAL64);
                    readyToBuy = readyToBuy.setScale(order.getInstrument().getSecondary().getCoin().scale(), RoundingMode.FLOOR);
                    filled = filled.min(readyToBuy);

                    BigDecimal liqInc = BigDecimal.valueOf(filled.doubleValue())
                            .multiply(BigDecimal.valueOf(priceLevel.doubleValue()));
                    liqInc = liqInc.setScale(Symbol.STK.getCoin().scale(), RoundingMode.HALF_DOWN);
                    
                    initUser.increase(liq.getInstrument().getPrimary(), filled.doubleValue());
                    liqudityPrivider.descrease(liq.getInstrument().getPrimary(), filled.doubleValue());
                    liqudityPrivider.increase(liq.getInstrument().getSecondary(), liqInc.doubleValue());
                    initUser.descrease(liq.getInstrument().getSecondary(), liqInc.doubleValue());
                }

                Trade clienttrade = new Trade(tidCounter.getAndIncrement(), filled.doubleValue(), priceLevel);
                order.addTrade(clienttrade);
                Trade liqudityTrade = new Trade(tidCounter.getAndIncrement(), filled.doubleValue(), priceLevel);
                liq.addTrade(liqudityTrade);
                log.info("Match amount {} for #{} {} and #{} {}", filled.toPlainString(), order.getId(),
                        order.getSide(), liq.getId(), liq.getSide());

                liqudityPrivider.send(liq);
                liqudityPrivider.sendAssests();

                TradeHistory history = new TradeHistory();
                history.setDateTime(new Date().toString());
                history.setAmount(filled.doubleValue());
                history.setPrice(priceLevel.doubleValue());
                history.setSide(order.getSide());
                broadcastMessage(history);

                if (liq.getRequiredAmount() == 0.0) {
                    toRemove.add(liq);
                }

                if (order.getRequiredAmount() == 0.0) {
                    break;
                }
            }

            market.get(priceLevel).removeAll(toRemove);
            /*
            StringBuffer sb = new StringBuffer();
            toRemove.forEach( l -> sb.append(l.getId()));
            log.info("Level {} remove orders {}", priceLevel, sb );
            */
            if (market.get(priceLevel).isEmpty()) {
                market.remove(priceLevel);
            }

            for (LimitOrderResponse liq : toRemove) {
                orderOfUser.get(liq).removeOrder(new Long(liq.getId()));
            }

            if (order.getRequiredAmount() == 0.0) {
                break;
            }

        }
    }

    protected boolean hasAsset(User user, MarketOrderRequest order) {
        Instrument instrument = order.getInstrument();
        Map<Symbol, Property> properties = user.getBank().getProperties();
        if (OrderSide.BUY.equals(order.getSide())) {
            if (properties.get(instrument.getSecondary()).getAmount().doubleValue() < order.getAmount()) {
                String text = "No asset " + instrument.getSecondary();
                RejectOrderResponse reject = new RejectOrderResponse(text);
                reject.setRejectType(RejectOrderType.NO_ASSET);
                user.send(reject);
                return false;
            }
        } else {
            if (properties.get(instrument.getPrimary()).getAmount().doubleValue() < order.getAmount()) {
                String text = "No asset " + instrument.getPrimary();
                RejectOrderResponse reject = new RejectOrderResponse(text);
                reject.setRejectType(RejectOrderType.NO_ASSET);
                user.send(reject);
                return false;
            }
        }
        return true;
    }

    public void onCancelOrderRequest(User user, CancelOrderRequest cancel) {
        Optional<LimitOrderResponse> optionalOrder = user.getAllOrders().stream()
                .filter(p -> p.getId() == cancel.getId()).findFirst();
        if (!optionalOrder.isPresent()) {
            RejectOrderResponse reject = new RejectOrderResponse("Order #" + cancel.getId() + " not found");
            reject.setRejectType(RejectOrderType.NO_ID);
            user.send(reject);
            return;
        } else {
            log.info("Queue size: {}", executor.getQueue().size());
            executor.submit(() -> {
                safeCancel(user, optionalOrder.get());
            });
        }

    }
    
    protected void safeCancel(User user, LimitOrderResponse cancel) {
        LimitOrderResponse order = cancel;
        user.removeOrder(order.getId());
        orderOfUser.remove(order);
        double priceLevel = order.getPrice();
        if (OrderSide.BUY.equals(order.getSide())) {
            buyPool.get(priceLevel).remove(order);
            if (buyPool.get(priceLevel).isEmpty()) {
                buyPool.remove(priceLevel);
            }
        } else {
            sellPool.get(order.getPrice()).remove(order);
            if (sellPool.get(priceLevel).isEmpty()) {
                sellPool.remove(priceLevel);
            }
        }

        CancelOrderResponse response = new CancelOrderResponse();
        response.setId(cancel.getId());
        user.send(response);

        OrderStatusResponse resp = new OrderStatusResponse();
        resp.getOrders().addAll(user.getAllOrders());
        user.send(resp);
        user.sendAssests();
        broadcastOrderBook();
    }

    protected OrderBook packOrderBook() {
        OrderBook book = new OrderBook();

        if (!buyPool.isEmpty()) {
            List<Double> buyeLevels = new ArrayList<>(buyPool.keySet());
            Collections.sort(buyeLevels);
            Collections.reverse(buyeLevels);
            buyeLevels = buyeLevels.subList(0, Math.min(LEVEL_SIZE, buyeLevels.size()));
            for (Double level : buyeLevels) {
                BigDecimal amount = BigDecimal.ZERO;
                for (LimitOrderResponse order : buyPool.get(level)) {
                    amount = amount.add(BigDecimal.valueOf(order.getRequiredAmount()));
                }
                book.getBuyLevels().put(level, amount.doubleValue());
            }
        }

        if (!sellPool.isEmpty()) {
            List<Double> sellLevels = new ArrayList<>(sellPool.keySet());
            Collections.sort(sellLevels);

            sellLevels = sellLevels.subList(0, Math.min(LEVEL_SIZE, sellLevels.size()));
            for (Double level : sellLevels) {
                BigDecimal amount = BigDecimal.ZERO;
                for (LimitOrderResponse order : sellPool.get(level)) {
                    amount = amount.add(BigDecimal.valueOf(order.getRequiredAmount()));
                }
                book.getSellLevels().put(level, amount.doubleValue());
            }
        }

        return book;
    }

    public void broadcastOrderBook() {
        OrderBook book = packOrderBook();
        broadcastMessage(book);
    }

    public void onOrderBookRequest(User user, OrderBookRequest request) {
        OrderBook book = packOrderBook();
        user.send(book);
    }
    
    public void broadcastMessage(Object message) {
        String json = resolver.pack(message);
        String jsonLog = json.replace("\\", "");
        log.info("Broad for {} users message {}", loginController.getAllLoginedUser().size(), jsonLog);
        for(User user : loginController.getAllLoginedUser()) {
            user.sendRawString(json);
        }
    }
    
    protected RejectOrderResponse checkAndRoundAmount(MarketOrderRequest order) {
        double amount = BigDecimal.valueOf(order.getAmount()).setScale(Symbol.STK.getCoin().scale(), RoundingMode.FLOOR).doubleValue();
        if (amount < Symbol.STK.getCoin().doubleValue()) {
            RejectOrderResponse reject = new RejectOrderResponse("Incorrect amount");
            reject.setRejectType(RejectOrderType.INVALID_CONDITION);
            return reject;
        } else {
            order.setAmount(amount);
            return null;
        }
    }
    
    protected RejectOrderResponse checkAndRoundPrice(LimitOrderRequest order) {
        double price = BigDecimal.valueOf(order.getPrice()).setScale(Symbol.MON.getCoin().scale(), RoundingMode.FLOOR).doubleValue();
        if(price < Symbol.MON.getCoin().doubleValue()) {
            RejectOrderResponse reject = new RejectOrderResponse("Incorrect price");
            reject.setRejectType(RejectOrderType.INVALID_CONDITION);
            return reject;
        } else {
            order.setPrice(price);
            return null;
        }
    }
    
    protected RejectOrderResponse checkInstrument(MarketOrderRequest order) {
        if(order.getInstrument() == null) {
            RejectOrderResponse reject = new RejectOrderResponse("Wrong instrument");
            reject.setRejectType(RejectOrderType.INVALID_CONDITION);
            return reject;
        } else {
            return null;
        }
    }
    
    protected RejectOrderResponse checkSide(MarketOrderRequest order) {
        if(order.getSide() == null) {
            RejectOrderResponse reject = new RejectOrderResponse("Wrong order side");
            reject.setRejectType(RejectOrderType.INVALID_CONDITION);
            return reject;
        } else {
            return null;
        }
    }
}
