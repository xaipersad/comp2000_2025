import java.awt.Graphics;
import java.util.List;
import java.util.Random;

public class BotMoving implements GameState {
  private final TargetingStrategy strategy = new NearestObjectStrategy();
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

        Cell chosen = strategy.chooseMove(player, possibleLocs, s);
        if (chosen == null) {
          // Fallback random move
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
