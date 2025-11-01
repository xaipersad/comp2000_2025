import java.util.List;

public interface TargetingStrategy {
  /**
   * Decide which cell to move to from possible locations for a given actor.
   */
  Cell chooseMove(Actor actor, List<Cell> possibleLocs, Stage s);
}
