package bymihaj;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.OrderStatusResponse;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class PendingPane extends ScrollPane {
	
	protected VBox vbox;
	protected Map<Long, Label> idMap;
	
	public PendingPane() {
		vbox = new VBox();
		setContent(vbox);
		idMap = new ConcurrentHashMap<>();
	}
	
	public void onPending(LimitOrderResponse limit) {
		Label label;
		if(idMap.containsKey(limit.getId())) {
			label = idMap.get(limit.getId());
		} else {
			label = new Label();
			vbox.getChildren().add(label);
			idMap.put(limit.getId(), label);
		}
		
		if(limit.getRequiredAmount() > 0.0) {
			label.setText("#"+limit.getId()+" ("+limit.getAmount()+"/"+limit.getFilledAmount()+") "+limit.getSide()+" "+limit.getPrice());
		} else {
			idMap.remove(limit.getId());
			vbox.getChildren().remove(label);
		}
	}
	
	public void onStatus(OrderStatusResponse resp) {
		resp.getOrders().forEach( l -> onPending(l) );
	}

}
