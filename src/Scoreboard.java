import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Scoreboard {
    public static void paint(Graphics g, List<Actor> actors, int dogScore, int catScore, int birdScore) {
        int scoreY = 60;
        g.setColor(Color.BLACK);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        g.drawString("Scoreboard:", 740, scoreY);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
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
        // Draw objective text under the last score
        g.setColor(Color.BLACK);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        g.drawString("Movement:", 740, scoreY+20);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g.drawString("Grass (green) is 2 squares", 740, scoreY+40);
        g.drawString("Water (blue) is 1 square", 740, scoreY+60);
        g.drawString("Do not touch lava -1 score and respawn", 740, scoreY+80);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        g.drawString("Objective:", 740, scoreY+120);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
          g.drawString("Collect 5 circles", 740, scoreY+140);
    }
}
