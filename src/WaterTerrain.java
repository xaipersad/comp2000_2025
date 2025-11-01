import java.awt.Color;

public class WaterTerrain implements TerrainType {
    private double level = 1.0;
    private Color currentColor = new Color(28, 107, 160);

    @Override
    public void applyWeather(String weatherType, double value) {
        switch (weatherType) {
            case "rainfall":
                // rainfall value is added proportionally
                level = Math.min(1.0, level + value / 100.0);
                break;
            case "temperature":
                // higher temperature evaporates water
                if (value > 30) {
                    level = Math.max(0.0, level - 0.05);
                }
                break;
        }
        updateColor();
    }

    private void updateColor() {
        int r = (int) (28 * level + 200 * (1 - level));
        int g = (int) (107 * level + 220 * (1 - level));
        int b = (int) (160 * level + 255 * (1 - level));
        currentColor = new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
    }

    @Override
    public Color getColor() {
        return currentColor;
    }

    @Override
    public String getType() {
        return "water";
    }

    @Override
    public void tick(Cell cell) {
        // if no rain water level slowly drops cos evaporation
        double rain = cell.getRainfall();
        double temp = cell.getTemperature();
        if (rain > 0) {
            // rain increases water level slowly
            level = Math.min(1.0, level + rain * 0.005);
        } else {
            // evaporate faster at temperatures above 25C
            double evap;
            if (temp > 25.0) {
                evap = 0.005;
            } else {
                evap = 0.002;
            }
            level = Math.max(0.0, level - evap);
        }
        updateColor();
    }
}
