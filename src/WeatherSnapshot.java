import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.EnumMap;

public class WeatherSnapshot {
  private final EnumMap<WeatherAttribute, Double> values = new EnumMap<>(WeatherAttribute.class);
  private volatile long lastUpdatedMillis;

  public WeatherSnapshot() {
    values.put(WeatherAttribute.RAIN, 0.0d);
    values.put(WeatherAttribute.WIND_X, 0.0d);
    values.put(WeatherAttribute.WIND_Y, 0.0d);
    values.put(WeatherAttribute.TEMPERATURE, 0.5d);
    lastUpdatedMillis = System.currentTimeMillis();
  }

  public synchronized void update(WeatherEvent event) {
    values.put(event.getAttribute(), event.getValue());
    lastUpdatedMillis = System.currentTimeMillis();
  }

  public double rainfall() {
    return values.getOrDefault(WeatherAttribute.RAIN, 0.0d);
  }

  public double windX() {
    return values.getOrDefault(WeatherAttribute.WIND_X, 0.0d);
  }

  public double windY() {
    return values.getOrDefault(WeatherAttribute.WIND_Y, 0.0d);
  }

  public double temperature() {
    return values.getOrDefault(WeatherAttribute.TEMPERATURE, 0.5d);
  }

  public Point2D.Double windVector() {
    return new Point2D.Double(windX(), windY());
  }

  public double windStrength() {
    return windVector().distance(0.0d, 0.0d);
  }

  public boolean isFlooded() {
    return rainfall() >= 0.7d;
  }

  public boolean isRecent(long nowMillis) {
    return nowMillis - lastUpdatedMillis < 30000L;
  }

  public boolean hasMeaningfulWeather() {
    return rainfall() > 0.05d || Math.abs(temperature() - 0.5d) > 0.1d || windStrength() > 0.3d;
  }

  public Color overlayColor() {
    float rain = clamp((float) rainfall());
    float heat = clamp((float) temperature());
    float wind = clamp((float) windStrength());
    float alpha = clamp(0.2f + rain * 0.5f + wind * 0.2f);
    float red = clamp(0.2f + heat * 0.8f);
    float blue = clamp(0.2f + rain * 0.8f);
    float green = clamp(0.3f + (1.0f - Math.abs(heat - 0.5f) * 2.0f) * 0.5f);
    return new Color(red, green, blue, alpha);
  }

  private float clamp(float value) {
    return Math.max(0.0f, Math.min(1.0f, value));
  }
}
