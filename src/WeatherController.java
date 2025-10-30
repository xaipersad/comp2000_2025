import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WeatherController implements WeatherListener {
  private final Grid grid;
  private final Map<Cell, WeatherSnapshot> cellSnapshots = new ConcurrentHashMap<>();

  public WeatherController(Grid grid) {
    this.grid = grid;
  }

  @Override
  public void onWeather(WeatherEvent event) {
    mapToCell(event.getX(), event.getY())
      .ifPresent(cell -> cellSnapshots.computeIfAbsent(cell, c -> new WeatherSnapshot()).update(event));
  }

  public boolean isCellBlocked(Cell cell) {
    long now = System.currentTimeMillis();
    return snapshotFor(cell)
      .filter(snapshot -> snapshot.isRecent(now))
      .map(WeatherSnapshot::isFlooded)
      .orElse(false);
  }

  public Optional<WeatherSnapshot> weatherAt(Cell cell) {
    long now = System.currentTimeMillis();
    return snapshotFor(cell).filter(snapshot -> snapshot.isRecent(now));
  }

  public Map<Cell, Color> overlayColors() {
    long now = System.currentTimeMillis();
    return cellSnapshots.entrySet()
      .stream()
      .filter(entry -> entry.getValue().isRecent(now))
      .filter(entry -> entry.getValue().hasMeaningfulWeather())
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().overlayColor()));
  }

  public void applyTemperature(Collection<Actor> actors) {
    long now = System.currentTimeMillis();
    actors.stream()
      .forEach(actor -> {
        double temperature = snapshotFor(actor.loc)
          .filter(snapshot -> snapshot.isRecent(now))
          .map(WeatherSnapshot::temperature)
          .orElse(0.5d);
        actor.setTemperatureModifier(temperature);
      });
  }

  public void updateMoveStrategy(Actor actor) {
    MoveStrategy base = actor.baseStrategy();
    MoveStrategy strategy = base;
    Optional<Point2D.Double> windVector = snapshotFor(actor.loc)
      .map(WeatherSnapshot::windVector);
    double windStrength = windVector.map(v -> v.distance(0.0d, 0.0d)).orElse(0.0d);
    if(actor.isBot() && windStrength > 0.25d) {
      strategy = new WindDrivenStrategy(this, base);
    }
    actor.setMover(strategy);
  }

  public Optional<Point2D.Double> windVectorFor(Cell cell) {
    long now = System.currentTimeMillis();
    return snapshotFor(cell)
      .filter(snapshot -> snapshot.isRecent(now))
      .map(WeatherSnapshot::windVector);
  }

  private Optional<Cell> mapToCell(int weatherX, int weatherY) {
    int colIndex = weatherX + grid.getColumnCount() / 2;
    int rowIndex = grid.getRowCount() / 2 - weatherY - 1;
    return grid.cellAtIndex(colIndex, rowIndex);
  }

  private Optional<WeatherSnapshot> snapshotFor(Cell cell) {
    return Optional.ofNullable(cellSnapshots.get(cell));
  }
}
