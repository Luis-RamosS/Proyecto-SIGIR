package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.CreditoControlador;
import sigir.modelo.AbonoCredito;
import sigir.modelo.Credito;
import sigir.util.FiltroTiempoReal;
import sigir.util.CampoSeleccionUtil;
import sigir.util.SelectorFechaUtil;

public class CreditosPanel extends javax.swing.JPanel {

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "HN"));

    private final CreditoControlador controlador;
    private boolean iniciado;
    private boolean actualizandoControles;

    public CreditosPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();

        controlador = new CreditoControlador(this);

        configurarEventos();

        FiltroTiempoReal.activar(
                txtBuscarCredito,
                controlador::buscarCreditos
        );

        FiltroTiempoReal.activar(
                txtBuscarAbono,
                controlador::buscarAbonos
        );

    }

    public void activar() {
        if (!iniciado) {
            iniciado = true;
            controlador.iniciarAsync();
            return;
        }

        controlador.recargarSiNecesario();
    }

    public void recargar() {
        controlador.recargarAsync();
    }

    private void configurarComponentes() {
        formatoMoneda.setMinimumFractionDigits(2);
        formatoMoneda.setMaximumFractionDigits(2);

        cmbEstadoCredito.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "PENDIENTE",
                            "VENCIDO",
                            "PAGADO",
                            "ANULADO"
                        }
                )
        );

        cmbMetodoPago.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "EFECTIVO",
                            "TRANSFERENCIA",
                            "TARJETA"
                        }
                )
        );

        txtClienteAbono.setEditable(false);
        txtFacturaAbono.setEditable(false);
        txtSaldoAbono.setEditable(false);
        txtCuotaAbono.setEditable(false);

        txtClienteAbono.setFocusable(false);
        txtFacturaAbono.setFocusable(false);
        txtSaldoAbono.setFocusable(false);
        txtCuotaAbono.setFocusable(false);

        tblCreditos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblAbonos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblCreditos.setAutoCreateRowSorter(true);
        tblAbonos.setAutoCreateRowSorter(true);

        txtFechaAbono.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        SelectorFechaUtil.instalar(txtFechaAbono, false);
        CampoSeleccionUtil.seleccionarTodoAlEnfocar(txtMontoAbono, txtReferencia, txtBuscarCredito, txtBuscarAbono);
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color verde = new Color(34, 155, 85);
        Color texto = new Color(24, 50, 87);

        javax.swing.JPanel[] paneles = {
            pnlTarjetaPendientes,
            pnlTarjetaVencidos,
            pnlTarjetaPagados,
            pnlCreditos,
            pnlAbono,
            pnlHistorial
        };

        for (javax.swing.JPanel panel : paneles) {
            panel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borde),
                            BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    )
            );
        }

        btnRegistrarAbono.setBackground(verde);
        btnRegistrarAbono.setForeground(Color.WHITE);
        btnRegistrarAbono.setBorderPainted(false);
        btnRegistrarAbono.setFocusPainted(false);

        btnEstadoCuenta.setBackground(Color.WHITE);
        btnEstadoCuenta.setForeground(texto);
        btnEstadoCuenta.setBorder(
                BorderFactory.createLineBorder(borde)
        );

        for (javax.swing.JButton boton
                : new javax.swing.JButton[]{
                    btnRegistrarAbono,
                    btnEstadoCuenta
                }) {
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        estilizarTabla(tblCreditos);
        estilizarTabla(tblAbonos);
    }

    private void estilizarTabla(javax.swing.JTable tabla) {
        tabla.setRowHeight(38);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(232, 237, 243));
        tabla.setSelectionBackground(new Color(229, 239, 252));
        tabla.setSelectionForeground(new Color(24, 50, 87));

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(new Color(248, 250, 253));
        cabecera.setForeground(new Color(34, 59, 94));
        cabecera.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cabecera.setReorderingAllowed(false);
    }

    private void configurarEventos() {
        cmbEstadoCredito.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbEstadoCredito.getItemCount() > 0) {

                controlador.buscarCreditos();
            }
        });

        cmbCreditoAbono.addActionListener(e -> {
            if (!actualizandoControles) {
                controlador.seleccionarCredito();
            }
        });

        btnRegistrarAbono.addActionListener(
                e -> controlador.registrarAbono()
        );

        btnEstadoCuenta.addActionListener(
                e -> controlador.verEstadoCuenta()
        );
    }

    public String getTextoBusquedaCredito() {
        return txtBuscarCredito.getText().trim();
    }

    public String getEstadoCreditoFiltro() {
        Object valor = cmbEstadoCredito.getSelectedItem();
        return valor == null ? "TODOS" : valor.toString();
    }

    public void mostrarCreditos(List<Credito> creditos) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                    "Crédito",
                    "Cliente",
                    "Factura",
                    "Inicio",
                    "Vencimiento",
                    "Total",
                    "Saldo",
                    "Cuota",
                    "Estado"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter fecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Credito c : creditos) {
            modelo.addRow(new Object[]{
                "CR-" + String.format("%05d", c.getIdCredito()),
                c.getNombreCliente(),
                c.getNumeroFactura(),
                c.getFechaInicio() == null
                        ? ""
                        : c.getFechaInicio().format(fecha),
                c.getFechaVencimiento() == null
                        ? "Sin fecha"
                        : c.getFechaVencimiento().format(fecha),
                formatearMoneda(c.getTotalCredito()),
                formatearMoneda(c.getSaldoPendiente()),
                c.getMontoCuota() == null
                        ? "No definida"
                        : formatearMoneda(c.getMontoCuota()),
                c.getEstado()
            });
        }

        tblCreditos.setModel(modelo);
        estilizarTabla(tblCreditos);
    }

    public int getFilaCreditoSeleccionadaModelo() {
        int fila = tblCreditos.getSelectedRow();

        return fila < 0
                ? -1
                : tblCreditos.convertRowIndexToModel(fila);
    }

    public void cargarCreditosParaAbono(
            List<Credito> creditos) {

        Credito seleccionado =
                getCreditoSeleccionadoParaAbono();

        int idSeleccionado =
                seleccionado == null
                        ? 0
                        : seleccionado.getIdCredito();

        actualizandoControles = true;

        try {
            DefaultComboBoxModel<Credito> modelo =
                    new DefaultComboBoxModel<>();

            modelo.addElement(null);

            for (Credito credito : creditos) {
                modelo.addElement(credito);
            }

            cmbCreditoAbono.setModel(modelo);

            if (idSeleccionado > 0) {
                for (int i = 0;
                        i < cmbCreditoAbono.getItemCount();
                        i++) {

                    Credito credito =
                            cmbCreditoAbono.getItemAt(i);

                    if (credito != null
                            && credito.getIdCredito()
                            == idSeleccionado) {

                        cmbCreditoAbono.setSelectedIndex(i);
                        break;
                    }
                }
            }

        } finally {
            actualizandoControles = false;
        }

        controlador.seleccionarCredito();
    }

    public Credito getCreditoSeleccionadoParaAbono() {
        Object valor = cmbCreditoAbono.getSelectedItem();

        return valor instanceof Credito credito
                ? credito
                : null;
    }

    public void mostrarDatosCredito(Credito credito) {
        if (credito == null) {
            txtClienteAbono.setText("");
            txtFacturaAbono.setText("");
            txtSaldoAbono.setText("0.00");
            txtCuotaAbono.setText("0.00");
            return;
        }

        txtClienteAbono.setText(credito.getNombreCliente());
        txtFacturaAbono.setText(credito.getNumeroFactura());
        txtSaldoAbono.setText(
                credito.getSaldoPendiente()
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString()
        );

        txtCuotaAbono.setText(
                credito.getMontoCuota() == null
                        ? "0.00"
                        : credito.getMontoCuota()
                                .setScale(2, RoundingMode.HALF_UP)
                                .toPlainString()
        );

        if (credito.getMontoCuota() != null) {
            txtMontoAbono.setText(
                    credito.getMontoCuota()
                            .min(credito.getSaldoPendiente())
                            .setScale(2, RoundingMode.HALF_UP)
                            .toPlainString()
            );
        }
    }

    public BigDecimal getMontoAbono() {
        String valor = txtMontoAbono.getText()
                .trim()
                .replace("L", "")
                .replace(",", "");

        try {
            return new BigDecimal(valor)
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Escribe un monto de abono válido."
            );
        }
    }

    public LocalDate getFechaAbono() {
        try {
            return LocalDate.parse(txtFechaAbono.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Selecciona una fecha válida para el abono.");
        }
    }

    public String getMetodoPagoAbono() {
        Object valor = cmbMetodoPago.getSelectedItem();
        return valor == null ? "" : valor.toString();
    }

    public String getReferenciaAbono() {
        String valor = txtReferencia.getText().trim();
        return valor.isBlank() ? null : valor;
    }

    public String getObservacionesAbono() {
        String valor = txtObservacionesAbono.getText().trim();
        return valor.isBlank() ? null : valor;
    }

    public void limpiarFormularioAbono() {
        cmbCreditoAbono.setSelectedIndex(0);
        txtClienteAbono.setText("");
        txtFacturaAbono.setText("");
        txtSaldoAbono.setText("0.00");
        txtCuotaAbono.setText("0.00");
        txtMontoAbono.setText("0.00");
        txtFechaAbono.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtReferencia.setText("");
        txtObservacionesAbono.setText("");
    }

    public void establecerProcesando(boolean procesando) {
        btnRegistrarAbono.setEnabled(!procesando);
        btnRegistrarAbono.setText(
                procesando
                        ? "Registrando..."
                        : "Registrar abono"
        );
    }

    public String getTextoBusquedaAbono() {
        return txtBuscarAbono.getText().trim();
    }

    public void mostrarAbonos(List<AbonoCredito> abonos) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                    "Abono",
                    "Crédito",
                    "Fecha",
                    "Cliente",
                    "Factura",
                    "Monto",
                    "Método",
                    "Referencia",
                    "Usuario"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter fecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (AbonoCredito a : abonos) {
            modelo.addRow(new Object[]{
                "AB-" + String.format("%05d", a.getIdAbono()),
                "CR-" + String.format("%05d", a.getIdCredito()),
                a.getFechaAbono() == null
                        ? ""
                        : a.getFechaAbono().format(fecha),
                a.getNombreCliente(),
                a.getNumeroFactura(),
                formatearMoneda(a.getMonto()),
                a.getMetodoPago(),
                a.getReferencia() == null
                        ? ""
                        : a.getReferencia(),
                a.getNombreUsuario()
            });
        }

        tblAbonos.setModel(modelo);
        estilizarTabla(tblAbonos);
    }

    public void actualizarIndicadores(
            int pendientes,
            int vencidos,
            int pagados) {

        lblPendientesValor.setText(
                String.valueOf(pendientes)
        );

        lblVencidosValor.setText(
                String.valueOf(vencidos)
        );

        lblPagadosValor.setText(
                String.valueOf(pagados)
        );
    }

    public void mostrarEstadoCuenta(Credito credito) {
        JTextArea area = new JTextArea(20, 48);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));

        String texto = """
                SIGIR - ESTADO DE CUENTA
                ============================================
                Crédito: CR-%05d
                Cliente: %s
                Identidad: %s
                Factura: %s
                Total del crédito: %s
                Saldo pendiente: %s
                Cuota: %s
                Estado: %s
                ============================================
                """.formatted(
                credito.getIdCredito(),
                credito.getNombreCliente(),
                credito.getNumeroIdentidad(),
                credito.getNumeroFactura(),
                formatearMoneda(credito.getTotalCredito()),
                formatearMoneda(credito.getSaldoPendiente()),
                credito.getMontoCuota() == null
                        ? "No definida"
                        : formatearMoneda(
                                credito.getMontoCuota()
                        ),
                credito.getEstado()
        );

        area.setText(texto);
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Estado de cuenta",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public String formatearMoneda(BigDecimal valor) {
        return formatoMoneda.format(
                valor == null
                        ? BigDecimal.ZERO
                        : valor
        );
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlTarjetaPendientes = new javax.swing.JPanel();
        lblPendientesTitulo = new javax.swing.JLabel();
        lblPendientesValor = new javax.swing.JLabel();
        pnlTarjetaVencidos = new javax.swing.JPanel();
        lblVencidosTitulo = new javax.swing.JLabel();
        lblVencidosValor = new javax.swing.JLabel();
        pnlTarjetaPagados = new javax.swing.JPanel();
        lblPagadosTitulo = new javax.swing.JLabel();
        lblPagadosValor = new javax.swing.JLabel();
        tabsCreditos = new javax.swing.JTabbedPane();
        pnlGestion = new javax.swing.JPanel();
        pnlCreditos = new javax.swing.JPanel();
        lblTituloCreditos = new javax.swing.JLabel();
        txtBuscarCredito = new javax.swing.JTextField();
        cmbEstadoCredito = new javax.swing.JComboBox<>();
        btnEstadoCuenta = new javax.swing.JButton();
        scrollCreditos = new javax.swing.JScrollPane();
        tblCreditos = new javax.swing.JTable();
        pnlAbono = new javax.swing.JPanel();
        lblTituloAbono = new javax.swing.JLabel();
        lblCredito = new javax.swing.JLabel();
        cmbCreditoAbono = new javax.swing.JComboBox<>();
        lblClienteAbono = new javax.swing.JLabel();
        txtClienteAbono = new javax.swing.JTextField();
        lblFacturaAbono = new javax.swing.JLabel();
        txtFacturaAbono = new javax.swing.JTextField();
        lblSaldoAbono = new javax.swing.JLabel();
        txtSaldoAbono = new javax.swing.JTextField();
        lblCuotaAbono = new javax.swing.JLabel();
        txtCuotaAbono = new javax.swing.JTextField();
        lblMontoAbono = new javax.swing.JLabel();
        txtMontoAbono = new javax.swing.JTextField();
        lblFechaAbono = new javax.swing.JLabel();
        txtFechaAbono = new javax.swing.JTextField();
        lblMetodoPago = new javax.swing.JLabel();
        cmbMetodoPago = new javax.swing.JComboBox<>();
        lblReferencia = new javax.swing.JLabel();
        txtReferencia = new javax.swing.JTextField();
        lblObservacionesAbono = new javax.swing.JLabel();
        scrollObservacionesAbono = new javax.swing.JScrollPane();
        txtObservacionesAbono = new javax.swing.JTextArea();
        btnRegistrarAbono = new javax.swing.JButton();
        pnlHistorial = new javax.swing.JPanel();
        lblTituloHistorial = new javax.swing.JLabel();
        txtBuscarAbono = new javax.swing.JTextField();
        scrollAbonos = new javax.swing.JScrollPane();
        tblAbonos = new javax.swing.JTable();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Gestión de Créditos y Abonos");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 520, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Administra los créditos otorgados y registra sus abonos.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 650, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 10, 1100, 76);

        pnlTarjetaPendientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaPendientes.setLayout(null);
        lblPendientesTitulo.setText("Créditos pendientes");
        pnlTarjetaPendientes.add(lblPendientesTitulo);
        lblPendientesTitulo.setBounds(18, 18, 180, 20);
        lblPendientesValor.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblPendientesValor.setText("0");
        pnlTarjetaPendientes.add(lblPendientesValor);
        lblPendientesValor.setBounds(18, 48, 120, 40);
        add(pnlTarjetaPendientes);
        pnlTarjetaPendientes.setBounds(28, 90, 330, 105);

        pnlTarjetaVencidos.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaVencidos.setLayout(null);
        lblVencidosTitulo.setText("Créditos vencidos");
        pnlTarjetaVencidos.add(lblVencidosTitulo);
        lblVencidosTitulo.setBounds(18, 18, 180, 20);
        lblVencidosValor.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblVencidosValor.setForeground(new java.awt.Color(192, 52, 52));
        lblVencidosValor.setText("0");
        pnlTarjetaVencidos.add(lblVencidosValor);
        lblVencidosValor.setBounds(18, 48, 120, 40);
        add(pnlTarjetaVencidos);
        pnlTarjetaVencidos.setBounds(372, 90, 330, 105);

        pnlTarjetaPagados.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaPagados.setLayout(null);
        lblPagadosTitulo.setText("Créditos pagados");
        pnlTarjetaPagados.add(lblPagadosTitulo);
        lblPagadosTitulo.setBounds(18, 18, 180, 20);
        lblPagadosValor.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblPagadosValor.setForeground(new java.awt.Color(34, 155, 85));
        lblPagadosValor.setText("0");
        pnlTarjetaPagados.add(lblPagadosValor);
        lblPagadosValor.setBounds(18, 48, 120, 40);
        add(pnlTarjetaPagados);
        pnlTarjetaPagados.setBounds(716, 90, 330, 105);

        pnlGestion.setBackground(new java.awt.Color(247, 249, 252));
        pnlGestion.setLayout(null);

        pnlCreditos.setBackground(new java.awt.Color(255, 255, 255));
        pnlCreditos.setLayout(null);
        lblTituloCreditos.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloCreditos.setText("Créditos registrados");
        pnlCreditos.add(lblTituloCreditos);
        lblTituloCreditos.setBounds(16, 8, 220, 26);
        pnlCreditos.add(txtBuscarCredito);
        txtBuscarCredito.setBounds(16, 42, 260, 34);
        pnlCreditos.add(cmbEstadoCredito);
        cmbEstadoCredito.setBounds(288, 42, 145, 34);
        btnEstadoCuenta.setText("Estado de cuenta");
        pnlCreditos.add(btnEstadoCuenta);
        btnEstadoCuenta.setBounds(445, 42, 150, 34);
        tblCreditos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Crédito", "Cliente", "Factura", "Inicio", "Vencimiento", "Total", "Saldo", "Cuota", "Estado"}
        ));
        scrollCreditos.setViewportView(tblCreditos);
        pnlCreditos.add(scrollCreditos);
        scrollCreditos.setBounds(0, 90, 690, 430);
        pnlGestion.add(pnlCreditos);
        pnlCreditos.setBounds(0, 8, 690, 530);

        pnlAbono.setBackground(new java.awt.Color(255, 255, 255));
        pnlAbono.setLayout(null);
        lblTituloAbono.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloAbono.setText("Registrar abono");
        pnlAbono.add(lblTituloAbono);
        lblTituloAbono.setBounds(16, 8, 190, 26);

        lblCredito.setText("Crédito");
        pnlAbono.add(lblCredito);
        lblCredito.setBounds(16, 42, 100, 18);
        pnlAbono.add(cmbCreditoAbono);
        cmbCreditoAbono.setBounds(16, 62, 310, 34);

        lblClienteAbono.setText("Cliente");
        pnlAbono.add(lblClienteAbono);
        lblClienteAbono.setBounds(16, 106, 100, 18);
        pnlAbono.add(txtClienteAbono);
        txtClienteAbono.setBounds(16, 126, 310, 34);

        lblFacturaAbono.setText("Factura");
        pnlAbono.add(lblFacturaAbono);
        lblFacturaAbono.setBounds(16, 170, 90, 18);
        pnlAbono.add(txtFacturaAbono);
        txtFacturaAbono.setBounds(16, 190, 145, 34);

        lblSaldoAbono.setText("Saldo pendiente");
        pnlAbono.add(lblSaldoAbono);
        lblSaldoAbono.setBounds(173, 170, 120, 18);
        pnlAbono.add(txtSaldoAbono);
        txtSaldoAbono.setBounds(173, 190, 153, 34);

        lblCuotaAbono.setText("Cuota sugerida");
        pnlAbono.add(lblCuotaAbono);
        lblCuotaAbono.setBounds(16, 234, 95, 18);
        pnlAbono.add(txtCuotaAbono);
        txtCuotaAbono.setBounds(16, 254, 94, 34);

        lblMontoAbono.setText("Monto del abono");
        pnlAbono.add(lblMontoAbono);
        lblMontoAbono.setBounds(118, 234, 100, 18);
        txtMontoAbono.setText("0.00");
        pnlAbono.add(txtMontoAbono);
        txtMontoAbono.setBounds(118, 254, 94, 34);

        lblFechaAbono.setText("Fecha del abono");
        pnlAbono.add(lblFechaAbono);
        lblFechaAbono.setBounds(220, 234, 106, 18);
        pnlAbono.add(txtFechaAbono);
        txtFechaAbono.setBounds(220, 254, 106, 34);

        lblMetodoPago.setText("Forma de pago");
        pnlAbono.add(lblMetodoPago);
        lblMetodoPago.setBounds(16, 298, 120, 18);
        pnlAbono.add(cmbMetodoPago);
        cmbMetodoPago.setBounds(16, 318, 145, 34);

        lblReferencia.setText("Referencia");
        pnlAbono.add(lblReferencia);
        lblReferencia.setBounds(173, 298, 100, 18);
        pnlAbono.add(txtReferencia);
        txtReferencia.setBounds(173, 318, 153, 34);

        lblObservacionesAbono.setText("Observaciones");
        pnlAbono.add(lblObservacionesAbono);
        lblObservacionesAbono.setBounds(16, 362, 120, 18);
        txtObservacionesAbono.setColumns(20);
        txtObservacionesAbono.setRows(5);
        scrollObservacionesAbono.setViewportView(txtObservacionesAbono);
        pnlAbono.add(scrollObservacionesAbono);
        scrollObservacionesAbono.setBounds(16, 382, 310, 78);

        btnRegistrarAbono.setText("Registrar abono");
        pnlAbono.add(btnRegistrarAbono);
        btnRegistrarAbono.setBounds(16, 474, 310, 40);

        pnlGestion.add(pnlAbono);
        pnlAbono.setBounds(704, 8, 345, 530);

        tabsCreditos.addTab("Gestión de créditos", pnlGestion);

        pnlHistorial.setBackground(new java.awt.Color(255, 255, 255));
        pnlHistorial.setLayout(null);
        lblTituloHistorial.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloHistorial.setText("Historial de abonos");
        pnlHistorial.add(lblTituloHistorial);
        lblTituloHistorial.setBounds(16, 10, 220, 26);
        pnlHistorial.add(txtBuscarAbono);
        txtBuscarAbono.setBounds(16, 44, 300, 34);
        tblAbonos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Abono", "Crédito", "Fecha", "Cliente", "Factura", "Monto", "Método", "Referencia", "Usuario"}
        ));
        scrollAbonos.setViewportView(tblAbonos);
        pnlHistorial.add(scrollAbonos);
        scrollAbonos.setBounds(0, 92, 1049, 445);
        tabsCreditos.addTab("Historial de abonos", pnlHistorial);

        add(tabsCreditos);
        tabsCreditos.setBounds(28, 205, 1070, 565);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEstadoCuenta;
    private javax.swing.JButton btnRegistrarAbono;
    private javax.swing.JComboBox<Credito> cmbCreditoAbono;
    private javax.swing.JComboBox<String> cmbEstadoCredito;
    private javax.swing.JComboBox<String> cmbMetodoPago;
    private javax.swing.JLabel lblClienteAbono;
    private javax.swing.JLabel lblCredito;
    private javax.swing.JLabel lblCuotaAbono;
    private javax.swing.JLabel lblFacturaAbono;
    private javax.swing.JLabel lblFechaAbono;
    private javax.swing.JLabel lblMetodoPago;
    private javax.swing.JLabel lblMontoAbono;
    private javax.swing.JLabel lblObservacionesAbono;
    private javax.swing.JLabel lblPagadosTitulo;
    private javax.swing.JLabel lblPagadosValor;
    private javax.swing.JLabel lblPendientesTitulo;
    private javax.swing.JLabel lblPendientesValor;
    private javax.swing.JLabel lblReferencia;
    private javax.swing.JLabel lblSaldoAbono;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloAbono;
    private javax.swing.JLabel lblTituloCreditos;
    private javax.swing.JLabel lblTituloHistorial;
    private javax.swing.JLabel lblVencidosTitulo;
    private javax.swing.JLabel lblVencidosValor;
    private javax.swing.JPanel pnlAbono;
    private javax.swing.JPanel pnlCreditos;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlGestion;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JPanel pnlTarjetaPagados;
    private javax.swing.JPanel pnlTarjetaPendientes;
    private javax.swing.JPanel pnlTarjetaVencidos;
    private javax.swing.JScrollPane scrollAbonos;
    private javax.swing.JScrollPane scrollCreditos;
    private javax.swing.JScrollPane scrollObservacionesAbono;
    private javax.swing.JTabbedPane tabsCreditos;
    private javax.swing.JTable tblAbonos;
    private javax.swing.JTable tblCreditos;
    private javax.swing.JTextField txtBuscarAbono;
    private javax.swing.JTextField txtBuscarCredito;
    private javax.swing.JTextField txtClienteAbono;
    private javax.swing.JTextField txtCuotaAbono;
    private javax.swing.JTextField txtFacturaAbono;
    private javax.swing.JTextField txtFechaAbono;
    private javax.swing.JTextField txtMontoAbono;
    private javax.swing.JTextArea txtObservacionesAbono;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JTextField txtSaldoAbono;
    // End of variables declaration//GEN-END:variables
}
