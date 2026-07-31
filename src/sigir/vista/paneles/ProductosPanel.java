package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.ProductoControlador;
import sigir.modelo.Categoria;
import sigir.modelo.Producto;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ProductosPanel extends javax.swing.JPanel {

    private final ProductoControlador controlador;
    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    new Locale("es", "HN")
            );

    public ProductosPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();
        configurarEventos();

        controlador = new ProductoControlador(this);
        
        configurarEventos();
        configurarBusquedaTiempoReal();
        
        controlador.iniciar();
    }

    public void recargar() {
        controlador.recargar();
    }

    private void configurarBusquedaTiempoReal() {

    Timer temporizadorBusqueda =
            new Timer(200, e -> controlador.buscar());

    temporizadorBusqueda.setRepeats(false);

    txtBuscar.getDocument().addDocumentListener(
            new DocumentListener() {

        private void actualizarBusqueda() {

            /*
             * Reinicia el temporizador cada vez que se escribe
             * o elimina una letra.
             */
            temporizadorBusqueda.restart();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            actualizarBusqueda();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            actualizarBusqueda();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            actualizarBusqueda();
        }
    });
}
    private void configurarComponentes() {
        formatoMoneda.setMinimumFractionDigits(2);
        formatoMoneda.setMaximumFractionDigits(2);

        tblProductos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tblProductos.setAutoCreateRowSorter(true);
        tblProductos.setFillsViewportHeight(true);

        txtStockActual.setEditable(false);
        txtStockActual.setFocusable(false);

        cmbEstado.setModel(new DefaultComboBoxModel<>(
                new String[]{"ACTIVO", "INACTIVO"}
        ));
    }

    private void aplicarEstilos() {
        Color fondo = new Color(247, 249, 252);
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        setBackground(fondo);

        pnlFiltros.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        pnlTabla.setBorder(BorderFactory.createLineBorder(borde));

        pnlDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        javax.swing.JTextField[] campos = {
            txtBuscar, txtCodigo, txtNombre, txtMarca, txtModelo,
            txtPrecioCompra, txtPrecioVenta, txtStockActual,
            txtStockMinimo
        };

        for (javax.swing.JTextField campo : campos) {
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(205, 216, 229)
                    ),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
            ));
        }

        txtDescripcion.setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );

        javax.swing.JButton[] botones = {
             btnNuevo, btnGuardar, btnDesactivar
        };

        for (javax.swing.JButton boton : botones) {
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
            boton.setFocusPainted(false);
        }

        

        btnNuevo.setBackground(azul);
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setBorderPainted(false);

        btnGuardar.setBackground(azul);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setBorderPainted(false);

        btnDesactivar.setBackground(Color.WHITE);
        btnDesactivar.setForeground(new Color(190, 55, 55));
        btnDesactivar.setBorder(
                BorderFactory.createLineBorder(
                        new Color(236, 170, 170)
                )
        );

        estilizarTabla();
    }

    private void estilizarTabla() {
        tblProductos.setRowHeight(42);
        tblProductos.setShowVerticalLines(false);
        tblProductos.setGridColor(new Color(232, 237, 243));
        tblProductos.setSelectionBackground(
                new Color(229, 239, 252)
        );
        tblProductos.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera = tblProductos.getTableHeader();
        cabecera.setBackground(new Color(248, 250, 253));
        cabecera.setForeground(new Color(34, 59, 94));
        cabecera.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        cabecera.setReorderingAllowed(false);

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(SwingConstants.CENTER);

        if (tblProductos.getColumnCount() >= 8) {
            tblProductos.getColumnModel()
                    .getColumn(5)
                    .setCellRenderer(centro);

            tblProductos.getColumnModel()
                    .getColumn(7)
                    .setCellRenderer(centro);
        }
    }

    private void configurarEventos() {
        
        btnNuevo.addActionListener(e -> controlador.nuevo());
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnDesactivar.addActionListener(
                e -> controlador.desactivar()
        );

        

        cmbFiltroCategoria.addActionListener(e -> {
            if (cmbFiltroCategoria.getItemCount() > 0) {
                controlador.buscar();
            }
        });

        tblProductos.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        controlador.seleccionarFila();
                    }
                });
    }

    public String getTextoBusqueda() {
        return txtBuscar.getText().trim();
    }

    public Categoria getCategoriaFiltro() {
        Object seleccionado =
                cmbFiltroCategoria.getSelectedItem();

        return seleccionado instanceof Categoria categoria
                ? categoria
                : null;
    }

    public void cargarCategorias(
            List<Categoria> categorias) {

        Categoria seleccionFiltro =
                getCategoriaFiltro();

        Categoria seleccionFormulario =
                getCategoriaFormulario();

        DefaultComboBoxModel<Categoria> modeloFiltro =
                new DefaultComboBoxModel<>();

        modeloFiltro.addElement(
                new Categoria(0, "Todas las categorías")
        );

        DefaultComboBoxModel<Categoria> modeloFormulario =
                new DefaultComboBoxModel<>();

        modeloFormulario.addElement(
                new Categoria(0, "Seleccione...")
        );

        for (Categoria categoria : categorias) {
            modeloFiltro.addElement(categoria);
            modeloFormulario.addElement(categoria);
        }

        cmbFiltroCategoria.setModel(modeloFiltro);
        cmbCategoria.setModel(modeloFormulario);

        seleccionarCategoria(
                cmbFiltroCategoria,
                seleccionFiltro == null
                        ? 0
                        : seleccionFiltro.getIdCategoria()
        );

        seleccionarCategoria(
                cmbCategoria,
                seleccionFormulario == null
                        ? 0
                        : seleccionFormulario.getIdCategoria()
        );
    }

    public void mostrarProductos(
            List<Producto> productos) {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                    "ID",
                    "Código",
                    "Producto",
                    "Categoría",
                    "Marca",
                    "Stock",
                    "P. venta",
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

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 5 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        for (Producto producto : productos) {
            modelo.addRow(new Object[]{
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getNombreCategoria(),
                texto(producto.getMarca()),
                producto.getStockActual(),
                formatoMoneda.format(
                        producto.getPrecioVenta()
                ),
                producto.getEstado()
            });
        }

        tblProductos.setModel(modelo);

        if (tblProductos.getColumnCount() > 0) {
            tblProductos.removeColumn(
                    tblProductos.getColumnModel().getColumn(0)
            );
        }

        if (tblProductos.getColumnCount() >= 7) {
            tblProductos.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(90);

            tblProductos.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(190);

            tblProductos.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(145);

            tblProductos.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(100);

            tblProductos.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(65);

            tblProductos.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(90);

            tblProductos.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(80);
        }

        estilizarTabla();
    }

    public Producto obtenerProductoFormulario() {

        Producto producto = new Producto();

        Categoria categoria = getCategoriaFormulario();

        producto.setIdCategoria(
                categoria == null
                        ? 0
                        : categoria.getIdCategoria()
        );

        producto.setCodigo(
                txtCodigo.getText().trim().toUpperCase()
        );

        producto.setNombre(txtNombre.getText().trim());
        producto.setMarca(txtMarca.getText().trim());
        producto.setModelo(txtModelo.getText().trim());
        producto.setDescripcion(
                txtDescripcion.getText().trim()
        );

        producto.setPrecioCompra(
                convertirDecimal(
                        txtPrecioCompra.getText(),
                        "precio de compra"
                )
        );

        producto.setPrecioVenta(
                convertirDecimal(
                        txtPrecioVenta.getText(),
                        "precio de venta"
                )
        );

        producto.setStockMinimo(
                convertirEntero(
                        txtStockMinimo.getText(),
                        "stock mínimo"
                )
        );

        producto.setManejaNumeroSerie(
                chkNumeroSerie.isSelected()
        );

        producto.setEstado(
                String.valueOf(cmbEstado.getSelectedItem())
        );

        return producto;
    }

    public void mostrarProducto(Producto producto) {
        txtCodigo.setText(texto(producto.getCodigo()));
        txtNombre.setText(texto(producto.getNombre()));
        txtMarca.setText(texto(producto.getMarca()));
        txtModelo.setText(texto(producto.getModelo()));

        txtPrecioCompra.setText(
                numero(producto.getPrecioCompra())
        );

        txtPrecioVenta.setText(
                numero(producto.getPrecioVenta())
        );

        txtStockActual.setText(
                String.valueOf(producto.getStockActual())
        );

        txtStockMinimo.setText(
                String.valueOf(producto.getStockMinimo())
        );

        txtDescripcion.setText(
                texto(producto.getDescripcion())
        );

        chkNumeroSerie.setSelected(
                producto.isManejaNumeroSerie()
        );

        cmbEstado.setSelectedItem(producto.getEstado());

        seleccionarCategoria(
                cmbCategoria,
                producto.getIdCategoria()
        );
    }

    public void limpiarFormulario() {
        tblProductos.clearSelection();

        txtCodigo.setText("");
        txtNombre.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtPrecioCompra.setText("0.00");
        txtPrecioVenta.setText("0.00");
        txtStockActual.setText("0");
        txtStockMinimo.setText("1");
        txtDescripcion.setText("");

        chkNumeroSerie.setSelected(false);
        cmbEstado.setSelectedItem("ACTIVO");

        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }

        txtCodigo.requestFocusInWindow();
    }

    public void setModoEdicion(boolean editando) {
        lblTituloDetalle.setText(
                editando
                        ? "Detalle del producto"
                        : "Nuevo producto"
        );

        btnGuardar.setText(
                editando
                        ? "Guardar cambios"
                        : "Registrar producto"
        );

        btnDesactivar.setEnabled(editando);
    }

    public int getFilaSeleccionadaModelo() {
        int filaVista = tblProductos.getSelectedRow();

        return filaVista < 0
                ? -1
                : tblProductos.convertRowIndexToModel(
                        filaVista
                );
    }

    public void seleccionarFilaModelo(int filaModelo) {
        if (filaModelo < 0
                || filaModelo >= tblProductos.getModel()
                        .getRowCount()) {

            return;
        }

        int filaVista =
                tblProductos.convertRowIndexToView(
                        filaModelo
                );

        tblProductos.setRowSelectionInterval(
                filaVista,
                filaVista
        );

        tblProductos.scrollRectToVisible(
                tblProductos.getCellRect(
                        filaVista,
                        0,
                        true
                )
        );
    }

    public void mostrarCantidad(int cantidad) {
        lblCantidad.setText(
                cantidad == 1
                        ? "Mostrando 1 producto"
                        : "Mostrando " + cantidad + " productos"
        );
    }

    public void enfocarCodigo() {
        txtCodigo.requestFocusInWindow();
    }

    public void enfocarNombre() {
        txtNombre.requestFocusInWindow();
    }

    public void enfocarCategoria() {
        cmbCategoria.requestFocusInWindow();
    }

    private Categoria getCategoriaFormulario() {
        Object seleccionado = cmbCategoria.getSelectedItem();

        return seleccionado instanceof Categoria categoria
                ? categoria
                : null;
    }

    private void seleccionarCategoria(
            javax.swing.JComboBox<Categoria> combo,
            int idCategoria) {

        for (int i = 0; i < combo.getItemCount(); i++) {
            Categoria categoria = combo.getItemAt(i);

            if (categoria != null
                    && categoria.getIdCategoria()
                    == idCategoria) {

                combo.setSelectedIndex(i);
                return;
            }
        }

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private BigDecimal convertirDecimal(
            String texto,
            String nombreCampo) {

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
                    .setScale(2, RoundingMode.HALF_UP);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El " + nombreCampo
                    + " debe ser un número válido."
            );
        }
    }

    private int convertirEntero(
            String texto,
            String nombreCampo) {

        String valor = texto == null ? "" : texto.trim();

        if (valor.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(valor);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El " + nombreCampo
                    + " debe ser un número entero."
            );
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private String numero(BigDecimal valor) {
        return valor == null
                ? "0.00"
                : valor.setScale(
                        2,
                        RoundingMode.HALF_UP
                ).toPlainString();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlFiltros = new javax.swing.JPanel();
        txtBuscar = new javax.swing.JTextField();
        cmbFiltroCategoria = new javax.swing.JComboBox<>();
        btnNuevo = new javax.swing.JButton();
        pnlTabla = new javax.swing.JPanel();
        scrollProductos = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        lblCantidad = new javax.swing.JLabel();
        pnlDetalle = new javax.swing.JPanel();
        lblTituloDetalle = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        lblMarca = new javax.swing.JLabel();
        txtMarca = new javax.swing.JTextField();
        lblModelo = new javax.swing.JLabel();
        txtModelo = new javax.swing.JTextField();
        lblPrecioCompra = new javax.swing.JLabel();
        txtPrecioCompra = new javax.swing.JTextField();
        lblPrecioVenta = new javax.swing.JLabel();
        txtPrecioVenta = new javax.swing.JTextField();
        lblStockActual = new javax.swing.JLabel();
        txtStockActual = new javax.swing.JTextField();
        lblStockMinimo = new javax.swing.JLabel();
        txtStockMinimo = new javax.swing.JTextField();
        chkNumeroSerie = new javax.swing.JCheckBox();
        lblDescripcion = new javax.swing.JLabel();
        scrollDescripcion = new javax.swing.JScrollPane();
        txtDescripcion = new javax.swing.JTextArea();
        lblEstado = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox<>();
        btnGuardar = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Gestión de Productos");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 390, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Administra el catálogo de productos de tu inventario.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 520, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 18, 1050, 76);

        pnlFiltros.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltros.setLayout(null);

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(36, 64, 101));
        txtBuscar.setToolTipText("Buscar por código, nombre, marca o modelo");
        pnlFiltros.add(txtBuscar);
        txtBuscar.setBounds(20, 10, 380, 42);

        cmbFiltroCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlFiltros.add(cmbFiltroCategoria);
        cmbFiltroCategoria.setBounds(410, 10, 190, 42);

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setText("+ Nuevo producto");
        pnlFiltros.add(btnNuevo);
        btnNuevo.setBounds(620, 10, 150, 42);

        add(pnlFiltros);
        pnlFiltros.setBounds(28, 98, 780, 70);

        pnlTabla.setBackground(new java.awt.Color(255, 255, 255));
        pnlTabla.setLayout(null);

        tblProductos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollProductos.setViewportView(tblProductos);

        pnlTabla.add(scrollProductos);
        scrollProductos.setBounds(0, 0, 780, 476);

        lblCantidad.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidad.setText("Mostrando 0 productos");
        pnlTabla.add(lblCantidad);
        lblCantidad.setBounds(16, 484, 280, 24);

        add(pnlTabla);
        pnlTabla.setBounds(28, 180, 780, 516);

        pnlDetalle.setBackground(new java.awt.Color(255, 255, 255));
        pnlDetalle.setLayout(null);

        lblTituloDetalle.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloDetalle.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloDetalle.setText("Nuevo producto");
        pnlDetalle.add(lblTituloDetalle);
        lblTituloDetalle.setBounds(18, 12, 260, 28);

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCodigo.setForeground(new java.awt.Color(38, 64, 99));
        lblCodigo.setText("Código");
        pnlDetalle.add(lblCodigo);
        lblCodigo.setBounds(18, 48, 120, 18);

        txtCodigo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDetalle.add(txtCodigo);
        txtCodigo.setBounds(18, 69, 150, 36);

        lblNombre.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(38, 64, 99));
        lblNombre.setText("Nombre del producto");
        pnlDetalle.add(lblNombre);
        lblNombre.setBounds(178, 48, 170, 18);

        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDetalle.add(txtNombre);
        txtNombre.setBounds(178, 69, 308, 36);

        lblCategoria.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCategoria.setForeground(new java.awt.Color(38, 64, 99));
        lblCategoria.setText("Categoría");
        pnlDetalle.add(lblCategoria);
        lblCategoria.setBounds(18, 112, 150, 18);

        cmbCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDetalle.add(cmbCategoria);
        cmbCategoria.setBounds(18, 133, 468, 36);

        lblMarca.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMarca.setForeground(new java.awt.Color(38, 64, 99));
        lblMarca.setText("Marca");
        pnlDetalle.add(lblMarca);
        lblMarca.setBounds(18, 176, 150, 18);

        txtMarca.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDetalle.add(txtMarca);
        txtMarca.setBounds(18, 197, 225, 36);

        lblModelo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblModelo.setForeground(new java.awt.Color(38, 64, 99));
        lblModelo.setText("Modelo");
        pnlDetalle.add(lblModelo);
        lblModelo.setBounds(253, 176, 150, 18);

        txtModelo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDetalle.add(txtModelo);
        txtModelo.setBounds(253, 197, 233, 36);

        lblPrecioCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPrecioCompra.setForeground(new java.awt.Color(38, 64, 99));
        lblPrecioCompra.setText("Precio compra");
        pnlDetalle.add(lblPrecioCompra);
        lblPrecioCompra.setBounds(18, 240, 150, 18);

        txtPrecioCompra.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPrecioCompra.setText("0.00");
        pnlDetalle.add(txtPrecioCompra);
        txtPrecioCompra.setBounds(18, 261, 150, 36);

        lblPrecioVenta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPrecioVenta.setForeground(new java.awt.Color(38, 64, 99));
        lblPrecioVenta.setText("Precio venta");
        pnlDetalle.add(lblPrecioVenta);
        lblPrecioVenta.setBounds(178, 240, 150, 18);

        txtPrecioVenta.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPrecioVenta.setText("0.00");
        pnlDetalle.add(txtPrecioVenta);
        txtPrecioVenta.setBounds(178, 261, 150, 36);

        lblStockActual.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStockActual.setForeground(new java.awt.Color(38, 64, 99));
        lblStockActual.setText("Stock actual");
        pnlDetalle.add(lblStockActual);
        lblStockActual.setBounds(338, 240, 110, 18);

        txtStockActual.setBackground(new java.awt.Color(244, 247, 251));
        txtStockActual.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtStockActual.setText("0");
        pnlDetalle.add(txtStockActual);
        txtStockActual.setBounds(338, 261, 68, 36);

        lblStockMinimo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStockMinimo.setForeground(new java.awt.Color(38, 64, 99));
        lblStockMinimo.setText("Stock mín.");
        pnlDetalle.add(lblStockMinimo);
        lblStockMinimo.setBounds(416, 240, 70, 18);

        txtStockMinimo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtStockMinimo.setText("1");
        pnlDetalle.add(txtStockMinimo);
        txtStockMinimo.setBounds(416, 261, 70, 36);

        chkNumeroSerie.setBackground(new java.awt.Color(255, 255, 255));
        chkNumeroSerie.setForeground(new java.awt.Color(38, 64, 99));
        chkNumeroSerie.setText("El producto maneja número de serie");
        pnlDetalle.add(chkNumeroSerie);
        chkNumeroSerie.setBounds(18, 306, 280, 26);

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDescripcion.setForeground(new java.awt.Color(38, 64, 99));
        lblDescripcion.setText("Descripción");
        pnlDetalle.add(lblDescripcion);
        lblDescripcion.setBounds(18, 338, 150, 18);

        txtDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtDescripcion.setColumns(20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setRows(5);
        txtDescripcion.setWrapStyleWord(true);
        scrollDescripcion.setViewportView(txtDescripcion);

        pnlDetalle.add(scrollDescripcion);
        scrollDescripcion.setBounds(18, 359, 468, 88);

        lblEstado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(38, 64, 99));
        lblEstado.setText("Estado");
        pnlDetalle.add(lblEstado);
        lblEstado.setBounds(18, 456, 100, 18);

        cmbEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlDetalle.add(cmbEstado);
        cmbEstado.setBounds(18, 477, 468, 36);

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setText("Registrar producto");
        pnlDetalle.add(btnGuardar);
        btnGuardar.setBounds(18, 532, 250, 44);

        btnDesactivar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDesactivar.setText("Desactivar");
        btnDesactivar.setEnabled(false);
        pnlDetalle.add(btnDesactivar);
        btnDesactivar.setBounds(278, 532, 208, 44);

        add(pnlDetalle);
        pnlDetalle.setBounds(824, 98, 504, 598);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JCheckBox chkNumeroSerie;
    private javax.swing.JComboBox<Categoria> cmbCategoria;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<Categoria> cmbFiltroCategoria;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblModelo;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPrecioCompra;
    private javax.swing.JLabel lblPrecioVenta;
    private javax.swing.JLabel lblStockActual;
    private javax.swing.JLabel lblStockMinimo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloDetalle;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlTabla;
    private javax.swing.JScrollPane scrollDescripcion;
    private javax.swing.JScrollPane scrollProductos;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextArea txtDescripcion;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtModelo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecioCompra;
    private javax.swing.JTextField txtPrecioVenta;
    private javax.swing.JTextField txtStockActual;
    private javax.swing.JTextField txtStockMinimo;
    // End of variables declaration//GEN-END:variables
}
