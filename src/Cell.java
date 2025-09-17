import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class Cell extends Rectangle {
  static int size = 35;
  char col;
  int row;
  int environment = 0; // environment type: 0 = grass, 1 = water, 2 = mountain

  public Cell(char inCol, int inRow, int x, int y) {
    super(x, y, size, size);
    col = inCol;
    row = inRow;
    environment = 0; 
  }

  // set the env of the cell
  public void setEnvironment(int f) {
    environment = f;
  }

  // get the env of the cell
  public int getEnvironment() {
    return environment;
  }

  public void paint(Graphics g, Point mousePos) {
    // set color based on environment
    Color cellColor;
    if (environment == 1) {
      cellColor = new Color(0,100,150); // water
    } else if (environment == 2) {
      cellColor = Color.darkGray; // mountain
    } else {
      cellColor = new Color(0,100,0); // grass
    }

    if (contains(mousePos)) {
      g.setColor(Color.GRAY);
    } else {
      g.setColor(cellColor);
    }
    g.fillRect(x, y, size, size);
    g.setColor(Color.BLACK);
    g.drawRect(x, y, size, size);
  }

  public boolean contains(Point p) {
    if(p != null) {
      return super.contains(p);
    } else {
      return false;
    }
  }
}
