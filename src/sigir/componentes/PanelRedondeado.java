package sigir.componentes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelRedondeado extends JPanel {
    private int radio = 18;
    private Color colorFondo = Color.WHITE;
    private Color colorBorde = new Color(225, 231, 239);
    private boolean sombra = true;

    public PanelRedondeado() { setOpaque(false); }
    public PanelRedondeado(int radio) { this.radio = radio; setOpaque(false); }
    public void setColorFondo(Color c) { colorFondo = c; repaint(); }
    public void setColorBorde(Color c) { colorBorde = c; repaint(); }
    public void setSombra(boolean valor) { sombra = valor; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int m = sombra ? 6 : 1;
        int w = getWidth() - m * 2;
        int h = getHeight() - m * 2;
        if (sombra) {
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(36, 54, 78, 3 + i));
                g2.fillRoundRect(m - i / 2, m + 2, w + i, h + i, radio + i, radio + i);
            }
        }
        g2.setColor(colorFondo);
        g2.fillRoundRect(m, m, w, h, radio, radio);
        g2.setColor(colorBorde);
        g2.drawRoundRect(m, m, w, h, radio, radio);
        g2.dispose();
        super.paintComponent(g);
    }
}
