import java.awt.Color;
import java.awt.Graphics;

public class Snake {

    public int[] x;
    public int[] y;
    public int bodyParts = 6;
    public int unitSize;

    // Direction for eye + animation logic
    private char lastDirection = 'R';

    // Animation tick (visual only)
    private int animTick = 0;

    public Snake(int maxUnits, int unitSize) {
        this.unitSize = unitSize;

        x = new int[maxUnits];
        y = new int[maxUnits];

        // Initial snake position
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - (i * unitSize);
            y[i] = 100;
        }
    }

    // Called from GamePanel
    public void move(char direction) {
        lastDirection = direction;

        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= unitSize;
            case 'D' -> y[0] += unitSize;
            case 'L' -> x[0] -= unitSize;
            case 'R' -> x[0] += unitSize;
        }
    }

    public void grow() {
        bodyParts++;
    }

    public int getHeadX() {
        return x[0];
    }

    public int getHeadY() {
        return y[0];
    }

    public boolean checkSelfCollision() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                return true;
            }
        }
        return false;
    }

    // ================= DRAW (ANIMATED) =================
    public void draw(Graphics g) {
        animTick++;

        for (int i = 0; i < bodyParts; i++) {

            // 🐍 Slither wave
            double wave = Math.sin(animTick * 0.25 - i * 0.6) * 2;

            int drawX = x[i];
            int drawY = y[i];

            if (lastDirection == 'U' || lastDirection == 'D') {
                drawX += (int) wave;
            } else {
                drawY += (int) wave;
            }

            if (i == 0) {
                // ===== HEAD =====
                double pulse = Math.sin(animTick * 0.3) * 3;
                int headSize = (int) (unitSize + pulse);

                g.setColor(new Color(92, 64, 51)); // Dark brown
                g.fillRect(
                    drawX - (headSize - unitSize) / 2,
                    drawY - (headSize - unitSize) / 2,
                    headSize,
                    headSize
                );

                // Eyes
                g.setColor(Color.BLACK);
                int eyeSize = unitSize / 5;

                int eyeX1 = drawX;
                int eyeY1 = drawY;
                int eyeX2 = drawX;
                int eyeY2 = drawY;

                switch (lastDirection) {
                    case 'U' -> {
                        eyeX1 += unitSize / 4;
                        eyeX2 += unitSize / 2;
                        eyeY1 += unitSize / 4;
                        eyeY2 += unitSize / 4;
                    }
                    case 'D' -> {
                        eyeX1 += unitSize / 4;
                        eyeX2 += unitSize / 2;
                        eyeY1 += unitSize / 2;
                        eyeY2 += unitSize / 2;
                    }
                    case 'L' -> {
                        eyeX1 += unitSize / 4;
                        eyeX2 += unitSize / 4;
                        eyeY1 += unitSize / 4;
                        eyeY2 += unitSize / 2;
                    }
                    case 'R' -> {
                        eyeX1 += unitSize / 2;
                        eyeX2 += unitSize / 2;
                        eyeY1 += unitSize / 4;
                        eyeY2 += unitSize / 2;
                    }
                }

                g.fillOval(eyeX1, eyeY1, eyeSize, eyeSize);
                g.fillOval(eyeX2, eyeY2, eyeSize, eyeSize);

            } else {
                // ===== BODY =====
                g.setColor(new Color(139, 69, 19)); // Brown
                g.fillRect(drawX, drawY, unitSize, unitSize);

                // Outline / shading
                g.setColor(new Color(101, 67, 33));
                g.drawRect(drawX, drawY, unitSize, unitSize);
            }
        }
    }
}
