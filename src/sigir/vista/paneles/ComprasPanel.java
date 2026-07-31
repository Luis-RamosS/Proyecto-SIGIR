package sigir.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.CompraControlador;
import sigir.modelo.Compra;
import sigir.modelo.DetalleCompra;
import sigir.modelo.Producto;
import sigir.modelo.Proveedor;
import sigir.util.Sesion;
import sigir.util.FiltroTiempoReal;

public class ComprasPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "HN"));

    private final CompraControlador controlador;
    private boolean actualizandoTablaDetalle;

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

        txtFechaCompra.setText(LocalDate.now().format(FORMATO_FECHA));
        txtFechaDesde.setText(
                LocalDate.now().minusMonths(1).format(FORMATO_FECHA)
        );
        txtFechaHasta.setText(LocalDate.now().format(FORMATO_FECHA));

        txtUsuario.setText(
                Sesion.haySesionActiva() ? Sesion.getNombreCompleto() : ""
        );
        txtUsuario.setEditable(false);
        txtUsuario.setFocusable(false);
        txtStockProducto.setEditable(false);
        txtStockProducto.setFocusable(false);

        cmbTipoPago.setModel(new DefaultComboBoxModel<>(
                new String[]{"CONTADO", "CREDITO", "TRANSFERENCIA"}
        ));
        cmbEstadoHistorial.setModel(new DefaultComboBoxModel<>(
                new String[]{"TODOS", "REGISTRADA", "ANULADA", "PENDIENTE"}
        ));

        tblDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDetalle.setFillsViewportHeight(true);
        tblHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistorial.setAutoCreateRowSorter(true);
        tblHistorial.setFillsViewportHeight(true);
        tabsCompras.setSelectedIndex(0);
        
        lblDescuento.setText(
                "Descuento total (%)"
        );

        txtDescuento.setEditable(false);
        txtDescuento.setFocusable(false);

        txtDescuento.setBackground(
                new java.awt.Color(
                        244,
                        247,
                        251
                )
        );

        txtDescuento.setText("0.00 %");
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        JPanel[] paneles = {
            pnlDatosCompra, pnlResumen, pnlAgregarProducto,
            pnlDetalleCompra, pnlFiltrosHistorial, pnlTablaHistorial
        };

        for (JPanel panel : paneles) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borde),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }

        javax.swing.JTextField[] campos = {
            txtNumeroDocumento, txtFechaCompra, txtUsuario,
            txtCantidad, txtCostoUnitario, txtStockProducto,
            txtDescuento, txtBuscarHistorial, txtFechaDesde,
            txtFechaHasta
        };

        for (javax.swing.JTextField campo : campos) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(205, 216, 229)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
            ));
        }

        javax.swing.JButton[] primarios = {
            btnAgregarProducto, btnGuardarCompra
        };

        for (javax.swing.JButton boton : primarios) {
            boton.setBackground(azul);
            boton.setForeground(Color.WHITE);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        javax.swing.JButton[] secundarios = {
            btnQuitarProducto, btnNuevaCompra,
            btnVerDetalle, btnAnularCompra, btnActualizarHistorial
        };

        for (javax.swing.JButton boton : secundarios) {
            boton.setBackground(Color.WHITE);
            boton.setForeground(texto);
            boton.setBorder(BorderFactory.createLineBorder(borde));
            boton.setFocusPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        btnAnularCompra.setForeground(new Color(192, 52, 52));
        lblAvisoSeries.setForeground(new Color(176, 106, 20));
        estilizarTabla(tblDetalle);
        estilizarTabla(tblHistorial);
    }

    private void estilizarTabla(javax.swing.JTable tabla) {
        tabla.setRowHeight(40);
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
        cmbProducto.addActionListener(e -> controlador.seleccionarProducto());
        btnAgregarProducto.addActionListener(e -> controlador.agregarProducto());
        btnQuitarProducto.addActionListener(e -> controlador.eliminarProducto());
        btnNuevaCompra.addActionListener(e -> controlador.nuevaCompra());
        btnGuardarCompra.addActionListener(e -> controlador.registrarCompra());
        btnActualizarHistorial.addActionListener(e -> controlador.recargar());
        btnVerDetalle.addActionListener(e -> controlador.verDetalleCompra());
        btnAnularCompra.addActionListener(e -> controlador.anularCompra());

        cmbEstadoHistorial.addActionListener(e -> {
            if (cmbEstadoHistorial.getItemCount() > 0) {
                controlador.buscarCompras();
            }
        });

        
    }

    public void cargarProveedores(List<Proveedor> proveedores) {
        Proveedor seleccionado = getProveedorSeleccionado();
        DefaultComboBoxModel<Proveedor> modelo = new DefaultComboBoxModel<>();

        Proveedor opcion = new Proveedor();
        opcion.setIdProveedor(0);
        opcion.setNombreProveedor("Seleccione un proveedor...");
        modelo.addElement(opcion);

        for (Proveedor proveedor : proveedores) {
            modelo.addElement(proveedor);
        }

        cmbProveedor.setModel(modelo);

        if (seleccionado != null) {
            seleccionarProveedor(seleccionado.getIdProveedor());
        }
    }

    public void cargarProductos(List<Producto> productos) {
        Producto seleccionado = getProductoSeleccionado();
        DefaultComboBoxModel<Producto> modelo = new DefaultComboBoxModel<>();

        Producto opcion = new Producto();
        opcion.setIdProducto(0);
        opcion.setCodigo("");
        opcion.setNombre("Seleccione un producto...");
        modelo.addElement(opcion);

        for (Producto producto : productos) {
            modelo.addElement(producto);
        }

        cmbProducto.setModel(modelo);

        if (seleccionado != null) {
            seleccionarProducto(seleccionado.getIdProducto());
        }
    }

    public Proveedor getProveedorSeleccionado() {
        Object seleccionado = cmbProveedor.getSelectedItem();
        return seleccionado instanceof Proveedor proveedor ? proveedor : null;
    }

    public Producto getProductoSeleccionado() {
        Object seleccionado = cmbProducto.getSelectedItem();
        return seleccionado instanceof Producto producto ? producto : null;
    }

    public String getNumeroDocumento() {
        return textoOpcional(txtNumeroDocumento.getText());
    }

    public LocalDate getFechaCompra() {
        return convertirFechaObligatoria(
                txtFechaCompra.getText(),
                "fecha de compra"
        );
    }

    public String getTipoPago() {
        Object valor = cmbTipoPago.getSelectedItem();
        return valor == null ? "" : valor.toString();
    }

    public String getObservaciones() {
        return textoOpcional(txtObservaciones.getText());
    }

    public int getCantidadProducto() {
        return convertirEntero(txtCantidad.getText(), "cantidad");
    }

    public BigDecimal getCostoProducto() {
        return convertirDecimal(txtCostoUnitario.getText(), "costo unitario");
    }

    public void setCostoProducto(BigDecimal costo) {
        txtCostoUnitario.setText(
                costo == null
                        ? "0.00"
                        : costo.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
    }

    public void mostrarStockProducto(int stock) {
        txtStockProducto.setText(String.valueOf(stock));
    }

    public void mostrarAvisoSeries(boolean manejaSeries) {
        lblAvisoSeries.setText(
                manejaSeries
                        ? "Este producto requiere un número de serie por unidad."
                        : "El producto no requiere números de serie."
        );
    }

    public List<String> solicitarNumerosSerie(
            Producto producto,
            int cantidad) {

        JTextArea area = new JTextArea(12, 42);
        area.setLineWrap(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(520, 300));
        panel.add(new JLabel(
                "<html>Escribe exactamente <b>" + cantidad
                + "</b> números de serie para <b>"
                + producto.getNombre()
                + "</b>.<br>Coloca una serie por línea.</html>"
        ), BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Números de serie",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return null;
        }

        List<String> series = Arrays.stream(area.getText().split("\\R"))
                .map(String::trim)
                .filter(valor -> !valor.isBlank())
                .toList();

        if (series.size() != cantidad) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingresaste " + series.size()
                    + " series, pero la cantidad es " + cantidad + ".",
                    "Cantidad incorrecta",
                    JOptionPane.WARNING_MESSAGE
            );
            return solicitarNumerosSerie(producto, cantidad);
        }

        return new ArrayList<>(series);
    }

    public void mostrarDetalles(
            List<DetalleCompra> detalles) {

        actualizandoTablaDetalle = true;

        DefaultTableModel modelo
                = new DefaultTableModel(
                        new String[]{
                            "Código",
                            "Producto",
                            "Cantidad",
                            "Costo unitario",
                            "Descuento (L)",
                            "Subtotal",
                            "Total",
                            "Series"
                        },
                        0
                ) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                /*
                     * Solamente puede editarse la columna
                     * del descuento.
                 */
                return column == 4;
            }

            @Override
            public Class<?> getColumnClass(
                    int columnIndex) {

                return columnIndex == 2
                        ? Integer.class
                        : String.class;
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
                detalle.getDescuentoLinea()
                .setScale(
                2,
                RoundingMode.HALF_UP
                )
                .toPlainString(),
                formatearMoneda(
                detalle.getSubtotal()
                ),
                formatearMoneda(
                detalle.getTotalLinea()
                ),
                detalle.getResumenSeries()
            });
        }

        modelo.addTableModelListener(e -> {

            if (actualizandoTablaDetalle) {
                return;
            }

            if (e.getType()
                    != javax.swing.event.TableModelEvent.UPDATE) {

                return;
            }

            if (e.getColumn() != 4) {
                return;
            }

            int fila = e.getFirstRow();

            Object valor
                    = modelo.getValueAt(fila, 4);

            String descuento = valor == null
                    ? ""
                    : valor.toString();

            javax.swing.SwingUtilities.invokeLater(
                    () -> controlador
                            .actualizarDescuentoDetalle(
                                    fila,
                                    descuento
                            )
            );
        });

        tblDetalle.setModel(modelo);

        if (tblDetalle.getColumnCount() >= 8) {

            tblDetalle.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(85);

            tblDetalle.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(210);

            tblDetalle.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(65);

            tblDetalle.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(105);

            tblDetalle.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(105);

            tblDetalle.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(100);

            tblDetalle.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(100);

            tblDetalle.getColumnModel()
                    .getColumn(7)
                    .setPreferredWidth(95);
        }

        actualizandoTablaDetalle = false;

        estilizarTabla(tblDetalle);
    }

    public int getFilaDetalleSeleccionadaModelo() {
        int filaVista = tblDetalle.getSelectedRow();
        return filaVista < 0
                ? -1
                : tblDetalle.convertRowIndexToModel(filaVista);
    }

    public void actualizarResumen(
            int productos,
            int unidades,
            BigDecimal subtotal,
            BigDecimal descuentoTotal,
            BigDecimal porcentajeDescuento,
            BigDecimal total) {

        lblProductosAgregadosValor.setText(
                String.valueOf(productos)
        );

        lblUnidadesValor.setText(
                String.valueOf(unidades)
        );

        lblSubtotalValor.setText(
                formatearMoneda(subtotal)
        );

        txtDescuento.setText(
                porcentajeDescuento
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        .toPlainString()
                + " %"
        );

        /*
     * Debajo del porcentaje se conserva el monto
     * total descontado.
         */
        lblDescuentoValor.setText(
                formatearMoneda(descuentoTotal)
        );

        lblTotalValor.setText(
                formatearMoneda(total)
        );
    }

    public void limpiarProductoSeleccionado() {
        if (cmbProducto.getItemCount() > 0) {
            cmbProducto.setSelectedIndex(0);
        }

        txtCantidad.setText("1");
        txtCostoUnitario.setText("0.00");
        txtStockProducto.setText("0");
        lblAvisoSeries.setText(
                "Selecciona un producto para ver sus datos."
        );
    }

    public void limpiarCompra() {
        if (cmbProveedor.getItemCount() > 0) {
            cmbProveedor.setSelectedIndex(0);
        }

        txtNumeroDocumento.setText("");
        txtFechaCompra.setText(LocalDate.now().format(FORMATO_FECHA));
        txtUsuario.setText(
                Sesion.haySesionActiva() ? Sesion.getNombreCompleto() : ""
        );
        cmbTipoPago.setSelectedItem("CONTADO");
        txtObservaciones.setText("");
        txtDescuento.setText("0.00");
        limpiarProductoSeleccionado();
        tabsCompras.setSelectedIndex(0);
    }

    public void establecerProcesando(boolean procesando) {
        btnGuardarCompra.setEnabled(!procesando);
        btnAgregarProducto.setEnabled(!procesando);
        btnQuitarProducto.setEnabled(!procesando);
        btnNuevaCompra.setEnabled(!procesando);
        btnGuardarCompra.setText(
                procesando ? "Guardando..." : "Guardar compra"
        );
    }

    public String getTextoBusquedaHistorial() {
        return txtBuscarHistorial.getText().trim();
    }

    public LocalDate getFechaDesdeFiltro() {
        return convertirFechaOpcional(txtFechaDesde.getText(), "fecha inicial");
    }

    public LocalDate getFechaHastaFiltro() {
        return convertirFechaOpcional(txtFechaHasta.getText(), "fecha final");
    }

    public String getEstadoFiltro() {
        Object valor = cmbEstadoHistorial.getSelectedItem();
        return valor == null ? "TODOS" : valor.toString();
    }

    public void mostrarCompras(List<Compra> compras) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                    "ID", "Documento", "Fecha", "Proveedor",
                    "Usuario", "Pago", "Total", "Estado"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter fechaHora =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Compra compra : compras) {
            modelo.addRow(new Object[]{
                compra.getIdCompra(),
                textoDocumento(compra.getNumeroDocumento()),
                compra.getFechaCompra() == null
                        ? ""
                        : compra.getFechaCompra().format(fechaHora),
                compra.getNombreProveedor(),
                compra.getNombreUsuario(),
                compra.getTipoPago(),
                formatearMoneda(compra.getTotal()),
                compra.getEstado()
            });
        }

        tblHistorial.setModel(modelo);
        estilizarTabla(tblHistorial);
    }

    public int getFilaCompraSeleccionadaModelo() {
        int filaVista = tblHistorial.getSelectedRow();
        return filaVista < 0
                ? -1
                : tblHistorial.convertRowIndexToModel(filaVista);
    }

    public void mostrarCantidadCompras(int cantidad) {
        lblCantidadHistorial.setText(
                cantidad == 1
                        ? "Mostrando 1 compra"
                        : "Mostrando " + cantidad + " compras"
        );
    }

    public void mostrarPestanaHistorial() {
        tabsCompras.setSelectedIndex(1);
    }

    public void mostrarDetalleCompra(Compra compra) {
        StringBuilder contenido = new StringBuilder();

        contenido.append("Compra #")
                .append(compra.getIdCompra())
                .append("\nDocumento: ")
                .append(textoDocumento(compra.getNumeroDocumento()))
                .append("\nProveedor: ")
                .append(compra.getNombreProveedor())
                .append("\nUsuario: ")
                .append(compra.getNombreUsuario())
                .append("\nTipo de pago: ")
                .append(compra.getTipoPago())
                .append("\nEstado: ")
                .append(compra.getEstado())
                .append("\n\nPRODUCTOS\n")
                .append("--------------------------------------------------\n");

        for (DetalleCompra detalle : compra.getDetalles()) {
            contenido.append(detalle.getCodigoProducto())
                    .append(" - ")
                    .append(detalle.getNombreProducto())
                    .append("\nCantidad: ")
                    .append(detalle.getCantidad())
                    .append(" | Costo: ")
                    .append(formatearMoneda(detalle.getCostoUnitario()))
                    .append(" | Subtotal: ")
                    .append(formatearMoneda(detalle.getSubtotal()))
                    .append("\n");

            if (detalle.isManejaNumeroSerie()) {
                contenido.append("Series: ")
                        .append(
                                detalle.getNumerosSerie().isEmpty()
                                        ? "No registradas"
                                        : String.join(", ", detalle.getNumerosSerie())
                        )
                        .append("\n");
            }

            contenido.append("\n");
        }

        contenido.append("--------------------------------------------------\n")
                .append("Subtotal: ")
                .append(formatearMoneda(compra.getSubtotal()))
                .append("\nDescuento: ")
                .append(formatearMoneda(compra.getDescuento()))
                .append("\nTOTAL: ")
                .append(formatearMoneda(compra.getTotal()))
                .append("\n");

        if (compra.getObservaciones() != null
                && !compra.getObservaciones().isBlank()) {
            contenido.append("\nObservaciones:\n")
                    .append(compra.getObservaciones());
        }

        JTextArea area = new JTextArea(contenido.toString(), 24, 58);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Detalle de compra",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public String formatearMoneda(BigDecimal valor) {
        return formatoMoneda.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private void seleccionarProveedor(int idProveedor) {
        for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
            Proveedor proveedor = cmbProveedor.getItemAt(i);
            if (proveedor != null
                    && proveedor.getIdProveedor() == idProveedor) {
                cmbProveedor.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarProducto(int idProducto) {
        for (int i = 0; i < cmbProducto.getItemCount(); i++) {
            Producto producto = cmbProducto.getItemAt(i);
            if (producto != null
                    && producto.getIdProducto() == idProducto) {
                cmbProducto.setSelectedIndex(i);
                return;
            }
        }
    }

    private BigDecimal convertirDecimal(String texto, String nombreCampo) {
        String valor = texto == null
                ? ""
                : texto.trim().replace("L", "").replace(",", "");

        if (valor.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El " + nombreCampo + " debe ser un número válido."
            );
        }
    }

    private int convertirEntero(String texto, String nombreCampo) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "La " + nombreCampo + " debe ser un número entero."
            );
        }
    }

    private LocalDate convertirFechaObligatoria(
            String texto,
            String nombreCampo) {

        LocalDate fecha = convertirFechaOpcional(texto, nombreCampo);
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "Ingresa la " + nombreCampo + " en formato dd/MM/yyyy."
            );
        }
        return fecha;
    }

    private LocalDate convertirFechaOpcional(
            String texto,
            String nombreCampo) {

        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La " + nombreCampo + " debe tener formato dd/MM/yyyy."
            );
        }
    }

    private String textoDocumento(String valor) {
        return valor == null || valor.isBlank() ? "Sin documento" : valor;
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
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
        pnlResumen = new javax.swing.JPanel();
        lblTituloResumen = new javax.swing.JLabel();
        lblProductosAgregados = new javax.swing.JLabel();
        lblProductosAgregadosValor = new javax.swing.JLabel();
        lblUnidades = new javax.swing.JLabel();
        lblUnidadesValor = new javax.swing.JLabel();
        lblSubtotal = new javax.swing.JLabel();
        lblSubtotalValor = new javax.swing.JLabel();
        lblDescuento = new javax.swing.JLabel();
        txtDescuento = new javax.swing.JTextField();
        lblDescuentoValor = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        pnlAgregarProducto = new javax.swing.JPanel();
        lblTituloAgregar = new javax.swing.JLabel();
        lblProducto = new javax.swing.JLabel();
        cmbProducto = new javax.swing.JComboBox<>();
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
        scrollDetalle = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        btnQuitarProducto = new javax.swing.JButton();
        btnNuevaCompra = new javax.swing.JButton();
        btnGuardarCompra = new javax.swing.JButton();
        pnlHistorial = new javax.swing.JPanel();
        pnlFiltrosHistorial = new javax.swing.JPanel();
        lblTituloFiltros = new javax.swing.JLabel();
        txtBuscarHistorial = new javax.swing.JTextField();
        lblDesde = new javax.swing.JLabel();
        txtFechaDesde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        txtFechaHasta = new javax.swing.JTextField();
        cmbEstadoHistorial = new javax.swing.JComboBox<>();
        pnlTablaHistorial = new javax.swing.JPanel();
        lblTituloHistorial = new javax.swing.JLabel();
        scrollHistorial = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();
        lblCantidadHistorial = new javax.swing.JLabel();
        btnVerDetalle = new javax.swing.JButton();
        btnAnularCompra = new javax.swing.JButton();
        btnActualizarHistorial = new javax.swing.JButton();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Registro de Compras");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 390, 40);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 12, 1110, 50);

        tabsCompras.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N

        pnlNuevaCompra.setBackground(new java.awt.Color(247, 249, 252));
        pnlNuevaCompra.setLayout(null);

        pnlDatosCompra.setBackground(new java.awt.Color(255, 255, 255));
        pnlDatosCompra.setLayout(null);

        lblTituloDatos.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloDatos.setText("Información de la compra");
        pnlDatosCompra.add(lblTituloDatos);
        lblTituloDatos.setBounds(16, 10, 260, 26);

        lblProveedor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblProveedor.setText("Proveedor");
        pnlDatosCompra.add(lblProveedor);
        lblProveedor.setBounds(16, 42, 150, 18);

        cmbProveedor.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDatosCompra.add(cmbProveedor);
        cmbProveedor.setBounds(16, 63, 345, 36);

        lblDocumento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDocumento.setText("Documento del proveedor");
        pnlDatosCompra.add(lblDocumento);
        lblDocumento.setBounds(374, 42, 190, 18);

        txtNumeroDocumento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDatosCompra.add(txtNumeroDocumento);
        txtNumeroDocumento.setBounds(374, 63, 225, 36);

        lblFecha.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblFecha.setText("Fecha (dd/MM/yyyy)");
        pnlDatosCompra.add(lblFecha);
        lblFecha.setBounds(16, 108, 180, 18);

        txtFechaCompra.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDatosCompra.add(txtFechaCompra);
        txtFechaCompra.setBounds(16, 129, 180, 36);

        lblTipoPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTipoPago.setText("Tipo de pago");
        pnlDatosCompra.add(lblTipoPago);
        lblTipoPago.setBounds(209, 108, 160, 18);

        cmbTipoPago.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDatosCompra.add(cmbTipoPago);
        cmbTipoPago.setBounds(209, 129, 190, 36);

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUsuario.setText("Usuario responsable");
        pnlDatosCompra.add(lblUsuario);
        lblUsuario.setBounds(412, 108, 180, 18);

        txtUsuario.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtUsuario.setBackground(new java.awt.Color(244, 247, 251));
        pnlDatosCompra.add(txtUsuario);
        txtUsuario.setBounds(412, 129, 187, 36);

        lblObservaciones.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblObservaciones.setText("Observaciones");
        pnlDatosCompra.add(lblObservaciones);
        lblObservaciones.setBounds(16, 174, 180, 18);

        txtObservaciones.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtObservaciones.setColumns(20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setRows(5);
        txtObservaciones.setWrapStyleWord(true);
        scrollObservaciones.setViewportView(txtObservaciones);

        pnlDatosCompra.add(scrollObservaciones);
        scrollObservaciones.setBounds(16, 195, 583, 60);

        pnlNuevaCompra.add(pnlDatosCompra);
        pnlDatosCompra.setBounds(0, 8, 620, 270);

        pnlResumen.setBackground(new java.awt.Color(255, 255, 255));
        pnlResumen.setLayout(null);

        lblTituloResumen.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloResumen.setText("Resumen de la compra");
        pnlResumen.add(lblTituloResumen);
        lblTituloResumen.setBounds(16, 10, 240, 26);

        lblProductosAgregados.setText("Productos agregados");
        pnlResumen.add(lblProductosAgregados);
        lblProductosAgregados.setBounds(16, 47, 180, 22);

        lblProductosAgregadosValor.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblProductosAgregadosValor.setText("0");
        pnlResumen.add(lblProductosAgregadosValor);
        lblProductosAgregadosValor.setBounds(215, 43, 90, 30);

        lblUnidades.setText("Unidades totales");
        pnlResumen.add(lblUnidades);
        lblUnidades.setBounds(16, 78, 180, 22);

        lblUnidadesValor.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblUnidadesValor.setText("0");
        pnlResumen.add(lblUnidadesValor);
        lblUnidadesValor.setBounds(215, 74, 90, 30);

        lblSubtotal.setText("Subtotal");
        pnlResumen.add(lblSubtotal);
        lblSubtotal.setBounds(16, 110, 120, 22);

        lblSubtotalValor.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblSubtotalValor.setText("L 0.00");
        pnlResumen.add(lblSubtotalValor);
        lblSubtotalValor.setBounds(150, 108, 155, 25);

        lblDescuento.setText("Descuento");
        pnlResumen.add(lblDescuento);
        lblDescuento.setBounds(16, 142, 100, 22);

        txtDescuento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtDescuento.setText("0.00");
        pnlResumen.add(txtDescuento);
        txtDescuento.setBounds(150, 139, 155, 34);

        lblDescuentoValor.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblDescuentoValor.setText("L 0.00");
        pnlResumen.add(lblDescuentoValor);
        lblDescuentoValor.setBounds(150, 176, 155, 18);

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblTotal.setText("TOTAL");
        pnlResumen.add(lblTotal);
        lblTotal.setBounds(16, 209, 100, 24);

        lblTotalValor.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTotalValor.setForeground(new java.awt.Color(49, 105, 181));
        lblTotalValor.setText("L 0.00");
        pnlResumen.add(lblTotalValor);
        lblTotalValor.setBounds(115, 203, 190, 36);

        pnlNuevaCompra.add(pnlResumen);
        pnlResumen.setBounds(634, 8, 325, 270);

        pnlAgregarProducto.setBackground(new java.awt.Color(255, 255, 255));
        pnlAgregarProducto.setLayout(null);

        lblTituloAgregar.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloAgregar.setText("Agregar producto");
        pnlAgregarProducto.add(lblTituloAgregar);
        lblTituloAgregar.setBounds(16, 8, 210, 26);

        lblProducto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblProducto.setText("Producto");
        pnlAgregarProducto.add(lblProducto);
        lblProducto.setBounds(16, 40, 120, 18);

        cmbProducto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlAgregarProducto.add(cmbProducto);
        cmbProducto.setBounds(16, 61, 380, 36);

        lblCantidad.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCantidad.setText("Cantidad");
        pnlAgregarProducto.add(lblCantidad);
        lblCantidad.setBounds(410, 40, 90, 18);

        txtCantidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCantidad.setText("1");
        pnlAgregarProducto.add(txtCantidad);
        txtCantidad.setBounds(410, 61, 90, 36);

        lblCosto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCosto.setText("Costo unitario");
        pnlAgregarProducto.add(lblCosto);
        lblCosto.setBounds(514, 40, 120, 18);

        txtCostoUnitario.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCostoUnitario.setText("0.00");
        pnlAgregarProducto.add(txtCostoUnitario);
        txtCostoUnitario.setBounds(514, 61, 130, 36);

        lblStockProducto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStockProducto.setText("Stock actual");
        pnlAgregarProducto.add(lblStockProducto);
        lblStockProducto.setBounds(658, 40, 100, 18);

        txtStockProducto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtStockProducto.setBackground(new java.awt.Color(244, 247, 251));
        txtStockProducto.setText("0");
        pnlAgregarProducto.add(txtStockProducto);
        txtStockProducto.setBounds(658, 61, 95, 36);

        btnAgregarProducto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarProducto.setText("+ Agregar");
        pnlAgregarProducto.add(btnAgregarProducto);
        btnAgregarProducto.setBounds(769, 52, 170, 45);

        lblAvisoSeries.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblAvisoSeries.setText("Selecciona un producto para ver sus datos.");
        pnlAgregarProducto.add(lblAvisoSeries);
        lblAvisoSeries.setBounds(16, 101, 710, 20);

        pnlNuevaCompra.add(pnlAgregarProducto);
        pnlAgregarProducto.setBounds(0, 290, 959, 130);

        pnlDetalleCompra.setBackground(new java.awt.Color(255, 255, 255));
        pnlDetalleCompra.setLayout(null);

        lblTituloDetalle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloDetalle.setText("Detalle de productos");
        pnlDetalleCompra.add(lblTituloDetalle);
        lblTituloDetalle.setBounds(16, 8, 230, 26);

        tblDetalle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollDetalle.setViewportView(tblDetalle);

        pnlDetalleCompra.add(scrollDetalle);
        scrollDetalle.setBounds(0, 40, 959, 145);

        btnQuitarProducto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnQuitarProducto.setText("Quitar producto");
        pnlDetalleCompra.add(btnQuitarProducto);
        btnQuitarProducto.setBounds(16, 195, 150, 38);

        btnNuevaCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevaCompra.setText("Nueva compra");
        pnlDetalleCompra.add(btnNuevaCompra);
        btnNuevaCompra.setBounds(600, 195, 150, 38);

        btnGuardarCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardarCompra.setText("Guardar compra");
        pnlDetalleCompra.add(btnGuardarCompra);
        btnGuardarCompra.setBounds(764, 195, 175, 38);

        pnlNuevaCompra.add(pnlDetalleCompra);
        pnlDetalleCompra.setBounds(0, 432, 959, 245);

        tabsCompras.addTab("tab1", pnlNuevaCompra);

        pnlHistorial.setBackground(new java.awt.Color(247, 249, 252));
        pnlHistorial.setLayout(null);

        pnlFiltrosHistorial.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltrosHistorial.setLayout(null);

        lblTituloFiltros.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloFiltros.setText("Filtros de búsqueda");
        pnlFiltrosHistorial.add(lblTituloFiltros);
        lblTituloFiltros.setBounds(20, 30, 220, 26);

        txtBuscarHistorial.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltrosHistorial.add(txtBuscarHistorial);
        txtBuscarHistorial.setBounds(20, 60, 270, 30);

        lblDesde.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblDesde.setText("Desde");
        pnlFiltrosHistorial.add(lblDesde);
        lblDesde.setBounds(300, 42, 80, 16);

        txtFechaDesde.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltrosHistorial.add(txtFechaDesde);
        txtFechaDesde.setBounds(300, 60, 135, 30);

        lblHasta.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblHasta.setText("Hasta");
        pnlFiltrosHistorial.add(lblHasta);
        lblHasta.setBounds(448, 42, 80, 16);

        txtFechaHasta.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltrosHistorial.add(txtFechaHasta);
        txtFechaHasta.setBounds(450, 60, 135, 30);

        cmbEstadoHistorial.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltrosHistorial.add(cmbEstadoHistorial);
        cmbEstadoHistorial.setBounds(597, 59, 150, 30);

        pnlHistorial.add(pnlFiltrosHistorial);
        pnlFiltrosHistorial.setBounds(0, 8, 959, 110);

        pnlTablaHistorial.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaHistorial.setLayout(null);

        lblTituloHistorial.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloHistorial.setText("Historial de compras");
        pnlTablaHistorial.add(lblTituloHistorial);
        lblTituloHistorial.setBounds(16, 10, 220, 26);

        tblHistorial.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollHistorial.setViewportView(tblHistorial);

        pnlTablaHistorial.add(scrollHistorial);
        scrollHistorial.setBounds(0, 42, 959, 440);

        lblCantidadHistorial.setText("Mostrando 0 compras");
        pnlTablaHistorial.add(lblCantidadHistorial);
        lblCantidadHistorial.setBounds(16, 490, 280, 24);

        btnVerDetalle.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVerDetalle.setText("Ver detalle");
        pnlTablaHistorial.add(btnVerDetalle);
        btnVerDetalle.setBounds(540, 486, 120, 38);

        btnAnularCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAnularCompra.setText("Anular compra");
        pnlTablaHistorial.add(btnAnularCompra);
        btnAnularCompra.setBounds(674, 486, 135, 38);

        btnActualizarHistorial.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnActualizarHistorial.setText("Actualizar");
        pnlTablaHistorial.add(btnActualizarHistorial);
        btnActualizarHistorial.setBounds(823, 486, 120, 38);

        pnlHistorial.add(pnlTablaHistorial);
        pnlTablaHistorial.setBounds(0, 130, 959, 540);

        tabsCompras.addTab("tab2", pnlHistorial);

        add(tabsCompras);
        tabsCompras.setBounds(20, 60, 980, 720);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizarHistorial;
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnAnularCompra;
    private javax.swing.JButton btnGuardarCompra;
    private javax.swing.JButton btnNuevaCompra;
    private javax.swing.JButton btnQuitarProducto;
    private javax.swing.JButton btnVerDetalle;
    private javax.swing.JComboBox<String> cmbEstadoHistorial;
    private javax.swing.JComboBox<Producto> cmbProducto;
    private javax.swing.JComboBox<Proveedor> cmbProveedor;
    private javax.swing.JComboBox<String> cmbTipoPago;
    private javax.swing.JLabel lblAvisoSeries;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCantidadHistorial;
    private javax.swing.JLabel lblCosto;
    private javax.swing.JLabel lblDescuento;
    private javax.swing.JLabel lblDescuentoValor;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblDocumento;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JLabel lblProductosAgregados;
    private javax.swing.JLabel lblProductosAgregadosValor;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblStockProducto;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblSubtotalValor;
    private javax.swing.JLabel lblTipoPago;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloAgregar;
    private javax.swing.JLabel lblTituloDatos;
    private javax.swing.JLabel lblTituloDetalle;
    private javax.swing.JLabel lblTituloFiltros;
    private javax.swing.JLabel lblTituloHistorial;
    private javax.swing.JLabel lblTituloResumen;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JLabel lblUnidades;
    private javax.swing.JLabel lblUnidadesValor;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlAgregarProducto;
    private javax.swing.JPanel pnlDatosCompra;
    private javax.swing.JPanel pnlDetalleCompra;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFiltrosHistorial;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JPanel pnlNuevaCompra;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlTablaHistorial;
    private javax.swing.JScrollPane scrollDetalle;
    private javax.swing.JScrollPane scrollHistorial;
    private javax.swing.JScrollPane scrollObservaciones;
    private javax.swing.JTabbedPane tabsCompras;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtBuscarHistorial;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCostoUnitario;
    private javax.swing.JTextField txtDescuento;
    private javax.swing.JTextField txtFechaCompra;
    private javax.swing.JTextField txtFechaDesde;
    private javax.swing.JTextField txtFechaHasta;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtStockProducto;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
