package bymihaj.client;

import java.util.Optional;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

import bymihaj.data.order.CancelOrderRequest;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.OrderStatusResponse;

public class PendingTab extends CellTable<LimitOrderResponse> {

    protected Connection conn;
    protected ListDataProvider<LimitOrderResponse> provider;
    
    public PendingTab(Connection conn) {
        this.conn = conn;
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return String.valueOf(object.getId());
            }
        }, "ID");
        
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return object.getInstrument().name();
            }
        }, "Instrument");
        
        addColumn(new TextColumn<LimitOrderResponse>() {

            @Override
            public String getValue(LimitOrderResponse object) {
                return String.valueOf(object.getRequiredAmount());
            }
        }, "Required");
        
        addColumn(new ButtonColumn() {
            
            @Override
            public String getValue(LimitOrderResponse object) {
                return "x";
            }
        }, "Action");
        
        
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
        
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
    }
    
    public void onPending(LimitOrderResponse limit) {
        Optional<LimitOrderResponse> inList = provider.getList().stream().filter( l -> l.getId() == limit.getId()).findFirst();
        
        if(inList.isPresent()) {
            provider.getList().remove(inList.get());
        }
        
        if(limit.getRequiredAmount() > 0.0) {
            provider.getList().add(limit);
        }
        
        provider.flush();
    }
    
    public void onStatus(OrderStatusResponse resp) {
        provider.getList().clear();
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
