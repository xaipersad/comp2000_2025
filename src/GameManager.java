public class GameManager {
    private static GameManager instance;
    private Grid grid;
    private WeatherObserver weatherObserver;

    private GameManager() {
        grid = new Grid();
        weatherObserver = new WeatherObserver(grid);
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
}