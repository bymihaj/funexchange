package bymihaj.data.order;

public class MarketOrderResponse extends MarketOrderRequest {
    
    private double filledAmount;

    public double getFilledAmount() {
        return filledAmount;
    }

    public void setFilledAmount(double filledAmount) {
        this.filledAmount = filledAmount;
    }
    
    public double getRequiredAmount() {
    	return getAmount() - getFilledAmount();
    }

}
