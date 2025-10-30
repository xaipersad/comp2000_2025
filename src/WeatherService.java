import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class WeatherService {
  private final String endpoint;
  private final List<WeatherListener> listeners = new CopyOnWriteArrayList<>();
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
    Thread thread = new Thread(r, "weather-stream");
    thread.setDaemon(true);
    return thread;
  });

  public WeatherService(String endpoint) {
    this.endpoint = endpoint;
  }

  public void addListener(WeatherListener listener) {
    listeners.add(listener);
  }

  public void start() {
    if(running.compareAndSet(false, true)) {
      executor.submit(this::poll);
    }
  }

  public void stop() {
    running.set(false);
    executor.shutdownNow();
  }

  private void poll() {
    while(running.get()) {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(0);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
          reader.lines()
            .takeWhile(line -> running.get())
            .map(WeatherEvent::parse)
            .flatMap(Optional::stream)
            .forEach(event -> listeners.forEach(listener -> listener.onWeather(event)));
        }
      } catch(IOException e) {
        System.out.println("Weather stream interrupted: " + e.getMessage());
        try {
          Thread.sleep(2000L);
        } catch(InterruptedException interruptedException) {
          Thread.currentThread().interrupt();
          running.set(false);
        }
      }
    }
  }
}
