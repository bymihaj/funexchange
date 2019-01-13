package bymihaj.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;

import bymihaj.TradeHistory;
import bymihaj.data.order.OrderSide;

public class HistoryPane extends DataGrid<TradeHistory>{
    
    protected List<TradeHistory> list;
    
    public HistoryPane() {
        addStyleName("trade-table");
        
        list = new ArrayList<>();
        
        addColumn(new TextColumn<TradeHistory>() {

            @Override
            public String getValue(TradeHistory object) {
                return  RoundHolder.dtf.format(new Date(object.getDateTime()));
            }
        }, "Timestamp");
        setColumnWidth(0, "140px");
        
        addColumn(new TextColumn<TradeHistory>() {

            @Override
            public String getValue(TradeHistory object) {
                return String.valueOf(object.getPrice());
            }
        }, "Price");
        setColumnWidth(1, "80px");
        
        addColumn(new TextColumn<TradeHistory>() {

            @Override
            public String getValue(TradeHistory object) {
                return String.valueOf(object.getAmount());
            }
        }, "Amount");
        setColumnWidth(2, "80px");
        
        
        
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
        
        setWidth("400px");
        setHeight("570px");
    }
    
    public void onTradeHistor(TradeHistory trade) {
        if(list.size() > 100) {
            list = list.subList(0, 100);
        }
        list.add(0, trade);
        setRowData(list);
    }
    
    public void reset() {
        list.clear();
        setRowData(list);
    }

}
