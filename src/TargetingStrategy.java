import java.util.List;

public interface TargetingStrategy {
  Cell chooseMove(Actor actor, List<Cell> possibleLocs, Stage s);
}
