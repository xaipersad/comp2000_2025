import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeatherObserver implements WeatherListener {
    private Grid grid;

    public WeatherObserver(Grid grid) {
        this.grid = grid;
    }
    public void processWeatherUpdate(String timestamp, String weatherType, int x, int y, double value) {
        onWeatherUpdate(timestamp, weatherType, x, y, value);
    }

    @Override
    public void onWeatherUpdate(String timestamp, String weatherType, int x, int y, double value) {
        int centerX = grid.serverToGridCol(x);
        int centerY = grid.serverToGridRow(y);
        if ("wind".equalsIgnoreCase(weatherType)) {
            Optional<Cell> centerOpt = grid.cellAtServerCoords(x, y);
            if (!centerOpt.isPresent()) return;
            Cell center = centerOpt.get();

            double strength = value;
            double fraction = Math.min(0.5, strength / 100.0); 
            double centerRain = center.getRainfall();
            double amountToMove = centerRain * fraction;
            if (amountToMove <= 0) {
                int minX = Math.max(0, centerX - 2);
                int maxX = Math.min(19, centerX + 2);
                int minY = Math.max(0, centerY - 2);
                int maxY = Math.min(19, centerY + 2);
                for (int i = minX; i <= maxX; i++) {
                    for (int j = minY; j <= maxY; j++) {
                        Optional<Cell> opt = grid.cellAtColRow(i, j);
                        if (opt.isPresent()) {
                            opt.get().updateWeather("wind", value);
                        }
                    }
                }
                return;
            }
            List<Cell> neighbors = new ArrayList<>();
            for (int i = Math.max(0, centerX - 1); i <= Math.min(19, centerX + 1); i++) {
                for (int j = Math.max(0, centerY - 1); j <= Math.min(19, centerY + 1); j++) {
                    if (i == centerX && j == centerY) continue;
                    Optional<Cell> opt = grid.cellAtColRow(i, j);
                    if (opt.isPresent()) {
                        neighbors.add(opt.get());
                    }
                }
            }

            if (!neighbors.isEmpty()) {
                double perNeighbor = amountToMove / neighbors.size();
                center.addRainfall(-amountToMove);
                for (int k = 0; k < neighbors.size(); k++) {
                    Cell n = neighbors.get(k);
                    n.addRainfall(perNeighbor);
                }
                center.updateWeather("rainfall", center.getRainfall());
                for (int k = 0; k < neighbors.size(); k++) {
                    Cell n = neighbors.get(k);
                    n.updateWeather("rainfall", n.getRainfall());
                }
            }
            int minX = Math.max(0, centerX - 2);
            int maxX = Math.min(19, centerX + 2);
            int minY = Math.max(0, centerY - 2);
            int maxY = Math.min(19, centerY + 2);
            for (Cell c : grid.squareIterable(minX, minY, maxX, maxY)) {
                c.updateWeather("wind", value);
            }
            return;
        }
        int minX = Math.max(0, centerX - 2);
        int maxX = Math.min(19, centerX + 2);
        int minY = Math.max(0, centerY - 2);
        int maxY = Math.min(19, centerY + 2);
        for (Cell cell : grid.squareIterable(minX, minY, maxX, maxY)) {
            cell.updateWeather(weatherType, value);
        }
    }
}