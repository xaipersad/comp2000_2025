import java.awt.Graphics;
import java.util.List;
import java.util.Random;

public class BotMoving implements GameState {
  @Override
  public void mouseClick(int x, int y, Stage s) {
    // no mouseClick activity for this GameState
  }

  @Override
  public void paint(Graphics g, Stage s) {
    // Work on a copy to avoid concurrent modification when collecting decorations
    java.util.ArrayList<Actor> snapshot = new java.util.ArrayList<Actor>(s.listOfPlayers);
    for(int p = 0; p < snapshot.size(); p++) {
      Actor player = snapshot.get(p);
      if(player.isBot()) {
        // Limit bot movement to 1 cell per bot phase to avoid long jumps
        List<Cell> possibleLocs = s.getClearRadius(player.loc, 1);
        if (possibleLocs.size() == 0) {
          continue;
        }

        // Find nearest decoration (Tree/Fish/Cactus)
        Cell target = null;
        int bestDistToObject = Integer.MAX_VALUE;
        for (int i = 0; i < s.listOfPlayers.size(); i++) {
          Actor a = s.listOfPlayers.get(i);
          if (a instanceof Tree || a instanceof Fish || a instanceof Cactus) {
            // distance by grid (Manhattan)
            int dx = Math.abs((int)player.loc.col - (int)a.loc.col);
            int dy = Math.abs(player.loc.row - a.loc.row);
            int d = dx + dy;
            if (d < bestDistToObject) {
              bestDistToObject = d;
              target = a.loc;
            }
          }
        }

        Cell chosen;
        if (target != null) {
          // Choose the possible location that gets closest to the target
          chosen = possibleLocs.get(0);
          int best = Integer.MAX_VALUE;
          for (int i = 0; i < possibleLocs.size(); i++) {
            Cell c = possibleLocs.get(i);
            int dx = Math.abs((int)c.col - (int)target.col);
            int dy = Math.abs(c.row - target.row);
            int d = dx + dy;
            if (d < best) {
              best = d;
              chosen = c;
            }
          }
        } else {
          // No decorations: move randomly
          int moveBotChooses = (new Random()).nextInt(possibleLocs.size());
          chosen = possibleLocs.get(moveBotChooses);
        }
        s.moveActorTo(player, chosen);
      }
    }
    s.currentState = new ChoosingActor();
    for(Actor player: s.listOfPlayers) {
      // Reset turns only for non-decoration actors
      if (!(player instanceof Tree) && !(player instanceof Fish) && !(player instanceof Cactus)) {
        player.turns = 1;
      }
    }
  }  

  public String toString() {
    return getClass().getSimpleName();
  }
}
