package bymihaj.data.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MarketOrderResponse extends MarketOrderRequest {
    
    private List<Trade> trades;
    
    public MarketOrderResponse() {
        trades = new ArrayList<>();
    }

    public double getFilledAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for(Trade trade : trades) {
            sum = sum.add(BigDecimal.valueOf(trade.getAmount()));
        }
        return sum.doubleValue();
    }

    public double getRequiredAmount() {
        return BigDecimal.valueOf(getAmount()).subtract(BigDecimal.valueOf(getFilledAmount())).doubleValue();
    }
    
    public double getAveragePrice() {
        if(trades.isEmpty()) {
            return 0.0;
        } else {
            return trades.stream().mapToDouble( t -> t.getPrice()).sum() / trades.size();
        }
    }

    public void addTrade(Trade trade) {
        trades.add(trade);
    }
    
    public List<Trade> getTrades() {
        return trades;
    }

}
