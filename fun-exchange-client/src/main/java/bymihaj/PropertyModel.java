package bymihaj;

import javafx.beans.property.SimpleStringProperty;

public class PropertyModel {
    
    protected SimpleStringProperty name;
    protected SimpleStringProperty amount;
    
    public PropertyModel(Property property) {
        name = new SimpleStringProperty(property.getName().name());
        amount = new SimpleStringProperty(property.getAmount().toPlainString());
    }
    
    public SimpleStringProperty getName() {
        return name;
    }
    public SimpleStringProperty getAmount() {
        return amount;
    }

}
