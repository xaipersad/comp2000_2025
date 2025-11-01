import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Grid implements Iterable<Cell> {
  Cell[][] cells = new Cell[20][20];
  
  public Grid() {
    for(int i=0; i<cells.length; i++) {
      for(int j=0; j<cells[i].length; j++) {
        cells[i][j] = new Cell(colToLabel(i), j, 10+Cell.size*i, 10+Cell.size*j);
        // make entire grid grass by default (Cell constructor sets GrassTerrain)
      }
    }
  }

  private char colToLabel(int col) {
    return (char) (col + Character.valueOf('A'));
  }

  private int labelToCol(char col) {
    return (int) (col - Character.valueOf('A'));
  }

  public void paint(Graphics g, Point mousePos) {
    for(int i=0; i<cells.length; i++) {
      for(int j=0; j<cells[i].length; j++) {
        cells[i][j].paint(g, mousePos);
      }
    }
  }

  /**
   * Called each frame to allow terrains to update over time (evaporation,
   * wetness accumulation, gradual transitions).
   */
  public void tick() {
    for(int i=0; i<cells.length; i++) {
      for(int j=0; j<cells[i].length; j++) {
        // decay transient weather values first (rain/wind), then let terrain update
        cells[i][j].tickDecay();
        TerrainType t = cells[i][j].getTerrain();
        if (t != null) {
          t.tick(cells[i][j]);
        }

        // Apply global terrain transitions based on temperature and rainfall.
        // Rules:
        // - Flood: when temp > 26 and rain > 0.36 -> becomes water (from any terrain)
  // - When temp > 27.5 and rain == 0 -> becomes sand (from any terrain except water handled below)
        // - When grass and temp < 25 and rain > 0.40 -> becomes water
        // - When sand and rain > 0.40 -> becomes grass
        double temp = cells[i][j].getTemperature();
        double rain = cells[i][j].getRainfall();
        boolean noRain = rain <= 0.001; // treat near-zero as zero
        TerrainType cur = cells[i][j].getTerrain();

        // Flood rule first so it takes priority when both conditions are met
        if (temp > 26.5 && rain > 0.41) {
          if (!(cur instanceof WaterTerrain)) {
            cells[i][j].setTerrain(new WaterTerrain());
          }
        // Water turns back into grass when rain reaches zero (don't do instant temp-based flip)
        } else if (cur instanceof WaterTerrain && noRain) {
          cells[i][j].setTerrain(new GrassTerrain());
        } else if (temp > 27.5 && noRain) {
          if (!(cur instanceof SandTerrain)) {
            cells[i][j].setTerrain(new SandTerrain());
          }
        } else if (cur instanceof GrassTerrain && temp < 25.0 && rain > 0.40) {
          cells[i][j].setTerrain(new WaterTerrain());
        } else if (cur instanceof SandTerrain && rain > 0.40) {
          cells[i][j].setTerrain(new GrassTerrain());
        }
      }
    }
  }

  public Optional<Cell> cellAtColRow(int c, int r) {
    if(c >= 0 && c < cells.length && r >=0 && r < cells[c].length) {
      return Optional.of(cells[c][r]);
    } else {
      return Optional.empty();
    }
  }

  public Optional<Cell> cellAtColRow(char c, int r) {
    return cellAtColRow(labelToCol(c), r);
  }

  public Optional<Cell> cellAtPoint(Point p) {
    for(int i=0; i < cells.length; i++) {
      for(int j=0; j < cells[i].length; j++) {
        if(cells[i][j].contains(p)) {
          return Optional.of(cells[i][j]);
        }
      }
    }
    return Optional.empty();
  }

  public List<Cell> getRadius(Cell from, int size) {
    int i = labelToCol(from.col);
    int j = from.row;
    Set<Cell> inRadius = new HashSet<Cell>();
  if (size > 0) {
    java.util.Optional<Cell> opt1 = cellAtColRow(colToLabel(i), j - 1);
    if (opt1.isPresent()) { inRadius.add(opt1.get()); }
    java.util.Optional<Cell> opt2 = cellAtColRow(colToLabel(i), j + 1);
    if (opt2.isPresent()) { inRadius.add(opt2.get()); }
    java.util.Optional<Cell> opt3 = cellAtColRow(colToLabel(i - 1), j);
    if (opt3.isPresent()) { inRadius.add(opt3.get()); }
    java.util.Optional<Cell> opt4 = cellAtColRow(colToLabel(i + 1), j);
    if (opt4.isPresent()) { inRadius.add(opt4.get()); }
  }

    for(Cell c: inRadius.toArray(new Cell[0])) {
        inRadius.addAll(getRadius(c, size - 1));
    }
    return new ArrayList<Cell>(inRadius);
  }

  /**
   * Convert server coordinates to a Cell on the grid.
   * The server uses J9 as the origin (0,0). So server x/y are offsets
   * from that origin. This method maps those server coordinates to
   * grid column/row indices and returns the corresponding Cell if it
   * exists.
   */
  public Optional<Cell> cellAtServerCoords(int sx, int sy) {
    int originCol = labelToCol('J');
    int originRow = 9;
    int c = originCol + sx;
    int r = originRow + sy;
    return cellAtColRow(c, r);
  }

  /**
   * Convert server X (with origin J9) to grid column index.
   */
  public int serverToGridCol(int sx) {
    return labelToCol('J') + sx;
  }

  /**
   * Convert server Y (with origin J9) to grid row index.
   */
  public int serverToGridRow(int sy) {
    return 9 + sy;
  }

  public void paintOverlay(Graphics g, List<Cell> cells, Color color) {
    g.setColor(color);
    for(Cell c: cells) {
      g.fillRect(c.x+2, c.y+2, c.width-4, c.height-4);
    }
  }

  // Iterator pattern: iterate all cells in row-major order
  @Override
  public java.util.Iterator<Cell> iterator() {
    return new java.util.Iterator<Cell>() {
      private int i = 0;
      private int j = 0;
      @Override
      public boolean hasNext() {
        return i < cells.length && j < cells[i].length;
      }
      @Override
      public Cell next() {
        Cell c = cells[i][j];
        j++;
        if (j >= cells[i].length) { j = 0; i++; }
        return c;
      }
    };
  }

  // Iterator pattern: iterate a square region [minX..maxX] x [minY..maxY]
  public Iterable<Cell> squareIterable(final int minX, final int minY, final int maxX, final int maxY) {
    return new Iterable<Cell>() {
      @Override
      public java.util.Iterator<Cell> iterator() {
        return new java.util.Iterator<Cell>() {
          private int i = Math.max(0, minX);
          private int j = Math.max(0, minY);
          private final int iMax = Math.min(cells.length - 1, maxX);
          private final int jMax = Math.min(cells[0].length - 1, maxY);
          @Override
          public boolean hasNext() {
            return i <= iMax && j <= jMax;
          }
          @Override
          public Cell next() {
            Cell c = cells[i][j];
            if (j < jMax) {
              j++;
            } else {
              j = Math.max(0, minY);
              i++;
            }
            return c;
          }
        };
      }
    };
  }
}
