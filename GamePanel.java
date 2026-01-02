import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.swing.*;

public final class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS =
            (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // Speed
    final int DEFAULT_DELAY = 150;
    final int MIN_DELAY = 25;
    int delay = DEFAULT_DELAY;

    // Arena shrinking
    int wallPadding = 0;
    final int MAX_SHRINK = SCREEN_WIDTH / 4;

    // Animation
    int animTick = 0;
    int shakeTimer = 0;

    Timer timer;
    Snake snake;

    int appleX, appleY;
    int applesEaten;

    static int topScore = 0;
    char direction = 'R';
    boolean running = false;

    Random random = new Random();

    Image backgroundImage;
    final String HIGHSCORE_FILE = "highscore.txt";

    // Bombs
    static final int MAX_BOMBS = 10;
    static final int BOMB_EXPLODE_TIME = 300;
    int[] bombX = new int[MAX_BOMBS];
    int[] bombY = new int[MAX_BOMBS];
    int[] bombTimer = new int[MAX_BOMBS];
    int bombCount = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(new MyKeyAdapter());

        try {
            backgroundImage = new ImageIcon("background.jpeg").getImage();
        } catch (Exception e) {}

        loadTopScore();
        startGame();
    }

    // ================= GRID-SAFE SPAWN HELPERS =================
    private int randomGridX() {
        int minCol = wallPadding / UNIT_SIZE;
        int maxCol = (SCREEN_WIDTH - wallPadding) / UNIT_SIZE - 1;
        return (random.nextInt(maxCol - minCol + 1) + minCol) * UNIT_SIZE;
    }

    private int randomGridY() {
        int minRow = wallPadding / UNIT_SIZE;
        int maxRow = (SCREEN_HEIGHT - wallPadding) / UNIT_SIZE - 1;
        return (random.nextInt(maxRow - minRow + 1) + minRow) * UNIT_SIZE;
    }

    // Check if a grid position is inside the current arena (not inside walls)
    private boolean isInsideArena(int x, int y) {
        return x >= wallPadding &&
               x < SCREEN_WIDTH - wallPadding &&
               y >= wallPadding &&
               y < SCREEN_HEIGHT - wallPadding;
    }

    // After shrinking the arena, make sure apple and bombs are still inside
    private void ensureItemsInsideArena() {
        // Reposition apple if it ended up inside the new wall area
        if (!isInsideArena(appleX, appleY)) {
            newApple();
        }

        // Reposition any bombs that are now outside the arena
        for (int i = 0; i < bombCount; i++) {
            if (!isInsideArena(bombX[i], bombY[i])) {
                boolean valid;
                int x, y;
                do {
                    valid = true;
                    x = randomGridX();
                    y = randomGridY();

                    // Don't spawn on apple
                    if (x == appleX && y == appleY) valid = false;

                    // Don't spawn on snake
                    for (int s = 0; s < snake.bodyParts; s++) {
                        if (snake.x[s] == x && snake.y[s] == y) {
                            valid = false;
                            break;
                        }
                    }

                    // Don't spawn on other bombs
                    for (int b = 0; b < bombCount; b++) {
                        if (b == i) continue;
                        if (bombX[b] == x && bombY[b] == y) {
                            valid = false;
                            break;
                        }
                    }
                } while (!valid);

                bombX[i] = x;
                bombY[i] = y;
                // Keep timer so their explode timing feels continuous
            }
        }
    }

    // ================= START / RESET =================
    public void startGame() {
        requestFocusInWindow();

        delay = DEFAULT_DELAY;
        applesEaten = 0;
        direction = 'R';
        running = true;

        wallPadding = 0;
        bombCount = 0;
        animTick = 0;
        shakeTimer = 0;

        snake = new Snake(GAME_UNITS, UNIT_SIZE);
        newApple();

        if (timer != null) timer.stop();
        timer = new Timer(delay, this);
        timer.start();
    }

    // ================= DRAW =================
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int shakeX = 0, shakeY = 0;
        if (shakeTimer > 0) {
            shakeX = random.nextInt(8) - 4;
            shakeY = random.nextInt(8) - 4;
            shakeTimer--;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.translate(shakeX, shakeY);
        draw(g2);
        g2.translate(-shakeX, -shakeY);
    }

    public void draw(Graphics g) {
        animTick++;

        g.drawImage(backgroundImage, 0, 0,
                SCREEN_WIDTH, SCREEN_HEIGHT, this);

        // Arena walls
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, SCREEN_WIDTH, wallPadding);
        g.fillRect(0, SCREEN_HEIGHT - wallPadding, SCREEN_WIDTH, wallPadding);
        g.fillRect(0, 0, wallPadding, SCREEN_HEIGHT);
        g.fillRect(SCREEN_WIDTH - wallPadding, 0, wallPadding, SCREEN_HEIGHT);

        if (running) {

            // 🍎 Apple animation
            double pulse = Math.sin(animTick * 0.2) * 4;
            int size = (int) (UNIT_SIZE + pulse);

            g.setColor(Color.red);
            g.fillOval(
                appleX - (size - UNIT_SIZE) / 2,
                appleY - (size - UNIT_SIZE) / 2,
                size, size
            );

            // 💣 Bombs
            for (int i = 0; i < bombCount; i++) {
                bombTimer[i]++;

                boolean exploding = bombTimer[i] >= BOMB_EXPLODE_TIME;
                int blinkSpeed = exploding ? 3 : Math.max(10, 40 - bombTimer[i] / 6);
                boolean blink = (animTick / blinkSpeed) % 2 == 0;

                g.setColor(blink ? Color.darkGray : Color.black);
                g.fillOval(bombX[i], bombY[i], UNIT_SIZE, UNIT_SIZE);

                g.setColor(Color.red);
                g.fillOval(
                    bombX[i] + UNIT_SIZE / 3,
                    bombY[i] + UNIT_SIZE / 4,
                    6, 6
                );

                // Explosion flash (visual only)
                if (exploding) {
                    g.setColor(new Color(255, 120, 0, 120));
                    g.fillOval(
                        bombX[i] - 10,
                        bombY[i] - 10,
                        UNIT_SIZE + 20,
                        UNIT_SIZE + 20
                    );
                    bombTimer[i] = 0;
                }
            }

            snake.draw(g);

            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 25));
            g.drawString("Score: " + applesEaten, 10, 25);
            g.drawString("Top Score: " + topScore,
                    SCREEN_WIDTH - 180, 25);

        } else {
            gameOver(g);
        }
    }

    // ================= SPAWNING =================
    public void newApple() {
        appleX = randomGridX();
        appleY = randomGridY();
    }

    public void spawnBomb() {
        int x, y;
        boolean valid;

        do {
            valid = true;
            x = randomGridX();
            y = randomGridY();

            if (x == appleX && y == appleY) valid = false;

            for (int i = 0; i < snake.bodyParts; i++) {
                if (snake.x[i] == x && snake.y[i] == y) valid = false;
            }

            for (int i = 0; i < bombCount; i++) {
                if (bombX[i] == x && bombY[i] == y) valid = false;
            }

        } while (!valid);

        bombX[bombCount] = x;
        bombY[bombCount] = y;
        bombTimer[bombCount] = 0;
        bombCount++;
    }

    // ================= LOGIC =================
    public void move() {
        snake.move(direction);
    }

    public void checkApple() {
        if (snake.getHeadX() == appleX &&
            snake.getHeadY() == appleY) {

            snake.grow();
            applesEaten++;
            newApple();

            if (delay > MIN_DELAY) {
                delay -= 7;
                timer.setDelay(delay);
            }

            // Shrink every 10 apples
            if (applesEaten % 10 == 0 &&
                wallPadding < MAX_SHRINK) {
                wallPadding += UNIT_SIZE;
                // Make sure apple and bombs stay inside the new arena
                ensureItemsInsideArena();
            }

            if (applesEaten >= 5 &&
                applesEaten % 5 == 0 &&
                bombCount < MAX_BOMBS) {
                spawnBomb();
            }

            if (applesEaten > topScore) {
                topScore = applesEaten;
                saveTopScore();
            }
        }
    }

    public void checkCollisions() {
        if (snake.checkSelfCollision()) running = false;

        if (snake.getHeadX() < wallPadding ||
            snake.getHeadX() >= SCREEN_WIDTH - wallPadding ||
            snake.getHeadY() < wallPadding ||
            snake.getHeadY() >= SCREEN_HEIGHT - wallPadding) {
            running = false;
        }

        // Only die if snake eats bomb
        for (int i = 0; i < bombCount; i++) {
            if (snake.getHeadX() == bombX[i] &&
                snake.getHeadY() == bombY[i]) {
                running = false;
                shakeTimer = 20;
            }
        }

        if (!running) timer.stop();
    }

    // ================= GAME OVER =================
    public void gameOver(Graphics g) {

    // GAME OVER text
    g.setColor(Color.red);
    g.setFont(new Font("Ink Free", Font.BOLD, 75));
    FontMetrics fm = getFontMetrics(g.getFont());

    String gameOverText = "Game Over";
    int x = (SCREEN_WIDTH - fm.stringWidth(gameOverText)) / 2;
    int y = SCREEN_HEIGHT / 2;

    g.drawString(gameOverText, x, y);

    // SCORE text
    g.setFont(new Font("Ink Free", Font.BOLD, 30));
    fm = getFontMetrics(g.getFont());

    String scoreText = "Score: " + applesEaten;
    x = (SCREEN_WIDTH - fm.stringWidth(scoreText)) / 2;
    y += 50;

    g.drawString(scoreText, x, y);

    // RESTART text
    g.setFont(new Font("Ink Free", Font.BOLD, 25));
    fm = getFontMetrics(g.getFont());

    String restartText = "Press R or SPACE to Restart";
    x = (SCREEN_WIDTH - fm.stringWidth(restartText)) / 2;
    y += 50;

    g.drawString(restartText, x, y);
}


    // ================= TIMER =================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    // ================= INPUT =================
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') direction = 'L';
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') direction = 'R';
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') direction = 'U';
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') direction = 'D';
                }
                case KeyEvent.VK_R, KeyEvent.VK_SPACE -> {
                    if (!running) startGame();
                }
                case KeyEvent.VK_ESCAPE -> System.exit(0);
            }
        }
    }

    // ================= SCORE =================
    private void saveTopScore() {
        try (FileWriter w = new FileWriter(HIGHSCORE_FILE)) {
            w.write(String.valueOf(topScore));
        } catch (IOException e) {}
    }

    private void loadTopScore() {
        try (BufferedReader r =
                     new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            topScore = Integer.parseInt(r.readLine());
        } catch (Exception e) {
            topScore = 0;
        }
    }
}
