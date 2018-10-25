package bymihaj.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;

public class FilledTab extends CellTable<String> {
    
    protected ListDataProvider<String> provider;
    
    public FilledTab() {
        addColumn(new TextColumn<String>() {

            @Override
            public String getValue(String object) {
                return object;
            }
        }, "Record");
        
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
    }
    
    public void onLimit(LimitOrderResponse limit) {
        if(limit.getRequiredAmount() == 0.0) {
            provider.getList().add("#"+limit.getId()+" ("+limit.getFilledAmount()+") "+limit.getPrice()+" LIMIT");
        }
    }
    
    public void onMarket(MarketOrderResponse market) {
        provider.getList().add("#"+market.getId()+" ("+market.getFilledAmount()+") MARKET");
    }

}
