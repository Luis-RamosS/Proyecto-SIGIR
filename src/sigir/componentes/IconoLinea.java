package sigir.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public class IconoLinea implements Icon {

    public enum Tipo {
        USUARIO,
        CANDADO,
        OJO
    }

    private final Tipo tipo;
    private final Color color;
    private final int size;

    public IconoLinea(Tipo tipo, Color color, int size) {
        this.tipo = tipo;
        this.color = color;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setColor(color);
        g2.setStroke(new BasicStroke(
                Math.max(1.8f, size / 12f),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        switch (tipo) {
            case USUARIO -> dibujarUsuario(g2);
            case CANDADO -> dibujarCandado(g2);
            case OJO -> dibujarOjo(g2);
        }

        g2.dispose();
    }

    private void dibujarUsuario(Graphics2D g2) {
        int head = Math.round(size * 0.30f);
        int headX = (size - head) / 2;
        int headY = Math.round(size * 0.08f);

        g2.drawOval(headX, headY, head, head);

        int bodyX = Math.round(size * 0.18f);
        int bodyY = Math.round(size * 0.52f);
        int bodyW = Math.round(size * 0.64f);
        int bodyH = Math.round(size * 0.34f);

        g2.drawArc(bodyX, bodyY, bodyW, bodyH, 0, 180);
        g2.drawLine(bodyX, bodyY + bodyH / 2, bodyX, bodyY + bodyH);
        g2.drawLine(
                bodyX + bodyW,
                bodyY + bodyH / 2,
                bodyX + bodyW,
                bodyY + bodyH
        );
    }

    private void dibujarCandado(Graphics2D g2) {
        int bodyX = Math.round(size * 0.20f);
        int bodyY = Math.round(size * 0.43f);
        int bodyW = Math.round(size * 0.60f);
        int bodyH = Math.round(size * 0.44f);

        g2.drawRoundRect(
                bodyX,
                bodyY,
                bodyW,
                bodyH,
                Math.round(size * 0.10f),
                Math.round(size * 0.10f)
        );

        int arcX = Math.round(size * 0.31f);
        int arcY = Math.round(size * 0.10f);
        int arcW = Math.round(size * 0.38f);
        int arcH = Math.round(size * 0.48f);

        g2.drawArc(arcX, arcY, arcW, arcH, 0, 180);
        g2.drawLine(arcX, arcY + arcH / 2, arcX, bodyY);
        g2.drawLine(arcX + arcW, arcY + arcH / 2, arcX + arcW, bodyY);
    }

    private void dibujarOjo(Graphics2D g2) {
        int left = Math.round(size * 0.06f);
        int top = Math.round(size * 0.28f);
        int width = Math.round(size * 0.88f);
        int height = Math.round(size * 0.44f);

        g2.drawArc(left, top, width, height, 0, 180);
        g2.drawArc(left, top, width, height, 180, 180);

        int pupil = Math.round(size * 0.20f);
        g2.drawOval(
                (size - pupil) / 2,
                (size - pupil) / 2,
                pupil,
                pupil
        );
    }
}
