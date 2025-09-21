import java.util.*;
public class GameResetHelper {
    public static void resetGame(Stage stage) {
        stage.grid = new Grid();
        stage.actors = new ArrayList<Actor>();
        Random rand = new Random();
        int x1, y1, x2, y2, x3, y3;
        do { x1 = rand.nextInt(20); y1 = rand.nextInt(20); } while (stage.grid.cells[x1][y1].getEnvironment() == 2);
        do { x2 = rand.nextInt(20); y2 = rand.nextInt(20); } while ((x2 == x1 && y2 == y1) || stage.grid.cells[x2][y2].getEnvironment() == 2);
        do { x3 = rand.nextInt(20); y3 = rand.nextInt(20); } while (((x3 == x1 && y3 == y1) || (x3 == x2 && y3 == y2)) || stage.grid.cells[x3][y3].getEnvironment() == 2);
        stage.actors.add(new Cat(stage.grid.cellAtColRow(x1, y1).get()));
        stage.dog = new Dog(stage.grid.cellAtColRow(x2, y2).get());
        stage.actors.add(stage.dog);
        stage.actors.add(new Bird(stage.grid.cellAtColRow(x3, y3).get()));
        stage.showDogMoves = false;
        stage.dogMoveOptions.clear();
        stage.dogScore = 0;
        stage.catScore = 0;
        stage.birdScore = 0;
        stage.gameOver = false;
        stage.gameOverText = "";
    }
}
