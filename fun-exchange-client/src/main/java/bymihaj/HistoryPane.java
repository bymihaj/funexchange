package bymihaj;

import bymihaj.data.order.OrderSide;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class HistoryPane extends ScrollPane {

    protected VBox vbox;
    
    public HistoryPane() {
        vbox = new VBox();
        setContent(vbox);
    }
    
    public void onHistory(TradeHistory history) {
        String text = history.getDateTime() + " "+history.getAmount() + " "+ history.getPrice();
        Label label = new Label(text);
        if(OrderSide.BUY.equals(history.getSide())) {
            label.setStyle("-fx-text-fill: green");
        } else {
            label.setStyle("-fx-text-fill: red");
        }
        vbox.getChildren().add(label);
    }
}
