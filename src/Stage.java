import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Stage {
  // Button bounds for restart/quit
  public java.awt.Rectangle restartBtn = new java.awt.Rectangle(740, 300, 120, 40);
  public java.awt.Rectangle quitBtn = new java.awt.Rectangle(740, 350, 120, 40);
  boolean gameOver = false;
  String gameOverText = "";
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
  

    // win/lose message
    if (gameOver) {
      java.awt.Font oldFont = g.getFont();
      java.awt.Font bigFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 48);
      g.setFont(bigFont);
      java.awt.FontMetrics fm = g.getFontMetrics(bigFont);
      int gridWidth = Cell.size * 20;
      int gridHeight = Cell.size * 20;
      int textWidth = fm.stringWidth(gameOverText);
      int textHeight = fm.getAscent();
      int x = 10 + (gridWidth - textWidth) / 2;
      int y = 10 + (gridHeight - textHeight) / 2 + textHeight;
      // Draw white background rectangle
      int padding = 20;
      g.setColor(Color.WHITE);
      g.fillRect(x - padding, y - textHeight, textWidth + 2 * padding, textHeight + 10);
      // Draw text
      g.setColor(Color.BLACK);
      g.drawString(gameOverText, x, y);
      g.setFont(oldFont);

  // Draw Restart and Quit buttons under scoreboard
  java.awt.Font btnFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 20);
  g.setFont(btnFont);
  g.setColor(new Color(200,200,200));
  g.fillRect(restartBtn.x, restartBtn.y, restartBtn.width, restartBtn.height);
  g.fillRect(quitBtn.x, quitBtn.y, quitBtn.width, quitBtn.height);
  g.setColor(Color.BLACK);
  g.drawRect(restartBtn.x, restartBtn.y, restartBtn.width, restartBtn.height);
  g.drawRect(quitBtn.x, quitBtn.y, quitBtn.width, quitBtn.height);
  g.drawString("Restart", restartBtn.x + 18, restartBtn.y + 28);
  g.drawString("Quit", quitBtn.x + 35, quitBtn.y + 28);
  g.setFont(oldFont);
    }
  }

  public void handleClick(Point mouseLoc) {
    if (gameOver) {
      // Check if restart or quit button was clicked
      if (restartBtn.contains(mouseLoc)) {
        // Reset all game state
        grid = new Grid();
        actors = new ArrayList<Actor>();
        Random rand = new Random();
        int x1, y1, x2, y2, x3, y3;
        do { x1 = rand.nextInt(20); y1 = rand.nextInt(20); } while (grid.cells[x1][y1].getEnvironment() == 2);
        do { x2 = rand.nextInt(20); y2 = rand.nextInt(20); } while ((x2 == x1 && y2 == y1) || grid.cells[x2][y2].getEnvironment() == 2);
        do { x3 = rand.nextInt(20); y3 = rand.nextInt(20); } while (((x3 == x1 && y3 == y1) || (x3 == x2 && y3 == y2)) || grid.cells[x3][y3].getEnvironment() == 2);
        actors.add(new Cat(grid.cellAtColRow(x1, y1).get()));
        dog = new Dog(grid.cellAtColRow(x2, y2).get());
        actors.add(dog);
        actors.add(new Bird(grid.cellAtColRow(x3, y3).get()));
        showDogMoves = false;
        dogMoveOptions.clear();
        dogScore = 0;
        catScore = 0;
        birdScore = 0;
        gameOver = false;
        gameOverText = "";
        return;
      } else if (quitBtn.contains(mouseLoc)) {
        System.exit(0);
      }
      return;
    }
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

        // If dog lands on lava, respawn dog at random non-lava cell
        if (cell.getEnvironment() == 2) {
          Random rand = new Random();
          int x, y;
          do {
            x = rand.nextInt(20);
            y = rand.nextInt(20);
          } while (grid.cells[x][y].getEnvironment() == 2);
          dog.setLocation(grid.cellAtColRow(x, y).get());
        }

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
        // win/lose check
        if (dogScore >= 0) {
          gameOver = true;
          gameOverText = "YOU WIN";
        } else if (catScore >= 5 || birdScore >= 5) {
          gameOver = true;
          gameOverText = "YOU LOSE";
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
