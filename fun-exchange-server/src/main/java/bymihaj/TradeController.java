package bymihaj;

import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.RejectOrderResponse;

public class TradeController {

    public void onMarketOrder(User user, MarketOrderRequest moReq) {
        // TODO after placing limit orders
        String rejectText = "No liqudity for " + moReq.getSide() + " " + moReq.getAmount();
        user.send(new RejectOrderResponse(rejectText));
    }
    
}
