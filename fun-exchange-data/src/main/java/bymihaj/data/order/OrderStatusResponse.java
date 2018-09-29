package bymihaj.data.order;

import java.util.ArrayList;
import java.util.List;

public class OrderStatusResponse {

	protected List<LimitOrderResponse> orders;
	
	public OrderStatusResponse() {
		orders = new ArrayList<>();
	}
	
	public List<LimitOrderResponse> getOrders() {
		return orders;
	}
}
