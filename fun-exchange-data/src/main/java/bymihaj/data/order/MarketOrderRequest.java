package bymihaj.data.order;

public class MarketOrderRequest extends AbstractOrder {
    
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    

}
