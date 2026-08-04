package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.ClienteControlador;
import sigir.modelo.Cliente;
import sigir.modelo.TipoCliente;
import sigir.util.FiltroTiempoReal;

public class ClientesPanel extends javax.swing.JPanel {

    private final ClienteControlador controlador;
    private boolean iniciado;
    private boolean actualizandoControles;

    public ClientesPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();
        

        controlador = new ClienteControlador(this);
        
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
        tblClientes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tblClientes.setAutoCreateRowSorter(true);
        tblClientes.setFillsViewportHeight(true);

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
            txtNombre,
            txtIdentidad,
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

        btnNuevo.setBackground(new Color(241, 246, 253));
        btnNuevo.setForeground(azul);
        btnNuevo.setBorder(
                BorderFactory.createLineBorder(
                        new Color(184, 207, 237)
                )
        );

        btnGuardar.setBackground(new Color(241, 246, 253));
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
        tblClientes.setRowHeight(42);
        tblClientes.setShowVerticalLines(false);
        tblClientes.setGridColor(new Color(232, 237, 243));
        tblClientes.setSelectionBackground(
                new Color(229, 239, 252)
        );
        tblClientes.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera = tblClientes.getTableHeader();
        cabecera.setBackground(new Color(248, 250, 253));
        cabecera.setForeground(new Color(34, 59, 94));
        cabecera.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        cabecera.setReorderingAllowed(false);

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(SwingConstants.CENTER);

        if (tblClientes.getColumnCount() >= 7) {
            tblClientes.getColumnModel()
                    .getColumn(0)
                    .setCellRenderer(centro);

            tblClientes.getColumnModel()
                    .getColumn(5)
                    .setCellRenderer(centro);

            tblClientes.getColumnModel()
                    .getColumn(6)
                    .setCellRenderer(centro);
        }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> controlador.nuevo());
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnCambiarEstado.addActionListener(
                e -> controlador.cambiarEstado()
        );
        btnActualizar.addActionListener(
                e -> controlador.recargarAsync()
        );
        

        cmbFiltroTipo.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbFiltroTipo.getItemCount() > 0) {

                controlador.buscar();
            }
        });

        cmbFiltroEstado.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbFiltroEstado.getItemCount() > 0) {

                controlador.buscar();
            }
        });

        tblClientes.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        controlador.seleccionarFila();
                    }
                });
    }

    public String getTextoBusqueda() {
        return txtBuscar.getText().trim();
    }

    public TipoCliente getTipoFiltro() {
        Object seleccionado =
                cmbFiltroTipo.getSelectedItem();

        return seleccionado instanceof TipoCliente tipo
                ? tipo
                : null;
    }

    public String getEstadoFiltro() {
        Object seleccionado =
                cmbFiltroEstado.getSelectedItem();

        return seleccionado == null
                ? "TODOS"
                : seleccionado.toString();
    }

    public void cargarTiposCliente(
            List<TipoCliente> tipos) {

        TipoCliente seleccionFiltro = getTipoFiltro();
        TipoCliente seleccionFormulario =
                getTipoFormulario();

        actualizandoControles = true;

        try {
            DefaultComboBoxModel<TipoCliente> modeloFiltro =
                    new DefaultComboBoxModel<>();

        modeloFiltro.addElement(
                new TipoCliente(0, "Todos los tipos")
        );

        DefaultComboBoxModel<TipoCliente> modeloFormulario =
                new DefaultComboBoxModel<>();

        modeloFormulario.addElement(
                new TipoCliente(0, "Seleccione...")
        );

        for (TipoCliente tipo : tipos) {
            modeloFiltro.addElement(tipo);
            modeloFormulario.addElement(tipo);
        }

        cmbFiltroTipo.setModel(modeloFiltro);
        cmbTipoCliente.setModel(modeloFormulario);

        seleccionarTipo(
                cmbFiltroTipo,
                seleccionFiltro == null
                        ? 0
                        : seleccionFiltro.getIdTipoCliente()
        );

            seleccionarTipo(
                    cmbTipoCliente,
                    seleccionFormulario == null
                            ? 0
                            : seleccionFormulario.getIdTipoCliente()
            );

        } finally {
            actualizandoControles = false;
        }
    }

    public void mostrarClientes(List<Cliente> clientes) {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                    "ID",
                    "Código",
                    "Nombre",
                    "Identidad / RTN",
                    "Teléfono",
                    "Tipo",
                    "Correo",
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

        for (Cliente cliente : clientes) {
            modelo.addRow(new Object[]{
                cliente.getIdCliente(),
                String.format(
                        "CLI-%04d",
                        cliente.getIdCliente()
                ),
                cliente.getNombreCompleto(),
                texto(cliente.getNumeroIdentidad()),
                texto(cliente.getTelefono()),
                cliente.getNombreTipoCliente(),
                texto(cliente.getCorreo()),
                cliente.getEstado()
            });
        }

        tblClientes.setModel(modelo);

        if (tblClientes.getColumnCount() > 0) {
            tblClientes.removeColumn(
                    tblClientes.getColumnModel().getColumn(0)
            );
        }

        if (tblClientes.getColumnCount() >= 7) {
            tblClientes.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(82);

            tblClientes.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(190);

            tblClientes.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(135);

            tblClientes.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(105);

            tblClientes.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(105);

            tblClientes.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(190);

            tblClientes.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(80);
        }

        estilizarTabla();
    }

    public Cliente obtenerClienteFormulario() {

        Cliente cliente = new Cliente();
        TipoCliente tipo = getTipoFormulario();

        cliente.setIdTipoCliente(
                tipo == null
                        ? 0
                        : tipo.getIdTipoCliente()
        );

        cliente.setNombreCompleto(
                txtNombre.getText().trim()
        );

        cliente.setNumeroIdentidad(
                textoOpcional(txtIdentidad.getText())
        );

        cliente.setTelefono(
                textoOpcional(txtTelefono.getText())
        );

        cliente.setCorreo(
                textoOpcional(
                        txtCorreo.getText().toLowerCase()
                )
        );

        cliente.setDireccion(
                textoOpcional(txtDireccion.getText())
        );

        cliente.setEstado(
                String.valueOf(cmbEstado.getSelectedItem())
        );

        return cliente;
    }

    public void mostrarCliente(Cliente cliente) {
        txtNombre.setText(
                texto(cliente.getNombreCompleto())
        );
        txtIdentidad.setText(
                texto(cliente.getNumeroIdentidad())
        );
        txtTelefono.setText(
                texto(cliente.getTelefono())
        );
        txtCorreo.setText(
                texto(cliente.getCorreo())
        );
        txtDireccion.setText(
                texto(cliente.getDireccion())
        );

        seleccionarTipo(
                cmbTipoCliente,
                cliente.getIdTipoCliente()
        );

        cmbEstado.setSelectedItem(cliente.getEstado());
    }

    public void limpiarFormulario() {
        tblClientes.clearSelection();

        txtNombre.setText("");
        txtIdentidad.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");

        if (cmbTipoCliente.getItemCount() > 0) {
            cmbTipoCliente.setSelectedIndex(0);
        }

        cmbEstado.setSelectedItem("ACTIVO");
        txtNombre.requestFocusInWindow();
    }

    public void setModoEdicion(
            boolean editando,
            String estado) {

        lblTituloInformacion.setText(
                editando
                        ? "Información del cliente seleccionado"
                        : "Información del nuevo cliente"
        );

        btnGuardar.setText(
                editando
                        ? "Guardar cambios"
                        : "Registrar cliente"
        );

        btnCambiarEstado.setEnabled(editando);

        boolean activo =
                "ACTIVO".equalsIgnoreCase(estado);

        btnCambiarEstado.setText(
                activo
                        ? "Desactivar cliente"
                        : "Activar cliente"
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
        int filaVista = tblClientes.getSelectedRow();

        return filaVista < 0
                ? -1
                : tblClientes.convertRowIndexToModel(
                        filaVista
                );
    }

    public void seleccionarFilaModelo(int filaModelo) {
        if (filaModelo < 0
                || filaModelo >= tblClientes.getModel()
                        .getRowCount()) {

            return;
        }

        int filaVista =
                tblClientes.convertRowIndexToView(
                        filaModelo
                );

        tblClientes.setRowSelectionInterval(
                filaVista,
                filaVista
        );

        tblClientes.scrollRectToVisible(
                tblClientes.getCellRect(
                        filaVista,
                        0,
                        true
                )
        );
    }

    public void mostrarCantidad(int cantidad) {
        lblCantidad.setText(
                cantidad == 1
                        ? "Mostrando 1 cliente"
                        : "Mostrando " + cantidad + " clientes"
        );
    }

    public void enfocarNombre() {
        txtNombre.requestFocusInWindow();
    }

    public void enfocarIdentidad() {
        txtIdentidad.requestFocusInWindow();
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

    public void enfocarTipo() {
        cmbTipoCliente.requestFocusInWindow();
    }

    private TipoCliente getTipoFormulario() {
        Object seleccionado =
                cmbTipoCliente.getSelectedItem();

        return seleccionado instanceof TipoCliente tipo
                ? tipo
                : null;
    }

    private void seleccionarTipo(
            javax.swing.JComboBox<TipoCliente> combo,
            int idTipo) {

        for (int i = 0; i < combo.getItemCount(); i++) {
            TipoCliente tipo = combo.getItemAt(i);

            if (tipo != null
                    && tipo.getIdTipoCliente() == idTipo) {

                combo.setSelectedIndex(i);
                return;
            }
        }

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlInformacion = new javax.swing.JPanel();
        lblTituloInformacion = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblIdentidad = new javax.swing.JLabel();
        txtIdentidad = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        lblTipoCliente = new javax.swing.JLabel();
        cmbTipoCliente = new javax.swing.JComboBox<>();
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
        cmbFiltroTipo = new javax.swing.JComboBox<>();
        cmbFiltroEstado = new javax.swing.JComboBox<>();
        scrollClientes = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        lblCantidad = new javax.swing.JLabel();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Gestión de Clientes");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 390, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Administra la información de tus clientes.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 520, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 18, 1100, 76);

        pnlInformacion.setBackground(new java.awt.Color(255, 255, 255));
        pnlInformacion.setLayout(null);

        lblTituloInformacion.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloInformacion.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloInformacion.setText("Información del nuevo cliente");
        pnlInformacion.add(lblTituloInformacion);
        lblTituloInformacion.setBounds(18, 12, 390, 28);

        lblNombre.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(38, 64, 99));
        lblNombre.setText("Nombre completo");
        pnlInformacion.add(lblNombre);
        lblNombre.setBounds(18, 50, 180, 18);

        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtNombre);
        txtNombre.setBounds(18, 72, 280, 38);

        lblIdentidad.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblIdentidad.setForeground(new java.awt.Color(38, 64, 99));
        lblIdentidad.setText("Identidad o RTN");
        pnlInformacion.add(lblIdentidad);
        lblIdentidad.setBounds(310, 50, 180, 18);

        txtIdentidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtIdentidad);
        txtIdentidad.setBounds(310, 72, 210, 38);

        lblTelefono.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTelefono.setForeground(new java.awt.Color(38, 64, 99));
        lblTelefono.setText("Teléfono");
        pnlInformacion.add(lblTelefono);
        lblTelefono.setBounds(532, 50, 150, 18);

        txtTelefono.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtTelefono);
        txtTelefono.setBounds(532, 72, 190, 38);

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCorreo.setForeground(new java.awt.Color(38, 64, 99));
        lblCorreo.setText("Correo electrónico");
        pnlInformacion.add(lblCorreo);
        lblCorreo.setBounds(18, 122, 190, 18);

        txtCorreo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtCorreo);
        txtCorreo.setBounds(18, 144, 330, 38);

        lblDireccion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDireccion.setForeground(new java.awt.Color(38, 64, 99));
        lblDireccion.setText("Dirección");
        pnlInformacion.add(lblDireccion);
        lblDireccion.setBounds(360, 122, 180, 18);

        txtDireccion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(txtDireccion);
        txtDireccion.setBounds(360, 144, 362, 38);

        lblTipoCliente.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTipoCliente.setForeground(new java.awt.Color(38, 64, 99));
        lblTipoCliente.setText("Tipo de cliente");
        pnlInformacion.add(lblTipoCliente);
        lblTipoCliente.setBounds(18, 194, 180, 18);

        cmbTipoCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(cmbTipoCliente);
        cmbTipoCliente.setBounds(18, 216, 330, 38);

        lblEstado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(38, 64, 99));
        lblEstado.setText("Estado");
        pnlInformacion.add(lblEstado);
        lblEstado.setBounds(360, 194, 180, 18);

        cmbEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnlInformacion.add(cmbEstado);
        cmbEstado.setBounds(360, 216, 362, 38);

        add(pnlInformacion);
        pnlInformacion.setBounds(28, 98, 748, 276);

        pnlAcciones.setBackground(new java.awt.Color(255, 255, 255));
        pnlAcciones.setLayout(null);

        lblTituloAcciones.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloAcciones.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloAcciones.setText("Acciones rápidas");
        pnlAcciones.add(lblTituloAcciones);
        lblTituloAcciones.setBounds(18, 12, 230, 28);

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNuevo.setText("+  Nuevo cliente");
        pnlAcciones.add(btnNuevo);
        btnNuevo.setBounds(18, 52, 294, 44);

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuardar.setText("Guardar cliente");
        pnlAcciones.add(btnGuardar);
        btnGuardar.setBounds(18, 106, 294, 44);

        btnCambiarEstado.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCambiarEstado.setText("Desactivar cliente");
        btnCambiarEstado.setEnabled(false);
        pnlAcciones.add(btnCambiarEstado);
        btnCambiarEstado.setBounds(18, 160, 294, 44);

        btnActualizar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnActualizar.setText("Actualizar lista");
        pnlAcciones.add(btnActualizar);
        btnActualizar.setBounds(18, 214, 294, 44);

        add(pnlAcciones);
        pnlAcciones.setBounds(792, 98, 330, 276);

        pnlLista.setBackground(new java.awt.Color(255, 255, 255));
        pnlLista.setLayout(null);

        lblTituloLista.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTituloLista.setForeground(new java.awt.Color(24, 50, 87));
        lblTituloLista.setText("Lista de clientes");
        pnlLista.add(lblTituloLista);
        lblTituloLista.setBounds(18, 12, 210, 28);

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscar.setToolTipText("Buscar por nombre, identidad, teléfono o correo");
        pnlLista.add(txtBuscar);
        txtBuscar.setBounds(245, 8, 270, 38);
        pnlLista.add(cmbFiltroTipo);
        cmbFiltroTipo.setBounds(527, 8, 180, 38);
        pnlLista.add(cmbFiltroEstado);
        cmbFiltroEstado.setBounds(719, 8, 130, 38);

        tblClientes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        scrollClientes.setViewportView(tblClientes);

        pnlLista.add(scrollClientes);
        scrollClientes.setBounds(0, 56, 1094, 250);

        lblCantidad.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidad.setText("Mostrando 0 clientes");
        pnlLista.add(lblCantidad);
        lblCantidad.setBounds(18, 312, 320, 24);

        add(pnlLista);
        pnlLista.setBounds(28, 390, 1094, 344);
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<String> cmbFiltroEstado;
    private javax.swing.JComboBox<TipoCliente> cmbFiltroTipo;
    private javax.swing.JComboBox<TipoCliente> cmbTipoCliente;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblIdentidad;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTipoCliente;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloAcciones;
    private javax.swing.JLabel lblTituloInformacion;
    private javax.swing.JLabel lblTituloLista;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlInformacion;
    private javax.swing.JPanel pnlLista;
    private javax.swing.JScrollPane scrollClientes;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtIdentidad;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration                   
}
