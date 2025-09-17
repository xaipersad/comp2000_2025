import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Stage {
  Grid grid;
  List<Actor> actors;
  Dog dog;
  boolean showDogMoves = false;
  List<Cell> dogMoveOptions = new ArrayList<>();

  public Stage() {
    grid = new Grid();
    actors = new ArrayList<Actor>();
    Random rand = new Random();
    int x1 = rand.nextInt(20);
    int y1 = rand.nextInt(20);
    int x2, y2, x3, y3;
    do {
      x2 = rand.nextInt(20);
      y2 = rand.nextInt(20);
    } while (x2 == x1 && y2 == y1);
    do {
      x3 = rand.nextInt(20);
      y3 = rand.nextInt(20);
    } while ((x3 == x1 && y3 == y1) || (x3 == x2 && y3 == y2));
    actors.add(new Cat(grid.cellAtColRow(x1, y1).get()));
    dog = new Dog(grid.cellAtColRow(x2, y2).get());
    actors.add(dog);
    actors.add(new Bird(grid.cellAtColRow(x3, y3).get()));
  }

  public void paint(Graphics g, Point mouseLoc) {
    grid.paint(g, mouseLoc);
    // shows dog moves
    if (showDogMoves && dogMoveOptions != null) {
      g.setColor(Color.LIGHT_GRAY);
      for (Cell c : dogMoveOptions) {
        g.fillRect(c.x, c.y, Cell.size, Cell.size);
        g.setColor(Color.BLACK);
        g.drawRect(c.x, c.y, Cell.size, Cell.size);
        g.setColor(Color.LIGHT_GRAY);
      }
    }
    for(Actor a: actors) {
      a.paint(g);
    }
    Optional<Cell> underMouse = grid.cellAtPoint(mouseLoc);
    if(underMouse.isPresent()) {
      Cell hoverCell = underMouse.get();
      g.setColor(Color.DARK_GRAY);
      g.drawString(String.valueOf(hoverCell.col) + String.valueOf(hoverCell.row), 740, 30);
    }
  }

  public void handleClick(Point mouseLoc) {
    Optional<Cell> clicked = grid.cellAtPoint(mouseLoc);
    if (clicked.isPresent()) {
      Cell cell = clicked.get();
      // when dog is clicked show moves
      if (!showDogMoves && cell == dog.loc) {
        showDogMoves = true;
        dogMoveOptions.clear();
        int[] dx = {-1, 0, 1, 0, -1, -1, 1, 1};
        int[] dy = {0, -1, 0, 1, -1, 1, -1, 1};
        int nx = cell.x / Cell.size;
        int ny = cell.y / Cell.size;
        for (int i = 0; i < dx.length; i++) {
          int tx = nx + dx[i];
          int ty = ny + dy[i];
          if (tx >= 0 && tx < 20 && ty >= 0 && ty < 20) {
            Optional<Cell> opt = grid.cellAtColRow(tx, ty);
            if (opt.isPresent()) dogMoveOptions.add(opt.get());
          }
        }
      } else if (showDogMoves && dogMoveOptions.contains(cell)) {
        // move dog to clicked cell
        dog.setLocation(cell);
        showDogMoves = false;
        dogMoveOptions.clear();

        // move actors toward their items
        // cat -> square bird -> triangle
        for (int i = 0; i < actors.size(); i++) {
          Actor a = actors.get(i);
          if (a != dog) {
            int targetX = -1, targetY = -1;
            if (a instanceof Cat) {
              targetX = grid.squareItem.getX();
              targetY = grid.squareItem.getY();
            } else if (a instanceof Bird) {
              targetX = grid.triangleItem.getX();
              targetY = grid.triangleItem.getY();
            } else {
              continue;
            }
            int ax = a.loc.x / Cell.size;
            int ay = a.loc.y / Cell.size;
            int dx = Integer.compare(targetX, ax);
            int dy = Integer.compare(targetY, ay);
            int newX = ax + dx;
            int newY = ay + dy;
            if (newX >= 0 && newX < 20 && newY >= 0 && newY < 20) {
              Optional<Cell> nextCell = grid.cellAtColRow(newX, newY);
              if (nextCell.isPresent()) {
                try {
                  a.getClass().getMethod("setLocation", Cell.class).invoke(a, nextCell.get());
                } catch (Exception ex) {
                  a.loc = nextCell.get();
                  // update actor on grid
                  if (a instanceof Cat) {
                    ((Cat)a).display = new java.util.ArrayList<>();
                    Cell l = a.loc;
                    java.awt.Polygon ear1 = new java.awt.Polygon();
                    ear1.addPoint(l.x + 11, l.y + 5);
                    ear1.addPoint(l.x + 15, l.y + 15);
                    ear1.addPoint(l.x + 7, l.y + 15);
                    java.awt.Polygon ear2 = new java.awt.Polygon();
                    ear2.addPoint(l.x + 22, l.y + 5);
                    ear2.addPoint(l.x + 26, l.y + 15);
                    ear2.addPoint(l.x + 18, l.y + 15);
                    java.awt.Polygon face = new java.awt.Polygon();
                    face.addPoint(l.x + 5, l.y + 15);
                    face.addPoint(l.x + 29, l.y + 15);
                    face.addPoint(l.x + 17, l.y + 30);
                    ((Cat)a).display.add(face);
                    ((Cat)a).display.add(ear1);
                    ((Cat)a).display.add(ear2);
                  } 
                  else if (a instanceof Bird) {
                    ((Bird)a).display = new java.util.ArrayList<>();
                    Cell l = a.loc;
                    java.awt.Polygon wing1 = new java.awt.Polygon();
                    wing1.addPoint(l.x + 5, l.y + 5);
                    wing1.addPoint(l.x + 15, l.y + 17);
                    wing1.addPoint(l.x + 5, l.y + 17);
                    java.awt.Polygon wing2 = new java.awt.Polygon();
                    wing2.addPoint(l.x + 30, l.y + 5);
                    wing2.addPoint(l.x + 20, l.y + 17);
                    wing2.addPoint(l.x + 30, l.y + 17);
                    java.awt.Polygon body = new java.awt.Polygon();
                    body.addPoint(l.x + 15, l.y + 10);
                    body.addPoint(l.x + 20, l.y + 10);
                    body.addPoint(l.x + 20, l.y + 25);
                    body.addPoint(l.x + 15, l.y + 25);
                    ((Bird)a).display.add(body);
                    ((Bird)a).display.add(wing1);
                    ((Bird)a).display.add(wing2);
                  }
                }
              }
            }
          }
        }
      } else {
        showDogMoves = false;
        dogMoveOptions.clear();
      }
    } else {
      showDogMoves = false;
      dogMoveOptions.clear();
    }
  }
}
