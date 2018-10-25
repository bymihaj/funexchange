package bymihaj.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

import bymihaj.TradeHistory;
import bymihaj.data.order.OrderSide;

public class HistoryPane extends CellTable<TradeHistory> {
    
    protected ListDataProvider<TradeHistory> provider;
    
    // TODO table with scroll, after general design 
    public HistoryPane() {
        
        addColumn(new TextColumn<TradeHistory>() {

            @Override
            public String getValue(TradeHistory object) {
                return object.getDateTime();
            }
        }, "Timestamp");
        
        addColumn(new TextColumn<TradeHistory>() {

            @Override
            public String getValue(TradeHistory object) {
                return String.valueOf(object.getAmount());
            }
        }, "Amount");
        
        addColumn(new TextColumn<TradeHistory>() {

            @Override
            public String getValue(TradeHistory object) {
                return String.valueOf(object.getPrice());
            }
        }, "Price");
        
        setRowStyles(new RowStyles<TradeHistory>() {
            
            @Override
            public String getStyleNames(TradeHistory row, int rowIndex) {
                if(OrderSide.BUY.equals(row.getSide())) {
                    return "buy";
                } else {
                    return "sell";
                }
            }
        });
        
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
    }
    
    public void onTradeHistor(TradeHistory trade) {
        provider.getList().add(trade);
    }

}
