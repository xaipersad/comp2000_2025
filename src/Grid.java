import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Grid {
  Cell[][] cells = new Cell[20][20];
  
  public Grid() {
    for(int i=0; i<cells.length; i++) {
      for(int j=0; j<cells[i].length; j++) {
        cells[i][j] = new Cell(colToLabel(i), j, 10+Cell.size*i, 10+Cell.size*j);
        // assign terrain types: left side water, then sand, then grass
        if (i < 4) {
          cells[i][j].setTerrain(new WaterTerrain());
        } else if (i < 8) {
          cells[i][j].setTerrain(new SandTerrain());
        } else {
          // default GrassTerrain already set in Cell constructor
        }
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
        cellAtColRow(colToLabel(i), j - 1).ifPresent(inRadius::add);
        cellAtColRow(colToLabel(i), j + 1).ifPresent(inRadius::add);
        cellAtColRow(colToLabel(i - 1), j).ifPresent(inRadius::add);
        cellAtColRow(colToLabel(i + 1), j).ifPresent(inRadius::add);
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
}
