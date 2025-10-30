import java.util.Optional;

public class WeatherEvent {
  private final long timestamp;
  private final WeatherAttribute attribute;
  private final int x;
  private final int y;
  private final double value;

  public WeatherEvent(long timestamp, WeatherAttribute attribute, int x, int y, double value) {
    this.timestamp = timestamp;
    this.attribute = attribute;
    this.x = x;
    this.y = y;
    this.value = value;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public WeatherAttribute getAttribute() {
    return attribute;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public double getValue() {
    return value;
  }

  public static Optional<WeatherEvent> parse(String line) {
    if(line == null || line.isBlank()) {
      return Optional.empty();
    }
    String[] parts = line.trim().split(" ");
    if(parts.length != 5) {
      return Optional.empty();
    }
    try {
      long timestamp = Long.parseLong(parts[0]);
      Optional<WeatherAttribute> attribute = WeatherAttribute.fromWireValue(parts[1]);
      if(attribute.isEmpty()) {
        return Optional.empty();
      }
      int x = Integer.parseInt(parts[2]);
      int y = Integer.parseInt(parts[3]);
      double value = Double.parseDouble(parts[4]);
      return Optional.of(new WeatherEvent(timestamp, attribute.get(), x, y, value));
    } catch(NumberFormatException ex) {
      return Optional.empty();
    }
  }
}
