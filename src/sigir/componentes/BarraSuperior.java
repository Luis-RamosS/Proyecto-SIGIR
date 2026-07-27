package sigir.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class BarraSuperior extends JPanel {
    public BarraSuperior(String usuario){setPreferredSize(new Dimension(1000,82));setBackground(Color.WHITE);setLayout(new BorderLayout());setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(231,235,241)));add(buscar(),BorderLayout.WEST);add(usuario(usuario),BorderLayout.EAST);}
    private JPanel buscar(){JPanel ext=new JPanel(new FlowLayout(FlowLayout.LEFT,28,18));ext.setOpaque(false);JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);p.setPreferredSize(new Dimension(500,46));p.setBorder(new CompoundBorder(new RoundedBorder(new Color(215,224,235),12,1.2f),new EmptyBorder(0,14,0,12)));JLabel i=new JLabel(new IconoMenu(IconoMenu.Tipo.BUSCAR,new Color(70,105,155),22));JTextField c=new JTextField("Buscar productos, clientes, documentos...");c.setBorder(null);c.setOpaque(false);c.setFont(new Font("SansSerif",Font.PLAIN,14));c.setForeground(new Color(111,134,167));JLabel a=new JLabel("Ctrl K",SwingConstants.CENTER);a.setFont(new Font("SansSerif",Font.PLAIN,11));a.setForeground(new Color(84,112,152));a.setPreferredSize(new Dimension(48,28));a.setBorder(new RoundedBorder(new Color(225,231,239),8,1f));p.add(i,BorderLayout.WEST);p.add(c,BorderLayout.CENTER);p.add(a,BorderLayout.EAST);ext.add(p);return ext;}
    private JPanel usuario(String u){JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,16,13));p.setOpaque(false);p.setBorder(new EmptyBorder(0,0,0,20));JButton camp=new JButton(new IconoMenu(IconoMenu.Tipo.CAMPANA,new Color(52,89,139),26));camp.setBorderPainted(false);camp.setContentAreaFilled(false);camp.setFocusPainted(false);JLabel sep=new JLabel("│");sep.setForeground(new Color(222,228,236));JLabel av=new JLabel("AD",SwingConstants.CENTER);av.setPreferredSize(new Dimension(46,46));av.setOpaque(true);av.setBackground(new Color(242,245,249));av.setForeground(new Color(69,96,136));av.setFont(new Font("SansSerif",Font.PLAIN,16));av.setBorder(new RoundedBorder(new Color(242,245,249),46,1f));JPanel datos=new JPanel();datos.setOpaque(false);datos.setLayout(new BoxLayout(datos,BoxLayout.Y_AXIS));JLabel n=new JLabel(u.equalsIgnoreCase("admin")?"Admin Demo":u);n.setFont(new Font("SansSerif",Font.BOLD,14));n.setForeground(new Color(28,39,55));JLabel r=new JLabel("Administrador");r.setFont(new Font("SansSerif",Font.PLAIN,12));r.setForeground(new Color(72,105,151));datos.add(n);datos.add(r);JLabel f=new JLabel("⌄");f.setFont(new Font("SansSerif",Font.BOLD,18));f.setForeground(new Color(54,88,136));p.add(camp);p.add(sep);p.add(av);p.add(datos);p.add(f);return p;}
}
