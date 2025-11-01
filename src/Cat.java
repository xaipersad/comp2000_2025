import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

public class Cat extends Actor {
  public static final int catMoves = 2;
  private int health = 3; 
  private int bubbles = 0; 
  private long lastBubbleMs = 0L; 
  private boolean inWater = false; // track if currently in water
  private int suns = 0; 
  private long lastSunMs = 0L;
  private boolean inSand = false;

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

  // cat health 
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

  // bubbles 
  public int getBubbles() { return bubbles; }
  public void setBubbles(int value) {
    if (value < 0) {
      bubbles = 0;
    } else if (value > 3) {
      bubbles = 3;
    } else {
      bubbles = value;
    }
  }
  public void removeOneBubble() { setBubbles(bubbles - 1); }
  public long getLastBubbleMs() { return lastBubbleMs; }
  public void setLastBubbleMs(long t) { lastBubbleMs = t; }
  public boolean isInWater() { return inWater; }
  public void setInWater(boolean v) { inWater = v; }

  // suns
  public int getSuns() { return suns; }
  public void setSuns(int value) {
    if (value < 0) {
      suns = 0;
    } else if (value > 3) {
      suns = 3;
    } else {
      suns = value;
    }
  }
  public void removeOneSun() { setSuns(suns - 1); }
  public long getLastSunMs() { return lastSunMs; }
  public void setLastSunMs(long t) { lastSunMs = t; }
  public boolean isInSand() { return inSand; }
  public void setInSand(boolean v) { inSand = v; }
}
