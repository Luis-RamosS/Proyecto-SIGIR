package sigir.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.CompraControlador;
import sigir.modelo.Compra;
import sigir.modelo.DetalleCompra;
import sigir.modelo.Producto;
import sigir.modelo.Proveedor;
import sigir.util.FiltroTiempoReal;
import sigir.util.Sesion;
import sigir.vista.dialogos.BusquedaProductoCompraDialog;

public class ComprasPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    new Locale("es", "HN")
            );

    private final CompraControlador controlador;
    private Producto productoSeleccionado;

    public ComprasPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();

        controlador = new CompraControlador(this);

        configurarEventos();

        FiltroTiempoReal.activar(
                txtBuscarHistorial,
                controlador::buscarCompras
        );

        controlador.iniciar();
    }

    public void recargar() {
        controlador.recargar();
    }

    private void configurarComponentes() {
        formatoMoneda.setMinimumFractionDigits(2);
        formatoMoneda.setMaximumFractionDigits(2);

        txtFechaCompra.setText(
                LocalDate.now().format(FORMATO_FECHA)
        );

        txtFechaDesde.setText(
                LocalDate.now()
                        .minusMonths(1)
                        .format(FORMATO_FECHA)
        );

        txtFechaHasta.setText(
                LocalDate.now().format(FORMATO_FECHA)
        );

        txtUsuario.setText(
                Sesion.haySesionActiva()
                        ? Sesion.getNombreCompleto()
                        : ""
        );

        javax.swing.JTextField[] soloLectura = {
            txtUsuario,
            txtCodigoProducto,
            txtNombreProducto,
            txtStockProducto
        };

        for (javax.swing.JTextField campo
                : soloLectura) {

            campo.setEditable(false);
            campo.setFocusable(false);
            campo.setBackground(
                    new Color(244, 247, 251)
            );
        }

        cmbTipoPago.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "CONTADO",
                            "CREDITO",
                            "TRANSFERENCIA"
                        }
                )
        );

        cmbEstadoHistorial.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "REGISTRADA",
                            "ANULADA",
                            "PENDIENTE"
                        }
                )
        );

        tblDetalle.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblDetalle.setFillsViewportHeight(true);

        tblHistorial.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblHistorial.setAutoCreateRowSorter(true);
        tblHistorial.setFillsViewportHeight(true);

        tabsCompras.setSelectedIndex(0);
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        JPanel[] paneles = {
            pnlDatosCompra,
            pnlAgregarProducto,
            pnlDetalleCompra,
            pnlFiltrosHistorial,
            pnlTablaHistorial
        };

        for (JPanel panel : paneles) {
            panel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borde),
                            BorderFactory.createEmptyBorder(
                                    8, 8, 8, 8
                            )
                    )
            );
        }

        javax.swing.JTextField[] campos = {
            txtNumeroDocumento,
            txtFechaCompra,
            txtUsuario,
            txtCodigoProducto,
            txtNombreProducto,
            txtCantidad,
            txtCostoUnitario,
            txtStockProducto,
            txtBuscarHistorial,
            txtFechaDesde,
            txtFechaHasta
        };

        for (javax.swing.JTextField campo
                : campos) {

            campo.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    new Color(205, 216, 229)
                            ),
                            BorderFactory.createEmptyBorder(
                                    0, 10, 0, 10
                            )
                    )
            );
        }

        javax.swing.JButton[] primarios = {
            btnBuscarProducto,
            btnAgregarProducto,
            btnGuardarCompra
        };

        for (javax.swing.JButton boton
                : primarios) {

            boton.setBackground(azul);
            boton.setForeground(Color.WHITE);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        javax.swing.JButton[] secundarios = {
            btnQuitarProducto,
            btnNuevaCompra,
            btnVerDetalle,
            btnAnularCompra,
            btnActualizarHistorial
        };

        for (javax.swing.JButton boton
                : secundarios) {

            boton.setBackground(Color.WHITE);
            boton.setForeground(texto);
            boton.setBorder(
                    BorderFactory.createLineBorder(borde)
            );
            boton.setFocusPainted(false);
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        btnAnularCompra.setForeground(
                new Color(192, 52, 52)
        );

        lblAvisoSeries.setForeground(
                new Color(176, 106, 20)
        );

        estilizarTabla(tblDetalle);
        estilizarTabla(tblHistorial);
    }

    private void estilizarTabla(
            javax.swing.JTable tabla) {

        tabla.setRowHeight(40);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(
                new Color(232, 237, 243)
        );

        tabla.setSelectionBackground(
                new Color(229, 239, 252)
        );

        tabla.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera =
                tabla.getTableHeader();

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
    }

    private void configurarEventos() {
        btnBuscarProducto.addActionListener(
                e -> controlador.buscarProductoAvanzado()
        );

        btnAgregarProducto.addActionListener(
                e -> controlador.agregarProducto()
        );

        btnQuitarProducto.addActionListener(
                e -> controlador.eliminarProducto()
        );

        btnNuevaCompra.addActionListener(
                e -> controlador.nuevaCompra()
        );

        btnGuardarCompra.addActionListener(
                e -> controlador.registrarCompra()
        );

        btnActualizarHistorial.addActionListener(
                e -> controlador.recargar()
        );

        btnVerDetalle.addActionListener(
                e -> controlador.verDetalleCompra()
        );

        btnAnularCompra.addActionListener(
                e -> controlador.anularCompra()
        );

        cmbEstadoHistorial.addActionListener(e -> {
            if (cmbEstadoHistorial.getItemCount() > 0) {
                controlador.buscarCompras();
            }
        });
    }

    public void cargarProveedores(
            List<Proveedor> proveedores) {

        Proveedor seleccionado =
                getProveedorSeleccionado();

        DefaultComboBoxModel<Proveedor> modelo =
                new DefaultComboBoxModel<>();

        Proveedor opcion = new Proveedor();

        opcion.setIdProveedor(0);
        opcion.setNombreProveedor(
                "Seleccione un proveedor..."
        );

        modelo.addElement(opcion);

        for (Proveedor proveedor : proveedores) {
            modelo.addElement(proveedor);
        }

        cmbProveedor.setModel(modelo);

        if (seleccionado != null) {
            seleccionarProveedor(
                    seleccionado.getIdProveedor()
            );
        }
    }

    public Producto solicitarProductoAvanzado(
            List<Producto> productos) {

        Window propietario =
                SwingUtilities.getWindowAncestor(this);

        BusquedaProductoCompraDialog dialogo =
                new BusquedaProductoCompraDialog(
                        propietario,
                        productos
                );

        return dialogo.mostrarDialogo();
    }

    public void establecerProductoSeleccionado(
            Producto producto) {

        productoSeleccionado = producto;

        txtCodigoProducto.setText(
                producto == null
                        ? ""
                        : texto(producto.getCodigo())
        );

        txtNombreProducto.setText(
                producto == null
                        ? ""
                        : texto(producto.getNombre())
        );
    }

    public Proveedor getProveedorSeleccionado() {
        Object seleccionado =
                cmbProveedor.getSelectedItem();

        return seleccionado instanceof Proveedor proveedor
                ? proveedor
                : null;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public String getNumeroDocumento() {
        return textoOpcional(
                txtNumeroDocumento.getText()
        );
    }

    public LocalDate getFechaCompra() {
        return convertirFechaObligatoria(
                txtFechaCompra.getText(),
                "fecha de compra"
        );
    }

    public String getTipoPago() {
        Object valor =
                cmbTipoPago.getSelectedItem();

        return valor == null
                ? ""
                : valor.toString();
    }

    public String getObservaciones() {
        return textoOpcional(
                txtObservaciones.getText()
        );
    }

    public int getCantidadProducto() {
        return convertirEntero(
                txtCantidad.getText(),
                "cantidad"
        );
    }

    public BigDecimal getCostoProducto() {
        return convertirDecimal(
                txtCostoUnitario.getText(),
                "costo unitario"
        );
    }

    public void setCostoProducto(
            BigDecimal costo) {

        txtCostoUnitario.setText(
                costo == null
                        ? "0.00"
                        : costo.setScale(
                                2,
                                RoundingMode.HALF_UP
                        ).toPlainString()
        );
    }

    public void mostrarStockProducto(int stock) {
        txtStockProducto.setText(
                String.valueOf(stock)
        );
    }

    public void mostrarAvisoSeries(
            boolean manejaSeries) {

        lblAvisoSeries.setText(
                manejaSeries
                        ? "Este producto requiere un número "
                        + "de serie por cada unidad comprada."
                        : "El producto no requiere números de serie."
        );
    }

    public List<String> solicitarNumerosSerie(
            Producto producto,
            int cantidad) {

        JTextArea area = new JTextArea(12, 42);

        area.setLineWrap(false);

        area.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        JPanel panel =
                new JPanel(
                        new BorderLayout(0, 8)
                );

        panel.setPreferredSize(
                new Dimension(520, 300)
        );

        panel.add(
                new JLabel(
                        "<html>Escribe exactamente <b>"
                        + cantidad
                        + "</b> números de serie para <b>"
                        + producto.getNombre()
                        + "</b>.<br>"
                        + "Coloca una serie por línea.</html>"
                ),
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(area),
                BorderLayout.CENTER
        );

        int respuesta =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Números de serie",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (respuesta != JOptionPane.OK_OPTION) {
            return null;
        }

        List<String> series =
                Arrays.stream(
                        area.getText().split("\\R")
                )
                .map(String::trim)
                .filter(valor -> !valor.isBlank())
                .toList();

        if (series.size() != cantidad) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingresaste "
                    + series.size()
                    + " series, pero la cantidad es "
                    + cantidad + ".",
                    "Cantidad incorrecta",
                    JOptionPane.WARNING_MESSAGE
            );

            return solicitarNumerosSerie(
                    producto,
                    cantidad
            );
        }

        return new ArrayList<>(series);
    }

    public void mostrarDetalles(
            List<DetalleCompra> detalles) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "Código",
                            "Producto",
                            "Cantidad",
                            "Costo unitario",
                            "Subtotal",
                            "Series"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        for (DetalleCompra detalle : detalles) {
            modelo.addRow(new Object[]{
                detalle.getCodigoProducto(),
                detalle.getNombreProducto(),
                detalle.getCantidad(),
                formatearMoneda(
                        detalle.getCostoUnitario()
                ),
                formatearMoneda(
                        detalle.getSubtotal()
                ),
                detalle.getResumenSeries()
            });
        }

        tblDetalle.setModel(modelo);
        estilizarTabla(tblDetalle);

        if (tblDetalle.getColumnCount() >= 6) {
            tblDetalle.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(90);

            tblDetalle.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(285);

            tblDetalle.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(70);

            tblDetalle.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(120);

            tblDetalle.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(120);

            tblDetalle.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(115);

            DefaultTableCellRenderer centro =
                    new DefaultTableCellRenderer();

            centro.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            tblDetalle.getColumnModel()
                    .getColumn(2)
                    .setCellRenderer(centro);
        }

        lblCantidadProductos.setText(
                detalles.size() == 1
                        ? "1 producto agregado"
                        : detalles.size()
                        + " productos agregados"
        );
    }

    public int getFilaDetalleSeleccionadaModelo() {
        int filaVista =
                tblDetalle.getSelectedRow();

        return filaVista < 0
                ? -1
                : tblDetalle.convertRowIndexToModel(
                        filaVista
                );
    }

    public void limpiarProductoSeleccionado() {
        productoSeleccionado = null;

        txtCodigoProducto.setText("");
        txtNombreProducto.setText("");
        txtCantidad.setText("1");
        txtCostoUnitario.setText("0.00");
        txtStockProducto.setText("0");

        lblAvisoSeries.setText(
                "Usa Búsqueda avanzada para seleccionar "
                + "el producto."
        );
    }

    public void limpiarCompra() {
        if (cmbProveedor.getItemCount() > 0) {
            cmbProveedor.setSelectedIndex(0);
        }

        txtNumeroDocumento.setText("");

        txtFechaCompra.setText(
                LocalDate.now().format(FORMATO_FECHA)
        );

        txtUsuario.setText(
                Sesion.haySesionActiva()
                        ? Sesion.getNombreCompleto()
                        : ""
        );

        cmbTipoPago.setSelectedItem("CONTADO");
        txtObservaciones.setText("");

        limpiarProductoSeleccionado();
        tabsCompras.setSelectedIndex(0);
    }

    public void establecerProcesando(
            boolean procesando) {

        btnGuardarCompra.setEnabled(!procesando);
        btnBuscarProducto.setEnabled(!procesando);
        btnAgregarProducto.setEnabled(!procesando);
        btnQuitarProducto.setEnabled(!procesando);
        btnNuevaCompra.setEnabled(!procesando);

        btnGuardarCompra.setText(
                procesando
                        ? "Guardando..."
                        : "Guardar compra"
        );
    }

    public String getTextoBusquedaHistorial() {
        return txtBuscarHistorial
                .getText()
                .trim();
    }

    public LocalDate getFechaDesdeFiltro() {
        return convertirFechaOpcional(
                txtFechaDesde.getText(),
                "fecha inicial"
        );
    }

    public LocalDate getFechaHastaFiltro() {
        return convertirFechaOpcional(
                txtFechaHasta.getText(),
                "fecha final"
        );
    }

    public String getEstadoFiltro() {
        Object valor =
                cmbEstadoHistorial.getSelectedItem();

        return valor == null
                ? "TODOS"
                : valor.toString();
    }

    public void mostrarCompras(
            List<Compra> compras) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "ID",
                            "Documento",
                            "Fecha",
                            "Proveedor",
                            "Usuario",
                            "Pago",
                            "Total",
                            "Estado"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        DateTimeFormatter fechaHora =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm"
                );

        for (Compra compra : compras) {
            modelo.addRow(new Object[]{
                compra.getIdCompra(),
                textoDocumento(
                        compra.getNumeroDocumento()
                ),
                compra.getFechaCompra() == null
                        ? ""
                        : compra.getFechaCompra()
                                .format(fechaHora),
                compra.getNombreProveedor(),
                compra.getNombreUsuario(),
                compra.getTipoPago(),
                formatearMoneda(
                        compra.getTotal()
                ),
                compra.getEstado()
            });
        }

        tblHistorial.setModel(modelo);
        estilizarTabla(tblHistorial);
    }

    public int getFilaCompraSeleccionadaModelo() {
        int filaVista =
                tblHistorial.getSelectedRow();

        return filaVista < 0
                ? -1
                : tblHistorial.convertRowIndexToModel(
                        filaVista
                );
    }

    public void mostrarCantidadCompras(int cantidad) {
        lblCantidadHistorial.setText(
                cantidad == 1
                        ? "Mostrando 1 compra"
                        : "Mostrando "
                        + cantidad
                        + " compras"
        );
    }

    public void mostrarPestanaHistorial() {
        tabsCompras.setSelectedIndex(1);
    }

    public void mostrarDetalleCompra(
            Compra compra) {

        StringBuilder contenido =
                new StringBuilder();

        contenido.append("Compra #")
                .append(compra.getIdCompra())
                .append("\nDocumento: ")
                .append(
                        textoDocumento(
                                compra.getNumeroDocumento()
                        )
                )
                .append("\nProveedor: ")
                .append(compra.getNombreProveedor())
                .append("\nUsuario: ")
                .append(compra.getNombreUsuario())
                .append("\nTipo de pago: ")
                .append(compra.getTipoPago())
                .append("\nEstado: ")
                .append(compra.getEstado())
                .append("\n\nPRODUCTOS\n")
                .append(
                        "--------------------------------------------------\n"
                );

        for (DetalleCompra detalle
                : compra.getDetalles()) {

            contenido.append(
                    detalle.getCodigoProducto()
            )
            .append(" - ")
            .append(
                    detalle.getNombreProducto()
            )
            .append("\nCantidad: ")
            .append(detalle.getCantidad())
            .append(" | Costo: ")
            .append(
                    formatearMoneda(
                            detalle.getCostoUnitario()
                    )
            )
            .append(" | Subtotal: ")
            .append(
                    formatearMoneda(
                            detalle.getSubtotal()
                    )
            )
            .append("\n");

            if (detalle.isManejaNumeroSerie()) {
                contenido.append("Series: ")
                        .append(
                                detalle.getNumerosSerie()
                                        .isEmpty()
                                        ? "No registradas"
                                        : String.join(
                                                ", ",
                                                detalle.getNumerosSerie()
                                        )
                        )
                        .append("\n");
            }

            contenido.append("\n");
        }

        contenido.append(
                "--------------------------------------------------\n"
        )
        .append("TOTAL: ")
        .append(
                formatearMoneda(
                        compra.getTotal()
                )
        )
        .append("\n");

        if (compra.getObservaciones() != null
                && !compra.getObservaciones()
                        .isBlank()) {

            contenido.append(
                    "\nObservaciones:\n"
            )
            .append(
                    compra.getObservaciones()
            );
        }

        JTextArea area =
                new JTextArea(
                        contenido.toString(),
                        24,
                        58
                );

        area.setEditable(false);

        area.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Detalle de compra",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public String formatearMoneda(
            BigDecimal valor) {

        return formatoMoneda.format(
                valor == null
                        ? BigDecimal.ZERO
                        : valor
        );
    }

    private void seleccionarProveedor(
            int idProveedor) {

        for (int i = 0;
                i < cmbProveedor.getItemCount();
                i++) {

            Proveedor proveedor =
                    cmbProveedor.getItemAt(i);

            if (proveedor != null
                    && proveedor.getIdProveedor()
                    == idProveedor) {

                cmbProveedor.setSelectedIndex(i);
                return;
            }
        }
    }

    private int convertirEntero(
            String texto,
            String campo) {

        try {
            return Integer.parseInt(
                    texto == null
                            ? ""
                            : texto.trim()
            );

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "La " + campo
                    + " debe ser un número entero."
            );
        }
    }

    private BigDecimal convertirDecimal(
            String texto,
            String campo) {

        String valor = texto == null
                ? ""
                : texto.trim()
                        .replace("L", "")
                        .replace(",", "");

        if (valor.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(valor)
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El " + campo
                    + " debe ser un número válido."
            );
        }
    }

    private LocalDate convertirFechaObligatoria(
            String texto,
            String campo) {

        LocalDate fecha =
                convertirFechaOpcional(
                        texto,
                        campo
                );

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "Ingresa la " + campo + "."
            );
        }

        return fecha;
    }

    private LocalDate convertirFechaOpcional(
            String texto,
            String campo) {

        if (texto == null
                || texto.trim().isEmpty()) {

            return null;
        }

        try {
            return LocalDate.parse(
                    texto.trim(),
                    FORMATO_FECHA
            );

        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La " + campo
                    + " debe tener el formato dd/MM/yyyy."
            );
        }
    }

    private String textoOpcional(
            String valor) {

        return valor == null
                || valor.trim().isEmpty()
                ? null
                : valor.trim();
    }

    private String textoDocumento(
            String documento) {

        return documento == null
                || documento.isBlank()
                ? "Sin documento"
                : documento;
    }

    private String texto(String valor) {
        return valor == null
                ? ""
                : valor;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        tabsCompras = new javax.swing.JTabbedPane();
        pnlNuevaCompra = new javax.swing.JPanel();
        pnlDatosCompra = new javax.swing.JPanel();
        lblTituloDatos = new javax.swing.JLabel();
        lblProveedor = new javax.swing.JLabel();
        cmbProveedor = new javax.swing.JComboBox<>();
        lblDocumento = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        lblFecha = new javax.swing.JLabel();
        txtFechaCompra = new javax.swing.JTextField();
        lblTipoPago = new javax.swing.JLabel();
        cmbTipoPago = new javax.swing.JComboBox<>();
        lblUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        lblObservaciones = new javax.swing.JLabel();
        scrollObservaciones = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        pnlAgregarProducto = new javax.swing.JPanel();
        lblTituloAgregar = new javax.swing.JLabel();
        lblCodigoProducto = new javax.swing.JLabel();
        txtCodigoProducto = new javax.swing.JTextField();
        lblProducto = new javax.swing.JLabel();
        txtNombreProducto = new javax.swing.JTextField();
        btnBuscarProducto = new javax.swing.JButton();
        lblCantidad = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        lblCosto = new javax.swing.JLabel();
        txtCostoUnitario = new javax.swing.JTextField();
        lblStockProducto = new javax.swing.JLabel();
        txtStockProducto = new javax.swing.JTextField();
        btnAgregarProducto = new javax.swing.JButton();
        lblAvisoSeries = new javax.swing.JLabel();
        pnlDetalleCompra = new javax.swing.JPanel();
        lblTituloDetalle = new javax.swing.JLabel();
        lblCantidadProductos = new javax.swing.JLabel();
        scrollDetalle = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        btnQuitarProducto = new javax.swing.JButton();
        btnNuevaCompra = new javax.swing.JButton();
        btnGuardarCompra = new javax.swing.JButton();
        pnlHistorial = new javax.swing.JPanel();
        pnlFiltrosHistorial = new javax.swing.JPanel();
        lblTituloFiltros = new javax.swing.JLabel();
        lblBuscarHistorial = new javax.swing.JLabel();
        txtBuscarHistorial = new javax.swing.JTextField();
        lblDesde = new javax.swing.JLabel();
        txtFechaDesde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        txtFechaHasta = new javax.swing.JTextField();
        lblEstadoHistorial = new javax.swing.JLabel();
        cmbEstadoHistorial = new javax.swing.JComboBox<>();
        btnActualizarHistorial = new javax.swing.JButton();
        pnlTablaHistorial = new javax.swing.JPanel();
        lblTituloTablaHistorial = new javax.swing.JLabel();
        scrollHistorial = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();
        lblCantidadHistorial = new javax.swing.JLabel();
        btnVerDetalle = new javax.swing.JButton();
        btnAnularCompra = new javax.swing.JButton();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Registro de Compras");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 390, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Registra compras usando una búsqueda avanzada de productos.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 720, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 12, 1110, 76);

        tabsCompras.setFont(new java.awt.Font("Segoe UI", 1, 13));

        pnlNuevaCompra.setBackground(new java.awt.Color(247, 249, 252));
        pnlNuevaCompra.setLayout(null);

        pnlDatosCompra.setBackground(new java.awt.Color(255, 255, 255));
        pnlDatosCompra.setLayout(null);

        lblTituloDatos.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloDatos.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloDatos.setText("Información de la compra");
        pnlDatosCompra.add(lblTituloDatos);
        lblTituloDatos.setBounds(16, 8, 260, 26);

        lblProveedor.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblProveedor.setText("Proveedor");
        pnlDatosCompra.add(lblProveedor);
        lblProveedor.setBounds(16, 38, 120, 16);

        pnlDatosCompra.add(cmbProveedor);
        cmbProveedor.setBounds(16, 56, 310, 36);

        lblDocumento.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblDocumento.setText("Documento del proveedor");
        pnlDatosCompra.add(lblDocumento);
        lblDocumento.setBounds(338, 38, 180, 16);

        pnlDatosCompra.add(txtNumeroDocumento);
        txtNumeroDocumento.setBounds(338, 56, 220, 36);

        lblFecha.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblFecha.setText("Fecha");
        pnlDatosCompra.add(lblFecha);
        lblFecha.setBounds(570, 38, 100, 16);

        pnlDatosCompra.add(txtFechaCompra);
        txtFechaCompra.setBounds(570, 56, 150, 36);

        lblTipoPago.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblTipoPago.setText("Tipo de pago");
        pnlDatosCompra.add(lblTipoPago);
        lblTipoPago.setBounds(732, 38, 110, 16);

        pnlDatosCompra.add(cmbTipoPago);
        cmbTipoPago.setBounds(732, 56, 140, 36);

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblUsuario.setText("Usuario responsable");
        pnlDatosCompra.add(lblUsuario);
        lblUsuario.setBounds(884, 38, 150, 16);

        pnlDatosCompra.add(txtUsuario);
        txtUsuario.setBounds(884, 56, 149, 36);

        lblObservaciones.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblObservaciones.setText("Observaciones");
        pnlDatosCompra.add(lblObservaciones);
        lblObservaciones.setBounds(16, 104, 150, 16);

        txtObservaciones.setColumns(20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setRows(5);
        txtObservaciones.setWrapStyleWord(true);
        scrollObservaciones.setViewportView(txtObservaciones);

        pnlDatosCompra.add(scrollObservaciones);
        scrollObservaciones.setBounds(16, 122, 1017, 66);

        pnlNuevaCompra.add(pnlDatosCompra);
        pnlDatosCompra.setBounds(0, 8, 1049, 205);

        pnlAgregarProducto.setBackground(new java.awt.Color(255, 255, 255));
        pnlAgregarProducto.setLayout(null);

        lblTituloAgregar.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloAgregar.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloAgregar.setText("Agregar producto");
        pnlAgregarProducto.add(lblTituloAgregar);
        lblTituloAgregar.setBounds(16, 8, 210, 26);

        lblCodigoProducto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblCodigoProducto.setText("Código");
        pnlAgregarProducto.add(lblCodigoProducto);
        lblCodigoProducto.setBounds(16, 40, 80, 16);

        pnlAgregarProducto.add(txtCodigoProducto);
        txtCodigoProducto.setBounds(16, 58, 110, 36);

        lblProducto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblProducto.setText("Producto seleccionado");
        pnlAgregarProducto.add(lblProducto);
        lblProducto.setBounds(138, 40, 160, 16);

        pnlAgregarProducto.add(txtNombreProducto);
        txtNombreProducto.setBounds(138, 58, 290, 36);

        btnBuscarProducto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnBuscarProducto.setText("Búsqueda avanzada");
        pnlAgregarProducto.add(btnBuscarProducto);
        btnBuscarProducto.setBounds(440, 52, 150, 42);

        lblCantidad.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblCantidad.setText("Cantidad");
        pnlAgregarProducto.add(lblCantidad);
        lblCantidad.setBounds(602, 40, 75, 16);

        txtCantidad.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCantidad.setText("1");
        pnlAgregarProducto.add(txtCantidad);
        txtCantidad.setBounds(602, 58, 70, 36);

        lblCosto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblCosto.setText("Costo de compra");
        pnlAgregarProducto.add(lblCosto);
        lblCosto.setBounds(684, 40, 120, 16);

        txtCostoUnitario.setText("0.00");
        pnlAgregarProducto.add(txtCostoUnitario);
        txtCostoUnitario.setBounds(684, 58, 120, 36);

        lblStockProducto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblStockProducto.setText("Stock actual");
        pnlAgregarProducto.add(lblStockProducto);
        lblStockProducto.setBounds(816, 40, 90, 16);

        txtStockProducto.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtStockProducto.setText("0");
        pnlAgregarProducto.add(txtStockProducto);
        txtStockProducto.setBounds(816, 58, 70, 36);

        btnAgregarProducto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnAgregarProducto.setText("+ Agregar");
        pnlAgregarProducto.add(btnAgregarProducto);
        btnAgregarProducto.setBounds(898, 52, 135, 42);

        lblAvisoSeries.setFont(new java.awt.Font("Segoe UI", 0, 10));
        lblAvisoSeries.setText("Usa Búsqueda avanzada para seleccionar el producto.");
        pnlAgregarProducto.add(lblAvisoSeries);
        lblAvisoSeries.setBounds(16, 103, 870, 20);

        pnlNuevaCompra.add(pnlAgregarProducto);
        pnlAgregarProducto.setBounds(0, 225, 1049, 135);

        pnlDetalleCompra.setBackground(new java.awt.Color(255, 255, 255));
        pnlDetalleCompra.setLayout(null);

        lblTituloDetalle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloDetalle.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloDetalle.setText("Detalle de productos");
        pnlDetalleCompra.add(lblTituloDetalle);
        lblTituloDetalle.setBounds(16, 8, 230, 26);

        lblCantidadProductos.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lblCantidadProductos.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidadProductos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCantidadProductos.setText("0 productos agregados");
        pnlDetalleCompra.add(lblCantidadProductos);
        lblCantidadProductos.setBounds(750, 10, 280, 22);

        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Producto", "Cantidad", "Costo unitario", "Subtotal", "Series"
            }
        ));
        scrollDetalle.setViewportView(tblDetalle);

        pnlDetalleCompra.add(scrollDetalle);
        scrollDetalle.setBounds(0, 40, 1049, 160);

        btnQuitarProducto.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnQuitarProducto.setText("Quitar producto");
        pnlDetalleCompra.add(btnQuitarProducto);
        btnQuitarProducto.setBounds(16, 210, 150, 38);

        btnNuevaCompra.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnNuevaCompra.setText("Nueva compra");
        pnlDetalleCompra.add(btnNuevaCompra);
        btnNuevaCompra.setBounds(697, 210, 145, 38);

        btnGuardarCompra.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnGuardarCompra.setText("Guardar compra");
        pnlDetalleCompra.add(btnGuardarCompra);
        btnGuardarCompra.setBounds(854, 210, 177, 38);

        pnlNuevaCompra.add(pnlDetalleCompra);
        pnlDetalleCompra.setBounds(0, 372, 1049, 260);

        tabsCompras.addTab("Nueva compra", pnlNuevaCompra);

        pnlHistorial.setBackground(new java.awt.Color(247, 249, 252));
        pnlHistorial.setLayout(null);

        pnlFiltrosHistorial.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltrosHistorial.setLayout(null);

        lblTituloFiltros.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloFiltros.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloFiltros.setText("Filtros de compras");
        pnlFiltrosHistorial.add(lblTituloFiltros);
        lblTituloFiltros.setBounds(16, 8, 220, 26);

        lblBuscarHistorial.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblBuscarHistorial.setText("Documento, proveedor o usuario");
        pnlFiltrosHistorial.add(lblBuscarHistorial);
        lblBuscarHistorial.setBounds(16, 40, 230, 16);

        pnlFiltrosHistorial.add(txtBuscarHistorial);
        txtBuscarHistorial.setBounds(16, 58, 250, 36);

        lblDesde.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblDesde.setText("Desde");
        pnlFiltrosHistorial.add(lblDesde);
        lblDesde.setBounds(280, 40, 90, 16);

        pnlFiltrosHistorial.add(txtFechaDesde);
        txtFechaDesde.setBounds(280, 58, 140, 36);

        lblHasta.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblHasta.setText("Hasta");
        pnlFiltrosHistorial.add(lblHasta);
        lblHasta.setBounds(434, 40, 90, 16);

        pnlFiltrosHistorial.add(txtFechaHasta);
        txtFechaHasta.setBounds(434, 58, 140, 36);

        lblEstadoHistorial.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblEstadoHistorial.setText("Estado");
        pnlFiltrosHistorial.add(lblEstadoHistorial);
        lblEstadoHistorial.setBounds(588, 40, 90, 16);

        pnlFiltrosHistorial.add(cmbEstadoHistorial);
        cmbEstadoHistorial.setBounds(588, 58, 170, 36);

        btnActualizarHistorial.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnActualizarHistorial.setText("Actualizar");
        pnlFiltrosHistorial.add(btnActualizarHistorial);
        btnActualizarHistorial.setBounds(772, 58, 130, 36);

        pnlHistorial.add(pnlFiltrosHistorial);
        pnlFiltrosHistorial.setBounds(0, 8, 1049, 110);

        pnlTablaHistorial.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaHistorial.setLayout(null);

        lblTituloTablaHistorial.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloTablaHistorial.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloTablaHistorial.setText("Historial de compras");
        pnlTablaHistorial.add(lblTituloTablaHistorial);
        lblTituloTablaHistorial.setBounds(16, 8, 240, 26);

        tblHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Documento", "Fecha", "Proveedor", "Usuario", "Pago", "Total", "Estado"
            }
        ));
        scrollHistorial.setViewportView(tblHistorial);

        pnlTablaHistorial.add(scrollHistorial);
        scrollHistorial.setBounds(0, 40, 1049, 430);

        lblCantidadHistorial.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lblCantidadHistorial.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidadHistorial.setText("Mostrando 0 compras");
        pnlTablaHistorial.add(lblCantidadHistorial);
        lblCantidadHistorial.setBounds(16, 478, 250, 22);

        btnVerDetalle.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnVerDetalle.setText("Ver detalle");
        pnlTablaHistorial.add(btnVerDetalle);
        btnVerDetalle.setBounds(748, 474, 120, 36);

        btnAnularCompra.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnAnularCompra.setText("Anular compra");
        pnlTablaHistorial.add(btnAnularCompra);
        btnAnularCompra.setBounds(880, 474, 150, 36);

        pnlHistorial.add(pnlTablaHistorial);
        pnlTablaHistorial.setBounds(0, 130, 1049, 520);

        tabsCompras.addTab("Historial", pnlHistorial);

        add(tabsCompras);
        tabsCompras.setBounds(28, 88, 1070, 680);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizarHistorial;
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnAnularCompra;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnGuardarCompra;
    private javax.swing.JButton btnNuevaCompra;
    private javax.swing.JButton btnQuitarProducto;
    private javax.swing.JButton btnVerDetalle;
    private javax.swing.JComboBox<Proveedor> cmbProveedor;
    private javax.swing.JComboBox<String> cmbEstadoHistorial;
    private javax.swing.JComboBox<String> cmbTipoPago;
    private javax.swing.JLabel lblAvisoSeries;
    private javax.swing.JLabel lblBuscarHistorial;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCantidadHistorial;
    private javax.swing.JLabel lblCantidadProductos;
    private javax.swing.JLabel lblCodigoProducto;
    private javax.swing.JLabel lblCosto;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblDocumento;
    private javax.swing.JLabel lblEstadoHistorial;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblStockProducto;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTipoPago;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloAgregar;
    private javax.swing.JLabel lblTituloDatos;
    private javax.swing.JLabel lblTituloDetalle;
    private javax.swing.JLabel lblTituloFiltros;
    private javax.swing.JLabel lblTituloTablaHistorial;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlAgregarProducto;
    private javax.swing.JPanel pnlDatosCompra;
    private javax.swing.JPanel pnlDetalleCompra;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFiltrosHistorial;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JPanel pnlNuevaCompra;
    private javax.swing.JPanel pnlTablaHistorial;
    private javax.swing.JScrollPane scrollDetalle;
    private javax.swing.JScrollPane scrollHistorial;
    private javax.swing.JScrollPane scrollObservaciones;
    private javax.swing.JTabbedPane tabsCompras;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtBuscarHistorial;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCodigoProducto;
    private javax.swing.JTextField txtCostoUnitario;
    private javax.swing.JTextField txtFechaCompra;
    private javax.swing.JTextField txtFechaDesde;
    private javax.swing.JTextField txtFechaHasta;
    private javax.swing.JTextField txtNombreProducto;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtStockProducto;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
