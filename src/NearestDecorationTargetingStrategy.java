import java.util.List;

public class NearestDecorationTargetingStrategy implements TargetingStrategy {
  @Override
  public Cell chooseMove(Actor actor, List<Cell> possibleLocs, Stage s) {
    if (possibleLocs == null || possibleLocs.size() == 0) {
      return null;
    }
    // Find nearest decoration (Tree/Fish/Cactus)
    Cell target = null;
    int bestDistToObject = Integer.MAX_VALUE;
    for (int i = 0; i < s.listOfPlayers.size(); i++) {
      Actor a = s.listOfPlayers.get(i);
      if (a instanceof Tree || a instanceof Fish || a instanceof Cactus) {
        int dx = Math.abs((int) actor.loc.col - (int) a.loc.col);
        int dy = Math.abs(actor.loc.row - a.loc.row);
        int d = dx + dy;
        if (d < bestDistToObject) {
          bestDistToObject = d;
          target = a.loc;
        }
      }
    }
    if (target == null) {
      // no decorations: just pick the first (caller can randomize if desired)
      return possibleLocs.get(0);
    }
    // Choose the possible location that gets closest to target
    Cell chosen = possibleLocs.get(0);
    int best = Integer.MAX_VALUE;
    for (int i = 0; i < possibleLocs.size(); i++) {
      Cell c = possibleLocs.get(i);
      int dx = Math.abs((int) c.col - (int) target.col);
      int dy = Math.abs(c.row - target.row);
      int d = dx + dy;
      if (d < best) {
        best = d;
        chosen = c;
      }
    }
    return chosen;
  }
}
