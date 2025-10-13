import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class MenuPanel extends JPanel implements KeyListener {

    private SnakeGame parentFrame;
    private Image backgroundImage;
    private int topScore = 0;

    public MenuPanel(SnakeGame parentFrame) {
        this.parentFrame = parentFrame;
        this.setPreferredSize(new Dimension(600, 600));
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(this);

        // Load background image
        try {
            backgroundImage = new ImageIcon("background.jpg").getImage();
        } catch (Exception e) {
            System.out.println("Could not load background.jpg");
        }

        loadTopScore();
    }

    private void loadTopScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                topScore = Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            topScore = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, 600, 600, this);

        // Title
        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 80));
        FontMetrics titleMetrics = getFontMetrics(g.getFont());
        String title = "SNAKE GAME";
        g.drawString(title, (600 - titleMetrics.stringWidth(title)) / 2, 200);

        // Press SPACE to Start (centered)
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics startMetrics = getFontMetrics(g.getFont());
        String startText = "Press SPACE to Start";
        g.drawString(startText, (600 - startMetrics.stringWidth(startText)) / 2, 300);

        // Top score
        g.setFont(new Font("Ink Free", Font.BOLD, 25));
        FontMetrics scoreMetrics = getFontMetrics(g.getFont());
        String scoreText = "Top Score: " + topScore;
        g.drawString(scoreText, (600 - scoreMetrics.stringWidth(scoreText)) / 2, 400);

        // Exit text
        g.setFont(new Font("Ink Free", Font.PLAIN, 18));
        FontMetrics exitMetrics = getFontMetrics(g.getFont());
        String exitText = "Press ESC to Exit";
        g.setColor(Color.WHITE);
        g.drawString(exitText, (600 - exitMetrics.stringWidth(exitText)) / 2, 480);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SPACE) {
            parentFrame.startGame();
        } else if (code == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
