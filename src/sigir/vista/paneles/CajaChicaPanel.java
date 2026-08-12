package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.CajaChicaControlador;
import sigir.modelo.CajaChicaResumen;
import sigir.modelo.MovimientoCajaChica;
import sigir.util.CampoSeleccionUtil;
import sigir.util.SelectorFechaUtil;

public class CajaChicaPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NumberFormat moneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "HN"));

    private final CajaChicaControlador controlador;
    private boolean iniciado;
    private boolean procesando;
    private boolean esDueno;

    private BigDecimal saldoActual = BigDecimal.ZERO;
    private BigDecimal reposicionSugerida = BigDecimal.ZERO;

    public CajaChicaPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();
        controlador = new CajaChicaControlador(this);
        configurarEventos();
    }

    public void activar() {
        if (!iniciado) {
            iniciado = true;
            controlador.iniciarAsync();
            return;
        }
        controlador.recargarAsync();
    }

    public void recargar() {
        controlador.recargarAsync();
    }

    private void configurarComponentes() {
        moneda.setMinimumFractionDigits(2);
        moneda.setMaximumFractionDigits(2);

        cmbCategoria.setModel(new DefaultComboBoxModel<>(new String[]{
            "PAPELERIA",
            "TRANSPORTE",
            "COMBUSTIBLE",
            "ALIMENTACION",
            "LIMPIEZA",
            "MANTENIMIENTO",
            "SERVICIOS",
            "OTROS"
        }));

        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);

        txtDesde.setText(lunes.format(FECHA));
        txtHasta.setText(hoy.format(FECHA));
        txtMonto.setText("0.00");
        txtSaldoFisico.setText("0.00");

        SelectorFechaUtil.instalar(txtDesde, false);
        SelectorFechaUtil.instalar(txtHasta, false);
        CampoSeleccionUtil.seleccionarTodoAlEnfocar(
                txtMonto,
                txtSaldoFisico,
                txtComprobante
        );

        tblMovimientos.setAutoCreateRowSorter(true);
        tblMovimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMovimientos.setFillsViewportHeight(true);
    }

    private void configurarEventos() {
        btnRegistrar.addActionListener(
                e -> controlador.registrarMovimiento()
        );

        btnReponer.addActionListener(
                e -> controlador.reponerFondo()
        );

        btnArqueo.addActionListener(
                e -> controlador.registrarArqueo()
        );

        btnActualizar.addActionListener(
                e -> controlador.recargarAsync()
        );

        cmbTipo.addActionListener(e -> actualizarTipoMovimiento());
    }

    private void actualizarTipoMovimiento() {
        String tipo = getTipoMovimiento();
        boolean egreso = "EGRESO".equals(tipo);
        cmbCategoria.setEnabled(egreso);
        lblCategoria.setEnabled(egreso);

        if (!egreso) {
            cmbCategoria.setSelectedItem("OTROS");
        }
    }

    public void configurarPermisos(boolean dueno) {
        this.esDueno = dueno;

        if (dueno) {
            cmbTipo.setModel(new DefaultComboBoxModel<>(new String[]{
                "EGRESO",
                "AJUSTE_ENTRADA",
                "AJUSTE_SALIDA"
            }));
        } else {
            cmbTipo.setModel(new DefaultComboBoxModel<>(new String[]{
                "EGRESO"
            }));
        }

        btnReponer.setVisible(dueno);
        btnArqueo.setVisible(dueno);
        txtSaldoFisico.setVisible(dueno);
        lblSaldoFisico.setVisible(dueno);
        lblArqueoAyuda.setVisible(dueno);
        actualizarTipoMovimiento();
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color verde = new Color(34, 155, 85);
        Color texto = new Color(24, 50, 87);

        javax.swing.JPanel[] paneles = {
            pnlFondo,
            pnlSaldo,
            pnlGastado,
            pnlMovimientosSemana,
            pnlRegistro,
            pnlControl,
            pnlHistorial
        };

        for (javax.swing.JPanel p : paneles) {
            p.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borde),
                            BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    )
            );
        }

        lblSaldoValor.setForeground(verde);
        lblGastadoValor.setForeground(new Color(196, 74, 74));
        lblFondoValor.setForeground(texto);
        lblMovimientosValor.setForeground(azul);

        btnRegistrar.setBackground(azul);
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setBorderPainted(false);

        btnReponer.setBackground(verde);
        btnReponer.setForeground(Color.WHITE);
        btnReponer.setBorderPainted(false);

        for (javax.swing.JButton b : new javax.swing.JButton[]{
            btnRegistrar,
            btnReponer,
            btnArqueo,
            btnActualizar
        }) {
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        estilizarTabla();
    }

    private void estilizarTabla() {
        tblMovimientos.setRowHeight(36);
        tblMovimientos.setShowVerticalLines(false);
        tblMovimientos.setGridColor(new Color(232, 237, 243));
        tblMovimientos.setSelectionBackground(new Color(229, 239, 252));
        tblMovimientos.setSelectionForeground(new Color(24, 50, 87));

        JTableHeader h = tblMovimientos.getTableHeader();
        h.setBackground(new Color(248, 250, 253));
        h.setForeground(new Color(34, 59, 94));
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setReorderingAllowed(false);
    }

    public String getTipoMovimiento() {
        Object valor = cmbTipo.getSelectedItem();
        return valor == null ? "" : valor.toString();
    }

    public String getCategoria() {
        Object valor = cmbCategoria.getSelectedItem();
        return valor == null ? null : valor.toString();
    }

    public String getConcepto() {
        return txtConcepto.getText().trim();
    }

    public BigDecimal getMonto() {
        return leerMoneda(txtMonto.getText(), "Escribe un monto válido.");
    }

    public BigDecimal getSaldoFisico() {
        return leerMoneda(
                txtSaldoFisico.getText(),
                "Escribe una cantidad válida para el arqueo."
        );
    }

    public String getComprobante() {
        String valor = txtComprobante.getText().trim();
        return valor.isBlank() ? null : valor;
    }

    public String getObservaciones() {
        String valor = txtObservaciones.getText().trim();
        return valor.isBlank() ? null : valor;
    }

    public LocalDate getFechaDesde() {
        return parseFecha(txtDesde.getText(), "fecha inicial");
    }

    public LocalDate getFechaHasta() {
        return parseFecha(txtHasta.getText(), "fecha final");
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public BigDecimal getReposicionSugerida() {
        return reposicionSugerida;
    }

    public String formatearMoneda(BigDecimal valor) {
        return moneda.format(valor == null ? BigDecimal.ZERO : valor);
    }

    public void mostrarResumen(CajaChicaResumen resumen) {
        saldoActual = resumen.getSaldoDisponible();
        reposicionSugerida = resumen.getReposicionSugerida();

        lblFondoValor.setText(formatearMoneda(resumen.getFondoMaximo()));
        lblSaldoValor.setText(formatearMoneda(saldoActual));
        lblGastadoValor.setText(formatearMoneda(resumen.getGastadoSemana()));
        lblMovimientosValor.setText(String.valueOf(resumen.getMovimientosSemana()));

        lblSemana.setText(
                "Semana: "
                + resumen.getInicioSemana().format(FECHA)
                + " al "
                + resumen.getFinSemana().format(FECHA)
        );

        lblReposicion.setText(
                reposicionSugerida.signum() > 0
                        ? "Reposición sugerida: " + formatearMoneda(reposicionSugerida)
                        : "Fondo completo: no requiere reposición"
        );

        txtSaldoFisico.setText(
                saldoActual.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
    }

    public void mostrarMovimientos(List<MovimientoCajaChica> movimientos) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                    "Fecha",
                    "Tipo",
                    "Categoría",
                    "Concepto",
                    "Monto",
                    "Saldo",
                    "Usuario",
                    "Comprobante"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (MovimientoCajaChica m : movimientos) {
            modelo.addRow(new Object[]{
                m.getFechaMovimiento() == null
                        ? ""
                        : m.getFechaMovimiento().format(FECHA_HORA),
                m.getTipo().replace('_', ' '),
                m.getCategoria() == null ? "—" : m.getCategoria(),
                m.getConcepto(),
                formatearMoneda(m.getMonto()),
                formatearMoneda(m.getSaldoPosterior()),
                m.getNombreUsuario(),
                m.getComprobante() == null ? "" : m.getComprobante()
            });
        }

        tblMovimientos.setModel(modelo);
        lblCantidad.setText(
                movimientos.size() == 1
                        ? "1 movimiento mostrado"
                        : movimientos.size() + " movimientos mostrados"
        );
        estilizarTabla();
    }

    public void limpiarMovimiento() {
        cmbTipo.setSelectedIndex(0);
        cmbCategoria.setSelectedItem("PAPELERIA");
        txtConcepto.setText("");
        txtMonto.setText("0.00");
        txtComprobante.setText("");
        txtObservaciones.setText("");
        actualizarTipoMovimiento();
    }

    public void establecerProcesando(boolean valor) {
        procesando = valor;
        btnRegistrar.setEnabled(!valor);
        btnActualizar.setEnabled(!valor);
        btnReponer.setEnabled(!valor && esDueno);
        btnArqueo.setEnabled(!valor && esDueno);
        btnRegistrar.setText(valor ? "Procesando..." : "Registrar movimiento");
    }

    private BigDecimal leerMoneda(String texto, String mensaje) {
        try {
            return new BigDecimal(
                    texto.trim().replace("L", "").replace(",", "")
            ).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private LocalDate parseFecha(String texto, String nombre) {
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "La " + nombre + " debe tener formato dd/MM/yyyy."
            );
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlFondo = new javax.swing.JPanel();
        lblFondoTitulo = new javax.swing.JLabel();
        lblFondoValor = new javax.swing.JLabel();
        pnlSaldo = new javax.swing.JPanel();
        lblSaldoTitulo = new javax.swing.JLabel();
        lblSaldoValor = new javax.swing.JLabel();
        pnlGastado = new javax.swing.JPanel();
        lblGastadoTitulo = new javax.swing.JLabel();
        lblGastadoValor = new javax.swing.JLabel();
        pnlMovimientosSemana = new javax.swing.JPanel();
        lblMovimientosTitulo = new javax.swing.JLabel();
        lblMovimientosValor = new javax.swing.JLabel();
        pnlRegistro = new javax.swing.JPanel();
        lblRegistroTitulo = new javax.swing.JLabel();
        lblTipo = new javax.swing.JLabel();
        cmbTipo = new javax.swing.JComboBox<>();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        lblConcepto = new javax.swing.JLabel();
        txtConcepto = new javax.swing.JTextField();
        lblMonto = new javax.swing.JLabel();
        txtMonto = new javax.swing.JTextField();
        lblComprobante = new javax.swing.JLabel();
        txtComprobante = new javax.swing.JTextField();
        lblObservaciones = new javax.swing.JLabel();
        scrollObservaciones = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        btnRegistrar = new javax.swing.JButton();
        pnlControl = new javax.swing.JPanel();
        lblControlTitulo = new javax.swing.JLabel();
        lblSemana = new javax.swing.JLabel();
        lblReposicion = new javax.swing.JLabel();
        btnReponer = new javax.swing.JButton();
        lblSaldoFisico = new javax.swing.JLabel();
        txtSaldoFisico = new javax.swing.JTextField();
        btnArqueo = new javax.swing.JButton();
        lblArqueoAyuda = new javax.swing.JLabel();
        pnlHistorial = new javax.swing.JPanel();
        lblHistorialTitulo = new javax.swing.JLabel();
        lblDesde = new javax.swing.JLabel();
        txtDesde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        txtHasta = new javax.swing.JTextField();
        btnActualizar = new javax.swing.JButton();
        lblCantidad = new javax.swing.JLabel();
        scrollMovimientos = new javax.swing.JScrollPane();
        tblMovimientos = new javax.swing.JTable();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1140, 750));
        setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Caja chica");
        add(lblTitulo);
        lblTitulo.setBounds(20, 10, 300, 40);

        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Control semanal del fondo fijo para gastos menores del negocio.");
        add(lblSubtitulo);
        lblSubtitulo.setBounds(20, 48, 720, 24);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setLayout(null);
        lblFondoTitulo.setText("Fondo máximo");
        pnlFondo.add(lblFondoTitulo);
        lblFondoTitulo.setBounds(14, 12, 150, 20);
        lblFondoValor.setFont(new java.awt.Font("Segoe UI", 1, 23));
        lblFondoValor.setText("L 2,500.00");
        pnlFondo.add(lblFondoValor);
        lblFondoValor.setBounds(14, 38, 210, 34);
        add(pnlFondo);
        pnlFondo.setBounds(20, 78, 250, 82);

        pnlSaldo.setBackground(new java.awt.Color(255, 255, 255));
        pnlSaldo.setLayout(null);
        lblSaldoTitulo.setText("Saldo disponible");
        pnlSaldo.add(lblSaldoTitulo);
        lblSaldoTitulo.setBounds(14, 12, 150, 20);
        lblSaldoValor.setFont(new java.awt.Font("Segoe UI", 1, 23));
        lblSaldoValor.setText("L 0.00");
        pnlSaldo.add(lblSaldoValor);
        lblSaldoValor.setBounds(14, 38, 210, 34);
        add(pnlSaldo);
        pnlSaldo.setBounds(282, 78, 250, 82);

        pnlGastado.setBackground(new java.awt.Color(255, 255, 255));
        pnlGastado.setLayout(null);
        lblGastadoTitulo.setText("Gastado esta semana");
        pnlGastado.add(lblGastadoTitulo);
        lblGastadoTitulo.setBounds(14, 12, 180, 20);
        lblGastadoValor.setFont(new java.awt.Font("Segoe UI", 1, 23));
        lblGastadoValor.setText("L 0.00");
        pnlGastado.add(lblGastadoValor);
        lblGastadoValor.setBounds(14, 38, 210, 34);
        add(pnlGastado);
        pnlGastado.setBounds(544, 78, 250, 82);

        pnlMovimientosSemana.setBackground(new java.awt.Color(255, 255, 255));
        pnlMovimientosSemana.setLayout(null);
        lblMovimientosTitulo.setText("Movimientos de la semana");
        pnlMovimientosSemana.add(lblMovimientosTitulo);
        lblMovimientosTitulo.setBounds(14, 12, 200, 20);
        lblMovimientosValor.setFont(new java.awt.Font("Segoe UI", 1, 23));
        lblMovimientosValor.setText("0");
        pnlMovimientosSemana.add(lblMovimientosValor);
        lblMovimientosValor.setBounds(14, 38, 210, 34);
        add(pnlMovimientosSemana);
        pnlMovimientosSemana.setBounds(806, 78, 300, 82);

        pnlRegistro.setBackground(new java.awt.Color(255, 255, 255));
        pnlRegistro.setLayout(null);
        lblRegistroTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblRegistroTitulo.setText("Registrar movimiento");
        pnlRegistro.add(lblRegistroTitulo);
        lblRegistroTitulo.setBounds(16, 10, 250, 26);
        lblTipo.setText("Tipo");
        pnlRegistro.add(lblTipo);
        lblTipo.setBounds(16, 46, 100, 18);
        pnlRegistro.add(cmbTipo);
        cmbTipo.setBounds(16, 66, 145, 34);
        lblCategoria.setText("Categoría");
        pnlRegistro.add(lblCategoria);
        lblCategoria.setBounds(173, 46, 100, 18);
        pnlRegistro.add(cmbCategoria);
        cmbCategoria.setBounds(173, 66, 155, 34);
        lblConcepto.setText("Concepto / descripción");
        pnlRegistro.add(lblConcepto);
        lblConcepto.setBounds(16, 112, 170, 18);
        pnlRegistro.add(txtConcepto);
        txtConcepto.setBounds(16, 132, 312, 34);
        lblMonto.setText("Monto");
        pnlRegistro.add(lblMonto);
        lblMonto.setBounds(16, 178, 100, 18);
        pnlRegistro.add(txtMonto);
        txtMonto.setBounds(16, 198, 145, 34);
        lblComprobante.setText("Comprobante");
        pnlRegistro.add(lblComprobante);
        lblComprobante.setBounds(173, 178, 110, 18);
        pnlRegistro.add(txtComprobante);
        txtComprobante.setBounds(173, 198, 155, 34);
        lblObservaciones.setText("Observaciones");
        pnlRegistro.add(lblObservaciones);
        lblObservaciones.setBounds(16, 244, 120, 18);
        txtObservaciones.setColumns(20);
        txtObservaciones.setRows(4);
        scrollObservaciones.setViewportView(txtObservaciones);
        pnlRegistro.add(scrollObservaciones);
        scrollObservaciones.setBounds(16, 264, 312, 92);
        btnRegistrar.setText("Registrar movimiento");
        pnlRegistro.add(btnRegistrar);
        btnRegistrar.setBounds(16, 374, 312, 40);
        add(pnlRegistro);
        pnlRegistro.setBounds(20, 174, 350, 434);

        pnlControl.setBackground(new java.awt.Color(255, 255, 255));
        pnlControl.setLayout(null);
        lblControlTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblControlTitulo.setText("Control semanal y arqueo");
        pnlControl.add(lblControlTitulo);
        lblControlTitulo.setBounds(16, 8, 260, 26);
        lblSemana.setText("Semana: --/--/---- al --/--/----");
        pnlControl.add(lblSemana);
        lblSemana.setBounds(16, 40, 300, 22);
        lblReposicion.setForeground(new java.awt.Color(98, 124, 159));
        lblReposicion.setText("Reposición sugerida: L 0.00");
        pnlControl.add(lblReposicion);
        lblReposicion.setBounds(16, 66, 280, 22);
        btnReponer.setText("Reponer hasta L 2,500");
        pnlControl.add(btnReponer);
        btnReponer.setBounds(320, 42, 190, 40);
        lblSaldoFisico.setText("Dinero contado");
        pnlControl.add(lblSaldoFisico);
        lblSaldoFisico.setBounds(530, 18, 120, 18);
        pnlControl.add(txtSaldoFisico);
        txtSaldoFisico.setBounds(530, 40, 130, 34);
        btnArqueo.setText("Guardar arqueo");
        pnlControl.add(btnArqueo);
        btnArqueo.setBounds(672, 40, 135, 34);
        lblArqueoAyuda.setForeground(new java.awt.Color(98, 124, 159));
        lblArqueoAyuda.setText("El arqueo compara el saldo de SIGIR con el dinero físico.");
        pnlControl.add(lblArqueoAyuda);
        lblArqueoAyuda.setBounds(530, 78, 330, 20);
        add(pnlControl);
        pnlControl.setBounds(390, 174, 716, 112);

        pnlHistorial.setBackground(new java.awt.Color(255, 255, 255));
        pnlHistorial.setLayout(null);
        lblHistorialTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblHistorialTitulo.setText("Movimientos de caja chica");
        pnlHistorial.add(lblHistorialTitulo);
        lblHistorialTitulo.setBounds(16, 8, 260, 26);
        lblDesde.setText("Desde");
        pnlHistorial.add(lblDesde);
        lblDesde.setBounds(306, 10, 60, 18);
        pnlHistorial.add(txtDesde);
        txtDesde.setBounds(350, 6, 120, 30);
        lblHasta.setText("Hasta");
        pnlHistorial.add(lblHasta);
        lblHasta.setBounds(484, 10, 60, 18);
        pnlHistorial.add(txtHasta);
        txtHasta.setBounds(526, 6, 120, 30);
        btnActualizar.setText("Actualizar");
        pnlHistorial.add(btnActualizar);
        btnActualizar.setBounds(658, 6, 100, 30);
        lblCantidad.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCantidad.setText("0 movimientos mostrados");
        pnlHistorial.add(lblCantidad);
        lblCantidad.setBounds(770, 8, 250, 24);
        tblMovimientos.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"Fecha", "Tipo", "Categoría", "Concepto", "Monto", "Saldo", "Usuario", "Comprobante"}
        ));
        scrollMovimientos.setViewportView(tblMovimientos);
        pnlHistorial.add(scrollMovimientos);
        scrollMovimientos.setBounds(0, 46, 716, 258);
        add(pnlHistorial);
        pnlHistorial.setBounds(390, 298, 716, 310);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnArqueo;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnReponer;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JLabel lblArqueoAyuda;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblComprobante;
    private javax.swing.JLabel lblConcepto;
    private javax.swing.JLabel lblControlTitulo;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblFondoTitulo;
    private javax.swing.JLabel lblFondoValor;
    private javax.swing.JLabel lblGastadoTitulo;
    private javax.swing.JLabel lblGastadoValor;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblHistorialTitulo;
    private javax.swing.JLabel lblMonto;
    private javax.swing.JLabel lblMovimientosTitulo;
    private javax.swing.JLabel lblMovimientosValor;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblRegistroTitulo;
    private javax.swing.JLabel lblReposicion;
    private javax.swing.JLabel lblSaldoFisico;
    private javax.swing.JLabel lblSaldoTitulo;
    private javax.swing.JLabel lblSaldoValor;
    private javax.swing.JLabel lblSemana;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlControl;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlGastado;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JPanel pnlMovimientosSemana;
    private javax.swing.JPanel pnlRegistro;
    private javax.swing.JPanel pnlSaldo;
    private javax.swing.JScrollPane scrollMovimientos;
    private javax.swing.JScrollPane scrollObservaciones;
    private javax.swing.JTable tblMovimientos;
    private javax.swing.JTextField txtComprobante;
    private javax.swing.JTextField txtConcepto;
    private javax.swing.JTextField txtDesde;
    private javax.swing.JTextField txtHasta;
    private javax.swing.JTextField txtMonto;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtSaldoFisico;
    // End of variables declaration//GEN-END:variables
}
