import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Stage {
  Grid grid;
  List<Actor> listOfPlayers;
  List<Cell> cellOverlay;
  Optional<Actor> playerInAction;
  // game over 
  private boolean gameOver = false;
  private boolean gameWon = false;
  // end-screen 
  private String actorReached3Name = null;
  // start screen
  private boolean gameStarted = false;
  private java.awt.Rectangle startPlayButtonRect = null;
  private java.awt.Rectangle startQuitButtonRect = null;
  // end-screen buttons
  private java.awt.Rectangle restartButtonRect = null;
  private java.awt.Rectangle quitButtonRect = null;
  // spawning support
  private long lastSpawnCheckMs = 0L;
  private static final long SPAWN_INTERVAL_MS = 5000L; 
  private static final double PROB_GRASS_TREE = 0.03;
  private static final double PROB_WATER_FISH = 0.03;
  private static final double PROB_SAND_CACTUS = 0.03;
  private java.util.Random rng = new java.util.Random();
  private static final int MAX_TREES = 3;
  private static final int MAX_FISH = 3;
  private static final int MAX_CACTUS = 3;
  // bot move timer
  private long lastBotMoveMs = 0L;
  private static final long BOT_MOVE_INTERVAL_MS = 2500L; 

  GameState currentState;
  Beat beat;
  private final ActorRenderer actorRenderer = new BaseActorRenderer();

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
    if (gameStarted) {
      checkGameOver();
    }
    int humansWithMovesLeft = 0;
    if (currentState instanceof ChoosingActor) {
      humansWithMovesLeft = (int) listOfPlayers.stream()
        .filter(a -> !a.isBot() && a.turns > 0)
        .filter(a -> !(a instanceof Tree) && !(a instanceof Fish) && !(a instanceof Cactus))
        .count();
    }
    currentState.paint(g, this);
    if (!gameOver && gameStarted) {
      grid.tick();
      purgeMismatchedDecorations();
      maybeSpawnDecorations();
      if (currentState instanceof ChoosingActor && humansWithMovesLeft == 0) {
        long nowBot = System.currentTimeMillis();
        if (nowBot - lastBotMoveMs >= BOT_MOVE_INTERVAL_MS) {
          (new BotMoving()).paint(g, this);
          lastBotMoveMs = nowBot;
        }
      }
      if (!(currentState instanceof SelectingNewLocation)) {
        updateCatBubblesTimer();
        updateCatSandTimer();
      }
    }
    grid.paint(g, mouseLoc);
    grid.paintOverlay(g, cellOverlay, new Color(0f, 0f, 1f, 0.5f));

    beat.ticktock();
    for(Actor player: listOfPlayers) {
      actorRenderer.render(g, player);
    }
    draw_sidepanel(g, mouseLoc);
    if (!gameStarted) {
      drawStartScreen(g);
    } else if (gameOver) {
      drawGameOver(g);
    }
  }

  private void checkGameOver() {
    if (gameOver) return;
    Cat theCat = listOfPlayers.stream()
      .filter(a -> a instanceof Cat)
      .map(a -> (Cat)a)
      .findFirst()
      .orElse(null);
    if (theCat == null) return;
    Optional<Actor> botAtThree = listOfPlayers.stream()
      .filter(a -> a.isBot() && a.getPoints() >= 3)
      .findFirst();
    if (botAtThree.isPresent()) {
      actorReached3Name = botAtThree.get().getClass().getSimpleName();
      gameOver = true;
      gameWon = false;
      return;
    }
    if (theCat.getHealth() <= 0) {
      gameOver = true;
      gameWon = false;
    } else if (theCat.getPoints() >= 5) {
      gameOver = true;
      gameWon = true;
    }
  }

  private void drawGameOver(Graphics g) {
    int gridOriginX = 10;
    int gridOriginY = 10;
    int cell = Cell.size;
    int rectX = gridOriginX + cell * 5; 
    int rectY = gridOriginY + cell * 5; 
    int rectW = cell * 10;
    int rectH = cell * 10;

    Color old = g.getColor();
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(rectX, rectY, rectW, rectH);

    java.awt.Font oldFont = g.getFont();
    java.awt.Font title = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 24.0f);
    java.awt.Font subtitle = oldFont.deriveFont(java.awt.Font.PLAIN, oldFont.getSize() + 10.0f);
    g.setFont(title);
    String msg;
    if (gameWon) {
      msg = "You Win!";
    } else {
      msg = "You Lose!";
    }
    int cx = rectX + rectW / 2 - 90; 
    int cy = rectY + rectH / 2 - 10;
    if (gameWon) {
      g.setColor(Color.WHITE);
    } else {
      g.setColor(Color.RED);
    }
    g.drawString(msg, cx, cy);
    g.setFont(subtitle);
    g.setColor(Color.WHITE);
    if (actorReached3Name != null) {
      g.drawString(actorReached3Name + " reached 3 points.", cx - 30, cy + 40);
    } else if (gameWon) {
      g.drawString("Collected 5 points.", cx - 10, cy + 40);
    } else {
      g.drawString("Health reached 0.", cx - 5, cy + 40);
    }

    int btnW = 140;
    int btnH = 36;
    int btnGap = 20;
    int totalBtnsW = btnW * 2 + btnGap;
    int btnStartX = rectX + (rectW - totalBtnsW) / 2;
    int btnY = cy + 70;
    // restart button
    restartButtonRect = new java.awt.Rectangle(btnStartX, btnY, btnW, btnH);
    g.setColor(new Color(255, 255, 255, 220));
    g.fillRect(restartButtonRect.x, restartButtonRect.y, restartButtonRect.width, restartButtonRect.height);
    g.setColor(Color.DARK_GRAY);
    g.drawRect(restartButtonRect.x, restartButtonRect.y, restartButtonRect.width, restartButtonRect.height);
    java.awt.Font btnFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 6.0f);
    g.setFont(btnFont);
    g.drawString("Restart", restartButtonRect.x + 24, restartButtonRect.y + 24);
    // quit button
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
    Cat theCat = listOfPlayers.stream()
      .filter(a -> a instanceof Cat)
      .map(a -> (Cat)a)
      .findFirst()
      .orElse(null);
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
      theCat.setLastBubbleMs(0L);
    }
    if (inWaterNow) {
      long elapsed = now - theCat.getLastBubbleMs();
      if (elapsed >= 2000L) { 
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
    Cat theCat = listOfPlayers.stream()
      .filter(a -> a instanceof Cat)
      .map(a -> (Cat)a)
      .findFirst()
      .orElse(null);
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
      if (elapsed >= 2000L) { 
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
    return listOfPlayers.stream().anyMatch(a -> a.loc == cell);
  }

  private void maybeSpawnDecorations() {
    long now = System.currentTimeMillis();
    if (now - lastSpawnCheckMs < SPAWN_INTERVAL_MS) {
      return;
    }
    lastSpawnCheckMs = now;

    // count existing objects to enforce caps
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
    // rebuild the list excluding objects that no longer match their terrain
    listOfPlayers = listOfPlayers.stream()
      .filter(a -> {
        if (a instanceof Tree) {
          return a.loc.getTerrain() instanceof GrassTerrain;
        } else if (a instanceof Fish) {
          return a.loc.getTerrain() instanceof WaterTerrain;
        } else if (a instanceof Cactus) {
          return a.loc.getTerrain() instanceof SandTerrain;
        } else {
          return true;
        }
      })
      .collect(Collectors.toList());
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

      // DEBUGGING INFO REMOVE LATER
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

    // cat health 
    yLoc = yLoc + blockVT;
    Cat theCat = listOfPlayers.stream()
      .filter(a -> a instanceof Cat)
      .map(a -> (Cat)a)
      .findFirst()
      .orElse(null);
      if (theCat != null) {
        g.setColor(Color.DARK_GRAY);
        java.awt.Font oldFont = g.getFont();
        java.awt.Font labelFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 6.0f);
        g.setFont(labelFont);
        g.drawString("Health:", margin, yLoc);
        int hp = theCat.getHealth();
        String hearts = "";
        for (int h = 0; h < hp; h++) { hearts = hearts + "\u2665 "; }
        for (int h = hp; h < 3; h++) { hearts = hearts + "\u2661 "; }
        java.awt.Font heartsFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 12.0f);
        g.setFont(heartsFont);
        g.setColor(Color.RED);
        g.drawString(hearts, margin + 120, yLoc);

      //  bubbles while in water
      if (theCat.isInWater()) {
        int bubbles = theCat.getBubbles();
        String bubblesStr = "";
        for (int b = 0; b < bubbles; b++) { bubblesStr = bubblesStr + "\u25CF "; } // filled circle
        for (int b = bubbles; b < 3; b++) { bubblesStr = bubblesStr + "\u25CB "; } // empty circle
        g.setColor(new Color(30, 144, 255)); // dodger blue
        java.awt.Font bubblesFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 10.0f);
        g.setFont(bubblesFont);
        g.drawString(bubblesStr, margin + 120, yLoc + 28);
      } else if (theCat.isInSand()) {
        //  sun while in sand
        int suns = theCat.getSuns();
        String sunsStr = "";
        for (int s = 0; s < suns; s++) { sunsStr = sunsStr + "\u2600 "; } // ☀ filled sun
        for (int s = suns; s < 3; s++) { sunsStr = sunsStr + "\u263C "; } // ☼ outlined sun
        g.setColor(new Color(255, 165, 0)); // orange-ish sun
        java.awt.Font sunsFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 10.0f);
        g.setFont(sunsFont);
        g.drawString(sunsStr, margin + 120, yLoc + 28);
      }
    g.setFont(oldFont);
    g.setColor(Color.DARK_GRAY);
      }
    final int vTab = 15;
    final int labelIndent = margin + hTab;
    final int valueIndent = margin + 3*blockVT;
    yLoc = yLoc + 2*blockVT;
    for(int i = 0; i < listOfPlayers.size(); i++){
      Actor a = listOfPlayers.get(i);
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
      g.drawString("points:", labelIndent, yLoc+3*vTab);
      g.drawString(Integer.toString(a.getPoints()), valueIndent, yLoc+3*vTab);
    }    
  }

  public List<Cell> getClearRadius(Cell from, int size) {
    List<Cell> init = grid.getRadius(from, size);
    for(int idx = 0; idx < listOfPlayers.size(); idx++) {
      Actor a = listOfPlayers.get(idx);
      if (!(a instanceof Tree) && !(a instanceof Fish) && !(a instanceof Cactus)) {
        init.remove(a.loc);
      }
    }
    return init;
  }

  public void mouseClicked(int x, int y) {
    if (!gameStarted) {
      if (startPlayButtonRect != null && startPlayButtonRect.contains(x, y)) {
        gameStarted = true;
        lastSpawnCheckMs = System.currentTimeMillis();
        lastBotMoveMs = System.currentTimeMillis();
        return;
      }
      if (startQuitButtonRect != null && startQuitButtonRect.contains(x, y)) {
        System.exit(0);
        return;
      }
      return;
    }
    if (gameOver) {
      if (restartButtonRect != null && restartButtonRect.contains(x, y)) {
        restartGame();
        return;
      }
      if (quitButtonRect != null && quitButtonRect.contains(x, y)) {
        System.exit(0);
        return;
      }
      return; 
    }
    currentState.mouseClick(x, y, this);
  }

  private void restartGame() {
    gameOver = false;
    gameWon = false;
  actorReached3Name = null;
    gameStarted = true;
    restartButtonRect = null;
    quitButtonRect = null;
    List<Actor> kept = new ArrayList<Actor>();
    Cat theCat = null;
    for (int i = 0; i < listOfPlayers.size(); i++) {
      Actor a = listOfPlayers.get(i);
      if (a instanceof Tree || a instanceof Fish || a instanceof Cactus) {
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
    if (theCat != null) {
      theCat.setHealth(3);
      theCat.setBubbles(0);
      theCat.setLastBubbleMs(0L);
      theCat.setInWater(false);
      theCat.setSuns(0);
      theCat.setLastSunMs(0L);
      theCat.setInSand(false);
    }
    playerInAction = Optional.empty();
    cellOverlay = new ArrayList<Cell>();
    currentState = new ChoosingActor();
    lastSpawnCheckMs = System.currentTimeMillis();
    lastBotMoveMs = System.currentTimeMillis();
  }

  private void drawStartScreen(Graphics g) {
    int gridOriginX = 10;
    int gridOriginY = 10;
    int cell = Cell.size;
    int rectX = gridOriginX + cell * 5; 
    int rectY = gridOriginY + cell * 5; 
    int rectW = cell * 10;
    int rectH = cell * 10;

    Color old = g.getColor();
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(rectX, rectY, rectW, rectH);

    java.awt.Font oldFont = g.getFont();
    java.awt.Font title = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 24.0f);
    java.awt.Font subtitle = oldFont.deriveFont(java.awt.Font.PLAIN, oldFont.getSize() + 10.0f);
    g.setFont(title);
    g.setColor(Color.WHITE);
    String msg = "Start Game";
    int cx = rectX + rectW / 2 - 90;
    int cy = rectY + rectH / 2 - 25;
    g.drawString(msg, cx, cy);
    g.setFont(subtitle);
    g.drawString("Collect 5 points to win.", cx - 25, cy + 40);

    int btnW = 140;
    int btnH = 36;
    int btnGap = 20;
    int totalBtnsW = btnW * 2 + btnGap;
    int btnStartX = rectX + (rectW - totalBtnsW) / 2;
    int btnY = cy + 70;
    startPlayButtonRect = new java.awt.Rectangle(btnStartX, btnY, btnW, btnH);
    g.setColor(new Color(255, 255, 255, 220));
    g.fillRect(startPlayButtonRect.x, startPlayButtonRect.y, startPlayButtonRect.width, startPlayButtonRect.height);
    g.setColor(Color.DARK_GRAY);
    g.drawRect(startPlayButtonRect.x, startPlayButtonRect.y, startPlayButtonRect.width, startPlayButtonRect.height);
    java.awt.Font btnFont = oldFont.deriveFont(java.awt.Font.BOLD, oldFont.getSize() + 6.0f);
    g.setFont(btnFont);
    g.drawString("Play", startPlayButtonRect.x + 48, startPlayButtonRect.y + 24);
    startQuitButtonRect = new java.awt.Rectangle(btnStartX + btnW + btnGap, btnY, btnW, btnH);
    g.setColor(new Color(255, 255, 255, 220));
    g.fillRect(startQuitButtonRect.x, startQuitButtonRect.y, startQuitButtonRect.width, startQuitButtonRect.height);
    g.setColor(Color.DARK_GRAY);
    g.drawRect(startQuitButtonRect.x, startQuitButtonRect.y, startQuitButtonRect.width, startQuitButtonRect.height);
    g.drawString("Quit", startQuitButtonRect.x + 48, startQuitButtonRect.y + 24);
    g.setFont(oldFont);
    g.setColor(old);
  }

  public void moveActorTo(Actor actor, Cell dest) {
    boolean srcIsWater = actor.loc.getTerrain() instanceof WaterTerrain;
    boolean destIsWater = dest.getTerrain() instanceof WaterTerrain;
    boolean srcIsSand = actor.loc.getTerrain() instanceof SandTerrain;
    boolean destIsSand = dest.getTerrain() instanceof SandTerrain;
    actor.setLocation(dest);
    Optional<Actor> decor = listOfPlayers.stream()
      .filter(o -> o != actor && o.loc == dest)
      .filter(o -> (o instanceof Tree) || (o instanceof Fish) || (o instanceof Cactus))
      .findFirst();
    if (decor.isPresent()) {
      Actor other = decor.get();
      listOfPlayers.remove(other);
      int delta;
      if (other instanceof Tree) {
        delta = 1;
      } else {
        delta = 2;
      }
      actor.addPoints(delta);
    }
    // if cat move in water -1 bubble or health
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
      if (srcIsSand && destIsSand) {
        if (c.getSuns() > 0) {
          c.removeOneSun();
        } else if (c.getHealth() > 0) {
          c.loseHeart();
        }
        c.setLastSunMs(System.currentTimeMillis());
      }
    }
  }
}
