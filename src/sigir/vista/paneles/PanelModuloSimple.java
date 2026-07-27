package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanelModuloSimple extends JPanel {
    public PanelModuloSimple(String modulo){setBackground(new Color(248,250,253));setLayout(new GridBagLayout());JLabel l=new JLabel("Módulo "+modulo+" — lo construiremos a continuación");l.setFont(new Font("SansSerif",Font.BOLD,24));l.setForeground(new Color(37,68,110));add(l);}
}
