package bymihaj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.layout.HBox;

public class OrderBookPane extends HBox {
    
    protected OrderBookTable buyTable;
    protected OrderBookTable sellTable;
    
    public OrderBookPane(Connection conn) {
        buyTable = new OrderBookTable("-fx-text-fill: green");
        sellTable = new OrderBookTable("-fx-text-fill: red");
        getChildren().addAll(buyTable, sellTable);
        conn.subscribe(OrderBook.class, this::onOrderBook);
        conn.send(new OrderBookRequest());
    }
    
    public void onOrderBook(OrderBook book) {
        List<Double> buyLevels = new ArrayList<>(book.getBuyLevels().keySet());
        Collections.sort(buyLevels);
        Collections.reverse(buyLevels);
        List<PriceLevelModel> buyList = new ArrayList<>();
        for(Double level : buyLevels) {
            PriceLevelModel model = new PriceLevelModel(level, book.getBuyLevels().get(level));
            buyList.add(model);
        }
        buyTable.getItems().setAll(buyList);
        
        List<Double> sellLevels = new ArrayList<>(book.getSellLevels().keySet());
        Collections.sort(sellLevels);
        List<PriceLevelModel> sellList = new ArrayList<>();
        for(Double level : sellLevels) {
            PriceLevelModel model = new PriceLevelModel(level, book.getSellLevels().get(level));
            sellList.add(model);
        }
        sellTable.getItems().setAll(sellList);
    }

}
