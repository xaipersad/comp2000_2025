import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.List;

public abstract class Actor implements Pulse {
  Color baseColor, color;
  Cell loc;
  List<Polygon> display;
  boolean bot;
  int moves;
  int turns;
  MoveStrategy mover;
  private double temperatureModifier;

  protected Actor(Cell inLoc, Color inColor, boolean isBot, int inMoves) {
    loc = inLoc;
    baseColor = inColor;
    color = inColor;
    bot = isBot;
    moves = inMoves;
    turns = 1;
    temperatureModifier = 0.5d;
    setPoly();
  }

  public void paint(Graphics g) {
    for(Polygon p: display) {
      g.setColor(color);
      g.fillPolygon(p);
      g.setColor(Color.GRAY);
      g.drawPolygon(p);
    }
  }

  protected abstract void setPoly();

  public boolean isBot() {
    return bot;
  }

  public void setLocation(Cell inLoc) {
    loc = inLoc;
    setMover(createBaseStrategy());
    setPoly();
  }

  protected MoveStrategy createBaseStrategy() {
    if(loc.row % 2 == 0) {
      return new MoveRandomly();
    } else {
      return new MoveLeft();
    }
  }

  public MoveStrategy baseStrategy() {
    return createBaseStrategy();
  }

  public MoveStrategy getMover() {
    return mover;
  }

  public void setMover(MoveStrategy strategy) {
    mover = strategy;
  }

  public void setTemperatureModifier(double modifier) {
    temperatureModifier = Math.max(0.0d, Math.min(1.0d, modifier));
  }

  public void pulsate(char phase, int percentage) {
    // Adjust color saturation according to the beat
    float[] hsbValues = new float[3];
    Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), hsbValues);
    hsbValues[1] = ((float) percentage) / 100.0f;
    float brightnessScale = (float) Math.min(1.0d, (0.6d + (temperatureModifier * 0.7d)));
    hsbValues[2] = Math.min(1.0f, hsbValues[2] * brightnessScale);
    color = Color.getHSBColor(hsbValues[0], hsbValues[1], hsbValues[2]);
  }
}
