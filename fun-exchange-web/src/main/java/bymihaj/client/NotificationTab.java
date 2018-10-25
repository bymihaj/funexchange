package bymihaj.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

import bymihaj.data.order.RejectOrderResponse;

public class NotificationTab extends CellTable<String> {
    
    protected ListDataProvider<String> provider;
    
    public NotificationTab() {
        addColumn(new TextColumn<String>() {

            @Override
            public String getValue(String object) {
                return object;
            }
        }, "Text");
        
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
    }
    
    public void onReject(RejectOrderResponse reject) {
        provider.getList().add(reject.getReason());
    }
    
    

}
