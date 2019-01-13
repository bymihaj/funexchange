package bymihaj.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;

// TODO remove 
public class OrderBookSideTable extends DataGrid<Map.Entry<Double, Double>> {

    //protected List<Map.Entry<Double, Double>> provider;
    
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
        
        
        //provider = new ArrayList<>();
        /*
        provider = new ListDataProvider<>();
        provider.addDataDisplay(this);
        */
    }
    
    public void update(Collection<Map.Entry<Double, Double>> collection) {
        //provider.getList().clear();
        //provider.getList().addAll(collection);
        //provider.clear();
        setRowData(new ArrayList<>(collection));
    }
}
