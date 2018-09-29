package bymihaj;



import bymihaj.data.order.LimitOrderRequest;
import bymihaj.data.order.MarketOrderRequest;
import bymihaj.data.order.OrderSide;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class PlaceOrderPane extends VBox {

    protected Connection conn;
    
    protected TextField amountField;
    protected RadioButton limitRb;
    protected TextField priceField;
    
    public PlaceOrderPane(Connection connection) {
        this.conn = connection;
        setSpacing(6);
        Button sellBtn = new Button("SELL");
        sellBtn.setOnAction(this:: sellAction);
        sellBtn.setFont(new Font(18));
        sellBtn.setMinWidth(67);
        
        Button buyBtn = new Button("BUY");
        buyBtn.setOnAction(this::buyAction);
        buyBtn.setFont(new Font(18));
        buyBtn.setMinWidth(67);
        
        HBox buttonHolder = new HBox(6);
        buttonHolder.getChildren().addAll(sellBtn, buyBtn);
        getChildren().add(buttonHolder);
        
        Label amountLbl = new Label("Amount:");
        amountLbl.setMinWidth(60);
        amountField = new TextField("10");
        amountField.setMaxWidth(80);
        HBox amountBox = new HBox();
        amountBox.getChildren().addAll(amountLbl, amountField);
        getChildren().add(amountBox);
        
        Label priceLbl = new Label("Price");
        priceLbl.setMinWidth(60);
        priceField = new TextField("1.00");
        priceField.setMaxWidth(80);
        HBox priceBox = new HBox();
        priceBox.getChildren().addAll(priceLbl, priceField);
        getChildren().add(priceBox);
        
        ToggleGroup typeToggle = new ToggleGroup();
        limitRb = new RadioButton("Limit");
        limitRb.setSelected(true);
        limitRb.setToggleGroup(typeToggle);
        RadioButton marketRb = new RadioButton("Market");
        marketRb.setToggleGroup(typeToggle);
        HBox typeBox = new HBox(6);
        typeBox.getChildren().addAll(limitRb, marketRb);
        getChildren().add(typeBox);
        
        limitRb.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                priceField.setDisable(!newValue);
            }
        });
    }
    
    public void sellAction(ActionEvent e) {
        validateInput();
        conn.send(createOrder(OrderSide.SELL));
    }
    
    public void buyAction(ActionEvent e) {
        validateInput();
        conn.send(createOrder(OrderSide.BUY));
    }
    
    protected MarketOrderRequest createOrder(OrderSide side) {
    	if(limitRb.isSelected()) {
    		LimitOrderRequest order = new LimitOrderRequest();
    		order.setAmount(Double.parseDouble(amountField.getText()));
    		order.setPrice(Double.parseDouble(priceField.getText()));
    		order.setSide(side);
    		order.setInstrument(Instrument.STKMON);
    		return order;
    	} else {
    		MarketOrderRequest order = new MarketOrderRequest();
    		order.setAmount(Double.parseDouble(amountField.getText()));
    		order.setSide(side);
    		order.setInstrument(Instrument.STKMON);
    		return order;
    	}
    }
    
    public boolean validateInput() {
        try {
            Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Placing order failed!");
            alert.setHeaderText("Invalid amount value!");
            alert.showAndWait();
            return false;
        }
        
        if (limitRb.isSelected()) {
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Placing order failed");
                alert.setHeaderText("Invalid price value");
                alert.showAndWait();
                return false;
            }
        }
        
        return true;
    }
    
    
    
}
