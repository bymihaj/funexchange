package bymihaj.data.order;

public class LimitOrderRequest extends MarketOrderRequest {
    
    private double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}
