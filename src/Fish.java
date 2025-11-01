import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

public class Fish extends Actor {
  public Fish(Cell inLoc, boolean isBot) {
    super(inLoc, new Color(30, 144, 255), isBot, 0);
  }

  protected void setPoly() {
    display = new ArrayList<Polygon>();
    Polygon body = new Polygon();
    body.addPoint(loc.x + 10, loc.y + 18);
    body.addPoint(loc.x + 17, loc.y + 12);
    body.addPoint(loc.x + 24, loc.y + 18);
    body.addPoint(loc.x + 17, loc.y + 24);
    Polygon tail = new Polygon();
    tail.addPoint(loc.x + 24, loc.y + 18);
    tail.addPoint(loc.x + 30, loc.y + 14);
    tail.addPoint(loc.x + 30, loc.y + 22);
    display.add(body);
    display.add(tail);
  }
}
