package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import jason.environment.grid.Location;
import jason.asSyntax.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Font;

public class RestaurantView extends GridWorldView {
    private RestaurantEnvironment env;
    private RestaurantWorldModel restaurantModel;

    public RestaurantView(RestaurantWorldModel model, int windowSize) {
        super(model, "Restaurant", windowSize);

        this.restaurantModel = model;

        setVisible(true);
        repaint();
    }

    public void setEnvironment(RestaurantEnvironment env) {
        this.env = env;
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
        case RestaurantWorldModel.TABLE:
            drawTable(g, x, y);
            break;
        case RestaurantWorldModel.DEBRY:
            drawDebry(g, x, y);
            break;
        case RestaurantWorldModel.COUNTER:
            drawCounter(g, x, y);
            break;
        }
    }

    private void drawCenteredText(Graphics g, String string, int x, int y) {
        Font font = g.getFont();
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(string);
        int textHeight = metrics.getHeight();
        int ascent = metrics.getAscent();

        g.drawString(string, x - textWidth / 2, y - textHeight / 2 + ascent);
    }

    private void drawTable(Graphics g, int x, int y) {
        g.setColor(new Color(97, 69, 43));
        g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 2, cellSizeH - 2);

        int guestId = restaurantModel.getGuestAtTable(new Location(x, y));
        if (guestId == -1) {
            return;
        }

        g.setColor(Color.white);

        drawCenteredText(
            g,
            Integer.toString(guestId),
            x * cellSizeW + cellSizeW / 2,
            y * cellSizeH + cellSizeH / 2
        );
    }

    private void drawDebry(Graphics g, int x, int y) {
        g.setColor(Color.gray);
        g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 2, cellSizeH - 2);
    }

    private void drawCounter(Graphics g, int x, int y) {
        g.setColor(Color.lightGray);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);

        int orderId = restaurantModel.getOrderAtCounter(new Location(x, y));
        if (orderId == -1) {
            return;
        }

        drawCenteredText(
            g,
            Integer.toString(orderId),
            x * cellSizeW + cellSizeW / 2,
            y * cellSizeH + cellSizeH / 2
        );
    }

    public void update(Location location) {
        update(location.x, location.y);
    }
}
