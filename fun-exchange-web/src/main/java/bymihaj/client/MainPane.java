package bymihaj.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import bymihaj.AssetsRequest;
import bymihaj.AssetsResponse;
import bymihaj.MessageListener;
import bymihaj.OrderBookRequest;
import bymihaj.RoundStatus;
import bymihaj.TradeHistory;
import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderStatusRequest;
import bymihaj.data.order.OrderStatusResponse;
import bymihaj.data.order.RejectOrderResponse;

public class MainPane extends VerticalPanel {
    
    protected Connection conn;
    protected HistoryPane history;
    protected AssetsPane assets;
    protected NotificationTab notificationTab;
    protected PendingTab pendingTab;
    protected FilledTab filledTab;
    
    public MainPane(Connection conn) {
        this.conn = conn;
        
        DecoratorPanel decOrder = new DecoratorPanel();
        decOrder.setWidget(new OrderPane(conn));
        
        assets = new AssetsPane(conn);
        conn.subscribe(AssetsResponse.class, assets::onAssetsResponse);
        DecoratorPanel decAsset = new DecoratorPanel();
        decAsset.setWidget(assets);
        
        OrderBookPane orderBook = new OrderBookPane(conn);
        DecoratorPanel decOrderBook = new DecoratorPanel();
        decOrderBook.setWidget(orderBook);
        
        history = new HistoryPane();
        conn.subscribe(TradeHistory.class, history::onTradeHistor);
        DecoratorPanel decHistory = new DecoratorPanel();
        decHistory.setWidget(history);
        
        notificationTab = new NotificationTab();
        conn.subscribe(RejectOrderResponse.class, notificationTab::onReject);
        
        filledTab = new FilledTab();
        conn.subscribe(MarketOrderResponse.class, filledTab::onMarket);
        conn.subscribe(LimitOrderResponse.class, filledTab::onLimit);
        
        pendingTab = new PendingTab(conn);
        conn.subscribe(LimitOrderResponse.class, pendingTab::onPending);
        conn.subscribe(OrderStatusResponse.class, pendingTab::onStatus);
        
        TabPanel tabPane = new TabPanel();
        tabPane.add(notificationTab, "Notification");
        tabPane.add(filledTab, "Filled");
        tabPane.add(pendingTab, "Pending");
        tabPane.selectTab(0);
        
        // fix gwt bug...
        tabPane.addSelectionHandler(new SelectionHandler<Integer>() {
            
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                switch (event.getSelectedItem()) {
                case 0:
                    notificationTab.redraw();
                    break;
                case 1:
                    filledTab.redraw();
                    break;
                case 2:
                    pendingTab.redraw();
                default:
                    break;
                }
            }
        });
        
        
        DecoratorPanel decTab = new DecoratorPanel();
        decTab.setWidget(tabPane);
        
        HorizontalPanel hr = new HorizontalPanel();
        hr.add(decHistory);
        hr.add(decOrderBook);
        
        HorizontalPanel smallPane = new HorizontalPanel();
        smallPane.add(decAsset);
        smallPane.add(decOrder);
        
        VerticalPanel side = new VerticalPanel();
        side.add(smallPane);
        side.add(decTab);
        hr.add(side);
        
        RoundHeader header = new RoundHeader(conn);
        conn.subscribe(RoundStatus.class, header::onRound);
        DecoratorPanel decHeader = new DecoratorPanel();
        decHeader.setWidget(header);
        
        add(decHeader);
        add(hr);
        
        
        conn.subscribe(RoundStatus.class, this::onRoundStatus);
    }
    
    public void onRoundStatus(RoundStatus status) {
        notificationTab.reset();
        history.reset();
        filledTab.reset();
        if(status.isStarted()) {
            conn.send(new OrderBookRequest());
            conn.send(new OrderStatusRequest());
        } else {
            assets.reset();
            pendingTab.reset();
        }
    }

}
