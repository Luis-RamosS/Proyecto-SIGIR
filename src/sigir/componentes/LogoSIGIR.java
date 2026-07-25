package sigir.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

public class LogoSIGIR extends JComponent {

    public LogoSIGIR() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 12;
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        GradientPaint gradient = new GradientPaint(
                x, y,
                new Color(166, 180, 197),
                x + size, y + size,
                new Color(82, 112, 151)
        );

        g2.setPaint(gradient);
        g2.setStroke(new BasicStroke(
                Math.max(6f, size * 0.085f),
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER
        ));

        Path2D frame = new Path2D.Double();
        frame.moveTo(x + size * 0.50, y + size * 0.07);
        frame.lineTo(x + size * 0.84, y + size * 0.27);
        frame.lineTo(x + size * 0.84, y + size * 0.67);
        frame.lineTo(x + size * 0.67, y + size * 0.77);

        frame.moveTo(x + size * 0.33, y + size * 0.77);
        frame.lineTo(x + size * 0.16, y + size * 0.67);
        frame.lineTo(x + size * 0.16, y + size * 0.27);
        frame.lineTo(x + size * 0.50, y + size * 0.07);

        g2.draw(frame);

        g2.setStroke(new BasicStroke(
                Math.max(7f, size * 0.09f),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND
        ));

        g2.drawLine(
                (int) (x + size * 0.37),
                (int) (y + size * 0.63),
                (int) (x + size * 0.37),
                (int) (y + size * 0.80)
        );

        g2.drawLine(
                (int) (x + size * 0.50),
                (int) (y + size * 0.50),
                (int) (x + size * 0.50),
                (int) (y + size * 0.87)
        );

        g2.drawLine(
                (int) (x + size * 0.63),
                (int) (y + size * 0.38),
                (int) (x + size * 0.63),
                (int) (y + size * 0.80)
        );

        g2.dispose();
    }
}
