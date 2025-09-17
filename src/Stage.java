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
    actors.add(new Dog(grid.cellAtColRow(x2, y2).get()));
    actors.add(new Bird(grid.cellAtColRow(x3, y3).get()));
  }

  public void paint(Graphics g, Point mouseLoc) {
    grid.paint(g, mouseLoc);
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
}
