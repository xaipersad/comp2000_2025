import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Scoreboard {
    public static void paint(Graphics g, List<Actor> actors, int dogScore, int catScore, int birdScore) {
        int scoreY = 60;
        g.setColor(Color.BLACK);
        g.drawString("Scoreboard:", 740, scoreY);
        scoreY += 20;
        for (int i = 0; i < actors.size(); i++) {
            Actor a = actors.get(i);
            String name = "Actor";
            int score = 0;
            if (a.getClass().getSimpleName().equals("Dog")) { name = "Dog"; score = dogScore; }
            else if (a.getClass().getSimpleName().equals("Cat")) { name = "Cat"; score = catScore; }
            else if (a.getClass().getSimpleName().equals("Bird")) { name = "Bird"; score = birdScore; }
            g.drawString(name + " score: " + score, 740, scoreY);
            scoreY += 20;
        }
    }
}
