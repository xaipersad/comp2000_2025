import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class WeatherObserver {
    private Grid grid;

    public WeatherObserver(Grid grid) {
        this.grid = grid;
    }

    public void processWeatherUpdate(String timestamp, String weatherType, int x, int y, double value) {
        // Convert server coordinates (origin J9) into grid indices
        int centerX = grid.serverToGridCol(x);
        int centerY = grid.serverToGridRow(y);

        // Special handling for wind: redistribute rainfall from center to neighbors
        if ("wind".equalsIgnoreCase(weatherType)) {
            Optional<Cell> centerOpt = grid.cellAtServerCoords(x, y);
            if (!centerOpt.isPresent()) return;
            Cell center = centerOpt.get();

            double strength = value; // assume 0..100 scale
            double fraction = Math.min(0.5, strength / 100.0); // up to 50% of rainfall can be moved
            double centerRain = center.getRainfall();
            double amountToMove = centerRain * fraction;
            if (amountToMove <= 0) {
                // nothing to move, but still notify terrains about wind
                applyToRadius(centerX, centerY, (c) -> c.updateWeather("wind", value));
                return;
            }

            // collect neighbors (8-way) around center
            List<Cell> neighbors = new ArrayList<>();
            for (int i = Math.max(0, centerX - 1); i <= Math.min(19, centerX + 1); i++) {
                for (int j = Math.max(0, centerY - 1); j <= Math.min(19, centerY + 1); j++) {
                    if (i == centerX && j == centerY) continue;
                    grid.cellAtColRow(i, j).ifPresent(neighbors::add);
                }
            }

            if (!neighbors.isEmpty()) {
                double perNeighbor = amountToMove / neighbors.size();
                center.addRainfall(-amountToMove);
                neighbors.forEach(n -> n.addRainfall(perNeighbor));
                // Update terrains for center and neighbors
                center.updateWeather("rainfall", center.getRainfall());
                neighbors.forEach(n -> n.updateWeather("rainfall", n.getRainfall()));
            }

            // also notify terrains in the larger radius about the wind itself
            applyToRadius(centerX, centerY, (c) -> c.updateWeather("wind", value));
            return;
        }

        // Use streams to apply weather effects to a radius around the weather event
        int minX = Math.max(0, centerX - 2);
        int maxX = Math.min(19, centerX + 2);
        int minY = Math.max(0, centerY - 2);
        int maxY = Math.min(19, centerY + 2);

        IntStream.rangeClosed(minX, maxX)
            .boxed()
            .flatMap(i -> IntStream.rangeClosed(minY, maxY)
                .mapToObj(j -> grid.cellAtColRow(i, j)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(cell -> {
                // update cell with the new weather
                cell.updateWeather(weatherType, value);
                // do not immediately change terrain here; rely on gradual tick-based transitions
            });
    }

    private interface CellAction { void apply(Cell c); }

    private void applyToRadius(int centerX, int centerY, CellAction action) {
        int minX = Math.max(0, centerX - 2);
        int maxX = Math.min(19, centerX + 2);
        int minY = Math.max(0, centerY - 2);
        int maxY = Math.min(19, centerY + 2);
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                grid.cellAtColRow(i, j).ifPresent(action::apply);
            }
        }
    }

    // If a cell accumulates enough rainfall, change its terrain type.
    // Simple rules:
    //  - sand -> water if rainfall >= 50
    //  - sand -> water if rainfall >= 0.9 (normalized)
    //  - grass -> water if rainfall >= 0.95 (normalized)
    //  - water -> sand if rainfall < 0.05 and temperature > 25 (evaporation)
        // Flooding decisions are handled gradually by terrain.tick() now.
}