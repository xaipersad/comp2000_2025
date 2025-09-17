import java.awt.Color;
import java.awt.Graphics;

public class Item {
    
    int type;
    int x, y; 

    public Item(int type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public void paint(Graphics g, int cellX, int cellY, int cellSize) {
         // square cat
        if (type == 0) {
            g.setColor(Color.red);
            g.fillRect(cellX + 8, cellY + 8, cellSize - 16, cellSize - 16);
        } 
        //circle dog
        else if (type == 1) {
            g.setColor(Color.magenta);
            g.fillOval(cellX + 8, cellY + 8, cellSize - 16, cellSize - 16);
        } 
        
        //triangle bird
        else if (type == 2) {
            g.setColor(Color.orange);
            int[] xPoints = {cellX + cellSize / 2, cellX + 10, cellX + cellSize - 10};
            int[] yPoints = {cellY + 10, cellY + cellSize - 10, cellY + cellSize - 10};
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }

    public int getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
