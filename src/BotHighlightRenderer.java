import java.awt.Color;
import java.awt.Graphics;

public class BotHighlightRenderer implements ActorRenderer {
  private final ActorRenderer inner;

  public BotHighlightRenderer(ActorRenderer inner) {
    this.inner = inner;
  }

  @Override
  public void render(Graphics g, Actor a) {
    // First render the base actor
    inner.render(g, a);
    // Then apply a subtle highlight for bots (Decorator pattern)
    if (a.isBot()) {
      Color old = g.getColor();
      g.setColor(new Color(255, 215, 0, 80)); // gold with transparency
      // draw a slightly larger rectangle around the cell
      g.fillRect(a.loc.x + 1, a.loc.y + 1, Cell.size - 2, Cell.size - 2);
      g.setColor(old);
    }
  }
}
