package sigir.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import sigir.vista.InicioFrame;
import sigir.vista.ModuloFrame;

public class MenuLateral extends JPanel {
    private final JFrame actual; private final String usuario; private final String activo;
    public MenuLateral(JFrame actual,String usuario,String activo){this.actual=actual;this.usuario=usuario;this.activo=activo;configurar();construir();}
    private void configurar(){setPreferredSize(new Dimension(255,800));setBackground(Color.WHITE);setLayout(new BorderLayout());setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(230,235,242)));}
    private void construir(){
        JPanel sup=new JPanel();sup.setOpaque(false);sup.setLayout(new BoxLayout(sup,BoxLayout.Y_AXIS));sup.setBorder(BorderFactory.createEmptyBorder(24,16,10,16));
        JPanel marca=new JPanel();marca.setOpaque(false);marca.setLayout(new BoxLayout(marca,BoxLayout.Y_AXIS));
        JPanel linea=new JPanel();linea.setOpaque(false);linea.setLayout(new BoxLayout(linea,BoxLayout.X_AXIS));
        LogoSIGIR logo=new LogoSIGIR();logo.setPreferredSize(new Dimension(58,58));logo.setMaximumSize(new Dimension(58,58));
        JLabel t=new JLabel("SIGIR");t.setFont(new Font("SansSerif",Font.BOLD,31));t.setForeground(new Color(21,50,91));
        linea.add(logo);linea.add(Box.createHorizontalStrut(8));linea.add(t);
        JLabel st=new JLabel("Sistema de Gestión de Inventario");st.setFont(new Font("SansSerif",Font.PLAIN,11));st.setForeground(new Color(78,111,154));
        marca.add(linea);marca.add(st);sup.add(marca);sup.add(Box.createVerticalStrut(22));
        boton(sup,"Inicio",IconoMenu.Tipo.INICIO,"Inicio"); boton(sup,"Ventas",IconoMenu.Tipo.VENTAS,"Ventas"); boton(sup,"Compras",IconoMenu.Tipo.COMPRAS,"Compras"); boton(sup,"Productos",IconoMenu.Tipo.PRODUCTOS,"Productos"); boton(sup,"Inventario",IconoMenu.Tipo.INVENTARIO,"Inventario"); boton(sup,"Clientes",IconoMenu.Tipo.CLIENTES,"Clientes"); boton(sup,"Proveedores",IconoMenu.Tipo.PROVEEDORES,"Proveedores"); boton(sup,"Créditos",IconoMenu.Tipo.CREDITOS,"Créditos"); boton(sup,"Reparaciones",IconoMenu.Tipo.REPARACIONES,"Reparaciones"); boton(sup,"Usuarios",IconoMenu.Tipo.USUARIOS,"Usuarios"); boton(sup,"Reportes",IconoMenu.Tipo.REPORTES,"Reportes"); boton(sup,"Configuración",IconoMenu.Tipo.CONFIGURACION,"Configuración");
        JPanel inf=new JPanel(new BorderLayout());inf.setOpaque(false);inf.setBorder(BorderFactory.createEmptyBorder(10,25,22,20));JLabel ver=new JLabel("◉   SIGIR v1.0.0");ver.setFont(new Font("SansSerif",Font.PLAIN,13));ver.setForeground(new Color(65,98,143));inf.add(ver,BorderLayout.WEST);
        add(sup,BorderLayout.NORTH);add(inf,BorderLayout.SOUTH);
    }
    private void boton(JPanel p,String texto,IconoMenu.Tipo icono,String modulo){boolean esActivo=modulo.equalsIgnoreCase(activo);BotonMenu b=new BotonMenu(texto,icono,esActivo);b.setAlignmentX(Component.LEFT_ALIGNMENT);b.addActionListener(e->{if(esActivo)return;JFrame n=modulo.equals("Inicio")?new InicioFrame(usuario):new ModuloFrame(modulo,usuario);n.setVisible(true);actual.dispose();});p.add(b);p.add(Box.createVerticalStrut(4));}
}
