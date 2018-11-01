package bymihaj.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

import bymihaj.data.order.CancelOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;

public class PendingTab extends DataGrid<LimitOrderResponse> {

    protected Connection conn;
    protected List<LimitOrderResponse> provider;
    
    public PendingTab(Connection conn) {
        provider = new ArrayList<>();
        this.conn = conn;
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return String.valueOf(object.getId());
            }
        }, "ID");
        setColumnWidth(0, "40px");
        
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return object.getInstrument().name();
            }
        }, "Instrument");
        setColumnWidth(1, "80px");
        
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return String.valueOf(object.getRequiredAmount());
            }
        }, "Required");
        setColumnWidth(2, "60px");
        
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return String.valueOf(object.getPrice());
            }
        }, "Price");
        setColumnWidth(3, "60px");
        
        addColumn(new ButtonColumn() {
            
            @Override
            public String getValue(LimitOrderResponse object) {
                return "x";
            }
        }, "Action");
        setColumnWidth(4, "40px");
        
        
        setRowStyles(new RowStyles<LimitOrderResponse>() {
            
            @Override
            public String getStyleNames(LimitOrderResponse row, int rowIndex) {
                if(OrderSide.BUY.equals(row.getSide())) {
                    return "buy";
                } else {
                    return "sell";
                }
            }
        });
        
       
        
        setWidth("425px");
        setHeight("322px");
        
        conn.send(new OrderStatusRequest());
    }
    
    public void onPending(LimitOrderResponse limit) {
        Optional<LimitOrderResponse> inList = provider.stream().filter( l -> l.getId() == limit.getId()).findFirst();
        
        if(inList.isPresent()) {
            provider.remove(inList.get());
        }
        
        if(limit.getRequiredAmount() > 0.0) {
            provider.add(limit);
        }
        
        setRowData(provider);
    }
    
    public void onStatus(OrderStatusResponse resp) {
        provider.clear();
        resp.getOrders().forEach( l -> onPending(l) );
    }
    
    
    abstract class ButtonColumn extends Column<LimitOrderResponse, String> {

        public ButtonColumn() {
            super(new ButtonCell());
            setFieldUpdater(new FieldUpdater<LimitOrderResponse, String>() {
                
                @Override
                public void update(int index, LimitOrderResponse object, String value) {
                    CancelOrderRequest cancel = new CancelOrderRequest();
                    cancel.setId(object.getId());
                    conn.send(cancel);
                }
            });
        }
    }
}
