package sigir.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class GraficoBarrasPanel extends JPanel {
    private final int[] valores={22,28,34,31,42,27,19}; private final String[] dias={"Lun","Mar","Mié","Jue","Vie","Sáb","Dom"};
    public GraficoBarrasPanel(){setOpaque(false);}
    @Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight(),left=42,right=18,top=28,bottom=42,cw=w-left-right,ch=h-top-bottom;g2.setFont(new Font("SansSerif",Font.PLAIN,11));for(int i=0;i<=5;i++){int y=top+ch-i*ch/5;g2.setColor(new Color(232,237,243));g2.drawLine(left,y,w-right,y);g2.setColor(new Color(126,147,176));g2.drawString(String.valueOf(i*10),10,y+4);}int slot=cw/valores.length,bw=Math.max(28,slot/2);for(int i=0;i<valores.length;i++){int bh=(int)(ch*(valores[i]/50.0)),x=left+i*slot+(slot-bw)/2,y=top+ch-bh;g2.setColor(new Color(74,113,165));g2.fillRoundRect(x,y,bw,bh,8,8);g2.setColor(new Color(35,69,116));String v=String.valueOf(valores[i]);g2.drawString(v,x+(bw-g2.getFontMetrics().stringWidth(v))/2,y-6);g2.setColor(new Color(80,105,142));g2.drawString(dias[i],x+(bw-g2.getFontMetrics().stringWidth(dias[i]))/2,h-16);}g2.dispose();}
}
