import java.awt.Color;

public interface TerrainType {
    void applyWeather(String weatherType, double value);
    Color getColor();
    String getType();
    default void tick(Cell cell) {
    }
}