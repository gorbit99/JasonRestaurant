

public class TableCell extends RestaurantCell {
	public enum OrderState {
		Empty,
		WaitingToOrder,
		WaitingForFood,
		Eating,
		WaitingToPay,
	}
	
	private OrderState state = OrderState.Empty;
	
	public OrderState getOrderState() {
		return state;
	}
	
	public void toNextOrderState() {
		OrderState order[] = {
			OrderState.Empty, 
			OrderState.WaitingToOrder, 
			OrderState.WaitingForFood, 
			OrderState.Eating,
			OrderState.WaitingToPay,
		};
		
		for (int i = 0; i < order.length; i++) {
			if (state == order[i]) {
				state = order[(i + 1) % order.length];
				break;
			}
		}
	}
}
