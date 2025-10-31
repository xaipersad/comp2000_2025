import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Stage {
  Grid grid;
  List<Actor> listOfPlayers;
  List<Cell> cellOverlay;
  Optional<Actor> playerInAction;
  // Spawning support
  private long lastSpawnCheckMs = 0L;
  private static final long SPAWN_INTERVAL_MS = 5000L; // 5 seconds
  private static final double PROB_GRASS_TREE = 0.03; // 3% per check
  private static final double PROB_WATER_FISH = 0.03;
  private static final double PROB_SAND_CACTUS = 0.03;
  private java.util.Random rng = new java.util.Random();
  private static final int MAX_TREES = 3;
  private static final int MAX_FISH = 3;
  private static final int MAX_CACTUS = 3;
  // Bot move timer
  private long lastBotMoveMs = 0L;
  private static final long BOT_MOVE_INTERVAL_MS = 2500L; // bots step roughly once per 2.5s

  GameState currentState;
  Beat beat;

  public Stage() {
    grid = GameManager.getInstance().getGrid();
    listOfPlayers = new ArrayList<Actor>();
    cellOverlay = new ArrayList<Cell>();
    playerInAction = Optional.empty();
    currentState = new ChoosingActor();
    beat = new AnimationBeat();
  }

  public void addPlayer(Actor player) {
    listOfPlayers.add(player);
    if(player.isBot()) {
      beat.punchIn(player);
    }
  }

  public void paint(Graphics g, Point mouseLoc) {
    // If there are no human moves left, automatically switch to bot phase
    if (currentState instanceof ChoosingActor) {
      int humansWithMovesLeft = 0;
      for (int i = 0; i < listOfPlayers.size(); i++) {
        Actor a = listOfPlayers.get(i);
        if (!a.isBot() && a.turns > 0) {
          humansWithMovesLeft++;
        }
      }
      if (humansWithMovesLeft == 0) {
        currentState = new BotMoving();
      }
    }
  // do we have bot moves to make?
  currentState.paint(g, this);
    // let the grid and its terrains update gradually each frame
    grid.tick();
  // remove any decorations whose terrain no longer matches their type
  purgeMismatchedDecorations();
    // maybe spawn decorations (tree/fish/cactus) every 5 seconds
    maybeSpawnDecorations();
    // move bots periodically regardless of human turn state
    long nowBot = System.currentTimeMillis();
    if (nowBot - lastBotMoveMs >= BOT_MOVE_INTERVAL_MS) {
      (new BotMoving()).paint(g, this);
      lastBotMoveMs = nowBot;
    }
    grid.paint(g, mouseLoc);
    // Blue cell selection overlay with 50% transparency
    grid.paintOverlay(g, cellOverlay, new Color(0f, 0f, 1f, 0.5f));

    beat.ticktock();
    for(Actor player: listOfPlayers) {
      player.paint(g);
    }
    draw_sidepanel(g, mouseLoc);
  }

  private boolean isOccupied(Cell cell) {
    for (int i = 0; i < listOfPlayers.size(); i++) {
      if (listOfPlayers.get(i).loc == cell) {
        return true;
      }
    }
    return false;
  }

  private void maybeSpawnDecorations() {
    long now = System.currentTimeMillis();
    if (now - lastSpawnCheckMs < SPAWN_INTERVAL_MS) {
      return;
    }
    lastSpawnCheckMs = now;

    // count existing decorations to enforce caps
    int trees = 0;
    int fish = 0;
    int cacti = 0;
    for (int idx = 0; idx < listOfPlayers.size(); idx++) {
      Actor a = listOfPlayers.get(idx);
      if (a instanceof Tree) {
        trees++;
      } else if (a instanceof Fish) {
        fish++;
      } else if (a instanceof Cactus) {
        cacti++;
      }
    }
    // iterate all cells and randomly spawn based on terrain type
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 20; j++) {
        Optional<Cell> opt = grid.cellAtColRow(i, j);
        if (!opt.isPresent()) continue;
        Cell cell = opt.get();
        if (isOccupied(cell)) continue;
        TerrainType t = cell.getTerrain();
        if (t instanceof GrassTerrain) {
          if (trees < MAX_TREES && rng.nextDouble() < PROB_GRASS_TREE) {
            addPlayer(new Tree(cell, false));
            trees++;
          }
        } else if (t instanceof WaterTerrain) {
          if (fish < MAX_FISH && rng.nextDouble() < PROB_WATER_FISH) {
            addPlayer(new Fish(cell, false));
            fish++;
          }
        } else if (t instanceof SandTerrain) {
          if (cacti < MAX_CACTUS && rng.nextDouble() < PROB_SAND_CACTUS) {
            addPlayer(new Cactus(cell, false));
            cacti++;
          }
        }
      }
    }
  }

  private void purgeMismatchedDecorations() {
    // rebuild the list excluding decorations that no longer match their terrain
    List<Actor> kept = new ArrayList<Actor>();
    for (int i = 0; i < listOfPlayers.size(); i++) {
      Actor a = listOfPlayers.get(i);
      if (a instanceof Tree) {
        // Tree must be on grass
        if (a.loc.getTerrain() instanceof GrassTerrain) {
          kept.add(a);
        }
      } else if (a instanceof Fish) {
        // Fish must be on water
        if (a.loc.getTerrain() instanceof WaterTerrain) {
          kept.add(a);
        }
      } else if (a instanceof Cactus) {
        // Cactus must be on sand
        if (a.loc.getTerrain() instanceof SandTerrain) {
          kept.add(a);
        }
      } else {
        // keep all other actors (cats/dogs/birds, etc.)
        kept.add(a);
      }
    }
    listOfPlayers = kept;
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
    Optional<Cell> underMouse = grid.cellAtPoint(mouseLoc);
    if(underMouse.isPresent()) {
      Cell hoverCell = underMouse.get();
      g.setColor(Color.DARK_GRAY);
      String coord = String.valueOf(hoverCell.col) + String.valueOf(hoverCell.row);
      g.drawString(coord, margin, yLoc);
      // show weather info for the hovered cell
      yLoc = yLoc + (blockVT/2);
      g.drawString(String.format("Temp: %.1f C", hoverCell.getTemperature()), margin, yLoc);
      yLoc = yLoc + (blockVT/2);
      g.drawString(String.format("Rain: %.2f (norm)", hoverCell.getRainfall()), margin, yLoc);
      yLoc = yLoc + (blockVT/2);
      g.drawString(String.format("WindX: %.2f", hoverCell.getWindX()), margin, yLoc);
      yLoc = yLoc + (blockVT/2);
      g.drawString(String.format("WindY: %.2f", hoverCell.getWindY()), margin, yLoc);
      yLoc = yLoc + (blockVT/2);
      g.drawString(String.format("WindMag: %.2f", hoverCell.getWind()), margin, yLoc);
    }

    // Cat health section (separate from other actor info)
    yLoc = yLoc + blockVT;
    Cat theCat = null;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      if (listOfPlayers.get(i) instanceof Cat) {
        theCat = (Cat) listOfPlayers.get(i);
        break;
      }
    }
      if (theCat != null) {
        g.setColor(Color.DARK_GRAY);
        java.awt.Font oldFont = g.getFont();
        // Bigger label font
        java.awt.Font labelFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 6.0f);
        g.setFont(labelFont);
        g.drawString("Health:", margin, yLoc);

        // Compose hearts string
        int hp = theCat.getHealth();
        String hearts = "";
        for (int h = 0; h < hp; h++) { hearts = hearts + "\u2665 "; }
        for (int h = hp; h < 3; h++) { hearts = hearts + "\u2661 "; }

        // Bigger hearts font
        java.awt.Font heartsFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 12.0f);
        g.setFont(heartsFont);
        g.setColor(Color.RED);
        g.drawString(hearts, margin + 120, yLoc);

        // restore defaults
        g.setFont(oldFont);
        g.setColor(Color.DARK_GRAY);
      }

    // agent display
    final int vTab = 15;
    final int labelIndent = margin + hTab;
    final int valueIndent = margin + 3*blockVT;
    yLoc = yLoc + 2*blockVT;
    for(int i = 0; i < listOfPlayers.size(); i++){
      Actor a = listOfPlayers.get(i);
      // Do not show decoration objects (Tree, Fish, Cactus) in the side panel
      if (a instanceof Tree) {
        continue;
      } else if (a instanceof Fish) {
        continue;
      } else if (a instanceof Cactus) {
        continue;
      }
      yLoc = yLoc + 2*blockVT;
      g.drawString(a.getClass().getName(), margin, yLoc);
      g.drawString("location:", labelIndent, yLoc+vTab);
      g.drawString(Character.toString(a.loc.col) + Integer.toString(a.loc.row), valueIndent, yLoc+vTab);
      g.drawString("player type:", labelIndent, yLoc+2*vTab);
      String playerType;
      if (a.isBot()) {
        playerType = "Bot";
      } else {
        playerType = "Human";
      }
      g.drawString(playerType, valueIndent, yLoc+2*vTab);
      // show points for players
      g.drawString("points:", labelIndent, yLoc+3*vTab);
      g.drawString(Integer.toString(a.getPoints()), valueIndent, yLoc+3*vTab);
      // remove mover from bot info (no mover line)
    }    
  }

  public List<Cell> getClearRadius(Cell from, int size) {
    List<Cell> init = grid.getRadius(from, size);
    for(int idx = 0; idx < listOfPlayers.size(); idx++) {
      Actor a = listOfPlayers.get(idx);
      // Only block cells occupied by non-decoration actors (players).
      if (!(a instanceof Tree) && !(a instanceof Fish) && !(a instanceof Cactus)) {
        init.remove(a.loc);
      }
    }
    return init;
  }

  public void mouseClicked(int x, int y) {
    currentState.mouseClick(x, y, this);
  }

  // Move an actor and collect any decoration at the destination cell
  public void moveActorTo(Actor actor, Cell dest) {
    actor.setLocation(dest);
    // check for collectible decorations at dest
    for (int i = 0; i < listOfPlayers.size(); i++) {
      Actor other = listOfPlayers.get(i);
      if (other == actor) {
        continue;
      }
      if (other.loc == dest) {
        if (other instanceof Tree || other instanceof Fish || other instanceof Cactus) {
          // collect it: remove and add a point
          listOfPlayers.remove(i);
          actor.addPoint();
          break;
        }
      }
    }
  }
}
