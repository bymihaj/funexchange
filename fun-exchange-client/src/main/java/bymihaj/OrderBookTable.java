package bymihaj;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;

public class OrderBookTable extends TableView<PriceLevelModel> {
    
    public OrderBookTable(String color) {
        TableColumn<PriceLevelModel, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(this::renderPrice);
        priceColumn.setStyle(color);
        
        TableColumn<PriceLevelModel, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(this::renderAmount);
        amountColumn.setStyle(color);
        
        getColumns().add(priceColumn);
        getColumns().add(amountColumn);
    }
    
    public ObservableValue<String> renderPrice(CellDataFeatures<PriceLevelModel, String> param) {
        return param.getValue().getPrice();
    }
    public ObservableValue<String> renderAmount(CellDataFeatures<PriceLevelModel, String> param) {
        return param.getValue().getAmount();
    }
}
