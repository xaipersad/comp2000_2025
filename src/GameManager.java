public class GameManager {
    private static GameManager instance;
    private Grid grid;
    private WeatherObserver weatherObserver;
    private WeatherEventBus weatherBus;

    private GameManager() {
        grid = new Grid();
        weatherObserver = new WeatherObserver(grid);
        weatherBus = new WeatherEventBus();
        weatherBus.addListener(weatherObserver);
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public Grid getGrid() {
        return grid;
    }

    public WeatherObserver getWeatherObserver() {
        return weatherObserver;
    }

    public WeatherEventBus getWeatherBus() {
        return weatherBus;
    }
}