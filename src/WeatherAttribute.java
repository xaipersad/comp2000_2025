import java.util.Locale;
import java.util.Optional;

public enum WeatherAttribute {
  RAIN,
  WIND_X,
  WIND_Y,
  TEMPERATURE;

  public static Optional<WeatherAttribute> fromWireValue(String value) {
    if(value == null) {
      return Optional.empty();
    }
    switch(value.toLowerCase(Locale.ROOT)) {
      case "rain":
        return Optional.of(RAIN);
      case "windx":
        return Optional.of(WIND_X);
      case "windy":
        return Optional.of(WIND_Y);
      case "temp":
        return Optional.of(TEMPERATURE);
      default:
        return Optional.empty();
    }
  }
}
