package bymihaj;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.RejectOrderResponse;

public class TradeController {

    protected AtomicLong counter = new AtomicLong(1);
    protected Map<LimitOrderResponse, User> orderOfUser;
    protected Map<Double, List<LimitOrderResponse>> sellPool;
    protected Map<Double, List<LimitOrderResponse>> buyPool;
    
    public void onMarketOrder(User user, MarketOrderRequest moReq) {
        // TODO after placing limit orders
        String rejectText = "No liqudity for " + moReq.getSide() + " " + moReq.getAmount() + "on market";
        user.send(new RejectOrderResponse(rejectText));
        orderOfUser = new ConcurrentHashMap<>();
        sellPool = new ConcurrentHashMap<>();
        buyPool = new ConcurrentHashMap<>();
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
        resp.setSide(loReq.getSide());
        resp.setPrice(loReq.getPrice());
        resp.setAmount(loReq.getAmount());
        resp.setFilledAmount(0);
        resp.setId(counter.getAndIncrement());
        
        user.addOrder(resp.getId(), resp);
        orderOfUser.put(resp, user);
        user.send(resp);
        
        // TODO add to pool
        
    }
}
