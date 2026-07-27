package sigir.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import sigir.componentes.BarraSuperior;
import sigir.componentes.MenuLateral;

public abstract class BaseFrame extends JFrame {
    protected final JPanel panelContenido; protected final String usuarioActual;
    protected BaseFrame(String titulo,String usuario,String activo){this.usuarioActual=usuario;setTitle("SIGIR - "+titulo);setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setMinimumSize(new Dimension(1200,760));setSize(1500,900);setLocationRelativeTo(null);setExtendedState(JFrame.MAXIMIZED_BOTH);JPanel raiz=new JPanel(new BorderLayout());raiz.setBackground(new Color(248,250,253));JPanel derecha=new JPanel(new BorderLayout());derecha.setBackground(new Color(248,250,253));panelContenido=new JPanel(new BorderLayout());panelContenido.setBackground(new Color(248,250,253));derecha.add(new BarraSuperior(usuario),BorderLayout.NORTH);derecha.add(panelContenido,BorderLayout.CENTER);raiz.add(new MenuLateral(this,usuario,activo),BorderLayout.WEST);raiz.add(derecha,BorderLayout.CENTER);setContentPane(raiz);}
    protected final void establecerContenido(JPanel p){panelContenido.removeAll();panelContenido.add(p,BorderLayout.CENTER);panelContenido.revalidate();panelContenido.repaint();}
}
