import java.awt.Color;

public interface TerrainType {
    void applyWeather(String weatherType, double value);
    Color getColor();
    String getType();
    /**
     * Called each frame/tick so terrains can gradually update internal state
     * and trigger terrain transitions when appropriate.
     */
    default void tick(Cell cell) {
        // default: do nothing
    }
}