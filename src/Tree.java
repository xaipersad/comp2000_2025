import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

public class Tree extends Actor {
  public Tree(Cell inLoc, boolean isBot) {
    super(inLoc, new Color(34, 139, 34), isBot, 0);
  }

  protected void setPoly() {
    display = new ArrayList<Polygon>();
    // simple tree: trunk + canopy triangle
    Polygon trunk = new Polygon();
    trunk.addPoint(loc.x + 15, loc.y + 20);
    trunk.addPoint(loc.x + 20, loc.y + 20);
    trunk.addPoint(loc.x + 20, loc.y + 30);
    trunk.addPoint(loc.x + 15, loc.y + 30);
    Polygon canopy = new Polygon();
    canopy.addPoint(loc.x + 17, loc.y + 5);
    canopy.addPoint(loc.x + 5, loc.y + 22);
    canopy.addPoint(loc.x + 29, loc.y + 22);
    display.add(trunk);
    display.add(canopy);
  }
}
