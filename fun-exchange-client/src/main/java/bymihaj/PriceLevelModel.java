package bymihaj;

import javafx.beans.property.SimpleStringProperty;

public class PriceLevelModel {
    
    private SimpleStringProperty price;
    private SimpleStringProperty amount;
    
    public PriceLevelModel(double price, double amount) {
        this.price = new SimpleStringProperty(String.valueOf(price));
        this.amount = new SimpleStringProperty(String.valueOf(amount));
    }

    public SimpleStringProperty getPrice() {
        return price;
    }

    public SimpleStringProperty getAmount() {
        return amount;
    }
    
}
