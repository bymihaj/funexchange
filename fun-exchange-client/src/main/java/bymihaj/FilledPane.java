package bymihaj;

import bymihaj.data.order.LimitOrderResponse;
import bymihaj.data.order.MarketOrderResponse;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class FilledPane extends ScrollPane {
	
	protected VBox vbox;
	
	public FilledPane() {
		vbox = new VBox();
		setContent(vbox);
	}
	
	public void onLimit(LimitOrderResponse limit) {
		if(limit.getRequiredAmount() == 0.0) {
			Label label = new Label();
			label.setText("#"+limit.getId()+" ("+limit.getFilledAmount()+") "+limit.getPrice()+" LIMIT");
			vbox.getChildren().add(label);
		}
	}
	
	public void onMarket(MarketOrderResponse market) {
		Label label = new Label();
		label.setText("#"+market.getId()+" ("+market.getFilledAmount()+") MARKET");
		vbox.getChildren().add(label);
	}

}
