import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

public class Cat extends Actor {
  public static final int catMoves = 2;
  private int health = 3; // 0..3 hearts only for Cat

  public Cat(Cell inLoc, boolean isBot) {
    super(inLoc, Color.BLUE, isBot, catMoves);
  }

  protected void setPoly() {
    display = new ArrayList<Polygon>();
    Polygon ear1 = new Polygon();
    ear1.addPoint(loc.x + 11, loc.y + 5);
    ear1.addPoint(loc.x + 15, loc.y + 15);
    ear1.addPoint(loc.x + 7, loc.y + 15);
    Polygon ear2 = new Polygon();
    ear2.addPoint(loc.x + 22, loc.y + 5);
    ear2.addPoint(loc.x + 26, loc.y + 15);
    ear2.addPoint(loc.x + 18, loc.y + 15);
    Polygon face = new Polygon();
    face.addPoint(loc.x + 5, loc.y + 15);
    face.addPoint(loc.x + 29, loc.y + 15);
    face.addPoint(loc.x + 17, loc.y + 30);
    display.add(face);
    display.add(ear1);
    display.add(ear2);
  }

  // Cat-specific health API
  public int getHealth() {
    return health;
  }

  public void setHealth(int value) {
    if (value < 0) {
      health = 0;
    } else if (value > 3) {
      health = 3;
    } else {
      health = value;
    }
  }

  public void loseHeart() { setHealth(health - 1); }
  public void gainHeart() { setHealth(health + 1); }
}
