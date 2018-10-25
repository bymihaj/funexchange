package bymihaj.client;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

public class OrderBookSideTable extends CellTable<Map.Entry<Double, Double>> {

    protected ListDataProvider<Map.Entry<Double, Double>> provider;
    
    public OrderBookSideTable(String color) {
        getElement().getStyle().setColor(color);
        addColumn(new TextColumn<Map.Entry<Double,Double>>() {

            @Override
            public String getValue(Entry<Double, Double> object) {
                return String.valueOf(object.getKey());
            }
        }, "Price");
        
        addColumn(new TextColumn<Map.Entry<Double,Double>>() {

            @Override
            public String getValue(Entry<Double, Double> object) {
                return String.valueOf(object.getValue());
            }
        }, "Amount");
        
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
    }
    
    public void update(Collection<Map.Entry<Double, Double>> collection) {
        provider.getList().clear();
        provider.getList().addAll(collection);
    }
}
