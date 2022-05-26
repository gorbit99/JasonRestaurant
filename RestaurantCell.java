
public class RestaurantCell {
	private boolean debry = false;
	
	public RestaurantCell() {
	}
	
	public void cleanDebry() {
		debry = false;
	}
	
	public void placeDebry() {
		debry = true;	
	}
	
	public boolean hasDebry() {
		return debry;	
	}
}
