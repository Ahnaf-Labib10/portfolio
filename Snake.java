import java.awt.*;

public class Snake {
    private final int[] x;
    private final int[] y;
    private int bodyParts = 6;
    private final int unitSize;

    public Snake(int maxUnits, int unitSize) {
        this.unitSize = unitSize;
        x = new int[maxUnits];
        y = new int[maxUnits];
    }

    public void move(char direction) {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= unitSize; break;
            case 'D': y[0] += unitSize; break;
            case 'L': x[0] -= unitSize; break;
            case 'R': x[0] += unitSize; break;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                // Snake Head — bright green with eyes
                g2d.setColor(new Color(0, 200, 0));
                g2d.fillRoundRect(x[i], y[i], unitSize, unitSize, 10, 10);

                // Eyes
                g2d.setColor(Color.black);
                int eyeSize = unitSize / 6;
                if (i + 1 < bodyParts) {
                    g2d.fillOval(x[i] + eyeSize, y[i] + eyeSize, eyeSize, eyeSize);
                    g2d.fillOval(x[i] + unitSize - 2 * eyeSize, y[i] + eyeSize, eyeSize, eyeSize);
                }
            } else {
                // Snake Body — gradient shading for realism
                float ratio = (float) i / bodyParts;
                Color bodyColor = new Color(0, (int) (180 - 80 * ratio), 0);
                g2d.setColor(bodyColor);
                g2d.fillRoundRect(x[i], y[i], unitSize, unitSize, 10, 10);

                // subtle scale pattern
                g2d.setColor(new Color(0, 100, 0, 80));
                g2d.drawOval(x[i] + 3, y[i] + 3, unitSize - 6, unitSize - 6);
            }
        }
    }

    public void grow() {
        bodyParts++;
    }

    public boolean checkSelfCollision() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) return true;
        }
        return false;
    }

    public int getHeadX() { return x[0]; }
    public int getHeadY() { return y[0]; }
}
