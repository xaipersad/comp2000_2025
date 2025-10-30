import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class WindDrivenStrategy implements MoveStrategy {
  private final WeatherController controller;
  private final MoveStrategy fallback;
  private final Random random = new Random();

  public WindDrivenStrategy(WeatherController controller, MoveStrategy fallback) {
    this.controller = controller;
    this.fallback = fallback;
  }

  @Override
  public Cell chooseNextLoc(List<Cell> possibleLocs, Actor currActor, List<Actor> otherActors) {
    if(possibleLocs.isEmpty()) {
      return currActor.loc;
    }
    return controller.windVectorFor(currActor.loc)
      .map(vector -> selectWithWind(possibleLocs, currActor, otherActors, vector))
      .orElseGet(() -> fallback.chooseNextLoc(possibleLocs, currActor, otherActors));
  }

  private Cell selectWithWind(List<Cell> possibleLocs, Actor currActor, List<Actor> otherActors, Point2D.Double windVector) {
    double windStrength = windVector.distance(0.0d, 0.0d);
    if(windStrength < 0.25d) {
      return fallback.chooseNextLoc(possibleLocs, currActor, otherActors);
    }
    Comparator<Cell> byAlignment = Comparator.comparingDouble(cell -> alignmentScore(currActor.loc, cell, windVector));
    return possibleLocs.stream()
      .max(byAlignment)
      .orElseGet(() -> possibleLocs.get(random.nextInt(possibleLocs.size())));
  }

  private double alignmentScore(Cell current, Cell candidate, Point2D.Double windVector) {
    int dx = candidate.col - current.col;
    int dy = current.row - candidate.row;
    double projection = dx * windVector.x + dy * windVector.y;
    return projection;
  }
}
