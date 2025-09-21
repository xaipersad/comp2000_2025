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
  int dogScore = 0;
  int catScore = 0;
  int birdScore = 0;

  public Stage() {
    grid = new Grid();
  actors = new ArrayList<Actor>();
    Random rand = new Random();
    int x1, y1, x2, y2, x3, y3;
    // Cat
    do {
      x1 = rand.nextInt(20);
      y1 = rand.nextInt(20);
    } while (grid.cells[x1][y1].getEnvironment() == 2);
    // Dog
    do {
      x2 = rand.nextInt(20);
      y2 = rand.nextInt(20);
    } while ((x2 == x1 && y2 == y1) || grid.cells[x2][y2].getEnvironment() == 2);
    // Bird
    do {
      x3 = rand.nextInt(20);
      y3 = rand.nextInt(20);
    } while (((x3 == x1 && y3 == y1) || (x3 == x2 && y3 == y2)) || grid.cells[x3][y3].getEnvironment() == 2);
    actors.add(new Cat(grid.cellAtColRow(x1, y1).get()));
    dog = new Dog(grid.cellAtColRow(x2, y2).get());
    actors.add(dog);
    actors.add(new Bird(grid.cellAtColRow(x3, y3).get()));
  }

  public void paint(Graphics g, Point mouseLoc) {
  grid.paint(g, mouseLoc);
  // shows dog moves
  if (showDogMoves && dogMoveOptions != null) {
    for (int i = 0; i < dogMoveOptions.size(); i++) {
      Cell c = dogMoveOptions.get(i);
      g.setColor(Color.WHITE);
      g.drawRect(c.x, c.y, Cell.size, Cell.size);
    }
  }
  for (int i = 0; i < actors.size(); i++) {
    Actor a = actors.get(i);
    a.paint(g);
  }
  Optional<Cell> underMouse = grid.cellAtPoint(mouseLoc);
  if (underMouse.isPresent()) {
    Cell hoverCell = underMouse.get();
    g.setColor(Color.DARK_GRAY);
    g.drawString(String.valueOf(hoverCell.col) + String.valueOf(hoverCell.row), 740, 30);
  }
  // scoreboard
  Scoreboard.paint(g, actors, dogScore, catScore, birdScore);
  }

  public void handleClick(Point mouseLoc) {
  Optional<Cell> clicked = grid.cellAtPoint(mouseLoc);
  if (clicked.isPresent()) {
    Cell cell = clicked.get();
    // when dog is clicked show moves
    if (!showDogMoves && cell == dog.loc) {
      DogMoveHelper.showDogMoves(this, cell);
    } else if (showDogMoves && dogMoveOptions.contains(cell)) {
      // move dog to clicked cell
      dog.setLocation(cell);
      showDogMoves = false;
      dogMoveOptions.clear();

      // move actors toward their items
      ActorMovement.moveActors(this);

      // check for item collect then respawn
      // dog
      if (dog.loc.x / Cell.size == grid.circleItem.getX() && dog.loc.y / Cell.size == grid.circleItem.getY()) {
        dogScore++;
        ItemRespawn.respawnCircle(this);
      }
      // cat and bird
      for (int i = 0; i < actors.size(); i++) {
        Actor a = actors.get(i);
        if (a.getClass().getSimpleName().equals("Cat")) {
          if (a.loc.x / Cell.size == grid.squareItem.getX() && a.loc.y / Cell.size == grid.squareItem.getY()) {
            catScore++;
            ItemRespawn.respawnSquare(this);
          }
        }
        if (a.getClass().getSimpleName().equals("Bird")) {
          if (a.loc.x / Cell.size == grid.triangleItem.getX() && a.loc.y / Cell.size == grid.triangleItem.getY()) {
            birdScore++;
            ItemRespawn.respawnTriangle(this);
          }
        }
      }
      // deduct score if on lava
      for (int i = 0; i < actors.size(); i++) {
        Actor a = actors.get(i);
        if (a.getClass().getSimpleName().equals("Dog")) {
          if (a.loc.getEnvironment() == 2 && dogScore > 0) dogScore--;
        } else if (a.getClass().getSimpleName().equals("Cat")) {
          if (a.loc.getEnvironment() == 2 && catScore > 0) catScore--;
        } else if (a.getClass().getSimpleName().equals("Bird")) {
          if (a.loc.getEnvironment() == 2 && birdScore > 0) birdScore--;
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
