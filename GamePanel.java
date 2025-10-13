import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    int delay = 150;
    final int MIN_DELAY = 40;
    Timer timer;
    Snake snake;
    int appleX;
    int appleY;
    int applesEaten;
    static int topScore = 0;
    char direction = 'R';
    boolean running = false;
    Random random;

    Image backgroundImage;
    final String HIGHSCORE_FILE = "highscore.txt";

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false); // ensures arrow keys are captured
        this.addKeyListener(new MyKeyAdapter());
        this.requestFocusInWindow();

        try {
            backgroundImage = new ImageIcon("background.jpeg").getImage();
        } catch (Exception e) {
            System.out.println("Could not load background.jpg");
        }

        loadTopScore();
        startGame();
    }

    public void startGame() {
        this.requestFocusInWindow();
        snake = new Snake(GAME_UNITS, UNIT_SIZE);
        newApple();
        running = true;
        applesEaten = 0;
        timer = new Timer(delay, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);

        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
            snake.draw(g);

            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 25));
            FontMetrics metrics = getFontMetrics(g.getFont());
            String currentScore = "Score: " + applesEaten;
            String recordScore = "Top Score: " + topScore;
            g.drawString(currentScore, 10, 25);
            g.drawString(recordScore, SCREEN_WIDTH - metrics.stringWidth(recordScore) - 10, 25);
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        snake.move(direction);
    }

    public void checkApple() {
        if ((snake.getHeadX() == appleX) && (snake.getHeadY() == appleY)) {
            snake.grow();
            applesEaten++;
            newApple();

            if (delay > MIN_DELAY) {
                delay -= 5;
                timer.setDelay(delay);
            }

            if (applesEaten > topScore) {
                topScore = applesEaten;
                saveTopScore();
            }
        }
    }

    public void checkCollisions() {
        if (snake.checkSelfCollision()) running = false;

        if (snake.getHeadX() < 0 || snake.getHeadX() >= SCREEN_WIDTH ||
            snake.getHeadY() < 0 || snake.getHeadY() >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) timer.stop();
    }

    public void gameOver(Graphics g) {
        if (applesEaten > topScore) {
            topScore = applesEaten;
            saveTopScore();
        }

        g.setColor(Color.red);

        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics fm = getFontMetrics(g.getFont());
        String scoreText = "Score: " + applesEaten;
        g.drawString(scoreText, (SCREEN_WIDTH - fm.stringWidth(scoreText)) / 2, g.getFont().getSize());

        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        fm = getFontMetrics(g.getFont());
        String overText = "Game Over";
        g.drawString(overText, (SCREEN_WIDTH - fm.stringWidth(overText)) / 2, SCREEN_HEIGHT / 2);

        g.setFont(new Font("Ink Free", Font.BOLD, 35));
        fm = getFontMetrics(g.getFont());
        String topText = "Top Score: " + topScore;
        g.drawString(topText, (SCREEN_WIDTH - fm.stringWidth(topText)) / 2, (SCREEN_HEIGHT / 2) + 60);

        g.setFont(new Font("Ink Free", Font.BOLD, 25));
        fm = getFontMetrics(g.getFont());
        String restartText = "Press R or SPACE to Restart | ESC to Quit";
        g.drawString(restartText, (SCREEN_WIDTH - fm.stringWidth(restartText)) / 2, (SCREEN_HEIGHT / 2) + 110);
    }

    private void saveTopScore() {
        try (FileWriter writer = new FileWriter(HIGHSCORE_FILE)) {
            writer.write(String.valueOf(topScore));
        } catch (IOException e) {
            System.out.println("Error saving high score.");
        }
    }

    private void loadTopScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                topScore = Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            topScore = 0;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
                case KeyEvent.VK_R:
                case KeyEvent.VK_SPACE:
                    if (!running) {
                        startGame();
                        requestFocusInWindow();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
        }
    }
}
