import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

public class Cactus extends Actor {
  public Cactus(Cell inLoc, boolean isBot) {
    super(inLoc, new Color(0, 150, 0), isBot, 0);
  }

  protected void setPoly() {
    display = new ArrayList<Polygon>();
    Polygon center = new Polygon();
    center.addPoint(loc.x + 16, loc.y + 8);
    center.addPoint(loc.x + 20, loc.y + 8);
    center.addPoint(loc.x + 20, loc.y + 30);
    center.addPoint(loc.x + 16, loc.y + 30);

    Polygon armLeft = new Polygon();
    armLeft.addPoint(loc.x + 10, loc.y + 14);
    armLeft.addPoint(loc.x + 14, loc.y + 14);
    armLeft.addPoint(loc.x + 14, loc.y + 22);
    armLeft.addPoint(loc.x + 10, loc.y + 22);

    Polygon armRight = new Polygon();
    armRight.addPoint(loc.x + 22, loc.y + 12);
    armRight.addPoint(loc.x + 26, loc.y + 12);
    armRight.addPoint(loc.x + 26, loc.y + 20);
    armRight.addPoint(loc.x + 22, loc.y + 20);

    display.add(center);
    display.add(armLeft);
    display.add(armRight);
  }
}
