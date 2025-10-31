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
  // Game over flags
  private boolean gameOver = false;
  private boolean gameWon = false;
  // End-screen buttons
  private java.awt.Rectangle restartButtonRect = null;
  private java.awt.Rectangle quitButtonRect = null;
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
    // Check win/lose conditions; if game is over, we will still render the grid
    // but skip updates and draw an overlay at the end.
    checkGameOver();
    // Count human moves left (used for bot scheduling below)
    int humansWithMovesLeft = 0;
    if (currentState instanceof ChoosingActor) {
      for (int i = 0; i < listOfPlayers.size(); i++) {
        Actor a = listOfPlayers.get(i);
        // Count only real human-controlled actors, not decorations
        if (!a.isBot() && a.turns > 0
            && !(a instanceof Tree)
            && !(a instanceof Fish)
            && !(a instanceof Cactus)) {
          humansWithMovesLeft++;
        }
      }
    }
    // Run current state logic (may draw overlays or handle clicks)
    currentState.paint(g, this);
    if (!gameOver) {
      // let the grid and its terrains update gradually each frame
      grid.tick();
      // remove any decorations whose terrain no longer matches their type
      purgeMismatchedDecorations();
      // maybe spawn decorations (tree/fish/cactus) every 5 seconds
      maybeSpawnDecorations();
      // Move bots periodically ONLY when it's effectively the bot phase:
      // - We're in ChoosingActor and no human has moves left
      // - Do NOT interrupt SelectingNewLocation (player choosing a tile)
      if (currentState instanceof ChoosingActor && humansWithMovesLeft == 0) {
        long nowBot = System.currentTimeMillis();
        if (nowBot - lastBotMoveMs >= BOT_MOVE_INTERVAL_MS) {
          (new BotMoving()).paint(g, this);
          lastBotMoveMs = nowBot;
        }
      }
      // Update timers only when not in the middle of selecting a move,
      // to avoid interfering with player input.
      if (!(currentState instanceof SelectingNewLocation)) {
        // update cat bubbles timer (2s intervals)
        updateCatBubblesTimer();
        // update cat sand (suns) timer (2s intervals)
        updateCatSandTimer();
      }
    }
    grid.paint(g, mouseLoc);
    // Blue cell selection overlay with 50% transparency
    grid.paintOverlay(g, cellOverlay, new Color(0f, 0f, 1f, 0.5f));

    beat.ticktock();
    for(Actor player: listOfPlayers) {
      player.paint(g);
    }
    draw_sidepanel(g, mouseLoc);
    if (gameOver) {
      drawGameOver(g);
    }
  }

  private void checkGameOver() {
    // Only evaluate once game not already over
    if (gameOver) return;
    Cat theCat = null;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      if (listOfPlayers.get(i) instanceof Cat) {
        theCat = (Cat) listOfPlayers.get(i);
        break;
      }
    }
    if (theCat == null) return;
    // Compute total points collected by all bots
    int botPoints = 0;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      Actor a = listOfPlayers.get(i);
      if (a.isBot()) {
        botPoints = botPoints + a.getPoints();
      }
    }
    // Lose has priority if multiple conditions happen simultaneously
    if (theCat.getHealth() <= 0 || botPoints >= 3) {
      gameOver = true;
      gameWon = false;
    } else if (theCat.getPoints() >= 3) {
      gameOver = true;
      gameWon = true;
    }
  }

  private void drawGameOver(Graphics g) {
    // Dark overlay only over a centered 10x10 cells area on the grid
    // Grid draws starting at (10,10) with Cell.size per cell and is 20x20
    int gridOriginX = 10;
    int gridOriginY = 10;
    int cell = Cell.size;
    int rectX = gridOriginX + cell * 5; // start at col 5
    int rectY = gridOriginY + cell * 5; // start at row 5
    int rectW = cell * 10;
    int rectH = cell * 10;

    Color old = g.getColor();
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(rectX, rectY, rectW, rectH);

    // Message text centered within the 10x10 overlay
    java.awt.Font oldFont = g.getFont();
    java.awt.Font title = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 24.0f);
    java.awt.Font subtitle = oldFont.deriveFont(java.awt.Font.PLAIN, oldFont.getSize() + 10.0f);
    g.setFont(title);
    String msg = gameWon ? "You Win!" : "You Lose!";
    int cx = rectX + rectW / 2 - 90; // approximate centering
    int cy = rectY + rectH / 2 - 10;
    if (gameWon) {
      g.setColor(Color.WHITE);
    } else {
      g.setColor(Color.RED);
    }
    g.drawString(msg, cx, cy);
    g.setFont(subtitle);
    g.setColor(Color.WHITE);
    if (gameWon) {
      g.drawString("Collected 1 point.", cx - 10, cy + 40);
    } else {
      g.drawString("Health reached 0.", cx - 5, cy + 40);
    }

    // Draw Restart and Quit buttons under the text
    int btnW = 140;
    int btnH = 36;
    int btnGap = 20;
    int totalBtnsW = btnW * 2 + btnGap;
    int btnStartX = rectX + (rectW - totalBtnsW) / 2;
    int btnY = cy + 70;
    // Restart button
    restartButtonRect = new java.awt.Rectangle(btnStartX, btnY, btnW, btnH);
    g.setColor(new Color(255, 255, 255, 220));
    g.fillRect(restartButtonRect.x, restartButtonRect.y, restartButtonRect.width, restartButtonRect.height);
    g.setColor(Color.DARK_GRAY);
    g.drawRect(restartButtonRect.x, restartButtonRect.y, restartButtonRect.width, restartButtonRect.height);
    java.awt.Font btnFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 6.0f);
    g.setFont(btnFont);
    g.drawString("Restart", restartButtonRect.x + 24, restartButtonRect.y + 24);
    // Quit button
    quitButtonRect = new java.awt.Rectangle(btnStartX + btnW + btnGap, btnY, btnW, btnH);
    g.setColor(new Color(255, 255, 255, 220));
    g.fillRect(quitButtonRect.x, quitButtonRect.y, quitButtonRect.width, quitButtonRect.height);
    g.setColor(Color.DARK_GRAY);
    g.drawRect(quitButtonRect.x, quitButtonRect.y, quitButtonRect.width, quitButtonRect.height);
    g.drawString("Quit", quitButtonRect.x + 48, quitButtonRect.y + 24);
    g.setFont(oldFont);
    g.setColor(old);
  }

  private void updateCatBubblesTimer() {
    // find the cat
    Cat theCat = null;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      if (listOfPlayers.get(i) instanceof Cat) {
        theCat = (Cat) listOfPlayers.get(i);
        break;
      }
    }
    if (theCat == null) return;
    boolean inWaterNow = theCat.loc.getTerrain() instanceof WaterTerrain;
    long now = System.currentTimeMillis();
    if (inWaterNow && !theCat.isInWater()) {
      // just entered water
      theCat.setBubbles(3);
      theCat.setLastBubbleMs(now);
      theCat.setInWater(true);
    } else if (!inWaterNow && theCat.isInWater()) {
      // just left water
      theCat.setBubbles(3);
      theCat.setInWater(false);
      theCat.setLastBubbleMs(0L); // reset timer so next entry starts fresh
    }
    if (inWaterNow) {
      long elapsed = now - theCat.getLastBubbleMs();
      if (elapsed >= 2000L) { // every 2 seconds in water
        if (theCat.getBubbles() > 0) {
          theCat.removeOneBubble();
        } else if (theCat.getHealth() > 0) {
          theCat.loseHeart();
        }
        theCat.setLastBubbleMs(now);
      }
    }
  }

  private void updateCatSandTimer() {
    // find the cat
    Cat theCat = null;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      if (listOfPlayers.get(i) instanceof Cat) {
        theCat = (Cat) listOfPlayers.get(i);
        break;
      }
    }
    if (theCat == null) return;
    boolean inSandNow = theCat.loc.getTerrain() instanceof SandTerrain;
    long now = System.currentTimeMillis();
    if (inSandNow && !theCat.isInSand()) {
      // just entered sand
      theCat.setSuns(3);
      theCat.setLastSunMs(now);
      theCat.setInSand(true);
    } else if (!inSandNow && theCat.isInSand()) {
      // just left sand
      theCat.setSuns(3);
      theCat.setInSand(false);
      theCat.setLastSunMs(0L);
    }
    if (inSandNow) {
      long elapsed = now - theCat.getLastSunMs();
      if (elapsed >= 2000L) { // every 2 seconds in sand
        if (theCat.getSuns() > 0) {
          theCat.removeOneSun();
        } else if (theCat.getHealth() > 0) {
          theCat.loseHeart();
        }
        theCat.setLastSunMs(now);
      }
    }
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

      // Draw bubbles only while in water
      if (theCat.isInWater()) {
        // Draw bubbles below hearts (3 bubbles shown)
        int bubbles = theCat.getBubbles();
        String bubblesStr = "";
        for (int b = 0; b < bubbles; b++) { bubblesStr = bubblesStr + "\u25CF "; } // filled circle
        for (int b = bubbles; b < 3; b++) { bubblesStr = bubblesStr + "\u25CB "; } // empty circle
        g.setColor(new Color(30, 144, 255)); // dodger blue
        java.awt.Font bubblesFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 10.0f);
        g.setFont(bubblesFont);
        g.drawString(bubblesStr, margin + 120, yLoc + 28);
      } else if (theCat.isInSand()) {
        // Draw suns below hearts while in sand
        int suns = theCat.getSuns();
        String sunsStr = "";
        for (int s = 0; s < suns; s++) { sunsStr = sunsStr + "\u2600 "; } // ☀ filled sun
        for (int s = suns; s < 3; s++) { sunsStr = sunsStr + "\u263C "; } // ☼ outlined sun
        g.setColor(new Color(255, 165, 0)); // orange-ish sun
        java.awt.Font sunsFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 10.0f);
        g.setFont(sunsFont);
        g.drawString(sunsStr, margin + 120, yLoc + 28);
      }

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
    if (gameOver) {
      // Allow clicks on buttons during game over
      if (restartButtonRect != null && restartButtonRect.contains(x, y)) {
        restartGame();
        return;
      }
      if (quitButtonRect != null && quitButtonRect.contains(x, y)) {
        System.exit(0);
        return;
      }
      return; // ignore other input when game is over
    }
    currentState.mouseClick(x, y, this);
  }

  private void restartGame() {
    // Clear game over flags and UI elements
    gameOver = false;
    gameWon = false;
    restartButtonRect = null;
    quitButtonRect = null;
    // Reset players state: remove decorations, reset turns, reset points/health for Cat
    List<Actor> kept = new ArrayList<Actor>();
    Cat theCat = null;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      Actor a = listOfPlayers.get(i);
      if (a instanceof Tree || a instanceof Fish || a instanceof Cactus) {
        // drop decorations
        continue;
      }
      a.turns = 1;
      a.resetPoints();
      if (a instanceof Cat) {
        theCat = (Cat) a;
      }
      kept.add(a);
    }
    listOfPlayers = kept;
    // Reset Cat specific state
    if (theCat != null) {
      theCat.setHealth(3);
      theCat.setBubbles(0);
      theCat.setLastBubbleMs(0L);
      theCat.setInWater(false);
      theCat.setSuns(0);
      theCat.setLastSunMs(0L);
      theCat.setInSand(false);
    }
    // Clear any selection overlays and reset state machine
    playerInAction = Optional.empty();
    cellOverlay = new ArrayList<Cell>();
    currentState = new ChoosingActor();
    // Reset timers to avoid immediate spawns/moves
    lastSpawnCheckMs = System.currentTimeMillis();
    lastBotMoveMs = System.currentTimeMillis();
  }

  // Move an actor and collect any decoration at the destination cell
  public void moveActorTo(Actor actor, Cell dest) {
    // track cat water movement for bubbles
    boolean srcIsWater = actor.loc.getTerrain() instanceof WaterTerrain;
    boolean destIsWater = dest.getTerrain() instanceof WaterTerrain;
    // track cat sand movement for suns
    boolean srcIsSand = actor.loc.getTerrain() instanceof SandTerrain;
    boolean destIsSand = dest.getTerrain() instanceof SandTerrain;
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
    // If cat moved inside water, remove a bubble immediately
    if (actor instanceof Cat) {
      Cat c = (Cat) actor;
      if (srcIsWater && destIsWater) {
        if (c.getBubbles() > 0) {
          c.removeOneBubble();
        } else if (c.getHealth() > 0) {
          c.loseHeart();
        }
        c.setLastBubbleMs(System.currentTimeMillis());
      }
      // If cat moved inside sand, remove a sun immediately
      if (srcIsSand && destIsSand) {
        if (c.getSuns() > 0) {
          c.removeOneSun();
        } else if (c.getHealth() > 0) {
          c.loseHeart();
        }
        c.setLastSunMs(System.currentTimeMillis());
      }
      // Do not manually toggle inWater/inSand here; let the timer methods
      // detect entry/exit based on current terrain and update flags and counters.
    }
  }
}
