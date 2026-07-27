package sigir.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.componentes.GraficoBarrasPanel;
import sigir.componentes.IconoMenu;
import sigir.componentes.PanelRedondeado;
import sigir.componentes.RoundedBorder;
import sigir.componentes.TarjetaResumen;

public class InicioPanel extends JPanel {
    public InicioPanel(){setLayout(new BorderLayout());setBackground(new Color(248,250,253));JScrollPane s=new JScrollPane(contenido());s.setBorder(null);s.getViewport().setBackground(new Color(248,250,253));s.getVerticalScrollBar().setUnitIncrement(18);add(s,BorderLayout.CENTER);}
    private JPanel contenido(){JPanel p=new JPanel(new GridBagLayout());p.setBackground(new Color(248,250,253));p.setBorder(new EmptyBorder(26,34,34,34));GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;g.anchor=GridBagConstraints.NORTHWEST;g.gridy=0;p.add(encabezado(),g);g.gridy++;g.insets=new Insets(20,0,0,0);p.add(tarjetas(),g);g.gridy++;p.add(tablas(),g);g.gridy++;p.add(actividad(),g);g.gridy++;g.weighty=1;p.add(new JPanel(),g);return p;}
    private JPanel encabezado(){JPanel p=new JPanel(new BorderLayout());p.setOpaque(false);JPanel tx=new JPanel();tx.setOpaque(false);tx.setLayout(new BoxLayout(tx,BoxLayout.Y_AXIS));JLabel t=new JLabel("¡Bienvenido, Admin!");t.setFont(new Font("SansSerif",Font.BOLD,25));t.setForeground(new Color(20,43,78));JLabel st=new JLabel("Resumen general de tu inventario y operaciones.");st.setFont(new Font("SansSerif",Font.PLAIN,14));st.setForeground(new Color(79,109,151));tx.add(t);tx.add(st);JButton f=new JButton("24 de mayo, 2025   ⌄",new IconoMenu(IconoMenu.Tipo.CALENDARIO,new Color(64,101,151),21));f.setIconTextGap(10);f.setFont(new Font("SansSerif",Font.PLAIN,13));f.setForeground(new Color(83,111,151));f.setBackground(Color.WHITE);f.setFocusPainted(false);f.setBorder(new RoundedBorder(new Color(218,226,236),12,1.2f));f.setPreferredSize(new Dimension(185,44));p.add(tx,BorderLayout.WEST);p.add(f,BorderLayout.EAST);return p;}
    private JPanel tarjetas(){JPanel p=new JPanel(new GridLayout(1,5,16,0));p.setOpaque(false);p.add(new TarjetaResumen("Ventas de hoy","24","Ver detalles",new Color(42,111,238),IconoMenu.Tipo.VENTAS));p.add(new TarjetaResumen("Productos registrados","256","Ver productos",new Color(41,170,85),IconoMenu.Tipo.PRODUCTOS));p.add(new TarjetaResumen("Stock bajo","18","Ver inventario",new Color(242,139,29),IconoMenu.Tipo.INVENTARIO));p.add(new TarjetaResumen("Créditos pendientes","12","Ver créditos",new Color(241,73,83),IconoMenu.Tipo.CREDITOS));p.add(new TarjetaResumen("Reparaciones pendientes","7","Ver reparaciones",new Color(145,61,224),IconoMenu.Tipo.REPARACIONES));return p;}
    private JPanel tablas(){JPanel p=new JPanel(new GridLayout(1,2,18,0));p.setOpaque(false);p.add(tabla("Ventas recientes",new String[]{"#","Cliente","Fecha","Estado"},new Object[][]{{"1","María González","24/05/2025 10:45","Completada"},{"2","Carlos Ramírez","24/05/2025 10:15","Completada"},{"3","Distribuidora del Sur","24/05/2025 09:50","Completada"},{"4","Laura Pérez","24/05/2025 09:20","Pendiente"},{"5","ElectroHogar S.A.","24/05/2025 08:55","Pendiente"}}));p.add(tabla("Productos con poco inventario",new String[]{"Producto","Stock actual","Stock mínimo","Estado"},new Object[][]{{"Cable HDMI 2m","3","10","Crítico"},{"Mouse inalámbrico","4","10","Crítico"},{"Teclado USB","6","10","Bajo"},{"Parlante Bluetooth","7","15","Bajo"},{"Cargador USB-C","8","15","Bajo"}}));return p;}
    private PanelRedondeado tabla(String titulo,String[] cols,Object[][] datos){PanelRedondeado card=new PanelRedondeado(18);card.setLayout(new BorderLayout());card.setPreferredSize(new Dimension(550,315));card.setBorder(new EmptyBorder(12,14,14,14));JPanel cab=new JPanel(new BorderLayout());cab.setOpaque(false);cab.setBorder(new EmptyBorder(2,2,10,2));JLabel lt=new JLabel(titulo);lt.setFont(new Font("SansSerif",Font.BOLD,16));lt.setForeground(new Color(27,52,88));JButton v=new JButton("Ver todas");v.setFont(new Font("SansSerif",Font.PLAIN,12));v.setForeground(new Color(75,102,142));v.setBackground(Color.WHITE);v.setFocusPainted(false);v.setBorder(new RoundedBorder(new Color(219,226,235),9,1f));v.setPreferredSize(new Dimension(85,34));cab.add(lt,BorderLayout.WEST);cab.add(v,BorderLayout.EAST);DefaultTableModel m=new DefaultTableModel(datos,cols){@Override public boolean isCellEditable(int r,int c){return false;}};JTable t=new JTable(m);estilo(t);JScrollPane s=new JScrollPane(t);s.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,234,240)));s.getViewport().setBackground(Color.WHITE);card.add(cab,BorderLayout.NORTH);card.add(s,BorderLayout.CENTER);return card;}
    private void estilo(JTable t){t.setRowHeight(42);t.setShowVerticalLines(false);t.setShowHorizontalLines(true);t.setGridColor(new Color(235,239,244));t.setBackground(Color.WHITE);t.setForeground(new Color(44,70,108));t.setFont(new Font("SansSerif",Font.PLAIN,12));t.setSelectionBackground(new Color(235,242,252));JTableHeader h=t.getTableHeader();h.setPreferredSize(new Dimension(0,38));h.setBackground(new Color(250,251,253));h.setForeground(new Color(39,65,101));h.setFont(new Font("SansSerif",Font.BOLD,11));h.setBorder(null);h.setReorderingAllowed(false);DefaultTableCellRenderer r=new DefaultTableCellRenderer();r.setBorder(new EmptyBorder(0,8,0,8));r.setHorizontalAlignment(SwingConstants.LEFT);for(int i=0;i<t.getColumnCount();i++)t.getColumnModel().getColumn(i).setCellRenderer(r);}
    private PanelRedondeado actividad(){PanelRedondeado p=new PanelRedondeado(18);p.setLayout(new BorderLayout(18,0));p.setPreferredSize(new Dimension(1100,260));p.setBorder(new EmptyBorder(16,18,16,18));JPanel izq=new JPanel(new BorderLayout());izq.setOpaque(false);JPanel txt=new JPanel();txt.setOpaque(false);txt.setLayout(new BoxLayout(txt,BoxLayout.Y_AXIS));JLabel t=new JLabel("Actividad semanal");t.setFont(new Font("SansSerif",Font.BOLD,16));t.setForeground(new Color(29,54,89));JLabel st=new JLabel("Operaciones");st.setFont(new Font("SansSerif",Font.PLAIN,11));st.setForeground(new Color(97,123,159));txt.add(t);txt.add(st);izq.add(txt,BorderLayout.NORTH);izq.add(new GraficoBarrasPanel(),BorderLayout.CENTER);PanelRedondeado res=new PanelRedondeado(14);res.setSombra(false);res.setColorFondo(new Color(248,250,254));res.setPreferredSize(new Dimension(260,200));res.setLayout(new BoxLayout(res,BoxLayout.Y_AXIS));res.setBorder(new EmptyBorder(22,24,18,20));dato(res,"Total de operaciones de la semana","203");dato(res,"Promedio diario","29");dato(res,"Mejor día","Viernes (42 operaciones)");p.add(izq,BorderLayout.CENTER);p.add(res,BorderLayout.EAST);return p;}
    private void dato(JPanel p,String a,String b){JLabel x=new JLabel(a);x.setFont(new Font("SansSerif",Font.PLAIN,12));x.setForeground(new Color(98,124,159));JLabel y=new JLabel(b);y.setFont(new Font("SansSerif",Font.BOLD,b.startsWith("Viernes")?15:21));y.setForeground(b.startsWith("Viernes")?new Color(28,103,215):new Color(24,50,87));p.add(x);p.add(y);p.add(javax.swing.Box.createVerticalStrut(15));}
}
