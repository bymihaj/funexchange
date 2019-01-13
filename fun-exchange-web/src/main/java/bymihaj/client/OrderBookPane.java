package bymihaj.client;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;

import bymihaj.OrderBook;
import bymihaj.OrderBookRequest;
import bymihaj.TradeHistory;
import bymihaj.data.order.OrderSide;

public class OrderBookPane extends HorizontalPanel {
    
    protected DataGrid<Map.Entry<Double, Double>> sell;
    protected DataGrid<Map.Entry<Double, Double>> buy;
    
    public OrderBookPane(Connection conn) {
        //sell = new OrderBookSideTable("red");
        //sell.setSize("150px", "490px");
        //buy = new OrderBookSideTable("green");
        //buy.setSize("150px", "490px");
        
        buy = new DataGrid<Map.Entry<Double,Double>>();
        buy.getElement().getStyle().setColor("#81ffa3");
        buy.setSize("200px", "570px");
        buy.addStyleName("trade-table");
        buy.addColumn(new TextColumn<Map.Entry<Double,Double>>() {

            @Override
            public String getValue(Entry<Double, Double> object) {
                return String.valueOf(object.getValue());
            }
        }, "Amount");
        buy.addColumn(new TextColumn<Map.Entry<Double,Double>>() {

            @Override
            public String getValue(Entry<Double, Double> object) {
                return String.valueOf(object.getKey());
            }
        }, "Price");
        buy.setRowStyles(new RowStyles<Map.Entry<Double,Double>>() {
            
            @Override
            public String getStyleNames(Entry<Double, Double> row, int rowIndex) {
                return "buy";
            }
        });
        
        sell = new DataGrid<Map.Entry<Double,Double>>();
        sell.getElement().getStyle().setColor("#ffa7be");
        
        sell.setSize("200px", "570px");
        sell.addStyleName("trade-table");
        sell.addColumn(new TextColumn<Map.Entry<Double,Double>>() {

            @Override
            public String getValue(Entry<Double, Double> object) {
                return String.valueOf(object.getKey());
            }
        }, "Price");
        sell.addColumn(new TextColumn<Map.Entry<Double,Double>>() {

            @Override
            public String getValue(Entry<Double, Double> object) {
                return String.valueOf(object.getValue());
            }
        }, "Amount");
        sell.setRowStyles(new RowStyles<Map.Entry<Double,Double>>() {
            
            @Override
            public String getStyleNames(Entry<Double, Double> row, int rowIndex) {
                return "sell";
            }
        });
        
        add(buy);
        add(sell);
        
        conn.subscribe(OrderBook.class, this::onOrderBook);
        conn.send(new OrderBookRequest());
    }
    
    public void onOrderBook(OrderBook orderBook) {
        sell.setRowData(new ArrayList<>(orderBook.getSellLevels().entrySet()));
        TreeMap<Double, Double> toReverse = new TreeMap<>(orderBook.getBuyLevels());
        buy.setRowData(new ArrayList<>(toReverse.descendingMap().entrySet()));
    }

}
