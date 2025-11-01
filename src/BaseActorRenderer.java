import java.awt.Graphics;

public class BaseActorRenderer implements ActorRenderer {
  @Override
  public void render(Graphics g, Actor a) {
    a.paint(g);
  }
}
