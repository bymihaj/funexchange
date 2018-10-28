package bymihaj.client;

import java.util.TreeMap;

import com.google.gwt.user.client.ui.HorizontalPanel;

import bymihaj.OrderBook;
import bymihaj.OrderBookRequest;

public class OrderBookPane extends HorizontalPanel {
    
    protected OrderBookSideTable sell;
    protected OrderBookSideTable buy;
    
    public OrderBookPane(Connection conn) {
        sell = new OrderBookSideTable("red");
        sell.setSize("150px", "490px");
        buy = new OrderBookSideTable("green");
        buy.setSize("150px", "490px");
        add(sell);
        add(buy);
        conn.subscribe(OrderBook.class, this::onOrderBook);
        conn.send(new OrderBookRequest());
    }
    
    public void onOrderBook(OrderBook orderBook) {
        sell.update(orderBook.getSellLevels().entrySet());
        TreeMap<Double, Double> toReverse = new TreeMap<>(orderBook.getBuyLevels());
        buy.update(toReverse.descendingMap().entrySet());
    }

}
