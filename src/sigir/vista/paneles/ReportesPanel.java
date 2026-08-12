package sigir.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import sigir.controlador.ReporteControlador;
import sigir.modelo.ReporteResultado;
import sigir.modelo.ResumenReportes;
import sigir.modelo.TipoReporte;
import sigir.modelo.UsuarioFiltro;
import sigir.util.FiltroTiempoReal;

public class ReportesPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    new Locale("es", "HN")
            );

    private final ReporteControlador controlador;

    private TableRowSorter<DefaultTableModel>
            ordenadorResultados;

    private boolean iniciado;
    private boolean actualizandoControles;
    private CajaChicaPanel cajaChicaPanel;
    private JTabbedPane tabsModuloReportes;

    public ReportesPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();

        controlador =
                new ReporteControlador(this);

        configurarEventos();

        FiltroTiempoReal.activar(
                txtBuscarResultados,
                this::filtrarResultadosLocales
        );

        instalarPestanasModulo();
    }

    public void activar() {
        if (!iniciado) {
            iniciado = true;
            controlador.iniciarAsync();
            cajaChicaPanel.activar();
            return;
        }

        controlador.recargarSiNecesario();
        cajaChicaPanel.recargar();
    }

    public void recargar() {
        controlador.recargarAsync();
        cajaChicaPanel.recargar();
    }

    private void instalarPestanasModulo() {
        Component[] componentesOriginales = getComponents();
        Dimension tamanoOriginal = getPreferredSize();

        JPanel pnlReportesGenerales = new JPanel(null);
        pnlReportesGenerales.setBackground(getBackground());
        pnlReportesGenerales.setPreferredSize(tamanoOriginal);

        for (Component componente : componentesOriginales) {
            Rectangle posicion = componente.getBounds();
            remove(componente);
            pnlReportesGenerales.add(componente);
            componente.setBounds(posicion);
        }

        JScrollPane scrollReportes = new JScrollPane(pnlReportesGenerales);
        scrollReportes.setBorder(null);
        scrollReportes.getVerticalScrollBar().setUnitIncrement(18);

        cajaChicaPanel = new CajaChicaPanel();
        JScrollPane scrollCaja = new JScrollPane(cajaChicaPanel);
        scrollCaja.setBorder(null);
        scrollCaja.getVerticalScrollBar().setUnitIncrement(18);

        tabsModuloReportes = new JTabbedPane();
        tabsModuloReportes.addTab("Reportes generales", scrollReportes);
        tabsModuloReportes.addTab("Caja chica", scrollCaja);

        setLayout(new BorderLayout());
        add(tabsModuloReportes, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void configurarComponentes() {
        formatoMoneda.setMinimumFractionDigits(2);
        formatoMoneda.setMaximumFractionDigits(2);

        LocalDate hoy = LocalDate.now();

        txtFechaDesde.setText(
                hoy.withDayOfMonth(1)
                        .format(FORMATO_FECHA)
        );

        txtFechaHasta.setText(
                hoy.format(FORMATO_FECHA)
        );

        txtDescripcionReporte.setEditable(false);
        txtDescripcionReporte.setFocusable(false);
        txtDescripcionReporte.setBackground(
                new Color(247, 249, 252)
        );

        tblResultados.setAutoCreateRowSorter(false);
        tblResultados.setFillsViewportHeight(true);

        graficoReportes.setDatos(
                "Gráfico del reporte",
                List.of()
        );

        btnExportar.setEnabled(false);
        btnImprimir.setEnabled(false);
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        javax.swing.JPanel[] paneles = {
            pnlTarjetaVentas,
            pnlTarjetaCompras,
            pnlTarjetaStock,
            pnlTarjetaReparaciones,
            pnlFiltros,
            pnlResultados,
            pnlGrafico
        };

        for (javax.swing.JPanel panel : paneles) {
            panel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borde),
                            BorderFactory.createEmptyBorder(
                                    8, 8, 8, 8
                            )
                    )
            );
        }

        btnConsultar.setBackground(azul);
        btnConsultar.setForeground(Color.WHITE);
        btnConsultar.setBorderPainted(false);

        javax.swing.JButton[] secundarios = {
            btnExportar,
            btnImprimir
        };

        for (javax.swing.JButton boton : secundarios) {
            boton.setBackground(Color.WHITE);
            boton.setForeground(texto);
            boton.setBorder(
                    BorderFactory.createLineBorder(borde)
            );
        }

        for (javax.swing.JButton boton
                : new javax.swing.JButton[]{
                    btnConsultar,
                    btnExportar,
                    btnImprimir
                }) {

            boton.setFocusPainted(false);
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        lblVentasValor.setForeground(
                new Color(34, 155, 85)
        );

        lblComprasValor.setForeground(
                new Color(49, 105, 181)
        );

        lblStockValor.setForeground(
                new Color(216, 126, 25)
        );

        lblReparacionesValor.setForeground(
                new Color(122, 73, 196)
        );

        estilizarTabla();
    }

    private void estilizarTabla() {
        tblResultados.setRowHeight(38);
        tblResultados.setShowVerticalLines(false);
        tblResultados.setGridColor(
                new Color(232, 237, 243)
        );

        tblResultados.setSelectionBackground(
                new Color(229, 239, 252)
        );

        tblResultados.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera =
                tblResultados.getTableHeader();

        cabecera.setBackground(
                new Color(248, 250, 253)
        );

        cabecera.setForeground(
                new Color(34, 59, 94)
        );

        cabecera.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        cabecera.setReorderingAllowed(false);

        DefaultTableCellRenderer moneda =
                new DefaultTableCellRenderer() {
                    @Override
                    protected void setValue(
                            Object value) {

                        if (value instanceof BigDecimal numero) {
                            setText(
                                    formatoMoneda.format(numero)
                            );
                        } else {
                            super.setValue(value);
                        }
                    }
                };

        moneda.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        DefaultTableCellRenderer fecha =
                new DefaultTableCellRenderer() {
                    @Override
                    protected void setValue(
                            Object value) {

                        if (value instanceof LocalDate dia) {
                            setText(
                                    dia.format(FORMATO_FECHA)
                            );

                        } else if (value
                                instanceof LocalDateTime momento) {

                            setText(
                                    momento.format(
                                            FORMATO_FECHA_HORA
                                    )
                            );

                        } else {
                            super.setValue(value);
                        }
                    }
                };

        tblResultados.setDefaultRenderer(
                BigDecimal.class,
                moneda
        );

        tblResultados.setDefaultRenderer(
                LocalDate.class,
                fecha
        );

        tblResultados.setDefaultRenderer(
                LocalDateTime.class,
                fecha
        );
    }

    private void configurarEventos() {
        cmbTipoReporte.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbTipoReporte.getItemCount() > 0) {

                controlador.cambiarTipoReporte();
                controlador.consultarAsync();
            }
        });

        cmbEstado.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbEstado.isEnabled()
                    && cmbEstado.getItemCount() > 0) {

                controlador.consultarAsync();
            }
        });

        cmbUsuario.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbUsuario.isEnabled()
                    && cmbUsuario.getItemCount() > 0) {

                controlador.consultarAsync();
            }
        });

        txtFechaDesde.addActionListener(
                e -> consultarPorCambioDeFecha()
        );

        txtFechaHasta.addActionListener(
                e -> consultarPorCambioDeFecha()
        );

        FocusAdapter fechaFocus =
                new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                consultarPorCambioDeFecha();
            }
        };

        txtFechaDesde.addFocusListener(fechaFocus);
        txtFechaHasta.addFocusListener(fechaFocus);

        btnConsultar.addActionListener(
                e -> controlador.consultarAsync()
        );

        btnExportar.addActionListener(
                e -> controlador.exportar()
        );

        btnImprimir.addActionListener(
                e -> controlador.imprimir()
        );
    }

    private void consultarPorCambioDeFecha() {
        if (actualizandoControles) {
            return;
        }

        TipoReporte tipo =
                getTipoReporteSeleccionado();

        if (tipo != null
                && tipo.isUsaFechas()) {

            controlador.consultarAsync();
        }
    }

    public void cargarTiposReporte(
            List<TipoReporte> tipos) {

        TipoReporte seleccionado =
                getTipoReporteSeleccionado();

        actualizandoControles = true;

        try {
            cmbTipoReporte.setModel(
                    new DefaultComboBoxModel<>(
                            tipos.toArray(
                                    TipoReporte[]::new
                            )
                    )
            );

            if (seleccionado != null) {
                cmbTipoReporte.setSelectedItem(
                        seleccionado
                );
            }

        } finally {
            actualizandoControles = false;
        }
    }

    public void cargarUsuarios(
            List<UsuarioFiltro> usuarios) {

        Integer anterior =
                getIdUsuarioSeleccionado();

        actualizandoControles = true;

        try {
            DefaultComboBoxModel<UsuarioFiltro> modelo =
                    new DefaultComboBoxModel<>();

        UsuarioFiltro todos = new UsuarioFiltro();
        todos.setIdUsuario(0);
        todos.setNombreCompleto("TODOS");
        todos.setNombreUsuario("");

        modelo.addElement(todos);

        for (UsuarioFiltro usuario : usuarios) {
            modelo.addElement(usuario);
        }

        cmbUsuario.setModel(modelo);

        if (anterior == null) {
            return;
        }

            for (int i = 0;
                    i < modelo.getSize();
                    i++) {

                if (modelo.getElementAt(i)
                        .getIdUsuario() == anterior) {

                    cmbUsuario.setSelectedIndex(i);
                    break;
                }
            }

        } finally {
            actualizandoControles = false;
        }
    }

    public TipoReporte
            getTipoReporteSeleccionado() {

        Object valor =
                cmbTipoReporte.getSelectedItem();

        return valor instanceof TipoReporte tipo
                ? tipo
                : null;
    }

    public LocalDate getFechaDesde() {
        return convertirFecha(
                txtFechaDesde.getText(),
                "fecha inicial"
        );
    }

    public LocalDate getFechaHasta() {
        return convertirFecha(
                txtFechaHasta.getText(),
                "fecha final"
        );
    }

    public String getEstadoSeleccionado() {
        Object valor =
                cmbEstado.getSelectedItem();

        return valor == null
                ? "TODOS"
                : valor.toString();
    }

    public Integer getIdUsuarioSeleccionado() {
        Object valor =
                cmbUsuario.getSelectedItem();

        if (valor instanceof UsuarioFiltro usuario
                && usuario.getIdUsuario() > 0) {

            return usuario.getIdUsuario();
        }

        return null;
    }

    public void configurarFiltros(
            TipoReporte tipo) {

        actualizandoControles = true;

        try {
            txtDescripcionReporte.setText(
                    tipo.getDescripcion()
            );

            txtFechaDesde.setEnabled(
                    tipo.isUsaFechas()
            );

            txtFechaHasta.setEnabled(
                    tipo.isUsaFechas()
            );

            cmbEstado.setEnabled(
                    tipo.isUsaEstado()
            );

            cmbUsuario.setEnabled(
                    tipo.isUsaUsuario()
            );

            String[] estados = switch (tipo) {
                case VENTAS ->
                    new String[]{
                        "TODOS",
                        "COMPLETADA",
                        "ANULADA",
                        "PENDIENTE"
                    };

                case COMPRAS ->
                    new String[]{
                        "TODOS",
                        "REGISTRADA",
                        "ANULADA",
                        "PENDIENTE"
                    };

                case MOVIMIENTOS ->
                    new String[]{
                        "TODOS",
                        "ENTRADA_COMPRA",
                        "SALIDA_VENTA",
                        "SALIDA_REPARACION",
                        "DEVOLUCION_CLIENTE",
                        "AJUSTE_ENTRADA",
                        "AJUSTE_SALIDA"
                    };

                case CREDITOS ->
                    new String[]{
                        "TODOS",
                        "PENDIENTE",
                        "VENCIDO",
                        "PAGADO",
                        "ANULADO"
                    };

                case REPARACIONES ->
                    new String[]{
                        "TODOS",
                        "RECIBIDO",
                        "DIAGNOSTICO",
                        "EN_REPARACION",
                        "LISTO",
                        "ENTREGADO",
                        "CANCELADO"
                    };

                case CAJA_CHICA ->
                    new String[]{
                        "TODOS",
                        "APERTURA",
                        "EGRESO",
                        "REPOSICION",
                        "AJUSTE_ENTRADA",
                        "AJUSTE_SALIDA",
                        "ACTIVOS",
                        "ANULADOS"
                    };

                default ->
                    new String[]{"TODOS"};
            };

            cmbEstado.setModel(
                    new DefaultComboBoxModel<>(
                            estados
                    )
            );

        } finally {
            actualizandoControles = false;
        }
    }

    public void mostrarResumen(
            ResumenReportes resumen) {

        lblVentasValor.setText(
                formatoMoneda.format(
                        resumen.getVentasPeriodo()
                )
        );

        lblComprasValor.setText(
                formatoMoneda.format(
                        resumen.getComprasPeriodo()
                )
        );

        lblStockValor.setText(
                String.valueOf(
                        resumen.getProductosStockBajo()
                )
        );

        lblReparacionesValor.setText(
                String.valueOf(
                        resumen.getReparacionesPendientes()
                )
        );
    }

    public void mostrarResultado(
            ReporteResultado reporte) {

        ModeloReporte modelo =
                new ModeloReporte(
                        reporte.getColumnas()
                                .toArray(String[]::new)
                );

        for (Object[] fila : reporte.getFilas()) {
            modelo.addRow(fila);
        }

        tblResultados.setModel(modelo);

        ordenadorResultados =
                new TableRowSorter<>(modelo);

        tblResultados.setRowSorter(
                ordenadorResultados
        );

        estilizarTabla();

        lblTituloResultados.setText(
                reporte.getTitulo()
        );

        lblDescripcionResultados.setText(
                reporte.getDescripcion()
        );

        String valor =
                reporte.isResumenMonetario()
                        ? formatoMoneda.format(
                                reporte.getValorResumen()
                        )
                        : reporte.getValorResumen()
                                .stripTrailingZeros()
                                .toPlainString();

        lblResumenResultados.setText(
                reporte.getCantidadRegistros()
                + " registros | "
                + reporte.getEtiquetaResumen()
                + ": "
                + valor
        );

        graficoReportes.setDatos(
                reporte.getTitulo(),
                reporte.getDatosGrafico()
        );

        txtBuscarResultados.setText("");

        boolean hayDatos =
                reporte.getCantidadRegistros() > 0;

        btnExportar.setEnabled(hayDatos);
        btnImprimir.setEnabled(hayDatos);
    }

    public JTable getTablaResultados() {
        return tblResultados;
    }

    public void establecerConsultando(
            boolean consultando) {

        btnConsultar.setEnabled(!consultando);

        btnConsultar.setText(
                consultando
                        ? "Consultando..."
                        : "Consultar"
        );
    }

    private void filtrarResultadosLocales() {
        if (ordenadorResultados == null) {
            return;
        }

        String texto =
                txtBuscarResultados
                        .getText()
                        .trim();

        if (texto.isBlank()) {
            ordenadorResultados.setRowFilter(null);
            return;
        }

        ordenadorResultados.setRowFilter(
                RowFilter.regexFilter(
                        "(?i)"
                        + Pattern.quote(texto)
                )
        );
    }

    private LocalDate convertirFecha(
            String valor,
            String nombreCampo) {

        try {
            return LocalDate.parse(
                    valor.trim(),
                    FORMATO_FECHA
            );

        } catch (NullPointerException
                | DateTimeParseException ex) {

            throw new IllegalArgumentException(
                    "La " + nombreCampo
                    + " debe tener formato dd/MM/yyyy."
            );
        }
    }

    private static final class ModeloReporte
            extends DefaultTableModel {

        ModeloReporte(String[] columnas) {
            super(columnas, 0);
        }

        @Override
        public boolean isCellEditable(
                int row,
                int column) {

            return false;
        }

        @Override
        public Class<?> getColumnClass(
                int columnIndex) {

            for (int fila = 0;
                    fila < getRowCount();
                    fila++) {

                Object valor =
                        getValueAt(
                                fila,
                                columnIndex
                        );

                if (valor != null) {
                    return valor.getClass();
                }
            }

            return Object.class;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlTarjetaVentas = new javax.swing.JPanel();
        lblVentasTitulo = new javax.swing.JLabel();
        lblVentasValor = new javax.swing.JLabel();
        lblVentasAyuda = new javax.swing.JLabel();
        pnlTarjetaCompras = new javax.swing.JPanel();
        lblComprasTitulo = new javax.swing.JLabel();
        lblComprasValor = new javax.swing.JLabel();
        lblComprasAyuda = new javax.swing.JLabel();
        pnlTarjetaStock = new javax.swing.JPanel();
        lblStockTitulo = new javax.swing.JLabel();
        lblStockValor = new javax.swing.JLabel();
        lblStockAyuda = new javax.swing.JLabel();
        pnlTarjetaReparaciones = new javax.swing.JPanel();
        lblReparacionesTitulo = new javax.swing.JLabel();
        lblReparacionesValor = new javax.swing.JLabel();
        lblReparacionesAyuda = new javax.swing.JLabel();
        pnlFiltros = new javax.swing.JPanel();
        lblTipoReporte = new javax.swing.JLabel();
        cmbTipoReporte = new javax.swing.JComboBox<>();
        lblFechaDesde = new javax.swing.JLabel();
        txtFechaDesde = new javax.swing.JTextField();
        lblFechaHasta = new javax.swing.JLabel();
        txtFechaHasta = new javax.swing.JTextField();
        lblEstado = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox<>();
        lblUsuario = new javax.swing.JLabel();
        cmbUsuario = new javax.swing.JComboBox<>();
        txtDescripcionReporte = new javax.swing.JTextField();
        btnConsultar = new javax.swing.JButton();
        btnExportar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        tabsResultados = new javax.swing.JTabbedPane();
        pnlResultados = new javax.swing.JPanel();
        lblTituloResultados = new javax.swing.JLabel();
        lblDescripcionResultados = new javax.swing.JLabel();
        txtBuscarResultados = new javax.swing.JTextField();
        lblResumenResultados = new javax.swing.JLabel();
        scrollResultados = new javax.swing.JScrollPane();
        tblResultados = new javax.swing.JTable();
        pnlGrafico = new javax.swing.JPanel();
        graficoReportes = new sigir.componentes.GraficoBarrasPanel();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Centro de Reportes");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 390, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Consulta, filtra, exporta e imprime información operativa del sistema.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 800, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 10, 1100, 76);

        crearTarjeta(pnlTarjetaVentas, lblVentasTitulo, lblVentasValor,
                lblVentasAyuda, "Ventas del período", "L 0.00",
                "Ventas completadas", 28);
        crearTarjeta(pnlTarjetaCompras, lblComprasTitulo, lblComprasValor,
                lblComprasAyuda, "Compras del período", "L 0.00",
                "Compras registradas", 292);
        crearTarjeta(pnlTarjetaStock, lblStockTitulo, lblStockValor,
                lblStockAyuda, "Productos con stock bajo", "0",
                "Incluye agotados", 556);
        crearTarjeta(pnlTarjetaReparaciones, lblReparacionesTitulo,
                lblReparacionesValor, lblReparacionesAyuda,
                "Reparaciones pendientes", "0",
                "Sin entregar o cancelar", 820);

        pnlFiltros.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltros.setLayout(null);

        agregarEtiqueta(pnlFiltros, lblTipoReporte,
                "Tipo de reporte", 16, 10, 120);
        pnlFiltros.add(cmbTipoReporte);
        cmbTipoReporte.setBounds(16, 30, 220, 34);

        agregarEtiqueta(pnlFiltros, lblFechaDesde,
                "Fecha inicial", 248, 10, 100);
        pnlFiltros.add(txtFechaDesde);
        txtFechaDesde.setBounds(248, 30, 125, 34);

        agregarEtiqueta(pnlFiltros, lblFechaHasta,
                "Fecha final", 385, 10, 100);
        pnlFiltros.add(txtFechaHasta);
        txtFechaHasta.setBounds(385, 30, 125, 34);

        agregarEtiqueta(pnlFiltros, lblEstado,
                "Estado / movimiento", 522, 10, 145);
        pnlFiltros.add(cmbEstado);
        cmbEstado.setBounds(522, 30, 165, 34);

        agregarEtiqueta(pnlFiltros, lblUsuario,
                "Usuario", 699, 10, 100);
        pnlFiltros.add(cmbUsuario);
        cmbUsuario.setBounds(699, 30, 240, 34);

        pnlFiltros.add(txtDescripcionReporte);
        txtDescripcionReporte.setBounds(16, 74, 671, 32);

        btnConsultar.setText("Actualizar");
        pnlFiltros.add(btnConsultar);
        btnConsultar.setBounds(699, 74, 105, 34);

        btnExportar.setText("Exportar CSV");
        pnlFiltros.add(btnExportar);
        btnExportar.setBounds(816, 74, 120, 34);

        btnImprimir.setText("Imprimir / PDF");
        pnlFiltros.add(btnImprimir);
        btnImprimir.setBounds(948, 74, 110, 34);

        add(pnlFiltros);
        pnlFiltros.setBounds(28, 200, 1070, 122);

        pnlResultados.setBackground(new java.awt.Color(255, 255, 255));
        pnlResultados.setLayout(null);

        lblTituloResultados.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloResultados.setText("Resultados del reporte");
        pnlResultados.add(lblTituloResultados);
        lblTituloResultados.setBounds(16, 8, 300, 26);

        lblDescripcionResultados.setForeground(new java.awt.Color(98, 124, 159));
        lblDescripcionResultados.setText("Consulta un reporte para mostrar sus resultados.");
        pnlResultados.add(lblDescripcionResultados);
        lblDescripcionResultados.setBounds(16, 34, 570, 20);

        pnlResultados.add(txtBuscarResultados);
        txtBuscarResultados.setBounds(730, 14, 300, 34);

        lblResumenResultados.setForeground(new java.awt.Color(98, 124, 159));
        lblResumenResultados.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblResumenResultados.setText("0 registros");
        pnlResultados.add(lblResumenResultados);
        lblResumenResultados.setBounds(620, 54, 410, 22);

        scrollResultados.setViewportView(tblResultados);
        pnlResultados.add(scrollResultados);
        scrollResultados.setBounds(0, 84, 1045, 322);

        tabsResultados.addTab("Resultados", pnlResultados);

        pnlGrafico.setBackground(new java.awt.Color(255, 255, 255));
        pnlGrafico.setLayout(null);
        pnlGrafico.add(graficoReportes);
        graficoReportes.setBounds(0, 0, 1045, 406);

        tabsResultados.addTab("Gráfico", pnlGrafico);

        add(tabsResultados);
        tabsResultados.setBounds(28, 334, 1070, 438);
    }// </editor-fold>//GEN-END:initComponents

    private void crearTarjeta(
            javax.swing.JPanel panel,
            javax.swing.JLabel titulo,
            javax.swing.JLabel valor,
            javax.swing.JLabel ayuda,
            String textoTitulo,
            String textoValor,
            String textoAyuda,
            int x) {

        panel.setBackground(Color.WHITE);
        panel.setLayout(null);

        titulo.setText(textoTitulo);
        panel.add(titulo);
        titulo.setBounds(18, 12, 190, 20);

        valor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valor.setText(textoValor);
        panel.add(valor);
        valor.setBounds(18, 36, 225, 32);

        ayuda.setForeground(new Color(98, 124, 159));
        ayuda.setText(textoAyuda);
        panel.add(ayuda);
        ayuda.setBounds(18, 70, 190, 18);

        add(panel);
        panel.setBounds(x, 88, 250, 100);
    }

    private void agregarEtiqueta(
            javax.swing.JPanel panel,
            javax.swing.JLabel etiqueta,
            String texto,
            int x,
            int y,
            int ancho) {

        etiqueta.setText(texto);
        panel.add(etiqueta);
        etiqueta.setBounds(x, y, ancho, 18);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExportar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<TipoReporte> cmbTipoReporte;
    private javax.swing.JComboBox<UsuarioFiltro> cmbUsuario;
    private sigir.componentes.GraficoBarrasPanel graficoReportes;
    private javax.swing.JLabel lblComprasAyuda;
    private javax.swing.JLabel lblComprasTitulo;
    private javax.swing.JLabel lblComprasValor;
    private javax.swing.JLabel lblDescripcionResultados;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblReparacionesAyuda;
    private javax.swing.JLabel lblReparacionesTitulo;
    private javax.swing.JLabel lblReparacionesValor;
    private javax.swing.JLabel lblResumenResultados;
    private javax.swing.JLabel lblStockAyuda;
    private javax.swing.JLabel lblStockTitulo;
    private javax.swing.JLabel lblStockValor;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTipoReporte;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloResultados;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblVentasAyuda;
    private javax.swing.JLabel lblVentasTitulo;
    private javax.swing.JLabel lblVentasValor;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlGrafico;
    private javax.swing.JPanel pnlResultados;
    private javax.swing.JPanel pnlTarjetaCompras;
    private javax.swing.JPanel pnlTarjetaReparaciones;
    private javax.swing.JPanel pnlTarjetaStock;
    private javax.swing.JPanel pnlTarjetaVentas;
    private javax.swing.JScrollPane scrollResultados;
    private javax.swing.JTabbedPane tabsResultados;
    private javax.swing.JTable tblResultados;
    private javax.swing.JTextField txtBuscarResultados;
    private javax.swing.JTextField txtDescripcionReporte;
    private javax.swing.JTextField txtFechaDesde;
    private javax.swing.JTextField txtFechaHasta;
    // End of variables declaration//GEN-END:variables
}
