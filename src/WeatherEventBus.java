import java.util.ArrayList;
import java.util.List;

public class WeatherEventBus {
  private final List<WeatherListener> listeners = new ArrayList<WeatherListener>();

  public void addListener(WeatherListener l) {
    if (l != null && !listeners.contains(l)) {
      listeners.add(l);
    }
  }

  public void removeListener(WeatherListener l) {
    listeners.remove(l);
  }

  public void notifyUpdate(String timestamp, String weatherType, int x, int y, double value) {
    // iterate over a snapshot to avoid concurrent modification
    java.util.ArrayList<WeatherListener> snapshot = new java.util.ArrayList<WeatherListener>(listeners);
    for (int i = 0; i < snapshot.size(); i++) {
      snapshot.get(i).onWeatherUpdate(timestamp, weatherType, x, y, value);
    }
  }
}
