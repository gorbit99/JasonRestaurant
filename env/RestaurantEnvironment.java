// Environment code for project restaurant.mas2j
package env;

import jason.asSyntax.*;
import jason.environment.*;
import jason.asSyntax.parser.*;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.util.Random;
import java.util.Set;

import env.RestaurantWorldModel.Move;
import env.RestaurantWorldModel.TableState;

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

    private int getAgNbFromName(String name) {
        if (name.startsWith("cook")) {
            return 0;
        }
        if (name.startsWith("waiter")) {
            return 1;
        }
        return -1;
    }

    @Override
    protected int requiredStepsForAction(String agentName, Structure action) {
        if (!action.getFunctor().equals("do")) {
            return super.requiredStepsForAction(agentName, action);
        }
        return 1;
    }

    @Override
    public boolean executeAction(String agent, Structure action) {
        int agentId = getAgNbFromName(agent);

        if (!action.getFunctor().equals("do")) {
            return false;
        }

        String command = action.getTerm(0).toString();

        if (command.equals("up")) {
            return model.move(Move.UP, agentId);
        } else if (command.equals("down")) {
            return model.move(Move.DOWN, agentId);
        } else if (command.equals("left")) {
            return model.move(Move.LEFT, agentId);
        } else if (command.equals("right")) {
            return model.move(Move.RIGHT, agentId);
        } else if (command.equals("takeOrder")) {
            return model.takeOrder(agentId);
        } else if (command.equals("pickUpFood")) {
            return model.pickUpFood(agentId);
        } else if (command.equals("serveFood")) {
            return model.serveFood(agentId);
        } else if (command.equals("takePayment")) {
            return model.takePayment(agentId);
        } else if (command.equals("cleanDebry")) {
            return model.cleanDebry(agentId);
        } else if (command.equals("cookFood")) {
            int foodId = Integer.parseInt(action.getTerm(1).toString());
            return model.cookFood(agentId, foodId);
        }

        return true;
    }
	
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
        super.init(new String[] { "100" });
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }

    int timeTillNextGuest = 0;
    int timeTillNextDebry = 13;

    @Override
    protected void stepStarted(int step) {
        logger.info("---------------------Step " + step + "-----------------");
        model.step();

        //Do Step
        if (timeTillNextGuest <= 0) {
            logger.info("New guest arrived!");
            placeNewGuest();
            timeTillNextGuest = random.nextInt(40) + 10;
        } else {
            timeTillNextGuest--;
        }

        if (timeTillNextDebry <= 0) {
            int x = random.nextInt(worldWidth - 6 - 6) + 9;
            int y = random.nextInt(worldWidth - 10) + 5;
            model.placeDebry(new Location(x, y));
            timeTillNextDebry = random.nextInt(20) + 10;
        } else {
            timeTillNextDebry--;
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

    @Override
    public void updateAgsPercept() {
        List<Integer> activeOrders = model.getActiveOrders();
        for (int order : activeOrders) {
            Literal p = ASSyntax.createLiteral(
                "activeOrder",
                ASSyntax.createNumber(order)
            );
            addPercept(p);
        }

        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    private String getAgNameFromID(int agentId) {
        if (agentId < RestaurantWorldModel.cookCount) {
            return "cook";
        }
        return "waiter";
    }

    private void updateAgPercept(int ag) {
        updateAgPercept(getAgNameFromID(ag), ag);
    }

    private void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        Literal pos = ASSyntax.createLiteral(
            "pos",
            ASSyntax.createNumber(l.x),
            ASSyntax.createNumber(l.y)
        );
        addPercept(agName, pos);

        int orderOnAgent = model.getOrderOnAgent(ag);
        if (orderOnAgent != -1) {
            Literal cg = ASSyntax.createLiteral("foodOnAgent", ASSyntax.createNumber(orderOnAgent));
            addPercept(agName, cg);
        }

        Set<Location> guests = model.getGuests();
        for (Location location : guests) {
            TableState state = model.getGuestStateAt(location);
            int guestId = model.getGuestAtTable(location);
            String percept = "";
            switch (state) {
                case WaitingToOrder:
                    percept = "waitingToOrder";
                    break;
                case WaitingForFood:
                    percept = "waitingForFood";
                    break;
                case Eating:
                    percept = "eating";
                    break;
                case WaitingToPay:
                    percept = "waitingToPay";
                    break;
            }
            Literal cg = ASSyntax.createLiteral(
                percept,
                ASSyntax.createNumber(guestId),
                ASSyntax.createNumber(location.x),
                ASSyntax.createNumber(location.y)
            );
            addPercept(agName, cg);
        }

        List<Location> counters = model.getCounters();
        for (Location location : counters) {
            int counterFood = model.getCounterFoodAt(location);
            if (counterFood == -1) {
                continue;
            }
            Literal cg = ASSyntax.createLiteral(
                "foodOnCounter",
                ASSyntax.createNumber(counterFood),
                ASSyntax.createNumber(location.x),
                ASSyntax.createNumber(location.y)
            );
            addPercept(agName, cg);
        }

        // what's around
        updateAgPercept(agName, ag, l.x - 1, l.y - 1);
        updateAgPercept(agName, ag, l.x - 1, l.y);
        updateAgPercept(agName, ag, l.x - 1, l.y + 1);
        updateAgPercept(agName, ag, l.x, l.y - 1);
        updateAgPercept(agName, ag, l.x, l.y);
        updateAgPercept(agName, ag, l.x, l.y + 1);
        updateAgPercept(agName, ag, l.x + 1, l.y - 1);
        updateAgPercept(agName, ag, l.x + 1, l.y);
        updateAgPercept(agName, ag, l.x + 1, l.y + 1);
    }

    public static Atom aDebry = new Atom("debry");
    public static Atom aEmpty = new Atom("empty");

    private void updateAgPercept(String agName, int agId, int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;
        boolean isEmpty = true;
        if (model.hasObject(RestaurantWorldModel.DEBRY, x, y)) {
            addPercept(agName, createCellPerception(x, y, aDebry));
            isEmpty = false;
        }


        int otherag = model.getAgAtPos(x, y);
        if (isEmpty) {
            addPercept(agName, createCellPerception(x, y, aEmpty));
        }
    }

    public static Literal createCellPerception(int x, int y, Atom obj) {
        return ASSyntax.createLiteral("cell",
                                      ASSyntax.createNumber(x),
                                      ASSyntax.createNumber(y),
                                      obj);
    }
}

