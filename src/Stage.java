import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Stage {
  Grid grid;
  List<Actor> listOfPlayers;
  List<Cell> cellOverlay;
  Optional<Actor> playerInAction;

  GameState currentState;
  Beat beat;
  private final WeatherController weatherController;
  private final WeatherService weatherService;

  public Stage() {
    grid = new Grid();
    listOfPlayers = new ArrayList<Actor>();
    cellOverlay = new ArrayList<Cell>();
    playerInAction = Optional.empty();
    currentState = new ChoosingActor();
    beat = new AnimationBeat();
    weatherController = new WeatherController(grid);
    weatherService = new WeatherService("http://13.238.167.130/weather");
    weatherService.addListener(weatherController);
    weatherService.start();
  }

  public void addPlayer(Actor player) {
    listOfPlayers.add(player);
    if(player.isBot()) {
      beat.punchIn(player);
    }
    weatherController.updateMoveStrategy(player);
  }

  public void paint(Graphics g, Point mouseLoc) {
    // do we have bot moves to make?
    currentState.paint(g, this);
    grid.paint(g, mouseLoc);
    // Blue cell selection overlay with 50% transparency
    grid.paintOverlay(g, cellOverlay, new Color(0f, 0f, 1f, 0.5f));
    Map<Cell, Color> weatherOverlays = weatherController.overlayColors();
    grid.paintOverlay(g, weatherOverlays);

    beat.ticktock();
    weatherController.applyTemperature(listOfPlayers);
    for(Actor player: listOfPlayers) {
      player.paint(g);
    }
    draw_sidepanel(g, mouseLoc);
  }

  private void draw_sidepanel(Graphics g, Point mouseLoc) {
    // lots of magic numbers here
    // they are used to calculate the coordinates of where to draw on the information panel
    final int hTab = 10;
    final int blockVT = 35;
    final int margin = 21*blockVT;
    int yLoc = 20;

    // state display
    g.setColor(Color.DARK_GRAY);
    g.drawString(currentState.toString(), margin, yLoc);
    yLoc = yLoc + blockVT;
    final int vTab = 15;
    final int labelIndent = margin + hTab;
    final int valueIndent = margin + 3*blockVT;
    Optional<Cell> underMouse = grid.cellAtPoint(mouseLoc);
    if(underMouse.isPresent()) {
      Cell hoverCell = underMouse.get();
      g.setColor(Color.DARK_GRAY);
      String coord = String.valueOf(hoverCell.col) + String.valueOf(hoverCell.row);
      g.drawString(coord, margin, yLoc);
      Optional<WeatherSnapshot> snapshotOptional = weatherController.weatherAt(hoverCell);
      if(snapshotOptional.isPresent()) {
        WeatherSnapshot snapshot = snapshotOptional.get();
        g.drawString(String.format("rain: %.0f%%", snapshot.rainfall() * 100), margin, yLoc + vTab);
        g.drawString(String.format("wind: %.0f%%", snapshot.windStrength() * 100), margin, yLoc + 2*vTab);
        g.drawString(String.format("temp: %.0f%%", snapshot.temperature() * 100), margin, yLoc + 3*vTab);
      }
    }

    // agent display
    yLoc = yLoc + 2*blockVT + 2*vTab;
    for(int i = 0; i < listOfPlayers.size(); i++){
      Actor a = listOfPlayers.get(i);
      yLoc = yLoc + 2*blockVT;
      g.drawString(a.getClass().getName(), margin, yLoc);
      g.drawString("location:", labelIndent, yLoc+vTab);
      g.drawString(Character.toString(a.loc.col) + Integer.toString(a.loc.row), valueIndent, yLoc+vTab);
      g.drawString("player type:", labelIndent, yLoc+2*vTab);
      g.drawString(a.isBot() ? "Bot" : "Human", valueIndent, yLoc+2*vTab);
      if(a.isBot() && a.mover != null) {
        g.drawString("mover:", labelIndent, yLoc+3*vTab);
        g.drawString(a.mover.getClass().getName(), valueIndent, yLoc+3*vTab);
      }
    }    
  }

  public List<Cell> getClearRadius(Cell from, int size) {
    List<Cell> init = grid.getRadius(from, size);
    for(Actor player: listOfPlayers) {
      init.remove(player.loc);
    }
    init.removeIf(weatherController::isCellBlocked);
    return init;
  }

  public void mouseClicked(int x, int y) {
    currentState.mouseClick(x, y, this);
  }

  public void onActorMoved(Actor actor) {
    weatherController.updateMoveStrategy(actor);
  }

  public WeatherController getWeatherController() {
    return weatherController;
  }
}
