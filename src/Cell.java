import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class Cell extends Rectangle {
  static int size = 35;
  char col;
  int row;
  private TerrainType terrain;
  private double temperature = 20.0;
  private double rainfall = 0.0;
  private double wind = 0.0;
  private double windX = 0.0;
  private double windY = 0.0;

  public Cell(char inCol, int inRow, int x, int y) {
    super(x, y, size, size);
    col = inCol;
    row = inRow;
    terrain = new GrassTerrain();
  }

  public void updateWeather(String type, double value) {
    // server sends normalized values (0..1) for temp/rain/wind components.
    // Map them into sensible internal units:
    // - temp: map 0..1 -> 0..50 C
    // - rain: keep as 0..1 (normalized intensity)
    // - windx/windy: keep as components 0..1
    switch (type.toLowerCase()) {
      case "temp":
      case "temperature":
        temperature = value * 50.0; // convert normalized to Celsius
        break;
      case "rain":
      case "rainfall":
        rainfall = value; // normalized 0..1
        break;
      case "wind":
        // legacy single-component wind magnitude
        wind = value;
        break;
      case "windx":
        windX = value;
        break;
      case "windy":
        windY = value;
        break;
    }
    terrain.applyWeather(type, value);
  }

  // Simple accessors so other classes (eg. WeatherObserver) can read/modify
  public double getTemperature() {
    return temperature;
  }

  public double getRainfall() {
    return rainfall;
  }

  public void addRainfall(double delta) {
    this.rainfall = Math.max(0.0, this.rainfall + delta);
    // let the terrain respond to the new rainfall amount
    terrain.applyWeather("rainfall", this.rainfall);
  }

  public double getWind() {
    // return magnitude using components if present
    double mag = Math.hypot(windX, windY);
    return Math.max(wind, mag);
  }

  public double getWindX() { return windX; }
  public double getWindY() { return windY; }

  public void setTerrain(TerrainType t) {
    if (t != null) {
      this.terrain = t;
    }
  }

  public TerrainType getTerrain() {
    return terrain;
  }

  public void paint(Graphics g, Point mousePos) {
    if(contains(mousePos)) {
      g.setColor(terrain.getColor().darker());
    } else {
      g.setColor(terrain.getColor());
    }
    g.fillRect(x, y, size, size);
    g.setColor(Color.BLACK);
    g.drawRect(x, y, size, size);
  }

  /**
   * Called each frame to decay transient weather measurements that may
   * not be continuously updated by the server (rain, wind components).
   */
  public void tickDecay() {
    // decay rainfall slowly toward 0 so single messages don't persist forever
    rainfall = Math.max(0.0, rainfall - 0.001);
    // decay wind components
    windX = Math.max(0.0, windX - 0.002);
    windY = Math.max(0.0, windY - 0.002);
    wind = Math.max(0.0, wind - 0.002);
  }

  @Override
  public boolean contains(Point p) {
    if(p != null) {
      return super.contains(p);
    } else {
      return false;
    }
  }

  public int leftOfComparison(Cell c) {
    return Integer.compare(col, c.col);
  }

  public int aboveComparison(Cell c) {
    return Integer.compare(row, c.row);
  }
}
