import java.awt.Graphics;
import java.util.List;

public class BotMoving implements GameState {
  @Override
  public void mouseClick(int x, int y, Stage s) {
    // no mouseClick activity for this GameState
  }

  @Override
  public void paint(Graphics g, Stage s) {
    for(Actor player: s.listOfPlayers) {
      if(player.isBot()) {
        List<Cell> possibleLocs = s.getClearRadius(player.loc, player.moves);
        if(possibleLocs.isEmpty()) {
          continue;
        }
        MoveStrategy mover = player.getMover();
        if(mover == null) {
          mover = player.baseStrategy();
          player.setMover(mover);
        }
        Cell destination = mover.chooseNextLoc(possibleLocs, player, s.listOfPlayers);
        if(destination != null) {
          player.setLocation(destination);
          s.onActorMoved(player);
        }
      }
    }
    s.currentState = new ChoosingActor();
    for(Actor player: s.listOfPlayers) {
      player.turns = 1;
    }
  }  

  public String toString() {
    return getClass().getSimpleName();
  }
}
