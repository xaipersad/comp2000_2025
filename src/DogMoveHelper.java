import java.util.Optional;

public class DogMoveHelper {
    public static void showDogMoves(Stage stage, Cell cell) {
        stage.showDogMoves = true;
        stage.dogMoveOptions.clear();
        int nx = cell.x / Cell.size;
        int ny = cell.y / Cell.size;
        int dogSteps = 3;
        if (cell.getEnvironment() == 1) dogSteps = 2;
        if (cell.getEnvironment() == 2) dogSteps = 1;
        for (int dx = -dogSteps; dx <= dogSteps; dx++) {
            for (int dy = -dogSteps; dy <= dogSteps; dy++) {
                if (dx == 0 && dy == 0) continue;
                int tx = nx + dx;
                int ty = ny + dy;
                if (tx >= 0 && tx < 20 && ty >= 0 && ty < 20) {
                    Optional<Cell> opt = stage.grid.cellAtColRow(tx, ty);
                    if (opt.isPresent()) stage.dogMoveOptions.add(opt.get());
                }
            }
        }
    }
}
