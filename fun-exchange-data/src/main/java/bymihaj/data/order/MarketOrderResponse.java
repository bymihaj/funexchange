package bymihaj.data.order;

import java.math.BigDecimal;

public class MarketOrderResponse extends MarketOrderRequest {
    
    private double filledAmount;
    private double filledPrice;

    public double getFilledAmount() {
        return filledAmount;
    }

    public void setFilledAmount(double filledAmount) {
        this.filledAmount = filledAmount;
    }
    
    public double getRequiredAmount() {
    	return BigDecimal.valueOf(getAmount()).subtract(BigDecimal.valueOf(getFilledAmount())).doubleValue();
    }

    public double getFilledPrice() {
        return filledPrice;
    }

    public void setFilledPrice(double filledPrice) {
        this.filledPrice = filledPrice;
    }

}
