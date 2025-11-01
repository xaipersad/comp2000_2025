import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Optional;

public class SelectingNewLocation implements GameState {
  @Override
  public void mouseClick(int x, int y, Stage s) {
    Optional<Cell> clicked = s.cellOverlay.stream()
      .filter(c -> c.contains(x, y))
      .findFirst();
    s.cellOverlay = new ArrayList<Cell>();
    if(clicked.isPresent() && s.playerInAction.isPresent()) {
      s.moveActorTo(s.playerInAction.get(), clicked.get());
      s.playerInAction.get().turns--;
      int humansWithMovesLeft = (int) s.listOfPlayers.stream()
        .filter(player -> !player.isBot() && player.turns > 0)
        .filter(player -> !(player instanceof Tree)
                          && !(player instanceof Fish)
                          && !(player instanceof Cactus))
        .count();
      if(humansWithMovesLeft > 0) {
        s.currentState = new ChoosingActor();
      } else {
        s.currentState = new BotMoving();
      }
    }
  }

  @Override
  public void paint(Graphics g, Stage s) {
  }

  public String toString() {
    return getClass().getSimpleName();
  }
}
