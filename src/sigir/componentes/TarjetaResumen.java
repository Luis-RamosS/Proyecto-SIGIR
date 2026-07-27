package sigir.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TarjetaResumen extends PanelRedondeado {
    public TarjetaResumen(String titulo,String valor,String enlace,Color color,IconoMenu.Tipo tipo){super(18);setLayout(new BorderLayout(14,0));setPreferredSize(new Dimension(210,150));setBorder(BorderFactory.createEmptyBorder(22,22,18,18));JLabel icono=new JLabel(new IconoMenu(tipo,color,28),JLabel.CENTER);icono.setPreferredSize(new Dimension(56,56));icono.setOpaque(true);icono.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue(),25));icono.setBorder(new RoundedBorder(icono.getBackground(),56,1f));JPanel tx=new JPanel();tx.setOpaque(false);tx.setLayout(new BoxLayout(tx,BoxLayout.Y_AXIS));JLabel a=new JLabel(titulo);a.setFont(new Font("SansSerif",Font.PLAIN,14));a.setForeground(new Color(67,93,130));JLabel b=new JLabel(valor);b.setFont(new Font("SansSerif",Font.BOLD,26));b.setForeground(new Color(20,43,78));JLabel c=new JLabel(enlace+"  →");c.setFont(new Font("SansSerif",Font.PLAIN,13));c.setForeground(new Color(31,102,220));tx.add(a);tx.add(b);tx.add(javax.swing.Box.createVerticalGlue());tx.add(c);add(icono,BorderLayout.WEST);add(tx,BorderLayout.CENTER);}
}
