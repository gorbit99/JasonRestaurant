// Environment code for project restaurant.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import jason.asSyntax.parser.*;

import java.util.logging.*;

public class RestaurantEnvironment extends Environment {

    private Logger logger = Logger.getLogger("restaurant.mas2j."+RestaurantEnvironment.class.getName());
	
    private RestaurantWorldModel model;
    private RestaurantView view;

    private final int worldWidth = 30;
    private final int worldHeight = 30;
    private final int numberOfAgents = 5;
    private final int windowSize = 600;
	
	public RestaurantEnvironment() {
        model = new RestaurantWorldModel(
            worldWidth,
            worldHeight,
            numberOfAgents
        );

        view = new RestaurantView(model, windowSize);
        view.setEnvironment(this);
	}

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        try {
			addPercept(ASSyntax.parseLiteral("percept(demo)"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action+", but not implemented!");
        if (true) { // you may improve this condition
             informAgsEnvironmentChanged();
        }
        return true; // the action was executed with success
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
