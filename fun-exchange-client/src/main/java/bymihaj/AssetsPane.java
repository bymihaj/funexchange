package bymihaj;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;

public class AssetsPane extends TableView<PropertyModel> {
    
    public AssetsPane(Connection conn) {
        TableColumn<PropertyModel, String> nameColumn = new TableColumn<>("Symbol");
        nameColumn.setCellValueFactory(this::renderName);
        
        TableColumn<PropertyModel, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(this::renderAmount);
        
        getColumns().add(nameColumn);
        getColumns().add(amountColumn);
        
        setItems(FXCollections.observableArrayList());
        conn.subscribe(AssetsResponse.class, this::onAssetsResponse);
        conn.send(new AssetsRequest());
        
    }
    
    
    public ObservableValue<String> renderName(CellDataFeatures<PropertyModel, String> param) {
        return param.getValue().getName();
    }
    
    public ObservableValue<String> renderAmount(CellDataFeatures<PropertyModel, String> param) {
        return param.getValue().getAmount();
    }
    
    public void onAssetsResponse(AssetsResponse response) {
        List<PropertyModel> items = new ArrayList<>();
        for(Property prop : response.getProperties().values()) {
            items.add(new PropertyModel(prop));
        }
        getItems().setAll(items);
    }
}
