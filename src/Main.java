import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JFrame {
    public static void main(String[] args) throws Exception {
      Main window = new Main();
      window.run();
    }

    class Canvas extends JPanel implements java.awt.event.MouseListener {
      Stage stage = new Stage();
      public Canvas() {
        setPreferredSize(new Dimension(1024, 720));
        addMouseListener(this);
      }

      @Override
      public void paint(Graphics g) {
        stage.paint(g, getMousePosition());
      }

      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        stage.handleClick(e.getPoint());
        repaint();
      }
      @Override public void mousePressed(java.awt.event.MouseEvent e) {}
      @Override public void mouseReleased(java.awt.event.MouseEvent e) {}
      @Override public void mouseEntered(java.awt.event.MouseEvent e) {}
      @Override public void mouseExited(java.awt.event.MouseEvent e) {}
    }

    private Main() {
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Canvas canvas = new Canvas();
      this.setContentPane(canvas);
      this.pack();
      this.setVisible(true);
    }

    public void run() {
      while(true) {
        repaint();
      }
    }
}
