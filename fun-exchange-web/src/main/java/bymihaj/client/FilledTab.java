package bymihaj.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;
import bymihaj.data.order.OrderSide;
import bymihaj.data.order.Trade;

public class FilledTab extends DataGrid<FilledTab.TradeRecord> {
    
    protected List<TradeRecord> provider;
    
    public FilledTab() {
        provider = new ArrayList<>();
        addColumn(new TextColumn<TradeRecord>() {

            @Override
            public String getValue(TradeRecord object) {
                return String.valueOf(object.orderId);
            }
        }, "OrderID");
        setColumnWidth(0, "80px");
        
        addColumn(new TextColumn<FilledTab.TradeRecord>() {

            @Override
            public String getValue(TradeRecord object) {
                return String.valueOf(object.tradeId);
            }
        }, "TradeID");
        setColumnWidth(1, "80px");
        
        addColumn(new TextColumn<FilledTab.TradeRecord>() {

            @Override
            public String getValue(TradeRecord object) {
                return String.valueOf(object.amount);
            }
        }, "Amount");
        setColumnWidth(2, "80px");
        
        addColumn(new TextColumn<FilledTab.TradeRecord>() {

            @Override
            public String getValue(TradeRecord object) {
                return String.valueOf(object.price);
            }
        }, "Price");
        setColumnWidth(3, "80px");
        
        addColumn(new TextColumn<FilledTab.TradeRecord>() {

            @Override
            public String getValue(TradeRecord object) {
                return object.side.name();
            }
        }, "Side");
        setColumnWidth(4, "80px");
        
        setWidth("425px");
        setHeight("322px");
        
    }
    
    public void onLimit(LimitOrderResponse limit) {
        addRecord(limit);
    }
    
    public void onMarket(MarketOrderResponse market) {
        addRecord(market);
    }
    
    protected void addRecord(MarketOrderResponse market) {
        for(Trade trade : market.getTrades()) {
            if(provider.stream().filter( r -> r.tradeId == trade.getTid()).collect(Collectors.toList()).isEmpty()) {
                TradeRecord rec = new TradeRecord();
                rec.orderId = market.getId();
                rec.tradeId = trade.getTid();
                rec.amount = trade.getAmount();
                rec.price = trade.getPrice();
                rec.side = market.getSide();
                provider.add(rec);
            }
        }
        setRowData(provider);
    }
    
    public void reset() {
        provider.clear();
        setRowData(provider);
    }
    
    static class TradeRecord {
        long orderId;
        long tradeId;
        double amount;
        double price;
        OrderSide side;
    }

}
