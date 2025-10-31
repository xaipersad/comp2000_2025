import java.awt.Color;

public class WaterTerrain implements TerrainType {
    private double level = 1.0; // 0..1, how much water
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
        // interpolate between light blue and deep blue based on level
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
        // if there's no rain, water level slowly drops due to evaporation
        double rain = cell.getRainfall();
        double temp = cell.getTemperature();
        if (rain > 0) {
            // rain increases water level slowly
            level = Math.min(1.0, level + rain * 0.005);
        } else {
            // evaporate faster at temperatures above 25C, but still gradual
            double evap = temp > 25.0 ? 0.005 : 0.002; // per-tick evaporation
            level = Math.max(0.0, level - evap);
        }
        updateColor();

        // no direct terrain conversion here; global rules in Grid.tick() handle transitions
    }
}
