// Environment code for project restaurant.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import jason.asSyntax.parser.*;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.util.Random;

public class RestaurantEnvironment extends TimeSteppedEnvironment {

    private Logger logger = Logger.getLogger("restaurant.mas2j."+RestaurantEnvironment.class.getName());
	
    Random random = new Random();

    private RestaurantWorldModel model;
    private RestaurantView view;

    private final int worldWidth = 30;
    private final int worldHeight = 30;
    private final int numberOfAgents =
        RestaurantWorldModel.cookCount + RestaurantWorldModel.waiterCount;
    private final int windowSize = 600;
	
	public RestaurantEnvironment() {
        model = new RestaurantWorldModel(
            worldWidth,
            worldHeight,
            numberOfAgents
        );

        view = new RestaurantView(model, windowSize);
        view.setEnvironment(this);
        model.setView(view);
	}

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(new String[] { "500" });
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

    int timeTillNextGuest = 0;

    @Override
    protected void stepStarted(int step) {
        logger.info("---------------------Step " + step + "-----------------");

        //Do Step
        if (timeTillNextGuest <= 0) {
            logger.info("New guest arrived!");
            placeNewGuest();
            timeTillNextGuest = random.nextInt(40) + 10;
        } else {
            timeTillNextGuest--;
        }
    }

    @Override
    protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
    }

    private void placeNewGuest() {
        List<Location> tables = model.getTableLocations();

        List<Location> emptyTables = new ArrayList<>();
        for (Location table : tables) {
            if (model.isTableOccupied(table)) {
                continue;
            }
            emptyTables.add(table);
        }

        if (emptyTables.size() == 0) {
            return;
        }

        int randomTable = random.nextInt(emptyTables.size());
        model.placeNewGuest(emptyTables.get(randomTable));
    }
}

