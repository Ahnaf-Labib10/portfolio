import javax.swing.*;

public class SnakeGame extends JFrame {

    public SnakeGame() {
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        // Start with MenuPanel first
        MenuPanel menuPanel = new MenuPanel(this);
        this.add(menuPanel);

        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }

    // Switch from menu to game
    public void startGame() {
        this.getContentPane().removeAll();
        GamePanel gp = new GamePanel();
        this.add(gp);
        this.revalidate();
        this.repaint();
        gp.requestFocusInWindow(); // ensure keys work immediately
    }

    public static void main(String[] args) {
        new SnakeGame();
    }
}
