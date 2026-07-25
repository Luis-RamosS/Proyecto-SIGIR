package sigir.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import sigir.util.Colores;

public class BotonGradiente extends JButton {

    private boolean hovered;

    public BotonGradiente(String text) {
        super(text);

        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        Color start = hovered
                ? new Color(127, 154, 190)
                : Colores.AZUL_CLARO;

        Color end = hovered
                ? new Color(73, 102, 143)
                : Colores.AZUL_OSCURO;

        g2.setPaint(new GradientPaint(
                0, 0, start,
                getWidth(), getHeight(), end
        ));

        g2.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                16,
                16
        );

        g2.dispose();
        super.paintComponent(g);
    }
}
