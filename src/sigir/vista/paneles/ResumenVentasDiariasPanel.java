package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import sigir.controlador.ResumenVentasDiariasControlador;
import sigir.modelo.ResumenVentasDiarias;
import sigir.util.SelectorFechaUtil;

public class ResumenVentasDiariasPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
    private final ResumenVentasDiariasControlador controlador;

    public ResumenVentasDiariasPanel() {
        initComponents();
        controlador = new ResumenVentasDiariasControlador(this);
        moneda.setMinimumFractionDigits(2);
        moneda.setMaximumFractionDigits(2);
        txtFecha.setText(LocalDate.now().format(FECHA));
        SelectorFechaUtil.instalar(txtFecha, false);
        btnActualizar.addActionListener(e -> controlador.recargar());
        aplicarEstilos();
    }

    public void recargar() {
        controlador.recargar();
    }

    public void recargarSiNecesario() {
        controlador.recargarSiNecesario();
    }

    public LocalDate getFechaSeleccionada() {
        return LocalDate.parse(txtFecha.getText().trim(), FECHA);
    }

    public void mostrarResumen(ResumenVentasDiarias r) {
        lblEfectivoValor.setText(formatear(r.getEfectivo()));
        lblTransferenciasValor.setText(formatear(r.getTransferencias()));
        lblCreditosValor.setText(formatear(r.getCreditos()));
        lblTarjetasValor.setText(formatear(r.getTarjetas()));
        lblVentasRapidasValor.setText(
                formatear(r.getVentasRapidas())
        );
        lblTotalValor.setText(formatear(r.getTotalVentas()));
    }

    public void establecerCargando(boolean cargando) {
        btnActualizar.setEnabled(!cargando);
        btnActualizar.setText(cargando ? "Actualizando..." : "Actualizar");
    }

    private String formatear(BigDecimal valor) {
        return moneda.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{
                pnlEfectivo, pnlTransferencias, pnlCreditos,
                pnlTarjetas, pnlVentasRapidas, pnlTotal}) {
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borde),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }
        btnActualizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtFecha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205,216,229)),
                BorderFactory.createEmptyBorder(0,10,0,10)
        ));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        btnActualizar = new javax.swing.JButton();
        pnlEfectivo = new javax.swing.JPanel();
        lblEfectivoTitulo = new javax.swing.JLabel();
        lblEfectivoValor = new javax.swing.JLabel();
        pnlTransferencias = new javax.swing.JPanel();
        lblTransferenciasTitulo = new javax.swing.JLabel();
        lblTransferenciasValor = new javax.swing.JLabel();
        pnlCreditos = new javax.swing.JPanel();
        lblCreditosTitulo = new javax.swing.JLabel();
        lblCreditosValor = new javax.swing.JLabel();
        pnlTarjetas = new javax.swing.JPanel();
        lblTarjetasTitulo = new javax.swing.JLabel();
        lblTarjetasValor = new javax.swing.JLabel();
        pnlVentasRapidas = new javax.swing.JPanel();
        lblVentasRapidasTitulo = new javax.swing.JLabel();
        lblVentasRapidasValor = new javax.swing.JLabel();
        pnlTotal = new javax.swing.JPanel();
        lblTotalTitulo = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        lblNota = new javax.swing.JLabel();

        setBackground(new java.awt.Color(247, 249, 252));
        setLayout(null);

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(24, 50, 87));
        lblTitulo.setText("Resumen diario de ventas");
        add(lblTitulo); lblTitulo.setBounds(24, 24, 420, 34);

        lblSubtitulo.setForeground(new Color(98,124,159));
        lblSubtitulo.setText("Ingresos realmente recibidos para la fecha seleccionada.");
        add(lblSubtitulo); lblSubtitulo.setBounds(24, 58, 450, 22);

        lblFecha.setText("Fecha");
        add(lblFecha); lblFecha.setBounds(650, 27, 60, 18);
        add(txtFecha); txtFecha.setBounds(650, 48, 145, 34);
        btnActualizar.setText("Actualizar");
        add(btnActualizar); btnActualizar.setBounds(810, 48, 130, 34);

        javax.swing.JPanel[] paneles = {
            pnlEfectivo,pnlTransferencias,pnlCreditos,
            pnlTarjetas,pnlVentasRapidas,pnlTotal
        };
        for(javax.swing.JPanel p:paneles){p.setBackground(Color.WHITE);p.setLayout(null);add(p);}

        lblEfectivoTitulo.setText("Ventas en efectivo");
        lblEfectivoValor.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblEfectivoValor.setForeground(new Color(24,50,87));
        lblEfectivoValor.setText("L 0.00");
        pnlEfectivo.add(lblEfectivoTitulo);
        lblEfectivoTitulo.setBounds(14,18,150,20);
        pnlEfectivo.add(lblEfectivoValor);
        lblEfectivoValor.setBounds(14,48,150,38);
        pnlEfectivo.setBounds(24,120,160,105);

        lblTransferenciasTitulo.setText("Transferencias");
        lblTransferenciasValor.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblTransferenciasValor.setForeground(new Color(24,50,87));
        lblTransferenciasValor.setText("L 0.00");
        pnlTransferencias.add(lblTransferenciasTitulo);
        lblTransferenciasTitulo.setBounds(14,18,150,20);
        pnlTransferencias.add(lblTransferenciasValor);
        lblTransferenciasValor.setBounds(14,48,150,38);
        pnlTransferencias.setBounds(194,120,160,105);

        lblCreditosTitulo.setText("Cobros de crédito");
        lblCreditosValor.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblCreditosValor.setForeground(new Color(24,50,87));
        lblCreditosValor.setText("L 0.00");
        pnlCreditos.add(lblCreditosTitulo);
        lblCreditosTitulo.setBounds(14,18,150,20);
        pnlCreditos.add(lblCreditosValor);
        lblCreditosValor.setBounds(14,48,150,38);
        pnlCreditos.setBounds(364,120,160,105);

        lblTarjetasTitulo.setText("Ventas por tarjeta");
        lblTarjetasValor.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblTarjetasValor.setForeground(new Color(24,50,87));
        lblTarjetasValor.setText("L 0.00");
        pnlTarjetas.add(lblTarjetasTitulo);
        lblTarjetasTitulo.setBounds(14,18,150,20);
        pnlTarjetas.add(lblTarjetasValor);
        lblTarjetasValor.setBounds(14,48,150,38);
        pnlTarjetas.setBounds(534,120,160,105);

        lblVentasRapidasTitulo.setText("Ventas rápidas");
        lblVentasRapidasValor.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblVentasRapidasValor.setForeground(new Color(24,50,87));
        lblVentasRapidasValor.setText("L 0.00");
        pnlVentasRapidas.add(lblVentasRapidasTitulo);
        lblVentasRapidasTitulo.setBounds(14,18,150,20);
        pnlVentasRapidas.add(lblVentasRapidasValor);
        lblVentasRapidasValor.setBounds(14,48,150,38);
        pnlVentasRapidas.setBounds(704,120,160,105);

        lblTotalTitulo.setText("Total ingresado");
        lblTotalValor.setFont(new Font("Segoe UI",Font.BOLD,22));
        lblTotalValor.setForeground(new Color(24,50,87));
        lblTotalValor.setText("L 0.00");
        pnlTotal.add(lblTotalTitulo);
        lblTotalTitulo.setBounds(14,18,150,20);
        pnlTotal.add(lblTotalValor);
        lblTotalValor.setBounds(14,48,150,38);
        pnlTotal.setBounds(874,120,160,105);

        lblNota.setForeground(new Color(79,109,151));
        lblNota.setText("El cierre diario cuenta solo dinero recibido: los créditos suman abonos, no el valor total pendiente. Las ventas rápidas cuentan al registrarse.");
        add(lblNota);lblNota.setBounds(24,255,980,24);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JLabel lblEfectivoTitulo;
    private javax.swing.JLabel lblEfectivoValor;
    private javax.swing.JLabel lblCreditosTitulo;
    private javax.swing.JLabel lblCreditosValor;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblNota;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTarjetasTitulo;
    private javax.swing.JLabel lblTarjetasValor;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JLabel lblTransferenciasTitulo;
    private javax.swing.JLabel lblTransferenciasValor;
    private javax.swing.JLabel lblVentasRapidasTitulo;
    private javax.swing.JLabel lblVentasRapidasValor;
    private javax.swing.JPanel pnlCreditos;
    private javax.swing.JPanel pnlEfectivo;
    private javax.swing.JPanel pnlTarjetas;
    private javax.swing.JPanel pnlTotal;
    private javax.swing.JPanel pnlTransferencias;
    private javax.swing.JPanel pnlVentasRapidas;
    private javax.swing.JTextField txtFecha;
    // End of variables declaration//GEN-END:variables
}
