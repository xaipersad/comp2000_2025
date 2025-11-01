import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        startListening();
    }

    /**
     * Start the client synchronously (blocking). Re-used by the async starter.
     */
    public static void startListening() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://13.238.167.130/weather"))
                .header("Accept", "text/event-stream")
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] pieces = line.split(" ");

                    if (pieces.length == 5) {
                        String timeStamp = pieces[0];
                        String weatherType = pieces[1];
                        int xPosition = Integer.parseInt(pieces[2]);
                        int yPosition = Integer.parseInt(pieces[3]);
                        double weatherValue = Double.parseDouble(pieces[4]);

                        // Observer pattern: publish update to all listeners via the event bus
                        GameManager.getInstance().getWeatherBus()
                            .notifyUpdate(timeStamp, weatherType, xPosition, yPosition, weatherValue);

                        // also print for debugging
                        System.out.printf("Time: %s, %s at location (%d, %d) is %.2f%n",
                            timeStamp, weatherType, xPosition, yPosition, weatherValue);
                    } else {
                        System.err.println("Error: Received incorrect data format: " + line);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error: Received data with invalid numbers: " + line);
                }
            }
        }
    }

    /**
     * Start the client in a background thread so the UI process can receive updates.
     * Use this when you run only `java Main` and want weather updates to affect that UI.
     */
    public static void startListeningAsync() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startListening();
                } catch (Exception e) {
                    // Only use the real HTTP server. Log the error and stop the client thread.
                    System.err.println("Weather client failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, "WeatherClient-Thread");
        t.setDaemon(true);
        t.start();
    }

    
}