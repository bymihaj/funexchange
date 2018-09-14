package bymihaj;

import java.util.Date;

import bymihaj.data.order.RejectOrderResponse;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class NotificationPane extends ScrollPane {
    
    protected VBox hbox;
    
    public NotificationPane() {
        hbox = new VBox();
        setContent(hbox);
    }
    
    public void onReject(RejectOrderResponse reject) {
        Label label = new Label(new Date().toString() + ": " + reject.getReason());
        hbox.getChildren().add(label);
    }

}
