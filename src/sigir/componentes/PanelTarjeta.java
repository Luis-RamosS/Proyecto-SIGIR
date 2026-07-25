package sigir.componentes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelTarjeta extends JPanel {

    private final int radius;

    public PanelTarjeta(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int margin = 12;
        int width = getWidth() - margin * 2;
        int height = getHeight() - margin * 2 - 5;

        for (int i = 12; i >= 1; i--) {
            int alpha = Math.max(2, 18 - i);
            g2.setColor(new Color(39, 55, 78, alpha));
            g2.fillRoundRect(
                    margin - i / 2,
                    margin + 5 - i / 3,
                    width + i,
                    height + i,
                    radius + i,
                    radius + i
            );
        }

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(margin, margin, width, height, radius, radius);

        g2.setColor(new Color(220, 226, 234));
        g2.drawRoundRect(margin, margin, width, height, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
