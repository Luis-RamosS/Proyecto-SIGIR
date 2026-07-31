package sigir.vista.dialogos;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.modelo.Producto;

public class BusquedaProductoCompraDialog
        extends javax.swing.JDialog {

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    new Locale("es", "HN")
            );

    private final List<Producto> productos;
    private List<Producto> productosFiltrados =
            new ArrayList<>();

    private Producto productoSeleccionado;

    public BusquedaProductoCompraDialog(
            Window propietario,
            List<Producto> productos) {

        super(
                propietario,
                "Búsqueda avanzada de productos",
                ModalityType.APPLICATION_MODAL
        );

        initComponents();

        this.productos = productos == null
                ? new ArrayList<>()
                : new ArrayList<>(productos);

        configurarComponentes();
        aplicarEstilos();
        configurarEventos();
        cargarFiltros();
        aplicarFiltros();

        setLocationRelativeTo(propietario);
    }

    public Producto mostrarDialogo() {
        setVisible(true);
        return productoSeleccionado;
    }

    private void configurarComponentes() {
        formatoMoneda.setMinimumFractionDigits(2);
        formatoMoneda.setMaximumFractionDigits(2);

        setDefaultCloseOperation(
                javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        );

        setResizable(false);

        tblProductos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblProductos.setAutoCreateRowSorter(true);
        tblProductos.setFillsViewportHeight(true);
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        pnlFiltros.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borde),
                        BorderFactory.createEmptyBorder(
                                8, 8, 8, 8
                        )
                )
        );

        pnlResultados.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borde),
                        BorderFactory.createEmptyBorder(
                                8, 8, 8, 8
                        )
                )
        );

        txtBuscar.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(205, 216, 229)
                        ),
                        BorderFactory.createEmptyBorder(
                                0, 10, 0, 10
                        )
                )
        );

        btnSeleccionar.setBackground(azul);
        btnSeleccionar.setForeground(Color.WHITE);
        btnSeleccionar.setBorderPainted(false);
        btnSeleccionar.setFocusPainted(false);

        javax.swing.JButton[] secundarios = {
            btnLimpiar,
            btnCancelar
        };

        for (javax.swing.JButton boton : secundarios) {
            boton.setBackground(Color.WHITE);
            boton.setForeground(texto);
            boton.setBorder(
                    BorderFactory.createLineBorder(borde)
            );
            boton.setFocusPainted(false);
        }

        javax.swing.JButton[] botones = {
            btnSeleccionar,
            btnLimpiar,
            btnCancelar
        };

        for (javax.swing.JButton boton : botones) {
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        estilizarTabla();
    }

    private void estilizarTabla() {
        tblProductos.setRowHeight(38);
        tblProductos.setShowVerticalLines(false);
        tblProductos.setGridColor(
                new Color(232, 237, 243)
        );
        tblProductos.setSelectionBackground(
                new Color(229, 239, 252)
        );
        tblProductos.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera =
                tblProductos.getTableHeader();

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
        txtBuscar.getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        aplicarFiltros();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        aplicarFiltros();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        aplicarFiltros();
                    }
                });

        cmbCategoria.addActionListener(
                e -> aplicarFiltros()
        );

        cmbExistencia.addActionListener(
                e -> aplicarFiltros()
        );

        cmbNumerosSerie.addActionListener(
                e -> aplicarFiltros()
        );

        btnLimpiar.addActionListener(
                e -> limpiarFiltros()
        );

        btnSeleccionar.addActionListener(
                e -> confirmarSeleccion()
        );

        btnCancelar.addActionListener(e -> {
            productoSeleccionado = null;
            dispose();
        });

        tblProductos.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e) {

                        if (e.getClickCount() == 2
                                && tblProductos.getSelectedRow()
                                >= 0) {

                            confirmarSeleccion();
                        }
                    }
                }
        );
    }

    private void cargarFiltros() {
        DefaultComboBoxModel<String> categorias =
                new DefaultComboBoxModel<>();

        categorias.addElement("TODAS");

        productos.stream()
                .map(Producto::getNombreCategoria)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(valor -> !valor.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(categorias::addElement);

        cmbCategoria.setModel(categorias);

        cmbExistencia.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "CON EXISTENCIA",
                            "SIN EXISTENCIA",
                            "STOCK BAJO"
                        }
                )
        );

        cmbNumerosSerie.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "CON NÚMERO DE SERIE",
                            "SIN NÚMERO DE SERIE"
                        }
                )
        );
    }

    private void aplicarFiltros() {
        String texto =
                normalizar(txtBuscar.getText());

        String categoria =
                valorCombo(cmbCategoria);

        String existencia =
                valorCombo(cmbExistencia);

        String serie =
                valorCombo(cmbNumerosSerie);

        productosFiltrados =
                productos.stream()
                        .filter(producto ->
                                coincideTexto(
                                        producto,
                                        texto
                                )
                        )
                        .filter(producto ->
                                coincideCategoria(
                                        producto,
                                        categoria
                                )
                        )
                        .filter(producto ->
                                coincideExistencia(
                                        producto,
                                        existencia
                                )
                        )
                        .filter(producto ->
                                coincideSerie(
                                        producto,
                                        serie
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        producto ->
                                                texto(
                                                        producto.getNombre()
                                                ),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        mostrarResultados();
    }

    private boolean coincideTexto(
            Producto producto,
            String filtro) {

        if (filtro.isBlank()) {
            return true;
        }

        String contenido =
                texto(producto.getCodigo())
                + " "
                + texto(producto.getNombre())
                + " "
                + texto(producto.getDescripcion())
                + " "
                + texto(producto.getMarca())
                + " "
                + texto(producto.getModelo())
                + " "
                + texto(producto.getNombreCategoria());

        return normalizar(contenido)
                .contains(filtro);
    }

    private boolean coincideCategoria(
            Producto producto,
            String categoria) {

        return "TODAS".equals(categoria)
                || categoria.equalsIgnoreCase(
                        texto(
                                producto.getNombreCategoria()
                        )
                );
    }

    private boolean coincideExistencia(
            Producto producto,
            String existencia) {

        return switch (existencia) {
            case "CON EXISTENCIA" ->
                producto.getStockActual() > 0;

            case "SIN EXISTENCIA" ->
                producto.getStockActual() == 0;

            case "STOCK BAJO" ->
                producto.getStockActual()
                <= producto.getStockMinimo();

            default -> true;
        };
    }

    private boolean coincideSerie(
            Producto producto,
            String serie) {

        return switch (serie) {
            case "CON NÚMERO DE SERIE" ->
                producto.isManejaNumeroSerie();

            case "SIN NÚMERO DE SERIE" ->
                !producto.isManejaNumeroSerie();

            default -> true;
        };
    }

    private void mostrarResultados() {
        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "Código",
                            "Producto",
                            "Categoría",
                            "Marca / Modelo",
                            "Stock",
                            "Stock mínimo",
                            "Último costo",
                            "Serie"
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

                        return columnIndex == 4
                                || columnIndex == 5
                                ? Integer.class
                                : String.class;
                    }
                };

        for (Producto producto
                : productosFiltrados) {

            modelo.addRow(new Object[]{
                producto.getCodigo(),
                producto.getNombre(),
                texto(producto.getNombreCategoria()),
                unirMarcaModelo(producto),
                producto.getStockActual(),
                producto.getStockMinimo(),
                formatearMoneda(
                        producto.getPrecioCompra()
                ),
                producto.isManejaNumeroSerie()
                        ? "Sí"
                        : "No"
            });
        }

        tblProductos.setModel(modelo);

        if (tblProductos.getColumnCount() >= 8) {
            tblProductos.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(85);

            tblProductos.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(215);

            tblProductos.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(130);

            tblProductos.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(160);

            tblProductos.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(65);

            tblProductos.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(85);

            tblProductos.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(105);

            tblProductos.getColumnModel()
                    .getColumn(7)
                    .setPreferredWidth(65);
        }

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        tblProductos.getColumnModel()
                .getColumn(4)
                .setCellRenderer(centro);

        tblProductos.getColumnModel()
                .getColumn(5)
                .setCellRenderer(centro);

        tblProductos.getColumnModel()
                .getColumn(7)
                .setCellRenderer(centro);

        lblCantidadResultados.setText(
                productosFiltrados.size() == 1
                        ? "1 producto encontrado"
                        : productosFiltrados.size()
                        + " productos encontrados"
        );

        estilizarTabla();
    }

    private void confirmarSeleccion() {
        int filaVista =
                tblProductos.getSelectedRow();

        if (filaVista < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un producto de la tabla.",
                    "Producto no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int filaModelo =
                tblProductos.convertRowIndexToModel(
                        filaVista
                );

        if (filaModelo < 0
                || filaModelo
                >= productosFiltrados.size()) {

            return;
        }

        productoSeleccionado =
                productosFiltrados.get(filaModelo);

        dispose();
    }

    private void limpiarFiltros() {
        txtBuscar.setText("");

        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }

        if (cmbExistencia.getItemCount() > 0) {
            cmbExistencia.setSelectedIndex(0);
        }

        if (cmbNumerosSerie.getItemCount() > 0) {
            cmbNumerosSerie.setSelectedIndex(0);
        }

        txtBuscar.requestFocusInWindow();
        aplicarFiltros();
    }

    private String valorCombo(
            javax.swing.JComboBox<String> combo) {

        Object valor = combo.getSelectedItem();

        return valor == null
                ? "TODOS"
                : valor.toString();
    }

    private String unirMarcaModelo(
            Producto producto) {

        String marca =
                texto(producto.getMarca()).trim();

        String modelo =
                texto(producto.getModelo()).trim();

        if (marca.isBlank()) {
            return modelo;
        }

        if (modelo.isBlank()) {
            return marca;
        }

        return marca + " / " + modelo;
    }

    private String formatearMoneda(
            BigDecimal valor) {

        return formatoMoneda.format(
                valor == null
                        ? BigDecimal.ZERO
                        : valor
        );
    }

    private String normalizar(String valor) {
        return texto(valor)
                .trim()
                .toLowerCase();
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
        pnlFiltros = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        lblExistencia = new javax.swing.JLabel();
        cmbExistencia = new javax.swing.JComboBox<>();
        lblNumerosSerie = new javax.swing.JLabel();
        cmbNumerosSerie = new javax.swing.JComboBox<>();
        btnLimpiar = new javax.swing.JButton();
        pnlResultados = new javax.swing.JPanel();
        scrollProductos = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        lblCantidadResultados = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        btnSeleccionar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(970, 620));
        setPreferredSize(new java.awt.Dimension(970, 620));
        getContentPane().setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Búsqueda avanzada de productos");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 3, 470, 34);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Busca por código, nombre, descripción, marca o modelo.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 38, 660, 22);

        getContentPane().add(pnlEncabezado);
        pnlEncabezado.setBounds(24, 12, 910, 66);

        pnlFiltros.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltros.setLayout(null);

        lblBuscar.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblBuscar.setText("Buscar");
        pnlFiltros.add(lblBuscar);
        lblBuscar.setBounds(14, 12, 90, 16);

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 12));
        pnlFiltros.add(txtBuscar);
        txtBuscar.setBounds(14, 30, 270, 34);

        lblCategoria.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblCategoria.setText("Categoría");
        pnlFiltros.add(lblCategoria);
        lblCategoria.setBounds(296, 12, 100, 16);

        pnlFiltros.add(cmbCategoria);
        cmbCategoria.setBounds(296, 30, 165, 34);

        lblExistencia.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblExistencia.setText("Existencia");
        pnlFiltros.add(lblExistencia);
        lblExistencia.setBounds(473, 12, 100, 16);

        pnlFiltros.add(cmbExistencia);
        cmbExistencia.setBounds(473, 30, 145, 34);

        lblNumerosSerie.setFont(new java.awt.Font("Segoe UI", 1, 11));
        lblNumerosSerie.setText("Número de serie");
        pnlFiltros.add(lblNumerosSerie);
        lblNumerosSerie.setBounds(630, 12, 120, 16);

        pnlFiltros.add(cmbNumerosSerie);
        cmbNumerosSerie.setBounds(630, 30, 170, 34);

        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnLimpiar.setText("Limpiar");
        pnlFiltros.add(btnLimpiar);
        btnLimpiar.setBounds(812, 29, 95, 36);

        getContentPane().add(pnlFiltros);
        pnlFiltros.setBounds(24, 86, 910, 80);

        pnlResultados.setBackground(new java.awt.Color(255, 255, 255));
        pnlResultados.setLayout(null);

        tblProductos.setFont(new java.awt.Font("Segoe UI", 0, 12));
        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Producto", "Categoría", "Marca / Modelo", "Stock", "Stock mínimo", "Último costo", "Serie"
            }
        ));
        scrollProductos.setViewportView(tblProductos);

        pnlResultados.add(scrollProductos);
        scrollProductos.setBounds(0, 0, 910, 345);

        lblCantidadResultados.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lblCantidadResultados.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidadResultados.setText("0 productos encontrados");
        pnlResultados.add(lblCantidadResultados);
        lblCantidadResultados.setBounds(14, 354, 250, 22);

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnCancelar.setText("Cancelar");
        pnlResultados.add(btnCancelar);
        btnCancelar.setBounds(665, 351, 105, 36);

        btnSeleccionar.setFont(new java.awt.Font("Segoe UI", 1, 11));
        btnSeleccionar.setText("Seleccionar producto");
        pnlResultados.add(btnSeleccionar);
        btnSeleccionar.setBounds(782, 351, 125, 36);

        getContentPane().add(pnlResultados);
        pnlResultados.setBounds(24, 178, 910, 400);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnSeleccionar;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JComboBox<String> cmbExistencia;
    private javax.swing.JComboBox<String> cmbNumerosSerie;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblCantidadResultados;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblExistencia;
    private javax.swing.JLabel lblNumerosSerie;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlResultados;
    private javax.swing.JScrollPane scrollProductos;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
