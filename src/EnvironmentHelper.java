public class EnvironmentHelper {
    public static boolean isLava(Cell cell) {
        return cell.getEnvironment() == 2;
    }
    public static boolean isWater(Cell cell) {
        return cell.getEnvironment() == 1;
    }
    public static boolean isGrass(Cell cell) {
        return cell.getEnvironment() == 0;
    }
}
