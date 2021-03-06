package env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class RestaurantWorldModel extends GridWorldModel {
    public static final int TABLE = 16;
    public static final int DEBRY = 32;
    public static final int COUNTER = 64;
    
    public static RestaurantWorldModel model;

    private int agentOrders[];

    private HashMap<Integer, Integer> timeToCook = new HashMap<>();

    public static final int cookCount = 1;
    public static final int waiterCount = 1;

    private List<Location> tableLocations;
    private List<Location> counterLocations;
    private List<Integer> activeOrders;

    public enum Move {
        UP, DOWN, LEFT, RIGHT
    };

    private RestaurantView restaurantView;

    public enum TableState {
        Empty,
        WaitingToOrder,
        WaitingForFood,
        Eating,
        WaitingToPay
    };

    public void step() {
        for (Location location : guestEatingTimes.keySet()) {
            int value = guestEatingTimes.get(location);
            value--;
            if (value != 0) {
                guestEatingTimes.put(location, value);
                continue;
            }
            guestEatingTimes.remove(location);
            advanceGuestAt(location);
        }

        for (int foodId : timeToCook.keySet()) {
            int value = timeToCook.get(foodId) - 1;
            if (value < 0) {
                continue;
            }
            if (value != 0) {
                timeToCook.put(foodId, value);
                continue;
            }

            boolean found = false;
            for (int i = 0; i < counterLocations.size(); i++) {
                if (counterFoodIds.containsKey(counterLocations.get(i))) {
                    continue;
                }

                Location location = counterLocations.get(i);
                counterFoodIds.put(location, foodId);
                restaurantView.update(location);

                activeOrders.remove(new Integer(foodId));

                timeToCook.put(foodId, -1);

                found = true;
                break;
            }
            if (!found) {
                timeToCook.put(foodId, 0);
            }
        }
    }

    HashMap<Location, Integer> tableGuestIds = new HashMap<>();
    HashMap<Location, TableState> tableGuestStates = new HashMap<>();
    HashMap<Location, Integer> guestEatingTimes = new HashMap<>();
    HashMap<Location, Integer> counterFoodIds = new HashMap<>();

    public static RestaurantWorldModel get() {
        return model;
    }

    @Override
    public void setView(GridWorldView view) {
        super.setView(view);
        restaurantView = (RestaurantView)view;
    }

    public RestaurantWorldModel(int width, int height, int numberOfAgents) {
        super(width, height, numberOfAgents);
        agentOrders = new int[numberOfAgents];
        for (int i = 0; i < numberOfAgents; i++) {
            agentOrders[i] = -1;
        }

        initWorld(width, height);
        model = this;
    }

    private void initWorld(int width, int height) {
        addWall(2, 5, width - 2, 5);
        addWall(2, 5, 2, height - 5);
        addWall(width - 2, 5, width - 2, height - 5);
        addWall(2, height - 5, width - 2, height - 5);
        addWall(7, 5, 7, height - 5 - 3 - 2);
        addWall(7, height - 5 - 3, 7, height - 5);

        activeOrders = new ArrayList<Integer>();

        tableLocations = new ArrayList<Location>();

        for (int x = 0; x < 6; x++) {
            add(TABLE, 10 + x * 3, 7);
            tableLocations.add(new Location(10 + x * 3, 7));
            add(TABLE, 10 + x * 3, height - 7);
            tableLocations.add(new Location(10 + x * 3, height - 7));
        }

        counterLocations = new ArrayList<Location>();

        for (int x = 3; x < 7; x++) {
            add(COUNTER, x, height - 5 - 5);
            counterLocations.add(new Location(x, height - 5 - 5));
        }

        for (int i = 0; i < cookCount; i++) {
            setAgPos(i, new Location(6, 6 + i));
        }

        for (int i = 0; i < waiterCount; i++) {
            setAgPos(cookCount + i, new Location(10 + i, height / 2));
        }
    }

    synchronized public boolean move(Move direction, int agent) {
        if (agent < 0) {
            return false;
        }

        Location agentLoc = getAgPos(agent);

        if (agentLoc == null) {
            return false;
        }

        Location next = null;
        switch (direction) {
        case UP:
            next = new Location(agentLoc.x, agentLoc.y - 1);
            break;
        case DOWN:
            next = new Location(agentLoc.x, agentLoc.y + 1);
            break;
        case LEFT:
            next = new Location(agentLoc.x - 1, agentLoc.y);
            break;
        case RIGHT:
            next = new Location(agentLoc.x + 1, agentLoc.y);
            break;
        }

        if (next == null) {
            return false;
        }
        if (!canMoveTo(agent, next)) {
            return false;
        }
        if (hasObject(AGENT, next)) {
            return false;
        }
        setAgPos(agent, next);
        restaurantView.update(agentLoc);
        restaurantView.update(next);
        return true;
    }

    private boolean canMoveTo(int agent, Location location) {
        return isFreeOfObstacle(location);
    }

    public int getGuestAtTable(Location location) {
        if (!tableGuestIds.containsKey(location)) {
            return -1;
        }
        return tableGuestIds.get(location);
    }

    public TableState getGuestStateAt(Location location) {
        if (!tableGuestIds.containsKey(location)) {
            return TableState.Empty;
        }
        return tableGuestStates.get(location);
    }

    public int getOrderAtCounter(Location location) {
        if (!counterFoodIds.containsKey(location)) {
            return -1;
        }
        return counterFoodIds.get(location);
    }

    public List<Location> getTableLocations() {
        return tableLocations;
    }

    public void placeNewGuest(Location location) {
        int newId = 0;
        while (tableGuestIds.containsValue(newId)) {
            newId++;
        }

        tableGuestIds.put(location, newId);
        tableGuestStates.put(location, TableState.WaitingToOrder);
        restaurantView.update(location);
    }

    public void advanceGuestAt(Location location) {
        TableState currentState = tableGuestStates.get(location);

        if (currentState == TableState.WaitingToPay) {
            tableGuestIds.remove(location);
            tableGuestStates.remove(location);
            restaurantView.update(location);
            return;
        }

        TableState[] states = {
            TableState.WaitingToOrder,
            TableState.WaitingForFood,
            TableState.Eating,
            TableState.WaitingToPay
        };

        for (int i = 0; i < states.length; i++) {
            if (states[i] != currentState) {
                continue;
            }
            tableGuestStates.put(location, states[i + 1]);
            restaurantView.update(location);
            if (states[i + 1] == TableState.Eating) {
                guestEatingTimes.put(location, 15);
            }
            break;
        }
    }

    public boolean isTableOccupied(Location location) {
        return tableGuestIds.containsKey(location);
    }

    public void placeFoodFor(Location location, int id) {
        counterFoodIds.put(location, id);
        restaurantView.update(location);
    }

    public boolean pickUpFood(int agentId) {
        Location location = getAgPos(agentId);
        if (!counterFoodIds.containsKey(location)) {
            return false;
        }
        int foodId = counterFoodIds.get(location);
        counterFoodIds.remove(location);
        agentOrders[agentId] = foodId;
        restaurantView.update(location);
        return true;
    }

    public boolean serveFood(int agentId) {
        Location location = getAgPos(agentId);
        if (!tableGuestIds.containsKey(location)) {
            return false;
        }
        if (tableGuestIds.get(location) != agentOrders[agentId]) {
            return false;
        }
        agentOrders[agentId] = -1;
        advanceGuestAt(location);
        return true;
    }

    public void placeDebry(Location location) {
        if (isFree(location)) {
            add(DEBRY, location);
        }
    }

    public void removeDebry(Location location) {
        remove(DEBRY, location);
    }

    public boolean takeOrder(int agentId) {
        Location agentPos = getAgPos(agentId);

        if (!hasObject(TABLE, agentPos.x, agentPos.y)) {
            return false;
        }

        if (!isTableOccupied(agentPos)) {
            return false;
        }

        if (tableGuestStates.get(agentPos) != TableState.WaitingToOrder) {
            return false;
        }

        activeOrders.add(tableGuestIds.get(agentPos));

        advanceGuestAt(agentPos);
        return true;
    }

    public boolean takePayment(int agentId) {
        Location agentPos = getAgPos(agentId);

        if (!tableGuestStates.containsKey(agentPos)) {
            return false;
        }
        if (tableGuestStates.get(agentPos) != TableState.WaitingToPay) {
            return false;
        }

        advanceGuestAt(agentPos);
        return true;
    }

    public boolean cleanDebry(int agentId) {
        Location location = getAgPos(agentId);

        if (!hasObject(DEBRY, location.x, location.y)) {
            return false;
        }

        remove(DEBRY, location.x, location.y);
        restaurantView.update(location);
        return true;
    }

    public boolean cookFood(int agentId, int foodId) {
        timeToCook.put(foodId, 15);
        return true;
    }

    public int getCounterFoodAt(Location location) {
        if (!counterFoodIds.containsKey(location)) {
            return -1;
        }
        return counterFoodIds.get(location);
    }

    public int getOrderOnAgent(int agentId) {
        return agentOrders[agentId];
    }

    public List<Integer> getActiveOrders() {
        return activeOrders;
    }

    public Set<Location> getGuests() {
        return tableGuestIds.keySet();
    }

    public List<Location> getCounters() {
        return counterLocations;
    }
}
