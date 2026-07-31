package sigir.vista;

import java.awt.BorderLayout;
import sigir.vista.paneles.VentasPanel;

public class FrmVenta extends javax.swing.JFrame {
    public FrmVenta(){ initComponents(); setTitle("SIGIR - Ventas"); setLocationRelativeTo(null); setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH); pnlContenedor.setLayout(new BorderLayout()); pnlContenedor.add(new VentasPanel(),BorderLayout.CENTER); }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents(){
        pnlContenedor=new javax.swing.JPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1180,760));
        getContentPane().setLayout(new java.awt.BorderLayout());
        pnlContenedor.setBackground(new java.awt.Color(247,249,252));
        getContentPane().add(pnlContenedor,java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlContenedor;
    // End of variables declaration//GEN-END:variables
}
