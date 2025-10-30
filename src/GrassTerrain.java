import java.awt.Color;

public class GrassTerrain implements TerrainType {
    private double moisture = 0.5;
    private Color currentColor = new Color(34, 139, 34);
    private double wetness = 0.0; // 0..1, how saturated before becoming water
    private double heatStress = 0.0; // 0..1, how much heat damage the grass has

    @Override
    public void applyWeather(String weatherType, double value) {
        switch (weatherType.toLowerCase()) {
            case "temperature":
                if (value > 30) {
                    moisture = Math.max(0, moisture - 0.05);
                }
                break;
            case "rain":
            case "rainfall":
                // server sends normalized rain (0..1) — increase moisture proportionally
                moisture = Math.min(1.0, moisture + value * 0.2);
                break;
        }
        updateColor();
    }

    private void updateColor() {
        // Wet grass should be a darker green. Blend between a dry bright
        // green and a wet dark green based on the moisture value.
        Color dry = new Color(34, 139, 34); // bright dry grass
        Color wet = new Color(0, 100, 0);   // dark wet grass
        double m = Math.max(0.0, Math.min(1.0, moisture));
        int r = (int) (dry.getRed() * (1.0 - m) + wet.getRed() * m);
        int g = (int) (dry.getGreen() * (1.0 - m) + wet.getGreen() * m);
        int b = (int) (dry.getBlue() * (1.0 - m) + wet.getBlue() * m);
        // Now apply heat stress: blend the base color towards a brownish tone
        // when the grass is heat-stressed (hot + low moisture).
        Color base = new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
        Color brown = new Color(139, 69, 19);
        double h = Math.max(0.0, Math.min(1.0, heatStress));
        int fr = (int) (base.getRed() * (1.0 - h) + brown.getRed() * h);
        int fg = (int) (base.getGreen() * (1.0 - h) + brown.getGreen() * h);
        int fb = (int) (base.getBlue() * (1.0 - h) + brown.getBlue() * h);
        currentColor = new Color(Math.min(255, Math.max(0, fr)), Math.min(255, Math.max(0, fg)), Math.min(255, Math.max(0, fb)));
    }

    @Override
    public void tick(Cell cell) {
        // wetness grows when rainfall present, otherwise decays
        double rain = cell.getRainfall();
        // Per-tick rates: ticks run ~50 times per second.
        // Use small per-tick increments so flooding requires sustained rain.
        if (rain > 0) {
            // make accumulation even slower so flooding is rare
            wetness = Math.min(1.0, wetness + rain * 0.0005); // ~0.025 per second at rain=1
        } else {
            // dry faster when no rain
            wetness = Math.max(0.0, wetness - 0.002); // ~0.1 per second drying
        }

        // moisture slowly decays when no rain so color returns to normal
        if (rain <= 0) {
            double old = moisture;
            moisture = Math.max(0.0, moisture - 0.0005);
            if (Math.abs(moisture - old) > 1e-6) updateColor();
        }

        // heat stress: increase when temperature is high and moisture is low
        double temp = cell.getTemperature();
        if (temp > 25.0 && moisture < 0.3) {
            // strength grows with how much above 25C the temperature is
            double factor = (temp - 25.0) / 25.0; // 0..1 for 25..50C
            heatStress = Math.min(1.0, heatStress + factor * 0.0015);
        } else {
            // recover slowly when conditions improve
            heatStress = Math.max(0.0, heatStress - 0.0008);
        }
        // Always update color so heat stress (and recovery) is visible immediately.
        updateColor();

        // if very wet for a very long time, flood into water terrain
        if (wetness > 0.995) {
            cell.setTerrain(new WaterTerrain());
        }
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