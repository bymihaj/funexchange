package bymihaj;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TradePane extends HBox {
    
    public TradePane(Connection conn) {
        //VBox placeOrderPane = new VBox();
        //placeOrderPane.getChildren().add(new PlaceOrderPane());
        
        //VBox assetsPane = new VBox();
        //assetsPane.getChildren().add(new Label("Assets table here"));
        
        
        VBox holder1 = new VBox(24);
        holder1.setPadding(new Insets(12));
        holder1.getChildren().addAll(new PlaceOrderPane(conn), new AssetsPane(conn));
        
        getChildren().add(holder1);
        
        HBox orderBookPane = new HBox();
        orderBookPane.getChildren().add(new Label("Order book here"));
        
        TabPane infoPane = new TabPane();
        Tab infoTab = new Tab("Notifications");
        infoTab.setClosable(false);
        infoPane.getTabs().add(infoTab);
        
        VBox holder2 = new VBox();
        holder2.getChildren().addAll(orderBookPane, infoPane);
        
        getChildren().add(holder2);
        
        
        VBox historyPane = new VBox();
        historyPane.getChildren().add(new Label("History here"));
        
        getChildren().add(historyPane);
       
        
    }

}
