package bymihaj.client;


import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import bymihaj.Instrument;
import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.OrderSide;



public class OrderPane extends FlexTable {

    protected Connection conn;
    protected TextBox buyAmountTbx;
    protected TextBox buyPriceTbx;
    protected TextBox sellAmountTbx;
    protected TextBox sellPriceTbx;
    
    //protected RadioButton limitRbt;
    
    public OrderPane(Connection conn) {
        setCellSpacing(10);
        this.conn = conn;
        
        /*
        Button marketSellBtn = new Button();
        marketSellBtn.setText("SELL");
        sellBtn.addClickHandler(e -> {
            validateInput();
            conn.send(createOrder(OrderSide.SELL));
        });
        
        Button buyBtn = new Button();
        buyBtn.setText("BUY");
        buyBtn.addClickHandler(e -> {
            validateInput();
            conn.send(createOrder(OrderSide.BUY));
        });
        */
        
        buyAmountTbx = new TextBox();
        buyAmountTbx.setText("10");
        buyAmountTbx.addStyleName("order-input");
        buyPriceTbx = new TextBox();
        buyPriceTbx.setText("1.00");
        buyPriceTbx.addStyleName("order-input");
        Button marketBuyBtn = new Button();
        marketBuyBtn.setText("MARKET BUY");
        marketBuyBtn.addStyleName("order-button");
        marketBuyBtn.addStyleName("order-market");
        marketBuyBtn.addClickHandler(e -> {
            validateInput(buyAmountTbx, buyPriceTbx, false);
            conn.send(createOrder(OrderSide.BUY, buyAmountTbx, buyPriceTbx, false));
        });
        Button limitBuyBtn = new Button();
        limitBuyBtn.addStyleName("order-button");
        limitBuyBtn.addStyleName("order-limit");
        limitBuyBtn.setText("LIMIT BUY");
        limitBuyBtn.addClickHandler(e -> {
            validateInput(buyAmountTbx, buyPriceTbx, true);
            conn.send(createOrder(OrderSide.BUY, buyAmountTbx, buyPriceTbx, true));
        });
        
        
        sellAmountTbx = new TextBox();
        sellAmountTbx.setText("10");
        sellAmountTbx.addStyleName("order-input");
        sellPriceTbx = new TextBox();
        sellPriceTbx.setText("1.00");
        sellPriceTbx.addStyleName("order-input");
        Button marketSellBtn = new Button();
        marketSellBtn.addStyleName("order-button");
        marketSellBtn.addStyleName("order-market");
        marketSellBtn.setText("MAKRET SELL");
        marketSellBtn.addClickHandler(e -> {
           validateInput(sellAmountTbx, sellPriceTbx, false); 
           conn.send(createOrder(OrderSide.SELL, sellAmountTbx, sellPriceTbx, false));
        });
        Button limitSellBtn = new Button();
        limitSellBtn.setText("LIMIT SELL");
        limitSellBtn.addStyleName("order-button");
        limitSellBtn.addStyleName("order-limit");
        limitSellBtn.addClickHandler(e -> {
            validateInput(sellAmountTbx, sellPriceTbx, true); 
            conn.send(createOrder(OrderSide.SELL, sellAmountTbx, sellPriceTbx, true));
        });
        
        
        Label amountLabel = new Label("amount");
        amountLabel.addStyleName("trade-minor-text");
        Label priceLabel = new Label("price");
        priceLabel.addStyleName("trade-minor-text");
        
        
        /*
        String group = "order-type";
        limitRbt = new RadioButton(group, "Limit");
        limitRbt.setValue(true);
        limitRbt.addClickHandler(e -> {
            priceTbx.setEnabled(true);
        });
        RadioButton marketRbt = new RadioButton(group, "Market");
        marketRbt.addClickHandler(e -> {
            priceTbx.setEnabled(false);
        });
        
        
        setWidget(0, 0, sellBtn);
        setWidget(0, 1, buyBtn);
        setWidget(1, 0, new Label("Amount"));
        setWidget(1, 1, amountTbx);
        setWidget(2, 0, new Label("Price"));
        setWidget(2, 1, priceTbx);
        setWidget(3, 0, limitRbt);
        setWidget(3, 1, marketRbt);
        */
        
        setWidget(0, 0, buyAmountTbx);
        setWidget(1, 0, buyPriceTbx);
        setWidget(2, 0, marketBuyBtn);
        setWidget(3, 0, limitBuyBtn);
        
        setWidget(0, 1, amountLabel);
        setWidget(1, 1, priceLabel);
        
        setWidget(0, 2, sellAmountTbx);
        setWidget(1, 2, sellPriceTbx);
        setWidget(2, 2, marketSellBtn);
        setWidget(3, 2, limitSellBtn);
        
        //setWidth("250px");
        //setHeight("120px");
    }
    
    public boolean validateInput(TextBox amount, TextBox price, boolean limit) {
        try {
            Double.parseDouble(amount.getText());
        } catch (NumberFormatException e) {
            Window.alert("Placing order failed! Invalid amount value!" );
            return false;
        }
        
        if (limit) {
            try {
                Double.parseDouble(price.getText());
            } catch (NumberFormatException e) {
                Window.alert("Placing order failed! Invalid price value");
                return false;
            }
        }
        
        return true;
    }
    
    protected MarketOrderRequest createOrder(OrderSide side, TextBox amount, TextBox price, boolean limit ) {
        if(limit) {
            LimitOrderRequest order = new LimitOrderRequest();
            order.setAmount(Double.parseDouble(amount.getText()));
            order.setPrice(Double.parseDouble(price.getText()));
            order.setSide(side);
            order.setInstrument(Instrument.STKMON);
            return order;
        } else {
            MarketOrderRequest order = new MarketOrderRequest();
            order.setAmount(Double.parseDouble(amount.getText()));
            order.setSide(side);
            order.setInstrument(Instrument.STKMON);
            return order;
        }
    }
}
