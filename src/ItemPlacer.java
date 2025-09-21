import java.util.Random;

public class ItemPlacer {
    public static void placeItems(Grid grid) {
        Random rand = new Random();
        int x1, y1, x2, y2, x3, y3;
        do {
            x1 = rand.nextInt(20);
            y1 = rand.nextInt(20);
        } while (grid.cells[x1][y1].getEnvironment() == 2);
        do {
            x2 = rand.nextInt(20);
            y2 = rand.nextInt(20);
        } while ((x2 == x1 && y2 == y1) || grid.cells[x2][y2].getEnvironment() == 2);
        do {
            x3 = rand.nextInt(20);
            y3 = rand.nextInt(20);
        } while (((x3 == x1 && y3 == y1) || (x3 == x2 && y3 == y2)) || grid.cells[x3][y3].getEnvironment() == 2);
        grid.squareItem = new Item(0, x1, y1);
        grid.circleItem = new Item(1, x2, y2);
        grid.triangleItem = new Item(2, x3, y3);
    }
}
