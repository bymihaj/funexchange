package bymihaj;

import bymihaj.data.order.MarketOrderRequest;

public class RightController {
    
    public boolean allowed(User user, Object msg) {
        if (msg instanceof AccountRequest) {
            return true;
        } else if(msg instanceof LoginRequest) {
            return true;
        } else if(msg instanceof AssetsRequest) {
            return user.isLogined && user.getBank() != null;
        } else if(msg instanceof MarketOrderRequest) {
            return user.isLogined && user.getBank() != null;
        } else {
            return false;
        } 
    }

}
