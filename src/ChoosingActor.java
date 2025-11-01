import java.awt.Graphics;

public class ChoosingActor implements GameState {
  @Override
  public void mouseClick(int x, int y, Stage s) {
    s.playerInAction = s.listOfPlayers.stream()
      .filter(player -> !player.isBot())
      .filter(player -> player.loc.contains(x, y))
      .findFirst();
    if (s.playerInAction.isPresent()) {
      Actor player = s.playerInAction.get();
      s.cellOverlay = s.grid.getRadius(player.loc, player.moves);
      s.currentState = new SelectingNewLocation();
    }
  }

  @Override
  public void paint(Graphics g, Stage s) {
  }  

  public String toString() {
    return getClass().getSimpleName();
  }
}
