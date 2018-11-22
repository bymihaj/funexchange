package bymihaj.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

import bymihaj.AssetsRequest;
import bymihaj.AssetsResponse;
import bymihaj.Property;

public class AssetsPane extends CellTable<Property> {
    
    protected ListDataProvider<Property> provider;
    
    public AssetsPane(Connection conn) {
        
        TextColumn<Property> symbolCol = new TextColumn<Property>() {
            
            @Override
            public String getValue(Property object) {
                return object.getName().name();
            }
        };
        
        TextColumn<Property> amountCol = new TextColumn<Property>() {
            
            @Override
            public String getValue(Property object) {
                return object.getAmount().toPlainString();
            }
        };
        
        addColumn(symbolCol, "Symbol");
        addColumn(amountCol, "Amount");
        
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
        
        conn.send(new AssetsRequest());
        
        setWidth("180px");
        setHeight("120px");
    }
    
    public void onAssetsResponse(AssetsResponse assets) {
        provider.getList().clear();
        provider.getList().addAll(assets.getProperties().values());
    }
    
    public void reset() {
        provider.getList().clear();
    }
        
}
