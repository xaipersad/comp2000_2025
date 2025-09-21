import java.util.Random;

public class ItemRespawn {
    public static void respawnCircle(Stage stage) {
        Random rand = new Random();
        int newX, newY;
        boolean conflict;
        do {
            newX = rand.nextInt(20);
            newY = rand.nextInt(20);
            conflict = false;
            if (stage.grid.cells[newX][newY].getEnvironment() == 2) conflict = true;
            for (int i = 0; i < stage.actors.size(); i++) {
                Actor a = stage.actors.get(i);
                if (a.loc.x / Cell.size == newX && a.loc.y / Cell.size == newY) conflict = true;
            }
            if ((newX == stage.grid.squareItem.getX() && newY == stage.grid.squareItem.getY()) || (newX == stage.grid.triangleItem.getX() && newY == stage.grid.triangleItem.getY())) conflict = true;
        } while (conflict);
        stage.grid.circleItem.x = newX;
        stage.grid.circleItem.y = newY;
    }

    public static void respawnSquare(Stage stage) {
        Random rand = new Random();
        int newX, newY;
        boolean conflict;
        do {
            newX = rand.nextInt(20);
            newY = rand.nextInt(20);
            conflict = false;
            if (stage.grid.cells[newX][newY].getEnvironment() == 2) conflict = true;
            for (int i = 0; i < stage.actors.size(); i++) {
                Actor a = stage.actors.get(i);
                if (a.loc.x / Cell.size == newX && a.loc.y / Cell.size == newY) conflict = true;
            }
            if ((newX == stage.grid.circleItem.getX() && newY == stage.grid.circleItem.getY()) || (newX == stage.grid.triangleItem.getX() && newY == stage.grid.triangleItem.getY())) conflict = true;
        } while (conflict);
        stage.grid.squareItem.x = newX;
        stage.grid.squareItem.y = newY;
    }

    public static void respawnTriangle(Stage stage) {
        Random rand = new Random();
        int newX, newY;
        boolean conflict;
        do {
            newX = rand.nextInt(20);
            newY = rand.nextInt(20);
            conflict = false;
            if (stage.grid.cells[newX][newY].getEnvironment() == 2) conflict = true;
            for (int i = 0; i < stage.actors.size(); i++) {
                Actor a = stage.actors.get(i);
                if (a.loc.x / Cell.size == newX && a.loc.y / Cell.size == newY) conflict = true;
            }
            if ((newX == stage.grid.squareItem.getX() && newY == stage.grid.squareItem.getY()) || (newX == stage.grid.circleItem.getX() && newY == stage.grid.circleItem.getY())) conflict = true;
        } while (conflict);
        stage.grid.triangleItem.x = newX;
        stage.grid.triangleItem.y = newY;
    }
}
