package bymihaj;

import java.util.Map;
import java.util.TreeMap;

public class OrderBook {

    private Map<Double, Double> buyLevels;
    private Map<Double, Double> sellLevels;
    
    public OrderBook() {
        buyLevels = new TreeMap<>();
        sellLevels = new TreeMap<>();
    }
    
    public Map<Double, Double> getBuyLevels() {
        return buyLevels;
    }
    
    public void setBuyLevels(Map<Double, Double> buyLevels) {
        this.buyLevels = buyLevels;
    }
    public Map<Double, Double> getSellLevels() {
        return sellLevels;
    }
    
    public void setSellLevels(Map<Double, Double> sellLevels) {
        this.sellLevels = sellLevels;
    }
}
