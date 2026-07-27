package sigir.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class BotonMenu extends JButton {
    private final boolean activo; private boolean hover;
    public BotonMenu(String texto, IconoMenu.Tipo tipo, boolean activo) {
        super(texto); this.activo=activo;
        setIcon(new IconoMenu(tipo, activo?Color.WHITE:new Color(65,106,161),24));
        setIconTextGap(16); setHorizontalAlignment(SwingConstants.LEFT);
        setFont(new Font("SansSerif", activo?Font.BOLD:Font.PLAIN, 15));
        setForeground(activo?Color.WHITE:new Color(49,85,132));
        setPreferredSize(new Dimension(218,48)); setMaximumSize(new Dimension(218,48));
        setBorderPainted(false); setContentAreaFilled(false); setFocusPainted(false); setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseEntered(java.awt.event.MouseEvent e){hover=true;repaint();}
            @Override public void mouseExited(java.awt.event.MouseEvent e){hover=false;repaint();}
        });
    }
    @Override protected void paintComponent(Graphics g){
        Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(activo){g2.setColor(new Color(72,117,174));g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);} else if(hover){g2.setColor(new Color(238,244,251));g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);} g2.dispose(); super.paintComponent(g);
    }
}
