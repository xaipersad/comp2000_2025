import java.util.Optional;

public class ActorMovement {
    public static void moveActors(Stage stage) {
        for (int i = 0; i < stage.actors.size(); i++) {
            Actor a = stage.actors.get(i);
            if (a != stage.dog) {
                if (a.getClass().getSimpleName().equals("Cat")) {
                    int targetX = stage.grid.squareItem.getX();
                    int targetY = stage.grid.squareItem.getY();
                    int ax = a.loc.x / Cell.size;
                    int ay = a.loc.y / Cell.size;
                    int catSteps = 3;
                    if (a.loc.getEnvironment() == 1) catSteps = 2;
                    if (a.loc.getEnvironment() == 2) catSteps = 1;
                    int bestDist = 1000;
                    Cell bestCell = a.loc;
                    for (int dx = -catSteps; dx <= catSteps; dx++) {
                        for (int dy = -catSteps; dy <= catSteps; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int newX = ax + dx;
                            int newY = ay + dy;
                            if (newX >= 0 && newX < 20 && newY >= 0 && newY < 20) {
                                Optional<Cell> nextCell = stage.grid.cellAtColRow(newX, newY);
                                if (nextCell.isPresent()) {
                                    int dist = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                                    if (dist < bestDist) {
                                        bestDist = dist;
                                        bestCell = nextCell.get();
                                    }
                                }
                            }
                        }
                    }
                    if (bestCell != a.loc) {
                        ((Cat)a).loc = bestCell;
                        ((Cat)a).display = new java.util.ArrayList<>();
                        Cell l = ((Cat)a).loc;
                        java.awt.Polygon ear1 = new java.awt.Polygon();
                        ear1.addPoint(l.x + 11, l.y + 5);
                        ear1.addPoint(l.x + 15, l.y + 15);
                        ear1.addPoint(l.x + 7, l.y + 15);
                        java.awt.Polygon ear2 = new java.awt.Polygon();
                        ear2.addPoint(l.x + 22, l.y + 5);
                        ear2.addPoint(l.x + 26, l.y + 15);
                        ear2.addPoint(l.x + 18, l.y + 15);
                        java.awt.Polygon face = new java.awt.Polygon();
                        face.addPoint(l.x + 5, l.y + 15);
                        face.addPoint(l.x + 29, l.y + 15);
                        face.addPoint(l.x + 17, l.y + 30);
                        ((Cat)a).display.add(face);
                        ((Cat)a).display.add(ear1);
                        ((Cat)a).display.add(ear2);
                    }
                } else if (a.getClass().getSimpleName().equals("Bird")) {
                    int targetX = stage.grid.triangleItem.getX();
                    int targetY = stage.grid.triangleItem.getY();
                    int ax = a.loc.x / Cell.size;
                    int ay = a.loc.y / Cell.size;
                    int birdSteps = 3;
                    if (a.loc.getEnvironment() == 1) birdSteps = 2;
                    if (a.loc.getEnvironment() == 2) birdSteps = 1;
                    int bestDist = 1000;
                    Cell bestCell = a.loc;
                    for (int dx = -birdSteps; dx <= birdSteps; dx++) {
                        for (int dy = -birdSteps; dy <= birdSteps; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int newX = ax + dx;
                            int newY = ay + dy;
                            if (newX >= 0 && newX < 20 && newY >= 0 && newY < 20) {
                                Optional<Cell> nextCell = stage.grid.cellAtColRow(newX, newY);
                                if (nextCell.isPresent()) {
                                    int dist = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                                    if (dist < bestDist) {
                                        bestDist = dist;
                                        bestCell = nextCell.get();
                                    }
                                }
                            }
                        }
                    }
                    if (bestCell != a.loc) {
                        ((Bird)a).loc = bestCell;
                        ((Bird)a).display = new java.util.ArrayList<>();
                        Cell l = ((Bird)a).loc;
                        java.awt.Polygon wing1 = new java.awt.Polygon();
                        wing1.addPoint(l.x + 5, l.y + 5);
                        wing1.addPoint(l.x + 15, l.y + 17);
                        wing1.addPoint(l.x + 5, l.y + 17);
                        java.awt.Polygon wing2 = new java.awt.Polygon();
                        wing2.addPoint(l.x + 30, l.y + 5);
                        wing2.addPoint(l.x + 20, l.y + 17);
                        wing2.addPoint(l.x + 30, l.y + 17);
                        java.awt.Polygon body = new java.awt.Polygon();
                        body.addPoint(l.x + 15, l.y + 10);
                        body.addPoint(l.x + 20, l.y + 10);
                        body.addPoint(l.x + 20, l.y + 25);
                        body.addPoint(l.x + 15, l.y + 25);
                        ((Bird)a).display.add(body);
                        ((Bird)a).display.add(wing1);
                        ((Bird)a).display.add(wing2);
                    }
                }
            }
        }
    }
}
