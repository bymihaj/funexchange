package bymihaj.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;


import bymihaj.data.order.RejectOrderResponse;

public class NotificationTab extends DataGrid<String> {
    
    protected List<String> provider;
    
    public NotificationTab() {
        addStyleName("trade-table");
        provider = new ArrayList<>();
        addColumn(new TextColumn<String>() {

            @Override
            public String getValue(String object) {
                return object;
            }
        }, "Text");
        setColumnWidth(0, "410px");
        
        setWidth("425px");
        setHeight("280px");
    }
    
    public void onReject(RejectOrderResponse reject) {
        provider.add(reject.getReason());
        setRowData(provider);
    }
    
    public void reset() {
        provider.clear();
        setRowData(provider);
    }
    
    
    
    

}
