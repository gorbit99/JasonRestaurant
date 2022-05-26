import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class RestaurantWorldModel extends GridWorldModel {
    public enum Move {
        UP, DOWN, LEFT, RIGHT
    };

    public RestaurantWorldModel(int width, int height, int numberOfAgents) {
        super(width, height, numberOfAgents);


    }

    public RestaurantWorldModel create(
        int width,
        int height,
        int numberOfAgents
    ) {
        return new RestaurantWorldModel(width, height, numberOfAgents);
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
        return true;
    }

    private boolean canMoveTo(int agent, Location location) {
        return isFreeOfObstacle(location);
    }
}
