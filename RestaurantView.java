import jason.environment.grid.GridWorldView;

public class RestaurantView extends GridWorldView {
    private RestaurantEnvironment env;

    public RestaurantView(RestaurantWorldModel model, int windowSize) {
        super(model, "Restaurant", windowSize);
        setVisible(true);
        repaint();
    }

    public void setEnvironment(RestaurantEnvironment env) {
        this.env = env;
    }
}
