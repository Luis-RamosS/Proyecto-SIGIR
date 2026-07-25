package sigir.componentes;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import sigir.util.Colores;
import javax.swing.JPanel;

public class PanelFondoLogin extends JPanel {

    public PanelFondoLogin() {
        setOpaque(true);
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

        g2.setPaint(new GradientPaint(
                0, 0, Colores.FONDO_SUPERIOR,
                0, h, Colores.FONDO_INFERIOR
        ));
        g2.fillRect(0, 0, w, h);

        dibujarOndas(g2, w, h);
        dibujarHexagonos(g2, w, h);

        g2.dispose();
    }

    private void dibujarOndas(Graphics2D g2, int w, int h) {
        g2.setComposite(AlphaComposite.SrcOver.derive(0.55f));

        Path2D superior = new Path2D.Double();
        superior.moveTo(0, 0);
        superior.lineTo(w * 0.32, 0);
        superior.curveTo(
                w * 0.20, h * 0.02,
                w * 0.10, h * 0.13,
                0, h * 0.28
        );
        superior.closePath();

        g2.setColor(new Color(222, 228, 237));
        g2.fill(superior);

        Path2D inferior = new Path2D.Double();
        inferior.moveTo(w, h);
        inferior.lineTo(w * 0.56, h);
        inferior.curveTo(
                w * 0.74, h * 0.92,
                w * 0.90, h * 0.77,
                w, h * 0.58
        );
        inferior.closePath();

        g2.setColor(new Color(218, 225, 235));
        g2.fill(inferior);

        Path2D inferior2 = new Path2D.Double();
        inferior2.moveTo(w, h);
        inferior2.lineTo(w * 0.68, h);
        inferior2.curveTo(
                w * 0.80, h * 0.94,
                w * 0.94, h * 0.83,
                w, h * 0.70
        );
        inferior2.closePath();

        g2.setColor(new Color(233, 238, 245));
        g2.fill(inferior2);

        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void dibujarHexagonos(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(192, 204, 219, 90));
        g2.setStroke(new BasicStroke(1.2f));

        dibujarHexagono(g2, w - 180, 170, 88);
        dibujarHexagono(g2, w - 105, 290, 88);
        dibujarHexagono(g2, w - 250, 305, 88);

        dibujarHexagono(g2, 110, h - 180, 92);
        dibujarHexagono(g2, 200, h - 90, 92);
        dibujarHexagono(g2, 20, h - 70, 92);
    }

    private void dibujarHexagono(Graphics2D g2, int centerX, int centerY, int radius) {
        Polygon polygon = new Polygon();

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            polygon.addPoint(
                    centerX + (int) (radius * Math.cos(angle)),
                    centerY + (int) (radius * Math.sin(angle))
            );
        }

        g2.drawPolygon(polygon);
    }
}
