package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.ProveedorControlador;
import sigir.modelo.Proveedor;
import sigir.vista.FrmInicio;
import sigir.util.FiltroTiempoReal;

public class ProveedoresPanel extends javax.swing.JPanel {

    private final ProveedorControlador controlador;
    private boolean iniciado;

    public ProveedoresPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();
        
        controlador = new ProveedorControlador(this);
        
        configurarEventos();
        FiltroTiempoReal.activar(
                txtBuscar,
                controlador::buscar
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
        tblProveedores.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblProveedores.setAutoCreateRowSorter(true);
        tblProveedores.setFillsViewportHeight(true);

        cmbEstado.setModel(new DefaultComboBoxModel<>(
                new String[]{"ACTIVO", "INACTIVO"}
        ));

        cmbFiltroEstado.setModel(new DefaultComboBoxModel<>(
                new String[]{"TODOS", "ACTIVO", "INACTIVO"}
        ));
    }

    private void aplicarEstilos() {
        Color fondo = new Color(247, 249, 252);
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(61, 109, 172);
        Color texto = new Color(24, 50, 87);

        setBackground(fondo);

        pnlInformacion.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borde),
                        BorderFactory.createEmptyBorder(
                                8, 8, 8, 8
                        )
                )
        );

        pnlAcciones.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borde),
                        BorderFactory.createEmptyBorder(
                                8, 8, 8, 8
                        )
                )
        );

        pnlLista.setBorder(
                BorderFactory.createLineBorder(borde)
        );

        javax.swing.JTextField[] campos = {
            txtNombreProveedor,
            txtRtn,
            txtContacto,
            txtTelefono,
            txtCorreo,
            txtDireccion,
            txtBuscar
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

        javax.swing.JButton[] botones = {
            btnNuevo,
            btnGuardar,
            btnCambiarEstado,
            btnActualizar,
            
        };

        for (javax.swing.JButton boton : botones) {
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            boton.setFocusPainted(false);
        }

        btnNuevo.setBackground(
                new Color(241, 246, 253)
        );
        btnNuevo.setForeground(azul);
        btnNuevo.setBorder(
                BorderFactory.createLineBorder(
                        new Color(184, 207, 237)
                )
        );

        btnGuardar.setBackground(
                new Color(241, 246, 253)
        );
        btnGuardar.setForeground(azul);
        btnGuardar.setBorder(
                BorderFactory.createLineBorder(
                        new Color(184, 207, 237)
                )
        );

        btnCambiarEstado.setBackground(
                new Color(255, 245, 245)
        );
        btnCambiarEstado.setForeground(
                new Color(206, 48, 48)
        );
        btnCambiarEstado.setBorder(
                BorderFactory.createLineBorder(
                        new Color(241, 183, 183)
                )
        );

        btnActualizar.setBackground(
                new Color(242, 250, 246)
        );
        btnActualizar.setForeground(
                new Color(32, 137, 82)
        );
        btnActualizar.setBorder(
                BorderFactory.createLineBorder(
                        new Color(176, 225, 199)
                )
        );

        

        estilizarTabla();
    }

    private void estilizarTabla() {
        tblProveedores.setRowHeight(42);
        tblProveedores.setShowVerticalLines(false);
        tblProveedores.setGridColor(
                new Color(232, 237, 243)
        );
        tblProveedores.setSelectionBackground(
                new Color(229, 239, 252)
        );
        tblProveedores.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera =
                tblProveedores.getTableHeader();

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

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        if (tblProveedores.getColumnCount() >= 8) {
            tblProveedores.getColumnModel()
                    .getColumn(0)
                    .setCellRenderer(centro);

            tblProveedores.getColumnModel()
                    .getColumn(7)
                    .setCellRenderer(centro);
        }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(
                e -> controlador.nuevo()
        );

        btnGuardar.addActionListener(
                e -> controlador.guardar()
        );

        btnCambiarEstado.addActionListener(
                e -> controlador.cambiarEstado()
        );

        btnActualizar.addActionListener(
                e -> controlador.recargarAsync()
        );

        

        

        cmbFiltroEstado.addActionListener(e -> {
            if (cmbFiltroEstado.getItemCount() > 0) {
                controlador.buscar();
            }
        });

        tblProveedores.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        controlador.seleccionarFila();
                    }
                });
    }

    public String firmaFormulario() {
        return String.join(
                "~|~",
                txtNombreProveedor.getText(),
                txtRtn.getText(),
                txtContacto.getText(),
                txtTelefono.getText(),
                txtCorreo.getText(),
                txtDireccion.getText(),
                String.valueOf(cmbEstado.getSelectedItem())
        );
    }

    public void limpiarSeleccionTabla() {
        tblProveedores.clearSelection();
    }

    public void abrirCompraConProveedor(
            Proveedor proveedor) {

        java.awt.Window ventana =
                SwingUtilities.getWindowAncestor(this);

        if (ventana instanceof FrmInicio inicio) {
            inicio.abrirCompraConProveedor(proveedor);
        }
    }

    public String getTextoBusqueda() {
        return txtBuscar.getText().trim();
    }

    public String getEstadoFiltro() {
        Object seleccionado =
                cmbFiltroEstado.getSelectedItem();

        return seleccionado == null
                ? "TODOS"
                : seleccionado.toString();
    }

    public void mostrarProveedores(
            List<Proveedor> proveedores) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "ID",
                            "Código",
                            "Proveedor",
                            "RTN",
                            "Contacto",
                            "Teléfono",
                            "Correo",
                            "Dirección",
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
                    public Class<?> getColumnClass(
                            int columnIndex) {

                        return columnIndex == 0
                                ? Integer.class
                                : String.class;
                    }
                };

        for (Proveedor proveedor : proveedores) {
            modelo.addRow(new Object[]{
                proveedor.getIdProveedor(),
                String.format(
                        "PRV-%04d",
                        proveedor.getIdProveedor()
                ),
                proveedor.getNombreProveedor(),
                texto(proveedor.getRtn()),
                texto(proveedor.getNombreContacto()),
                texto(proveedor.getTelefono()),
                texto(proveedor.getCorreo()),
                texto(proveedor.getDireccion()),
                proveedor.getEstado()
            });
        }

        tblProveedores.setModel(modelo);

        if (tblProveedores.getColumnCount() > 0) {
            tblProveedores.removeColumn(
                    tblProveedores.getColumnModel()
                            .getColumn(0)
            );
        }

        if (tblProveedores.getColumnCount() >= 8) {
            tblProveedores.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(85);

            tblProveedores.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(175);

            tblProveedores.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(120);

            tblProveedores.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(130);

            tblProveedores.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(95);

            tblProveedores.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(170);

            tblProveedores.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(220);

            tblProveedores.getColumnModel()
                    .getColumn(7)
                    .setPreferredWidth(80);
        }

        estilizarTabla();
    }

    public Proveedor obtenerProveedorFormulario() {

        Proveedor proveedor = new Proveedor();

        proveedor.setNombreProveedor(
                txtNombreProveedor.getText().trim()
        );

        proveedor.setRtn(
                textoOpcional(txtRtn.getText())
        );

        proveedor.setNombreContacto(
                textoOpcional(txtContacto.getText())
        );

        proveedor.setTelefono(
                textoOpcional(txtTelefono.getText())
        );

        proveedor.setCorreo(
                textoOpcional(
                        txtCorreo.getText().toLowerCase()
                )
        );

        proveedor.setDireccion(
                textoOpcional(txtDireccion.getText())
        );

        proveedor.setEstado(
                String.valueOf(
                        cmbEstado.getSelectedItem()
                )
        );

        return proveedor;
    }

    public void mostrarProveedor(Proveedor proveedor) {
        txtNombreProveedor.setText(
                texto(proveedor.getNombreProveedor())
        );

        txtRtn.setText(
                texto(proveedor.getRtn())
        );

        txtContacto.setText(
                texto(proveedor.getNombreContacto())
        );

        txtTelefono.setText(
                texto(proveedor.getTelefono())
        );

        txtCorreo.setText(
                texto(proveedor.getCorreo())
        );

        txtDireccion.setText(
                texto(proveedor.getDireccion())
        );

        cmbEstado.setSelectedItem(
                proveedor.getEstado()
        );
    }

    public void limpiarFormulario() {
        tblProveedores.clearSelection();

        txtNombreProveedor.setText("");
        txtRtn.setText("");
        txtContacto.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");

        cmbEstado.setSelectedItem("ACTIVO");
        txtNombreProveedor.requestFocusInWindow();
    }

    public void setModoEdicion(
            boolean editando,
            String estado) {

        lblTituloInformacion.setText(
                editando
                        ? "Información del proveedor seleccionado"
                        : "Información del nuevo proveedor"
        );

        btnGuardar.setText(
                editando
                        ? "Guardar cambios"
                        : "Registrar proveedor"
        );

        btnCambiarEstado.setEnabled(editando);

        boolean activo =
                "ACTIVO".equalsIgnoreCase(estado);

        btnCambiarEstado.setText(
                activo
                        ? "Desactivar proveedor"
                        : "Activar proveedor"
        );

        if (editando && !activo) {
            btnCambiarEstado.setBackground(
                    new Color(242, 250, 246)
            );
            btnCambiarEstado.setForeground(
                    new Color(32, 137, 82)
            );
            btnCambiarEstado.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(176, 225, 199)
                    )
            );
        } else {
            btnCambiarEstado.setBackground(
                    new Color(255, 245, 245)
            );
            btnCambiarEstado.setForeground(
                    new Color(206, 48, 48)
            );
            btnCambiarEstado.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(241, 183, 183)
                    )
            );
        }
    }

    public int getFilaSeleccionadaModelo() {
        int filaVista =
                tblProveedores.getSelectedRow();

        return filaVista < 0
                ? -1
                : tblProveedores.convertRowIndexToModel(
                        filaVista
                );
    }

    public void seleccionarFilaModelo(int filaModelo) {
        if (filaModelo < 0
                || filaModelo >= tblProveedores
                        .getModel()
                        .getRowCount()) {

            return;
        }

        int filaVista =
                tblProveedores.convertRowIndexToView(
                        filaModelo
                );

        tblProveedores.setRowSelectionInterval(
                filaVista,
                filaVista
        );

        tblProveedores.scrollRectToVisible(
                tblProveedores.getCellRect(
                        filaVista,
                        0,
                        true
                )
        );
    }

    public void mostrarCantidad(int cantidad) {
        lblCantidad.setText(
                cantidad == 1
                        ? "Mostrando 1 proveedor"
                        : "Mostrando "
                        + cantidad
                        + " proveedores"
        );
    }

    public void enfocarNombreProveedor() {
        txtNombreProveedor.requestFocusInWindow();
    }

    public void enfocarRtn() {
        txtRtn.requestFocusInWindow();
    }

    public void enfocarContacto() {
        txtContacto.requestFocusInWindow();
    }

    public void enfocarTelefono() {
        txtTelefono.requestFocusInWindow();
    }

    public void enfocarCorreo() {
        txtCorreo.requestFocusInWindow();
    }

    public void enfocarDireccion() {
        txtDireccion.requestFocusInWindow();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private String textoOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlInformacion = new javax.swing.JPanel();
        lblTituloInformacion = new javax.swing.JLabel();
        lblNombreProveedor = new javax.swing.JLabel();
        txtNombreProveedor = new javax.swing.JTextField();
        lblRtn = new javax.swing.JLabel();
        txtRtn = new javax.swing.JTextField();
        lblContacto = new javax.swing.JLabel();
        txtContacto = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        lblEstado = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox<>();
        pnlAcciones = new javax.swing.JPanel();
        lblTituloAcciones = new javax.swing.JLabel();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnCambiarEstado = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        pnlLista = new javax.swing.JPanel();
        lblTituloLista = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        cmbFiltroEstado = new javax.swing.JComboBox<>();
        scrollProveedores = new javax.swing.JScrollPane();
        tblProveedores = new javax.swing.JTable();
        lblCantidad = new javax.swing.JLabel();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Gestión de Proveedores");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 430, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Administra los proveedores utilizados en las compras.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 570, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 18, 1100, 76);

        pnlInformacion.setBackground(new java.awt.Color(255, 255, 255));
        pnlInformacion.setLayout(null);

        lblTituloInformacion.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloInformacion.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloInformacion.setText("Información del nuevo proveedor");
        pnlInformacion.add(lblTituloInformacion);
        lblTituloInformacion.setBounds(18, 12, 410, 28);

        lblNombreProveedor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNombreProveedor.setForeground(new java.awt.Color(38, 64, 99));
        lblNombreProveedor.setText("Nombre del proveedor");
        pnlInformacion.add(lblNombreProveedor);
        lblNombreProveedor.setBounds(18, 50, 200, 18);

        txtNombreProveedor.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtNombreProveedor);
        txtNombreProveedor.setBounds(18, 72, 330, 38);

        lblRtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRtn.setForeground(new java.awt.Color(38, 64, 99));
        lblRtn.setText("RTN");
        pnlInformacion.add(lblRtn);
        lblRtn.setBounds(360, 50, 170, 18);

        txtRtn.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtRtn);
        txtRtn.setBounds(360, 72, 210, 38);

        lblContacto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblContacto.setForeground(new java.awt.Color(38, 64, 99));
        lblContacto.setText("Nombre de contacto");
        pnlInformacion.add(lblContacto);
        lblContacto.setBounds(582, 50, 180, 18);

        txtContacto.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtContacto);
        txtContacto.setBounds(582, 72, 240, 38);

        lblTelefono.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTelefono.setForeground(new java.awt.Color(38, 64, 99));
        lblTelefono.setText("Teléfono");
        pnlInformacion.add(lblTelefono);
        lblTelefono.setBounds(18, 122, 150, 18);

        txtTelefono.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtTelefono);
        txtTelefono.setBounds(18, 144, 220, 38);

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCorreo.setForeground(new java.awt.Color(38, 64, 99));
        lblCorreo.setText("Correo electrónico");
        pnlInformacion.add(lblCorreo);
        lblCorreo.setBounds(250, 122, 190, 18);

        txtCorreo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtCorreo);
        txtCorreo.setBounds(250, 144, 320, 38);

        lblDireccion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDireccion.setForeground(new java.awt.Color(38, 64, 99));
        lblDireccion.setText("Dirección");
        pnlInformacion.add(lblDireccion);
        lblDireccion.setBounds(582, 122, 180, 18);

        txtDireccion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtDireccion);
        txtDireccion.setBounds(582, 144, 240, 38);

        lblEstado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(38, 64, 99));
        lblEstado.setText("Estado");
        pnlInformacion.add(lblEstado);
        lblEstado.setBounds(18, 194, 180, 18);

        cmbEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(cmbEstado);
        cmbEstado.setBounds(18, 216, 804, 38);

        add(pnlInformacion);
        pnlInformacion.setBounds(28, 98, 848, 276);

        pnlAcciones.setBackground(new java.awt.Color(255, 255, 255));
        pnlAcciones.setLayout(null);

        lblTituloAcciones.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloAcciones.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloAcciones.setText("Acciones rápidas");
        pnlAcciones.add(lblTituloAcciones);
        lblTituloAcciones.setBounds(18, 12, 230, 28);

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setText("+  Nuevo proveedor");
        pnlAcciones.add(btnNuevo);
        btnNuevo.setBounds(18, 52, 210, 44);

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setText("Guardar proveedor");
        pnlAcciones.add(btnGuardar);
        btnGuardar.setBounds(18, 106, 210, 44);

        btnCambiarEstado.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCambiarEstado.setText("Desactivar proveedor");
        btnCambiarEstado.setEnabled(false);
        pnlAcciones.add(btnCambiarEstado);
        btnCambiarEstado.setBounds(18, 160, 210, 44);

        btnActualizar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnActualizar.setText("Actualizar lista");
        pnlAcciones.add(btnActualizar);
        btnActualizar.setBounds(18, 214, 210, 44);

        add(pnlAcciones);
        pnlAcciones.setBounds(892, 98, 246, 276);

        pnlLista.setBackground(new java.awt.Color(255, 255, 255));
        pnlLista.setLayout(null);

        lblTituloLista.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloLista.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloLista.setText("Lista de proveedores");
        pnlLista.add(lblTituloLista);
        lblTituloLista.setBounds(18, 12, 230, 28);

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setToolTipText("Buscar por proveedor, RTN, contacto, teléfono o correo");
        pnlLista.add(txtBuscar);
        txtBuscar.setBounds(285, 8, 390, 38);
        pnlLista.add(cmbFiltroEstado);
        cmbFiltroEstado.setBounds(687, 8, 150, 38);

        tblProveedores.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollProveedores.setViewportView(tblProveedores);

        pnlLista.add(scrollProveedores);
        scrollProveedores.setBounds(0, 56, 1110, 250);

        lblCantidad.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidad.setText("Mostrando 0 proveedores");
        pnlLista.add(lblCantidad);
        lblCantidad.setBounds(18, 312, 340, 24);

        add(pnlLista);
        pnlLista.setBounds(28, 390, 1110, 344);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<String> cmbFiltroEstado;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblContacto;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblNombreProveedor;
    private javax.swing.JLabel lblRtn;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloAcciones;
    private javax.swing.JLabel lblTituloInformacion;
    private javax.swing.JLabel lblTituloLista;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlInformacion;
    private javax.swing.JPanel pnlLista;
    private javax.swing.JScrollPane scrollProveedores;
    private javax.swing.JTable tblProveedores;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtContacto;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtNombreProveedor;
    private javax.swing.JTextField txtRtn;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
