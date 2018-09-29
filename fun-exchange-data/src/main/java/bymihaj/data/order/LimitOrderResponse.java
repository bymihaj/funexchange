package bymihaj.data.order;

public class LimitOrderResponse extends MarketOrderResponse {
    
    private double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
}
