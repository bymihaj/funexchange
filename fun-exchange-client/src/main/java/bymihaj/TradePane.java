package bymihaj;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;
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
        
        HBox orderBookPane = new OrderBookPane(conn);
        
        TabPane infoPane = new TabPane();
        infoPane.setMinHeight(200);
        infoPane.setMinWidth(400);
        
        NotificationPane notificationPane = new NotificationPane();
        conn.subscribe(RejectOrderResponse.class, notificationPane::onReject);
        Tab infoTab = new Tab("Notifications");
        infoTab.setClosable(false);
        infoTab.setContent(notificationPane);
        infoPane.getTabs().add(infoTab);
        
        PendingPane pendingPane = new PendingPane();
        conn.subscribe(LimitOrderResponse.class, pendingPane::onPending);
        conn.subscribe(OrderStatusResponse.class, pendingPane::onStatus);
        Tab pendingTab = new Tab("Pending");
        pendingTab.setClosable(false);
        pendingTab.setContent(pendingPane);
        infoPane.getTabs().add(pendingTab);
        conn.send(new OrderStatusRequest());
        
        FilledPane filledPane = new FilledPane();
        conn.subscribe(LimitOrderResponse.class, filledPane::onLimit);
        conn.subscribe(MarketOrderResponse.class, filledPane::onMarket);
        Tab filledTab = new Tab("Filled");
        filledTab.setClosable(false);
        filledTab.setContent(filledPane);
        infoPane.getTabs().add(filledTab);
        
        VBox holder2 = new VBox();
        holder2.getChildren().addAll(orderBookPane, infoPane);
        
        getChildren().add(holder2);
        
        
        HistoryPane historyPane = new HistoryPane();
        conn.subscribe(TradeHistory.class, historyPane::onHistory);
        getChildren().add(historyPane);
       
        
    }

}
