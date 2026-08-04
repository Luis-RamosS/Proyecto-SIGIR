package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.InventarioControlador;
import sigir.modelo.Categoria;
import sigir.modelo.MovimientoInventario;
import sigir.modelo.Producto;
import sigir.modelo.ResumenInventario;
import sigir.util.FiltroTiempoReal;

public class InventarioPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    new Locale("es", "HN")
            );

    private final InventarioControlador controlador;
    private boolean iniciado;
    private boolean actualizandoControles;

    public InventarioPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();
        

        controlador = new InventarioControlador(this);
        configurarEventos();
        
        FiltroTiempoReal.activar(
            txtBuscarExistencias,
            controlador::buscarExistencias
        );
        
        FiltroTiempoReal.activar(
            txtBuscarMovimientos,
            controlador::buscarMovimientos
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

        cmbNivelStock.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "DISPONIBLE",
                            "STOCK_BAJO",
                            "AGOTADO",
                            "INACTIVO"
                        }
                )
        );

        cmbTipoMovimiento.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "ENTRADA_COMPRA",
                            "SALIDA_VENTA",
                            "SALIDA_REPARACION",
                            "AJUSTE_ENTRADA",
                            "AJUSTE_SALIDA",
                            "DEVOLUCION_CLIENTE",
                            "DEVOLUCION_PROVEEDOR"
                        }
                )
        );

        cmbTipoAjuste.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "AJUSTE_ENTRADA",
                            "AJUSTE_SALIDA"
                        }
                )
        );

        txtFechaDesde.setText(
                LocalDate.now()
                        .minusMonths(1)
                        .format(FORMATO_FECHA)
        );

        txtFechaHasta.setText(
                LocalDate.now().format(FORMATO_FECHA)
        );

        txtStockActualAjuste.setEditable(false);
        txtStockActualAjuste.setFocusable(false);

        tblExistencias.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tblExistencias.setAutoCreateRowSorter(true);
        tblExistencias.setFillsViewportHeight(true);

        tblMovimientos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tblMovimientos.setAutoCreateRowSorter(true);
        tblMovimientos.setFillsViewportHeight(true);
    }

    private void aplicarEstilos() {
        Color fondo = new Color(247, 249, 252);
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        setBackground(fondo);

        javax.swing.JPanel[] tarjetas = {
            pnlTotalProductos,
            pnlStockBajo,
            pnlAgotados,
            pnlMovimientosHoy,
            pnlValorInventario
        };

        for (javax.swing.JPanel tarjeta : tarjetas) {
            tarjeta.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borde),
                            BorderFactory.createEmptyBorder(
                                    8, 8, 8, 8
                            )
                    )
            );
        }

        javax.swing.JPanel[] paneles = {
            pnlFiltrosExistencias,
            pnlTablaExistencias,
            pnlFiltrosMovimientos,
            pnlTablaMovimientos,
            pnlFormularioAjuste,
            pnlAvisoAjuste
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

        javax.swing.JTextField[] campos = {
            txtBuscarExistencias,
            txtBuscarMovimientos,
            txtFechaDesde,
            txtFechaHasta,
            txtCantidadAjuste,
            txtStockActualAjuste
        };

        for (javax.swing.JTextField campo : campos) {
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

        txtMotivoAjuste.setBorder(
                BorderFactory.createEmptyBorder(
                        8, 8, 8, 8
                )
        );

        javax.swing.JButton[] botonesPrimarios = {
            btnRegistrarAjuste
        };

        for (javax.swing.JButton boton : botonesPrimarios) {
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

        javax.swing.JButton[] botonesSecundarios = {
            
            btnActualizarExistencias,
            btnActualizarMovimientos,
            btnLimpiarAjuste
        };

        for (javax.swing.JButton boton : botonesSecundarios) {
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

        estilizarTabla(tblExistencias);
        estilizarTabla(tblMovimientos);
    }

    private void estilizarTabla(javax.swing.JTable tabla) {
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

        JTableHeader cabecera = tabla.getTableHeader();

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
        

        btnActualizarExistencias.addActionListener(
                e -> controlador.recargarAsync()
        );

        

        cmbCategoria.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbCategoria.getItemCount() > 0) {

                controlador.buscarExistencias();
            }
        });

        cmbNivelStock.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbNivelStock.getItemCount() > 0) {

                controlador.buscarExistencias();
            }
        });

        

        btnActualizarMovimientos.addActionListener(
                e -> controlador.recargarAsync()
        );

        

        cmbTipoMovimiento.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbTipoMovimiento.getItemCount() > 0) {

                controlador.buscarMovimientos();
            }
        });

        cmbProductoAjuste.addActionListener(
                e -> controlador.seleccionarProductoAjuste()
        );

        btnRegistrarAjuste.addActionListener(
                e -> controlador.registrarAjuste()
        );

        btnLimpiarAjuste.addActionListener(
                e -> limpiarAjuste()
        );
    }

    public void mostrarResumen(ResumenInventario resumen) {
        lblTotalProductosValor.setText(
                String.valueOf(resumen.getTotalProductos())
        );

        lblStockBajoValor.setText(
                String.valueOf(resumen.getStockBajo())
        );

        lblAgotadosValor.setText(
                String.valueOf(resumen.getAgotados())
        );

        lblMovimientosHoyValor.setText(
                String.valueOf(resumen.getMovimientosHoy())
        );

        lblValorInventarioValor.setText(
                formatoMoneda.format(
                        resumen.getValorInventario()
                )
        );
    }

    public void cargarCategorias(
            List<Categoria> categorias) {

        Categoria seleccionada = getCategoriaFiltro();

        actualizandoControles = true;

        try {
            DefaultComboBoxModel<Categoria> modelo =
                    new DefaultComboBoxModel<>();

        modelo.addElement(
                new Categoria(
                        0,
                        "Todas las categorías"
                )
        );

        for (Categoria categoria : categorias) {
            modelo.addElement(categoria);
        }

        cmbCategoria.setModel(modelo);

            if (seleccionada != null) {
                seleccionarCategoria(
                        seleccionada.getIdCategoria()
                );
            }

        } finally {
            actualizandoControles = false;
        }
    }

    public Categoria getCategoriaFiltro() {
        Object seleccionado =
                cmbCategoria.getSelectedItem();

        return seleccionado instanceof Categoria categoria
                ? categoria
                : null;
    }

    public String getTextoBusquedaExistencias() {
        return txtBuscarExistencias.getText().trim();
    }

    public String getNivelStockFiltro() {
        Object valor = cmbNivelStock.getSelectedItem();

        return valor == null ? "TODOS" : valor.toString();
    }

    public void mostrarExistencias(
            List<Producto> productos) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "Código",
                            "Producto",
                            "Categoría",
                            "Marca",
                            "Stock actual",
                            "Stock mínimo",
                            "Costo",
                            "Estado de stock"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }

                    @Override
                    public Class<?> getColumnClass(
                            int columnIndex) {

                        return switch (columnIndex) {
                            case 4, 5 -> Integer.class;
                            default -> String.class;
                        };
                    }
                };

        for (Producto producto : productos) {
            modelo.addRow(new Object[]{
                producto.getCodigo(),
                producto.getNombre(),
                producto.getNombreCategoria(),
                texto(producto.getMarca()),
                producto.getStockActual(),
                producto.getStockMinimo(),
                formatoMoneda.format(
                        producto.getPrecioCompra()
                ),
                obtenerEstadoStock(producto)
            });
        }

        tblExistencias.setModel(modelo);

        if (tblExistencias.getColumnCount() >= 8) {
            tblExistencias.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(95);

            tblExistencias.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(220);

            tblExistencias.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(145);

            tblExistencias.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(100);

            tblExistencias.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(85);

            tblExistencias.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(85);

            tblExistencias.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(100);

            tblExistencias.getColumnModel()
                    .getColumn(7)
                    .setPreferredWidth(105);
        }

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        tblExistencias.getColumnModel()
                .getColumn(4)
                .setCellRenderer(centro);

        tblExistencias.getColumnModel()
                .getColumn(5)
                .setCellRenderer(centro);

        estilizarTabla(tblExistencias);
    }

    public void mostrarCantidadProductos(int cantidad) {
        lblCantidadProductos.setText(
                cantidad == 1
                        ? "Mostrando 1 producto"
                        : "Mostrando " + cantidad + " productos"
        );
    }

    public String getTextoBusquedaMovimientos() {
        return txtBuscarMovimientos.getText().trim();
    }

    public String getTipoMovimientoFiltro() {
        Object valor = cmbTipoMovimiento.getSelectedItem();

        return valor == null ? "TODOS" : valor.toString();
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

    public void mostrarMovimientos(
            List<MovimientoInventario> movimientos) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "ID",
                            "Fecha",
                            "Producto",
                            "Tipo",
                            "Cantidad",
                            "Stock anterior",
                            "Stock nuevo",
                            "Usuario",
                            "Origen",
                            "Motivo"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }

                    @Override
                    public Class<?> getColumnClass(
                            int columnIndex) {

                        return switch (columnIndex) {
                            case 0, 4, 5, 6 -> Integer.class;
                            default -> String.class;
                        };
                    }
                };

        DateTimeFormatter fechaHora =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm"
                );

        for (MovimientoInventario movimiento
                : movimientos) {

            modelo.addRow(new Object[]{
                movimiento.getIdMovimiento(),
                movimiento.getFechaMovimiento() == null
                        ? ""
                        : movimiento.getFechaMovimiento()
                                .format(fechaHora),
                movimiento.getCodigoProducto()
                        + " - "
                        + movimiento.getNombreProducto(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getNombreUsuario(),
                movimiento.getOrigen(),
                texto(movimiento.getMotivo())
            });
        }

        tblMovimientos.setModel(modelo);

        if (tblMovimientos.getColumnCount() >= 10) {
            tblMovimientos.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(55);

            tblMovimientos.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(125);

            tblMovimientos.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(220);

            tblMovimientos.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(145);

            tblMovimientos.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(75);

            tblMovimientos.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(90);

            tblMovimientos.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(90);

            tblMovimientos.getColumnModel()
                    .getColumn(7)
                    .setPreferredWidth(150);

            tblMovimientos.getColumnModel()
                    .getColumn(8)
                    .setPreferredWidth(100);

            tblMovimientos.getColumnModel()
                    .getColumn(9)
                    .setPreferredWidth(230);
        }

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        tblMovimientos.getColumnModel()
                .getColumn(4)
                .setCellRenderer(centro);

        tblMovimientos.getColumnModel()
                .getColumn(5)
                .setCellRenderer(centro);

        tblMovimientos.getColumnModel()
                .getColumn(6)
                .setCellRenderer(centro);

        estilizarTabla(tblMovimientos);
    }

    public void mostrarCantidadMovimientos(int cantidad) {
        lblCantidadMovimientos.setText(
                cantidad == 1
                        ? "Mostrando 1 movimiento"
                        : "Mostrando "
                        + cantidad
                        + " movimientos"
        );
    }

    public void cargarProductosAjustables(
            List<Producto> productos) {

        Producto seleccionado =
                getProductoAjusteSeleccionado();

        actualizandoControles = true;

        try {
            DefaultComboBoxModel<Producto> modelo =
                    new DefaultComboBoxModel<>();

        Producto opcion = new Producto();
        opcion.setIdProducto(0);
        opcion.setNombre(
                "Seleccione un producto..."
        );
        opcion.setCodigo("");

        modelo.addElement(opcion);

        for (Producto producto : productos) {
            modelo.addElement(producto);
        }

        cmbProductoAjuste.setModel(modelo);

            if (seleccionado != null) {
                seleccionarProductoAjuste(
                        seleccionado.getIdProducto()
                );
            }

        } finally {
            actualizandoControles = false;
        }
    }

    public Producto getProductoAjusteSeleccionado() {
        Object seleccionado =
                cmbProductoAjuste.getSelectedItem();

        return seleccionado instanceof Producto producto
                ? producto
                : null;
    }

    public String getTipoAjuste() {
        Object valor = cmbTipoAjuste.getSelectedItem();

        return valor == null ? "" : valor.toString();
    }

    public int getCantidadAjuste() {
        String valor = txtCantidadAjuste.getText().trim();

        try {
            return Integer.parseInt(valor);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser un número entero."
            );
        }
    }

    public String getMotivoAjuste() {
        return txtMotivoAjuste.getText().trim();
    }

    public void mostrarDatosProductoAjuste(
            int stockActual,
            boolean manejaSerie) {

        txtStockActualAjuste.setText(
                String.valueOf(stockActual)
        );

        lblAvisoSerieAjuste.setText(
                manejaSerie
                        ? "Este producto maneja números de serie "
                        + "y no admite ajustes manuales."
                        : "El producto permite ajuste manual autorizado."
        );

        btnRegistrarAjuste.setEnabled(
                btnRegistrarAjuste.isVisible()
                && !manejaSerie
        );
    }

    public void configurarPermisoAjustes(
            boolean permitido) {

        cmbProductoAjuste.setEnabled(permitido);
        cmbTipoAjuste.setEnabled(permitido);
        txtCantidadAjuste.setEnabled(permitido);
        txtMotivoAjuste.setEnabled(permitido);
        btnRegistrarAjuste.setVisible(permitido);
        btnLimpiarAjuste.setEnabled(permitido);

        lblPermisoAjuste.setText(
                permitido
                        ? "Los ajustes se registrarán con el usuario actual."
                        : "Solo el usuario con rol DUEÑO puede "
                        + "realizar ajustes manuales."
        );
    }

    public void limpiarAjuste() {
        if (cmbProductoAjuste.getItemCount() > 0) {
            cmbProductoAjuste.setSelectedIndex(0);
        }

        cmbTipoAjuste.setSelectedItem("AJUSTE_ENTRADA");
        txtCantidadAjuste.setText("1");
        txtStockActualAjuste.setText("0");
        txtMotivoAjuste.setText("");
        lblAvisoSerieAjuste.setText(
                "Selecciona un producto para ver sus datos."
        );
    }

    public void mostrarPestanaMovimientos() {
        tabsInventario.setSelectedIndex(1);
    }

    private String obtenerEstadoStock(
            Producto producto) {

        if ("INACTIVO".equalsIgnoreCase(
                producto.getEstado())) {

            return "INACTIVO";
        }

        if (producto.getStockActual() == 0) {
            return "AGOTADO";
        }

        if (producto.getStockActual()
                <= producto.getStockMinimo()) {

            return "STOCK BAJO";
        }

        return "DISPONIBLE";
    }

    private void seleccionarCategoria(
            int idCategoria) {

        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            Categoria categoria =
                    cmbCategoria.getItemAt(i);

            if (categoria != null
                    && categoria.getIdCategoria()
                    == idCategoria) {

                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarProductoAjuste(
            int idProducto) {

        for (int i = 0;
                i < cmbProductoAjuste.getItemCount();
                i++) {

            Producto producto =
                    cmbProductoAjuste.getItemAt(i);

            if (producto != null
                    && producto.getIdProducto()
                    == idProducto) {

                cmbProductoAjuste.setSelectedIndex(i);
                return;
            }
        }
    }

    private LocalDate convertirFechaOpcional(
            String texto,
            String nombreCampo) {

        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(
                    texto.trim(),
                    FORMATO_FECHA
            );

        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La " + nombreCampo
                    + " debe tener el formato dd/MM/yyyy."
            );
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlTotalProductos = new javax.swing.JPanel();
        lblTotalProductosTitulo = new javax.swing.JLabel();
        lblTotalProductosValor = new javax.swing.JLabel();
        pnlStockBajo = new javax.swing.JPanel();
        lblStockBajoTitulo = new javax.swing.JLabel();
        lblStockBajoValor = new javax.swing.JLabel();
        pnlAgotados = new javax.swing.JPanel();
        lblAgotadosTitulo = new javax.swing.JLabel();
        lblAgotadosValor = new javax.swing.JLabel();
        pnlMovimientosHoy = new javax.swing.JPanel();
        lblMovimientosHoyTitulo = new javax.swing.JLabel();
        lblMovimientosHoyValor = new javax.swing.JLabel();
        pnlValorInventario = new javax.swing.JPanel();
        lblValorInventarioTitulo = new javax.swing.JLabel();
        lblValorInventarioValor = new javax.swing.JLabel();
        tabsInventario = new javax.swing.JTabbedPane();
        pnlExistencias = new javax.swing.JPanel();
        pnlFiltrosExistencias = new javax.swing.JPanel();
        lblTituloFiltrosExistencias = new javax.swing.JLabel();
        txtBuscarExistencias = new javax.swing.JTextField();
        cmbCategoria = new javax.swing.JComboBox<>();
        cmbNivelStock = new javax.swing.JComboBox<>();
        btnActualizarExistencias = new javax.swing.JButton();
        pnlTablaExistencias = new javax.swing.JPanel();
        lblTituloTablaExistencias = new javax.swing.JLabel();
        scrollExistencias = new javax.swing.JScrollPane();
        tblExistencias = new javax.swing.JTable();
        lblCantidadProductos = new javax.swing.JLabel();
        pnlMovimientos = new javax.swing.JPanel();
        pnlFiltrosMovimientos = new javax.swing.JPanel();
        lblTituloFiltrosMovimientos = new javax.swing.JLabel();
        txtBuscarMovimientos = new javax.swing.JTextField();
        cmbTipoMovimiento = new javax.swing.JComboBox<>();
        lblFechaDesde = new javax.swing.JLabel();
        txtFechaDesde = new javax.swing.JTextField();
        lblFechaHasta = new javax.swing.JLabel();
        txtFechaHasta = new javax.swing.JTextField();
        btnActualizarMovimientos = new javax.swing.JButton();
        pnlTablaMovimientos = new javax.swing.JPanel();
        lblTituloTablaMovimientos = new javax.swing.JLabel();
        scrollMovimientos = new javax.swing.JScrollPane();
        tblMovimientos = new javax.swing.JTable();
        lblCantidadMovimientos = new javax.swing.JLabel();
        pnlAjusteManual = new javax.swing.JPanel();
        pnlFormularioAjuste = new javax.swing.JPanel();
        lblTituloAjuste = new javax.swing.JLabel();
        lblProductoAjuste = new javax.swing.JLabel();
        cmbProductoAjuste = new javax.swing.JComboBox<>();
        lblTipoAjuste = new javax.swing.JLabel();
        cmbTipoAjuste = new javax.swing.JComboBox<>();
        lblCantidadAjuste = new javax.swing.JLabel();
        txtCantidadAjuste = new javax.swing.JTextField();
        lblStockActualAjuste = new javax.swing.JLabel();
        txtStockActualAjuste = new javax.swing.JTextField();
        lblMotivoAjuste = new javax.swing.JLabel();
        scrollMotivoAjuste = new javax.swing.JScrollPane();
        txtMotivoAjuste = new javax.swing.JTextArea();
        lblAvisoSerieAjuste = new javax.swing.JLabel();
        btnLimpiarAjuste = new javax.swing.JButton();
        btnRegistrarAjuste = new javax.swing.JButton();
        pnlAvisoAjuste = new javax.swing.JPanel();
        lblTituloSeguridad = new javax.swing.JLabel();
        lblPermisoAjuste = new javax.swing.JLabel();
        lblDescripcionSeguridad = new javax.swing.JLabel();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Control de Inventario");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 390, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Monitorea existencias, niveles críticos y movimientos.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 620, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 12, 1110, 76);

        pnlTotalProductos.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotalProductos.setLayout(null);

        lblTotalProductosTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTotalProductosTitulo.setForeground(new java.awt.Color(75, 99, 132));
        lblTotalProductosTitulo.setText("Total productos");
        pnlTotalProductos.add(lblTotalProductosTitulo);
        lblTotalProductosTitulo.setBounds(16, 14, 170, 22);

        lblTotalProductosValor.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblTotalProductosValor.setForeground(new java.awt.Color(24, 50, 87));
        lblTotalProductosValor.setText("0");
        pnlTotalProductos.add(lblTotalProductosValor);
        lblTotalProductosValor.setBounds(16, 40, 170, 36);

        add(pnlTotalProductos);
        pnlTotalProductos.setBounds(28, 92, 200, 92);

        pnlStockBajo.setBackground(new java.awt.Color(255, 255, 255));
        pnlStockBajo.setLayout(null);

        lblStockBajoTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblStockBajoTitulo.setForeground(new java.awt.Color(75, 99, 132));
        lblStockBajoTitulo.setText("Stock bajo");
        pnlStockBajo.add(lblStockBajoTitulo);
        lblStockBajoTitulo.setBounds(16, 14, 170, 22);

        lblStockBajoValor.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblStockBajoValor.setForeground(new java.awt.Color(219, 127, 35));
        lblStockBajoValor.setText("0");
        pnlStockBajo.add(lblStockBajoValor);
        lblStockBajoValor.setBounds(16, 40, 170, 36);

        add(pnlStockBajo);
        pnlStockBajo.setBounds(240, 92, 200, 92);

        pnlAgotados.setBackground(new java.awt.Color(255, 255, 255));
        pnlAgotados.setLayout(null);

        lblAgotadosTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAgotadosTitulo.setForeground(new java.awt.Color(75, 99, 132));
        lblAgotadosTitulo.setText("Agotados");
        pnlAgotados.add(lblAgotadosTitulo);
        lblAgotadosTitulo.setBounds(16, 14, 170, 22);

        lblAgotadosValor.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblAgotadosValor.setForeground(new java.awt.Color(201, 57, 57));
        lblAgotadosValor.setText("0");
        pnlAgotados.add(lblAgotadosValor);
        lblAgotadosValor.setBounds(16, 40, 170, 36);

        add(pnlAgotados);
        pnlAgotados.setBounds(452, 92, 200, 92);

        pnlMovimientosHoy.setBackground(new java.awt.Color(255, 255, 255));
        pnlMovimientosHoy.setLayout(null);

        lblMovimientosHoyTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblMovimientosHoyTitulo.setForeground(new java.awt.Color(75, 99, 132));
        lblMovimientosHoyTitulo.setText("Movimientos hoy");
        pnlMovimientosHoy.add(lblMovimientosHoyTitulo);
        lblMovimientosHoyTitulo.setBounds(16, 14, 170, 22);

        lblMovimientosHoyValor.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblMovimientosHoyValor.setForeground(new java.awt.Color(101, 66, 188));
        lblMovimientosHoyValor.setText("0");
        pnlMovimientosHoy.add(lblMovimientosHoyValor);
        lblMovimientosHoyValor.setBounds(16, 40, 170, 36);

        add(pnlMovimientosHoy);
        pnlMovimientosHoy.setBounds(664, 92, 200, 92);

        pnlValorInventario.setBackground(new java.awt.Color(255, 255, 255));
        pnlValorInventario.setLayout(null);

        lblValorInventarioTitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblValorInventarioTitulo.setForeground(new java.awt.Color(75, 99, 132));
        lblValorInventarioTitulo.setText("Valor del inventario");
        pnlValorInventario.add(lblValorInventarioTitulo);
        lblValorInventarioTitulo.setBounds(16, 14, 220, 22);

        lblValorInventarioValor.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblValorInventarioValor.setForeground(new java.awt.Color(32, 137, 82));
        lblValorInventarioValor.setText("L 0.00");
        pnlValorInventario.add(lblValorInventarioValor);
        lblValorInventarioValor.setBounds(16, 40, 220, 36);

        add(pnlValorInventario);
        pnlValorInventario.setBounds(876, 92, 250, 92);

        tabsInventario.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N

        pnlExistencias.setBackground(new java.awt.Color(247, 249, 252));
        pnlExistencias.setLayout(null);

        pnlFiltrosExistencias.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltrosExistencias.setLayout(null);

        lblTituloFiltrosExistencias.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloFiltrosExistencias.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloFiltrosExistencias.setText("Filtros de existencias");
        pnlFiltrosExistencias.add(lblTituloFiltrosExistencias);
        lblTituloFiltrosExistencias.setBounds(16, 10, 230, 26);

        txtBuscarExistencias.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltrosExistencias.add(txtBuscarExistencias);
        txtBuscarExistencias.setBounds(16, 45, 300, 38);
        pnlFiltrosExistencias.add(cmbCategoria);
        cmbCategoria.setBounds(328, 45, 205, 38);
        pnlFiltrosExistencias.add(cmbNivelStock);
        cmbNivelStock.setBounds(545, 45, 170, 38);

        btnActualizarExistencias.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnActualizarExistencias.setText("Actualizar");
        pnlFiltrosExistencias.add(btnActualizarExistencias);
        btnActualizarExistencias.setBounds(839, 45, 110, 38);

        pnlExistencias.add(pnlFiltrosExistencias);
        pnlFiltrosExistencias.setBounds(0, 8, 1080, 100);

        pnlTablaExistencias.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaExistencias.setLayout(null);

        lblTituloTablaExistencias.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloTablaExistencias.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloTablaExistencias.setText("Existencias actuales");
        pnlTablaExistencias.add(lblTituloTablaExistencias);
        lblTituloTablaExistencias.setBounds(16, 10, 220, 26);

        tblExistencias.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollExistencias.setViewportView(tblExistencias);

        pnlTablaExistencias.add(scrollExistencias);
        scrollExistencias.setBounds(0, 42, 1080, 400);

        lblCantidadProductos.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidadProductos.setText("Mostrando 0 productos");
        pnlTablaExistencias.add(lblCantidadProductos);
        lblCantidadProductos.setBounds(16, 450, 300, 24);

        pnlExistencias.add(pnlTablaExistencias);
        pnlTablaExistencias.setBounds(0, 120, 1080, 485);

        tabsInventario.addTab("tab1", pnlExistencias);

        pnlMovimientos.setBackground(new java.awt.Color(247, 249, 252));
        pnlMovimientos.setLayout(null);

        pnlFiltrosMovimientos.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltrosMovimientos.setLayout(null);

        lblTituloFiltrosMovimientos.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloFiltrosMovimientos.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloFiltrosMovimientos.setText("Filtros de movimientos");
        pnlFiltrosMovimientos.add(lblTituloFiltrosMovimientos);
        lblTituloFiltrosMovimientos.setBounds(16, 10, 250, 26);

        txtBuscarMovimientos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltrosMovimientos.add(txtBuscarMovimientos);
        txtBuscarMovimientos.setBounds(16, 47, 250, 38);
        pnlFiltrosMovimientos.add(cmbTipoMovimiento);
        cmbTipoMovimiento.setBounds(278, 47, 205, 38);

        lblFechaDesde.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblFechaDesde.setForeground(new java.awt.Color(38, 64, 99));
        lblFechaDesde.setText("Desde");
        pnlFiltrosMovimientos.add(lblFechaDesde);
        lblFechaDesde.setBounds(495, 41, 80, 16);
        pnlFiltrosMovimientos.add(txtFechaDesde);
        txtFechaDesde.setBounds(495, 60, 125, 33);

        lblFechaHasta.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblFechaHasta.setForeground(new java.awt.Color(38, 64, 99));
        lblFechaHasta.setText("Hasta");
        pnlFiltrosMovimientos.add(lblFechaHasta);
        lblFechaHasta.setBounds(632, 41, 80, 16);
        pnlFiltrosMovimientos.add(txtFechaHasta);
        txtFechaHasta.setBounds(632, 60, 125, 33);

        btnActualizarMovimientos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnActualizarMovimientos.setText("Actualizar");
        pnlFiltrosMovimientos.add(btnActualizarMovimientos);
        btnActualizarMovimientos.setBounds(876, 47, 105, 38);

        pnlMovimientos.add(pnlFiltrosMovimientos);
        pnlFiltrosMovimientos.setBounds(0, 8, 1080, 105);

        pnlTablaMovimientos.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaMovimientos.setLayout(null);

        lblTituloTablaMovimientos.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTituloTablaMovimientos.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloTablaMovimientos.setText("Historial de movimientos");
        pnlTablaMovimientos.add(lblTituloTablaMovimientos);
        lblTituloTablaMovimientos.setBounds(16, 10, 260, 26);

        scrollMovimientos.setViewportView(tblMovimientos);

        pnlTablaMovimientos.add(scrollMovimientos);
        scrollMovimientos.setBounds(0, 42, 1080, 400);

        lblCantidadMovimientos.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidadMovimientos.setText("Mostrando 0 movimientos");
        pnlTablaMovimientos.add(lblCantidadMovimientos);
        lblCantidadMovimientos.setBounds(16, 450, 320, 24);

        pnlMovimientos.add(pnlTablaMovimientos);
        pnlTablaMovimientos.setBounds(0, 125, 1080, 485);

        tabsInventario.addTab("tab2", pnlMovimientos);

        pnlAjusteManual.setBackground(new java.awt.Color(247, 249, 252));
        pnlAjusteManual.setLayout(null);

        pnlFormularioAjuste.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormularioAjuste.setLayout(null);

        lblTituloAjuste.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTituloAjuste.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloAjuste.setText("Registrar ajuste manual");
        pnlFormularioAjuste.add(lblTituloAjuste);
        lblTituloAjuste.setBounds(18, 14, 300, 30);

        lblProductoAjuste.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblProductoAjuste.setForeground(new java.awt.Color(38, 64, 99));
        lblProductoAjuste.setText("Producto");
        pnlFormularioAjuste.add(lblProductoAjuste);
        lblProductoAjuste.setBounds(18, 58, 160, 18);

        cmbProductoAjuste.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFormularioAjuste.add(cmbProductoAjuste);
        cmbProductoAjuste.setBounds(18, 80, 480, 38);

        lblTipoAjuste.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTipoAjuste.setForeground(new java.awt.Color(38, 64, 99));
        lblTipoAjuste.setText("Tipo de ajuste");
        pnlFormularioAjuste.add(lblTipoAjuste);
        lblTipoAjuste.setBounds(18, 132, 160, 18);

        cmbTipoAjuste.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFormularioAjuste.add(cmbTipoAjuste);
        cmbTipoAjuste.setBounds(18, 154, 225, 38);

        lblCantidadAjuste.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCantidadAjuste.setForeground(new java.awt.Color(38, 64, 99));
        lblCantidadAjuste.setText("Cantidad");
        pnlFormularioAjuste.add(lblCantidadAjuste);
        lblCantidadAjuste.setBounds(255, 132, 100, 18);

        txtCantidadAjuste.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCantidadAjuste.setText("1");
        pnlFormularioAjuste.add(txtCantidadAjuste);
        txtCantidadAjuste.setBounds(255, 154, 110, 38);

        lblStockActualAjuste.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStockActualAjuste.setForeground(new java.awt.Color(38, 64, 99));
        lblStockActualAjuste.setText("Stock actual");
        pnlFormularioAjuste.add(lblStockActualAjuste);
        lblStockActualAjuste.setBounds(377, 132, 110, 18);

        txtStockActualAjuste.setBackground(new java.awt.Color(244, 247, 251));
        txtStockActualAjuste.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtStockActualAjuste.setText("0");
        pnlFormularioAjuste.add(txtStockActualAjuste);
        txtStockActualAjuste.setBounds(377, 154, 121, 38);

        lblMotivoAjuste.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMotivoAjuste.setForeground(new java.awt.Color(38, 64, 99));
        lblMotivoAjuste.setText("Motivo obligatorio");
        pnlFormularioAjuste.add(lblMotivoAjuste);
        lblMotivoAjuste.setBounds(18, 208, 180, 18);

        txtMotivoAjuste.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMotivoAjuste.setColumns(20);
        txtMotivoAjuste.setLineWrap(true);
        txtMotivoAjuste.setRows(5);
        txtMotivoAjuste.setWrapStyleWord(true);
        scrollMotivoAjuste.setViewportView(txtMotivoAjuste);

        pnlFormularioAjuste.add(scrollMotivoAjuste);
        scrollMotivoAjuste.setBounds(18, 230, 480, 115);

        lblAvisoSerieAjuste.setForeground(new java.awt.Color(176, 106, 20));
        lblAvisoSerieAjuste.setText("Selecciona un producto para ver sus datos.");
        pnlFormularioAjuste.add(lblAvisoSerieAjuste);
        lblAvisoSerieAjuste.setBounds(18, 354, 480, 24);

        btnLimpiarAjuste.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiarAjuste.setText("Limpiar");
        pnlFormularioAjuste.add(btnLimpiarAjuste);
        btnLimpiarAjuste.setBounds(18, 397, 145, 42);

        btnRegistrarAjuste.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRegistrarAjuste.setText("Registrar ajuste");
        pnlFormularioAjuste.add(btnRegistrarAjuste);
        btnRegistrarAjuste.setBounds(175, 397, 323, 42);

        pnlAjusteManual.add(pnlFormularioAjuste);
        pnlFormularioAjuste.setBounds(20, 28, 536, 470);

        pnlAvisoAjuste.setBackground(new java.awt.Color(255, 255, 255));
        pnlAvisoAjuste.setLayout(null);

        lblTituloSeguridad.setFont(new java.awt.Font("Segoe UI", 1, 19)); // NOI18N
        lblTituloSeguridad.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloSeguridad.setText("Seguridad de los ajustes");
        pnlAvisoAjuste.add(lblTituloSeguridad);
        lblTituloSeguridad.setBounds(22, 22, 330, 30);

        lblPermisoAjuste.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblPermisoAjuste.setForeground(new java.awt.Color(49, 105, 181));
        lblPermisoAjuste.setText("Solo el dueño puede realizar ajustes.");
        pnlAvisoAjuste.add(lblPermisoAjuste);
        lblPermisoAjuste.setBounds(22, 72, 440, 28);

        lblDescripcionSeguridad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDescripcionSeguridad.setForeground(new java.awt.Color(75, 99, 132));
        lblDescripcionSeguridad.setText("<html>Todo ajuste queda guardado con usuario, fecha, stock anterior, stock nuevo y motivo.<br><br>Las salidas que producirían stock negativo se bloquean. Los productos con número de serie no admiten ajustes manuales.</html>");
        pnlAvisoAjuste.add(lblDescripcionSeguridad);
        lblDescripcionSeguridad.setBounds(22, 118, 440, 200);

        pnlAjusteManual.add(pnlAvisoAjuste);
        pnlAvisoAjuste.setBounds(580, 28, 480, 350);

        tabsInventario.addTab("tab3", pnlAjusteManual);

        add(tabsInventario);
        tabsInventario.setBounds(28, 198, 1100, 650);
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnActualizarExistencias;
    private javax.swing.JButton btnActualizarMovimientos;
    private javax.swing.JButton btnLimpiarAjuste;
    private javax.swing.JButton btnRegistrarAjuste;
    private javax.swing.JComboBox<Categoria> cmbCategoria;
    private javax.swing.JComboBox<String> cmbNivelStock;
    private javax.swing.JComboBox<Producto> cmbProductoAjuste;
    private javax.swing.JComboBox<String> cmbTipoAjuste;
    private javax.swing.JComboBox<String> cmbTipoMovimiento;
    private javax.swing.JLabel lblAgotadosTitulo;
    private javax.swing.JLabel lblAgotadosValor;
    private javax.swing.JLabel lblAvisoSerieAjuste;
    private javax.swing.JLabel lblCantidadAjuste;
    private javax.swing.JLabel lblCantidadMovimientos;
    private javax.swing.JLabel lblCantidadProductos;
    private javax.swing.JLabel lblDescripcionSeguridad;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblMotivoAjuste;
    private javax.swing.JLabel lblMovimientosHoyTitulo;
    private javax.swing.JLabel lblMovimientosHoyValor;
    private javax.swing.JLabel lblPermisoAjuste;
    private javax.swing.JLabel lblProductoAjuste;
    private javax.swing.JLabel lblStockActualAjuste;
    private javax.swing.JLabel lblStockBajoTitulo;
    private javax.swing.JLabel lblStockBajoValor;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTipoAjuste;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloAjuste;
    private javax.swing.JLabel lblTituloFiltrosExistencias;
    private javax.swing.JLabel lblTituloFiltrosMovimientos;
    private javax.swing.JLabel lblTituloSeguridad;
    private javax.swing.JLabel lblTituloTablaExistencias;
    private javax.swing.JLabel lblTituloTablaMovimientos;
    private javax.swing.JLabel lblTotalProductosTitulo;
    private javax.swing.JLabel lblTotalProductosValor;
    private javax.swing.JLabel lblValorInventarioTitulo;
    private javax.swing.JLabel lblValorInventarioValor;
    private javax.swing.JPanel pnlAgotados;
    private javax.swing.JPanel pnlAjusteManual;
    private javax.swing.JPanel pnlAvisoAjuste;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlExistencias;
    private javax.swing.JPanel pnlFiltrosExistencias;
    private javax.swing.JPanel pnlFiltrosMovimientos;
    private javax.swing.JPanel pnlFormularioAjuste;
    private javax.swing.JPanel pnlMovimientos;
    private javax.swing.JPanel pnlMovimientosHoy;
    private javax.swing.JPanel pnlStockBajo;
    private javax.swing.JPanel pnlTablaExistencias;
    private javax.swing.JPanel pnlTablaMovimientos;
    private javax.swing.JPanel pnlTotalProductos;
    private javax.swing.JPanel pnlValorInventario;
    private javax.swing.JScrollPane scrollExistencias;
    private javax.swing.JScrollPane scrollMotivoAjuste;
    private javax.swing.JScrollPane scrollMovimientos;
    private javax.swing.JTabbedPane tabsInventario;
    private javax.swing.JTable tblExistencias;
    private javax.swing.JTable tblMovimientos;
    private javax.swing.JTextField txtBuscarExistencias;
    private javax.swing.JTextField txtBuscarMovimientos;
    private javax.swing.JTextField txtCantidadAjuste;
    private javax.swing.JTextField txtFechaDesde;
    private javax.swing.JTextField txtFechaHasta;
    private javax.swing.JTextArea txtMotivoAjuste;
    private javax.swing.JTextField txtStockActualAjuste;
    // End of variables declaration                   
}
