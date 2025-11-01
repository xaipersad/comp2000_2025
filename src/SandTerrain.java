import java.awt.Color;

public class SandTerrain implements TerrainType {
    private double moisture = 0.0; // 0..1
    private Color currentColor = new Color(194, 178, 128);
    private double wetness = 0.0;

    @Override
    public void applyWeather(String weatherType, double value) {
        switch (weatherType.toLowerCase()) {
            case "rain":
            case "rainfall":
                moisture = Math.min(1.0, moisture + value * 0.2);
                break;
            case "temperature":
                if (value > 30) {
                    moisture = Math.max(0.0, moisture - 0.05);
                }
                break;
        }
        updateColor();
    }

    private void updateColor() {
        // wet sand is darker, mix towards brown
        int r = (int) (194 * (1 - moisture) + 150 * moisture);
        int g = (int) (178 * (1 - moisture) + 120 * moisture);
        int b = (int) (128 * (1 - moisture) + 100 * moisture);
        currentColor = new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
    }

    @Override
    public void tick(Cell cell) {
        double rain = cell.getRainfall();
        if (rain > 0) {
            // sand accumulates very slowly
            wetness = Math.min(1.0, wetness + rain * 0.0006);
        } else {
            wetness = Math.max(0.0, wetness - 0.002);
        }
        // moisture slowly decays when no rain so color returns to normal
        if (rain <= 0) {
            double old = moisture;
            moisture = Math.max(0.0, moisture - 0.0005);
            if (Math.abs(moisture - old) > 1e-6) updateColor();
        }
    }

    @Override
    public Color getColor() {
        return currentColor;
    }

    @Override
    public String getType() {
        return "sand";
    }
}
