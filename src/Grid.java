import java.awt.Graphics;
import java.awt.Point;
import java.util.Optional;
import java.util.Random;

public class Grid {
  Cell[][] cells = new Cell[20][20];
  Item squareItem;
  Item circleItem;
  Item triangleItem;

  public Grid() {
    Random rand = new Random();
    for(int i=0; i<cells.length; i++) {
      for(int j=0; j<cells[i].length; j++) {
        cells[i][j] = new Cell(colToLabel(i), j, 10+Cell.size*i, 10+Cell.size*j);
        int randomEnv = rand.nextInt(3); 
        cells[i][j].setEnvironment(randomEnv);
      }
    }

    // use ItemManager to place items
    ItemPlacer.placeItems(this);
  }

  private char colToLabel(int col) {
    return (char) (col + Character.valueOf('A'));
  }

  private int labelToCol(char col) {
    return (int) (col - Character.valueOf('A'));
  }

  public void paint(Graphics g, Point mousePos) {
    for(int i=0; i<cells.length; i++) {
      for(int j=0; j<cells[i].length; j++) {
        cells[i][j].paint(g, mousePos);
      }
    }

    // draw items
    if (squareItem != null) {
      int cx = 10 + Cell.size * squareItem.getX();
      int cy = 10 + Cell.size * squareItem.getY();
      squareItem.paint(g, cx, cy, Cell.size);
    }
    if (circleItem != null) {
      int cx = 10 + Cell.size * circleItem.getX();
      int cy = 10 + Cell.size * circleItem.getY();
      circleItem.paint(g, cx, cy, Cell.size);
    }
    if (triangleItem != null) {
      int cx = 10 + Cell.size * triangleItem.getX();
      int cy = 10 + Cell.size * triangleItem.getY();
      triangleItem.paint(g, cx, cy, Cell.size);
    }
  }

  public Optional<Cell> cellAtColRow(int c, int r) {
    if(c >= 0 && c < cells.length && r >=0 && r < cells[c].length) {
      return Optional.of(cells[c][r]);
    } else {
      return Optional.empty();
    }
  }

  public Optional<Cell> cellAtColRow(char c, int r) {
    return cellAtColRow(labelToCol(c), r);
  }

  public Optional<Cell> cellAtPoint(Point p) {
    for(int i=0; i < cells.length; i++) {
      for(int j=0; j < cells[i].length; j++) {
        if(cells[i][j].contains(p)) {
          return Optional.of(cells[i][j]);
        }
      }
    }
    return Optional.empty();
  }
}
