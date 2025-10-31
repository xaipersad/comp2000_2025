// GrassTerrain class handling moisture and heat stress for grass
import java.awt.Color;

public class GrassTerrain implements TerrainType {
    private double moisture = 0.5;
    private Color currentColor = new Color(34, 139, 34);
    private double wetness = 0.0; // 0..1, how saturated before becoming water
    private double heatStress = 0.0; // 0..1, how much heat damage the grass has

    @Override
    public void applyWeather(String weatherType, double value) {
        // Color and moisture are updated in tick() for smooth transitions.
        // We keep applyWeather lightweight to avoid sudden jumps.
        // Still allow immediate visual nudge if temperature is very high.
        if ("temperature".equalsIgnoreCase(weatherType) && value > 35) {
            // tiny immediate nudge towards dryness for very hot spikes
            moisture = Math.max(0.0, moisture - 0.005);
        }
    }

    private void updateColor() {
        // Wet grass should be a darker green. Blend between a dry bright
        // green and a wet dark green based on the moisture value.
    Color dry = new Color(124, 252, 0); // bright dry grass
    Color wet = new Color(0, 70, 0);    // darker wet grass
        // treat very small moisture as zero so color reverts cleanly
        double m;
        if (moisture > 0.01) {
            m = moisture;
        } else {
            m = 0.0;
        }
        int r = (int) (dry.getRed() * (1.0 - m) + wet.getRed() * m);
        int g = (int) (dry.getGreen() * (1.0 - m) + wet.getGreen() * m);
        int b = (int) (dry.getBlue() * (1.0 - m) + wet.getBlue() * m);
        // Now apply heat stress: blend the base color towards a brownish tone
        // when the grass is heat-stressed (hot + low moisture).
        Color base = new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
        // brownish-green for heat-stressed dry grass
        Color brownGreen = new Color(120, 90, 40);
        double h = Math.max(0.0, Math.min(1.0, heatStress));
        int fr = (int) (base.getRed() * (1.0 - h) + brownGreen.getRed() * h);
        int fg = (int) (base.getGreen() * (1.0 - h) + brownGreen.getGreen() * h);
        int fb = (int) (base.getBlue() * (1.0 - h) + brownGreen.getBlue() * h);
        currentColor = new Color(Math.min(255, Math.max(0, fr)), Math.min(255, Math.max(0, fg)), Math.min(255, Math.max(0, fb)));
    }

    @Override
    public void tick(Cell cell) {
        // Smooth moisture towards current rainfall so color follows rain and
        // reverts gradually as rain approaches zero (EMA smoothing).
        double rain = cell.getRainfall();
        double alpha = 0.05; // 5% towards target per tick (~1 second to settle)
        moisture = Math.max(0.0, Math.min(1.0, moisture + (rain - moisture) * alpha));

        // wetness grows for flooding decision extremely slowly; dry slowly when no rain
        if (rain > 0) {
            wetness = Math.min(1.0, wetness + rain * 0.0005);
        } else {
            wetness = Math.max(0.0, wetness - 0.002);
        }

        // heat stress: increase only when truly dry (moisture near zero) and temperature is high (>26C)
        double temp = cell.getTemperature();
        boolean trulyDry = moisture <= 0.01; // treat tiny moisture as dry
        if (temp > 26.0 && trulyDry) {
            double factor = (temp - 26.0) / 24.0; // 0..1 for 26..50C
            heatStress = Math.min(1.0, heatStress + factor * 0.003);
        } else {
            // recover faster when conditions improve
            heatStress = Math.max(0.0, heatStress - 0.002);
        }
        // Always update color so smoothing, wetness and heat stress are visible immediately.
        updateColor();

        // no direct terrain conversion here; global rules in Grid.tick() handle transitions
    }

    @Override
    public Color getColor() {
        return currentColor;
    }

    @Override
    public String getType() {
        return "grass";
    }
}