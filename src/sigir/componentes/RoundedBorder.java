package sigir.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;
import javax.swing.border.Border;

public class RoundedBorder implements Border {

    private final Color color;
    private final int radius;
    private final float thickness;

    public RoundedBorder(Color color, int radius, float thickness) {
        this.color = color;
        this.radius = radius;
        this.thickness = thickness;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int value = Math.max(2, Math.round(thickness));
        return new Insets(value, value, value, value);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(
            Component c, Graphics g, int x, int y, int width, int height) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        int adjustment = Math.max(1, Math.round(thickness));

        g2.drawRoundRect(
                x + adjustment,
                y + adjustment,
                width - adjustment * 2 - 1,
                height - adjustment * 2 - 1,
                radius,
                radius
        );

        g2.dispose();
    }
}
