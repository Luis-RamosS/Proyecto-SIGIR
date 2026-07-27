package sigir.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public class IconoMenu implements Icon {
    public enum Tipo { INICIO, VENTAS, COMPRAS, PRODUCTOS, INVENTARIO, CLIENTES,
        PROVEEDORES, CREDITOS, REPARACIONES, USUARIOS, REPORTES, CONFIGURACION,
        BUSCAR, CAMPANA, CALENDARIO }

    private final Tipo tipo; private final Color color; private final int size;
    public IconoMenu(Tipo tipo, Color color, int size) { this.tipo=tipo; this.color=color; this.size=size; }
    @Override public int getIconWidth(){ return size; }
    @Override public int getIconHeight(){ return size; }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2=(Graphics2D)g.create(); g2.translate(x,y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color); g2.setStroke(new BasicStroke(Math.max(1.6f,size/12f),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        switch(tipo){
            case INICIO -> casa(g2); case VENTAS -> carrito(g2); case COMPRAS -> bolsa(g2);
            case PRODUCTOS -> cubo(g2); case INVENTARIO -> almacen(g2);
            case CLIENTES, USUARIOS -> usuarios(g2); case PROVEEDORES -> camion(g2);
            case CREDITOS -> documento(g2); case REPARACIONES -> herramienta(g2);
            case REPORTES -> reporte(g2); case CONFIGURACION -> engranaje(g2);
            case BUSCAR -> buscar(g2); case CAMPANA -> campana(g2); case CALENDARIO -> calendario(g2);
        }
        g2.dispose();
    }
    private void casa(Graphics2D g){ g.drawLine(s(.12),s(.46),s(.50),s(.14)); g.drawLine(s(.50),s(.14),s(.88),s(.46)); g.drawRoundRect(s(.22),s(.42),s(.56),s(.43),3,3); g.drawRect(s(.44),s(.60),s(.14),s(.25)); }
    private void carrito(Graphics2D g){ g.drawLine(s(.10),s(.18),s(.20),s(.18)); g.drawLine(s(.20),s(.20),s(.30),s(.62)); g.drawLine(s(.30),s(.62),s(.76),s(.62)); g.drawLine(s(.76),s(.62),s(.87),s(.32)); g.drawLine(s(.87),s(.32),s(.28),s(.32)); g.drawOval(s(.30),s(.72),s(.10),s(.10)); g.drawOval(s(.67),s(.72),s(.10),s(.10)); }
    private void bolsa(Graphics2D g){ g.drawRoundRect(s(.20),s(.30),s(.60),s(.56),4,4); g.drawArc(s(.35),s(.10),s(.30),s(.38),0,180); }
    private void cubo(Graphics2D g){ int[] xs={s(.50),s(.85),s(.85),s(.50),s(.15),s(.15)}; int[] ys={s(.10),s(.30),s(.70),s(.90),s(.70),s(.30)}; g.drawPolygon(xs,ys,6); g.drawLine(s(.15),s(.30),s(.50),s(.50)); g.drawLine(s(.85),s(.30),s(.50),s(.50)); g.drawLine(s(.50),s(.50),s(.50),s(.90)); }
    private void almacen(Graphics2D g){ g.drawRect(s(.18),s(.30),s(.64),s(.55)); g.drawLine(s(.10),s(.30),s(.50),s(.10)); g.drawLine(s(.90),s(.30),s(.50),s(.10)); for(int i=0;i<3;i++) g.drawLine(s(.27),s(.43+i*.13),s(.73),s(.43+i*.13)); }
    private void usuarios(Graphics2D g){ g.drawOval(s(.18),s(.18),s(.24),s(.24)); g.drawOval(s(.57),s(.23),s(.19),s(.19)); g.drawArc(s(.08),s(.50),s(.46),s(.34),0,180); g.drawArc(s(.48),s(.53),s(.38),s(.27),0,180); }
    private void camion(Graphics2D g){ g.drawRect(s(.10),s(.30),s(.46),s(.40)); g.drawRect(s(.56),s(.42),s(.28),s(.28)); g.drawLine(s(.84),s(.42),s(.90),s(.55)); g.drawOval(s(.22),s(.65),s(.13),s(.13)); g.drawOval(s(.68),s(.65),s(.13),s(.13)); }
    private void documento(Graphics2D g){ g.drawRoundRect(s(.22),s(.10),s(.56),s(.78),3,3); for(int i=0;i<3;i++) g.drawLine(s(.34),s(.35+i*.15),s(.66),s(.35+i*.15)); }
    private void herramienta(Graphics2D g){ g.drawLine(s(.25),s(.78),s(.70),s(.33)); g.drawOval(s(.14),s(.66),s(.24),s(.24)); g.drawArc(s(.58),s(.12),s(.30),s(.30),40,210); }
    private void reporte(Graphics2D g){ g.drawLine(s(.15),s(.84),s(.86),s(.84)); g.drawRect(s(.24),s(.48),s(.12),s(.28)); g.drawRect(s(.45),s(.31),s(.12),s(.45)); g.drawRect(s(.66),s(.18),s(.12),s(.58)); }
    private void engranaje(Graphics2D g){ g.drawOval(s(.25),s(.25),s(.50),s(.50)); g.drawOval(s(.41),s(.41),s(.18),s(.18)); for(int i=0;i<8;i++){double a=Math.toRadians(i*45);g.drawLine((int)(size*.5+Math.cos(a)*size*.31),(int)(size*.5+Math.sin(a)*size*.31),(int)(size*.5+Math.cos(a)*size*.42),(int)(size*.5+Math.sin(a)*size*.42));}}
    private void buscar(Graphics2D g){ g.drawOval(s(.12),s(.10),s(.56),s(.56)); g.drawLine(s(.61),s(.61),s(.88),s(.88)); }
    private void campana(Graphics2D g){ g.drawArc(s(.25),s(.17),s(.50),s(.58),0,180); g.drawLine(s(.25),s(.46),s(.20),s(.70)); g.drawLine(s(.75),s(.46),s(.80),s(.70)); g.drawLine(s(.20),s(.70),s(.80),s(.70)); g.drawArc(s(.42),s(.65),s(.16),s(.18),180,180); }
    private void calendario(Graphics2D g){ g.drawRoundRect(s(.14),s(.20),s(.72),s(.64),4,4); g.drawLine(s(.14),s(.38),s(.86),s(.38)); g.drawLine(s(.31),s(.10),s(.31),s(.29)); g.drawLine(s(.69),s(.10),s(.69),s(.29)); }
    private int s(double v){ return (int)(size*v); }
}
