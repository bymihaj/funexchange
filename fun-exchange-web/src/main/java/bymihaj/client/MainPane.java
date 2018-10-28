package bymihaj.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import bymihaj.AssetsResponse;

import bymihaj.TradeHistory;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;

public class MainPane extends HorizontalPanel {
    
    protected Connection conn;
    
    public MainPane(Connection conn) {
        this.conn = conn;
        
        DecoratorPanel decOrder = new DecoratorPanel();
        decOrder.setWidget(new OrderPane(conn));
        //add(decOrder);
        
        AssetsPane assets = new AssetsPane(conn);
        conn.subscribe(AssetsResponse.class, assets::onAssetsResponse);
        DecoratorPanel decAsset = new DecoratorPanel();
        decAsset.setWidget(assets);
        //add(decAsset);
        
        OrderBookPane orderBook = new OrderBookPane(conn);
        DecoratorPanel decOrderBook = new DecoratorPanel();
        decOrderBook.setWidget(orderBook);
        //add(decOrderBook);
        
        
        
        
        
        HistoryPane history = new HistoryPane();
        conn.subscribe(TradeHistory.class, history::onTradeHistor);
        DecoratorPanel decHistory = new DecoratorPanel();
        decHistory.setWidget(history);
        //add(decHistory);
        
        NotificationTab notificationTab = new NotificationTab();
        conn.subscribe(RejectOrderResponse.class, notificationTab::onReject);
        
        FilledTab filledTab = new FilledTab();
        conn.subscribe(MarketOrderResponse.class, filledTab::onMarket);
        conn.subscribe(LimitOrderResponse.class, filledTab::onLimit);
        
        PendingTab pendingTab = new PendingTab(conn);
        conn.subscribe(LimitOrderResponse.class, pendingTab::onPending);
        conn.subscribe(OrderStatusResponse.class, pendingTab::onStatus);
        
        TabPanel tabPane = new TabPanel();
        tabPane.add(notificationTab, "Notification");
        tabPane.add(filledTab, "Filled");
        tabPane.add(pendingTab, "Pending");
        tabPane.selectTab(0);
        
        DecoratorPanel decTab = new DecoratorPanel();
        decTab.setWidget(tabPane);
        //add(decTab);
       
        add(decHistory);
        add(decOrderBook);
        
        VerticalPanel side = new VerticalPanel();
        side.add(decOrder);
        side.add(decAsset);
        side.add(decTab);
        add(side);
    }

}
