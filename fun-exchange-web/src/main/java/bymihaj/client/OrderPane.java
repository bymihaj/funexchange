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
    protected TextBox amountTbx;
    protected TextBox priceTbx;
    
    protected RadioButton limitRbt;
    
    public OrderPane(Connection conn) {
        this.conn = conn;
        
        Button sellBtn = new Button();
        sellBtn.setText("SELL");
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
        
        amountTbx = new TextBox();
        amountTbx.setText("10");
        priceTbx = new TextBox();
        priceTbx.setText("1.00");
        
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
        
        setWidth("250px");
        setHeight("120px");
    }
    
    public boolean validateInput() {
        try {
            Double.parseDouble(amountTbx.getText());
        } catch (NumberFormatException e) {
            Window.alert("Placing order failed! Invalid amount value!" );
            return false;
        }
        
        if (limitRbt.getValue()) {
            try {
                Double.parseDouble(priceTbx.getText());
            } catch (NumberFormatException e) {
                Window.alert("Placing order failed! Invalid price value");
                return false;
            }
        }
        
        return true;
    }
    
    protected MarketOrderRequest createOrder(OrderSide side) {
        if(limitRbt.getValue()) {
            LimitOrderRequest order = new LimitOrderRequest();
            order.setAmount(Double.parseDouble(amountTbx.getText()));
            order.setPrice(Double.parseDouble(priceTbx.getText()));
            order.setSide(side);
            order.setInstrument(Instrument.STKMON);
            return order;
        } else {
            MarketOrderRequest order = new MarketOrderRequest();
            order.setAmount(Double.parseDouble(amountTbx.getText()));
            order.setSide(side);
            order.setInstrument(Instrument.STKMON);
            return order;
        }
    }
}
