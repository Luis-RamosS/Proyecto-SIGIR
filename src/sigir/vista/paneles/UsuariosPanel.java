package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.UsuarioControlador;
import sigir.modelo.RolSistema;
import sigir.modelo.UsuarioGestion;
import sigir.util.FiltroTiempoReal;
import sigir.util.Sesion;

public class UsuariosPanel
        extends javax.swing.JPanel {

    private static final DateTimeFormatter
            FORMATO_FECHA_HORA =
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm"
                    );

    private final UsuarioControlador controlador;
    private boolean iniciado;
    private boolean actualizandoControles;

    public UsuariosPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();

        controlador =
                new UsuarioControlador(this);

        configurarEventos();

        FiltroTiempoReal.activar(
                txtBuscarUsuario,
                controlador::buscarUsuarios
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
        cmbEstadoFiltro.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "TODOS",
                            "ACTIVO",
                            "INACTIVO",
                            "BLOQUEADO"
                        }
                )
        );

        cmbEstadoFormulario.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "ACTIVO",
                            "INACTIVO",
                            "BLOQUEADO"
                        }
                )
        );

        txtIdUsuario.setEditable(false);
        txtIdUsuario.setFocusable(false);
        txtIdUsuario.setBackground(
                new Color(244, 247, 251)
        );

        tblUsuarios.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblUsuarios.setAutoCreateRowSorter(true);
        tblUsuarios.setFillsViewportHeight(true);

        txtContrasena.setEchoChar('●');
        txtConfirmarContrasena.setEchoChar('●');

        btnRestablecerContrasena.setEnabled(false);
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);
        Color verde = new Color(34, 155, 85);

        javax.swing.JPanel[] paneles = {
            pnlTarjetaTotal,
            pnlTarjetaActivos,
            pnlTarjetaBloqueados,
            pnlListaUsuarios,
            pnlFormularioUsuario
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

        btnGuardarUsuario.setBackground(azul);
        btnGuardarUsuario.setForeground(Color.WHITE);
        btnGuardarUsuario.setBorderPainted(false);
        btnGuardarUsuario.setFocusPainted(false);

        btnNuevoUsuario.setBackground(Color.WHITE);
        btnNuevoUsuario.setForeground(texto);
        btnNuevoUsuario.setBorder(
                BorderFactory.createLineBorder(borde)
        );

        btnActualizar.setBackground(Color.WHITE);
        btnActualizar.setForeground(texto);
        btnActualizar.setBorder(
                BorderFactory.createLineBorder(borde)
        );

        btnRestablecerContrasena.setBackground(
                new Color(245, 249, 255)
        );
        btnRestablecerContrasena.setForeground(azul);
        btnRestablecerContrasena.setBorder(
                BorderFactory.createLineBorder(
                        new Color(183, 205, 232)
                )
        );

        lblActivosValor.setForeground(verde);
        lblBloqueadosValor.setForeground(
                new Color(192, 52, 52)
        );

        for (javax.swing.JButton boton
                : new javax.swing.JButton[]{
                    btnGuardarUsuario,
                    btnNuevoUsuario,
                    btnActualizar,
                    btnRestablecerContrasena
                }) {

            boton.setFocusPainted(false);
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        estilizarTabla();
    }

    private void estilizarTabla() {
        tblUsuarios.setRowHeight(40);
        tblUsuarios.setShowVerticalLines(false);
        tblUsuarios.setGridColor(
                new Color(232, 237, 243)
        );

        tblUsuarios.setSelectionBackground(
                new Color(229, 239, 252)
        );

        tblUsuarios.setSelectionForeground(
                new Color(24, 50, 87)
        );

        JTableHeader cabecera =
                tblUsuarios.getTableHeader();

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
        cmbRolFiltro.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbRolFiltro.getItemCount() > 0) {

                controlador.buscarUsuarios();
            }
        });

        cmbEstadoFiltro.addActionListener(e -> {
            if (!actualizandoControles
                    && cmbEstadoFiltro.getItemCount() > 0) {

                controlador.buscarUsuarios();
            }
        });

        btnActualizar.addActionListener(
                e -> controlador.recargarAsync()
        );

        btnNuevoUsuario.addActionListener(
                e -> controlador.nuevoUsuario()
        );

        btnGuardarUsuario.addActionListener(
                e -> controlador.guardarUsuario()
        );

        btnRestablecerContrasena.addActionListener(
                e -> controlador
                        .restablecerContrasena()
        );

        chkMostrarContrasena.addActionListener(
                e -> alternarContrasenas()
        );

        tblUsuarios.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e) {

                        controlador.seleccionarUsuario();
                    }
                }
        );
    }

    public void cargarRoles(
            List<RolSistema> roles) {

        int rolFormulario =
                getRolFormulario() == null
                        ? 0
                        : getRolFormulario()
                                .getIdRol();

        int rolFiltro =
                getIdRolFiltro() == null
                        ? 0
                        : getIdRolFiltro();

        actualizandoControles = true;

        try {
            DefaultComboBoxModel<RolSistema>
                    modeloFormulario =
                            new DefaultComboBoxModel<>();

        for (RolSistema rol : roles) {
            modeloFormulario.addElement(rol);
        }

        cmbRolFormulario.setModel(
                modeloFormulario
        );

        DefaultComboBoxModel<RolSistema>
                modeloFiltro =
                        new DefaultComboBoxModel<>();

        RolSistema todos = new RolSistema();
        todos.setIdRol(0);
        todos.setNombre("TODOS");

        modeloFiltro.addElement(todos);

        for (RolSistema rol : roles) {
            modeloFiltro.addElement(rol);
        }

        cmbRolFiltro.setModel(modeloFiltro);

        seleccionarRol(
                cmbRolFormulario,
                rolFormulario
        );

            seleccionarRol(
                    cmbRolFiltro,
                    rolFiltro
            );

        } finally {
            actualizandoControles = false;
        }
    }

    public String getTextoBusqueda() {
        return txtBuscarUsuario
                .getText()
                .trim();
    }

    public Integer getIdRolFiltro() {
        Object seleccionado =
                cmbRolFiltro.getSelectedItem();

        if (seleccionado
                instanceof RolSistema rol
                && rol.getIdRol() > 0) {

            return rol.getIdRol();
        }

        return null;
    }

    public String getEstadoFiltro() {
        Object estado =
                cmbEstadoFiltro.getSelectedItem();

        return estado == null
                ? "TODOS"
                : estado.toString();
    }

    public void mostrarUsuarios(
            List<UsuarioGestion> usuarios) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "ID",
                            "Nombre completo",
                            "Usuario",
                            "Correo",
                            "Rol",
                            "Teléfono",
                            "Verificado",
                            "Estado",
                            "Último acceso"
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

        for (UsuarioGestion usuario
                : usuarios) {

            modelo.addRow(new Object[]{
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getNombreUsuario(),
                usuario.getCorreo(),
                usuario.getNombreRolVisible(),
                texto(usuario.getTelefono()),
                usuario.isCorreoVerificado()
                        ? "Sí"
                        : "No",
                usuario.getEstado(),
                usuario.getUltimoAcceso() == null
                        ? "Nunca"
                        : usuario.getUltimoAcceso()
                                .format(
                                        FORMATO_FECHA_HORA
                                )
            });
        }

        tblUsuarios.setModel(modelo);
        estilizarTabla();

        if (tblUsuarios.getColumnCount() >= 9) {
            tblUsuarios.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(45);

            tblUsuarios.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(190);

            tblUsuarios.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(105);

            tblUsuarios.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(190);

            tblUsuarios.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(90);

            tblUsuarios.getColumnModel()
                    .getColumn(5)
                    .setPreferredWidth(90);

            tblUsuarios.getColumnModel()
                    .getColumn(6)
                    .setPreferredWidth(75);

            tblUsuarios.getColumnModel()
                    .getColumn(7)
                    .setPreferredWidth(85);

            tblUsuarios.getColumnModel()
                    .getColumn(8)
                    .setPreferredWidth(130);
        }

        lblCantidadUsuarios.setText(
                usuarios.size() == 1
                        ? "1 usuario encontrado"
                        : usuarios.size()
                        + " usuarios encontrados"
        );
    }

    public int
            getFilaUsuarioSeleccionadaModelo() {

        int fila =
                tblUsuarios.getSelectedRow();

        return fila < 0
                ? -1
                : tblUsuarios
                        .convertRowIndexToModel(fila);
    }

    public void mostrarUsuario(
            UsuarioGestion usuario) {

        txtIdUsuario.setText(
                String.valueOf(
                        usuario.getIdUsuario()
                )
        );

        txtNombreCompleto.setText(
                usuario.getNombreCompleto()
        );

        txtNombreUsuario.setText(
                usuario.getNombreUsuario()
        );

        txtCorreo.setText(
                usuario.getCorreo()
        );

        txtTelefono.setText(
                texto(usuario.getTelefono())
        );

        seleccionarRol(
                cmbRolFormulario,
                usuario.getIdRol()
        );

        cmbEstadoFormulario.setSelectedItem(
                usuario.getEstado()
        );

        chkCorreoVerificado.setSelected(
                usuario.isCorreoVerificado()
        );

        txtContrasena.setText("");
        txtConfirmarContrasena.setText("");

        lblModoFormulario.setText(
                "Editando usuario #"
                + usuario.getIdUsuario()
        );

        lblAyudaContrasena.setText(
                "<html>Para cambiar la contraseña usa "
                + "<b>Restablecer contraseña</b>.</html>"
        );

        txtContrasena.setEnabled(false);
        txtConfirmarContrasena.setEnabled(false);
        chkMostrarContrasena.setEnabled(false);
        btnRestablecerContrasena.setEnabled(true);
    }

    public void limpiarFormulario() {
        txtIdUsuario.setText("0");
        txtNombreCompleto.setText("");
        txtNombreUsuario.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");

        if (cmbRolFormulario.getItemCount() > 0) {
            cmbRolFormulario.setSelectedIndex(0);
        }

        cmbEstadoFormulario.setSelectedItem(
                "ACTIVO"
        );

        chkCorreoVerificado.setSelected(false);
        chkMostrarContrasena.setSelected(false);

        txtContrasena.setText("");
        txtConfirmarContrasena.setText("");

        txtContrasena.setEchoChar('●');
        txtConfirmarContrasena.setEchoChar('●');

        txtContrasena.setEnabled(true);
        txtConfirmarContrasena.setEnabled(true);
        chkMostrarContrasena.setEnabled(true);

        lblModoFormulario.setText(
                "Nuevo usuario"
        );

        lblAyudaContrasena.setText(
                "<html>Mínimo 8 caracteres con mayúscula, "
                + "minúscula, número y símbolo.</html>"
        );

        btnRestablecerContrasena.setEnabled(false);
        tblUsuarios.clearSelection();

        txtNombreCompleto.requestFocusInWindow();
    }

    public UsuarioGestion
            construirUsuarioFormulario() {

        UsuarioGestion usuario =
                new UsuarioGestion();

        usuario.setIdUsuario(
                getIdUsuarioFormulario()
        );

        RolSistema rol =
                getRolFormulario();

        usuario.setIdRol(
                rol == null
                        ? 0
                        : rol.getIdRol()
        );

        usuario.setNombreRol(
                rol == null
                        ? null
                        : rol.getNombre()
        );

        usuario.setNombreCompleto(
                txtNombreCompleto
                        .getText()
                        .trim()
        );

        usuario.setNombreUsuario(
                txtNombreUsuario
                        .getText()
                        .trim()
        );

        usuario.setCorreo(
                txtCorreo
                        .getText()
                        .trim()
        );

        usuario.setCorreoVerificado(
                chkCorreoVerificado
                        .isSelected()
        );

        usuario.setTelefono(
                textoOpcional(
                        txtTelefono.getText()
                )
        );

        Object estado =
                cmbEstadoFormulario
                        .getSelectedItem();

        usuario.setEstado(
                estado == null
                        ? "ACTIVO"
                        : estado.toString()
        );

        return usuario;
    }

    public int getIdUsuarioFormulario() {
        try {
            return Integer.parseInt(
                    txtIdUsuario
                            .getText()
                            .trim()
            );

        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public char[] getContrasena() {
        return txtContrasena.getPassword();
    }

    public char[] getConfirmacionContrasena() {
        return txtConfirmarContrasena
                .getPassword();
    }

    public char[][] solicitarNuevaContrasena() {
        JPasswordField nueva =
                new JPasswordField();

        JPasswordField confirmar =
                new JPasswordField();

        JCheckBox mostrar =
                new JCheckBox(
                        "Mostrar contraseñas"
                );

        JLabel reglas =
                new JLabel(
                        "<html>Mínimo 8 caracteres e incluir "
                        + "mayúscula, minúscula, número "
                        + "y símbolo.</html>"
                );

        JPanel panel = new JPanel();
        panel.setLayout(
                new javax.swing.BoxLayout(
                        panel,
                        javax.swing.BoxLayout.Y_AXIS
                )
        );

        panel.add(
                new JLabel("Nueva contraseña:")
        );

        panel.add(nueva);

        panel.add(
                javax.swing.Box
                        .createVerticalStrut(10)
        );

        panel.add(
                new JLabel(
                        "Confirmar contraseña:"
                )
        );

        panel.add(confirmar);

        panel.add(
                javax.swing.Box
                        .createVerticalStrut(8)
        );

        panel.add(mostrar);
        panel.add(reglas);

        mostrar.addActionListener(e -> {
            char eco =
                    mostrar.isSelected()
                            ? (char) 0
                            : '●';

            nueva.setEchoChar(eco);
            confirmar.setEchoChar(eco);
        });

        int respuesta =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Restablecer contraseña",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (respuesta
                != JOptionPane.OK_OPTION) {

            return null;
        }

        return new char[][]{
            nueva.getPassword(),
            confirmar.getPassword()
        };
    }

    public void actualizarIndicadores(
            int total,
            int activos,
            int bloqueados) {

        lblTotalValor.setText(
                String.valueOf(total)
        );

        lblActivosValor.setText(
                String.valueOf(activos)
        );

        lblBloqueadosValor.setText(
                String.valueOf(bloqueados)
        );
    }

    public void mostrarSinAcceso() {
        pnlListaUsuarios.setVisible(false);
        pnlFormularioUsuario.setVisible(false);

        lblTitulo.setText(
                "Acceso restringido"
        );

        lblSubtitulo.setText(
                "Solo el dueño puede administrar "
                + "las cuentas de acceso."
        );

        lblTotalValor.setText("-");
        lblActivosValor.setText("-");
        lblBloqueadosValor.setText("-");
    }

    private RolSistema getRolFormulario() {
        Object seleccionado =
                cmbRolFormulario
                        .getSelectedItem();

        return seleccionado
                instanceof RolSistema rol
                        ? rol
                        : null;
    }

    private void seleccionarRol(
            javax.swing.JComboBox<RolSistema> combo,
            int idRol) {

        if (idRol <= 0) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
            return;
        }

        for (int i = 0;
                i < combo.getItemCount();
                i++) {

            RolSistema rol =
                    combo.getItemAt(i);

            if (rol != null
                    && rol.getIdRol() == idRol) {

                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void alternarContrasenas() {
        char eco =
                chkMostrarContrasena
                        .isSelected()
                        ? (char) 0
                        : '●';

        txtContrasena.setEchoChar(eco);
        txtConfirmarContrasena.setEchoChar(eco);
    }

    private String textoOpcional(
            String valor) {

        return valor == null
                || valor.trim().isBlank()
                        ? null
                        : valor.trim();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlTarjetaTotal = new javax.swing.JPanel();
        lblTotalTitulo = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        pnlTarjetaActivos = new javax.swing.JPanel();
        lblActivosTitulo = new javax.swing.JLabel();
        lblActivosValor = new javax.swing.JLabel();
        pnlTarjetaBloqueados = new javax.swing.JPanel();
        lblBloqueadosTitulo = new javax.swing.JLabel();
        lblBloqueadosValor = new javax.swing.JLabel();
        pnlListaUsuarios = new javax.swing.JPanel();
        lblTituloLista = new javax.swing.JLabel();
        txtBuscarUsuario = new javax.swing.JTextField();
        cmbRolFiltro = new javax.swing.JComboBox<>();
        cmbEstadoFiltro = new javax.swing.JComboBox<>();
        btnActualizar = new javax.swing.JButton();
        scrollUsuarios = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        lblCantidadUsuarios = new javax.swing.JLabel();
        pnlFormularioUsuario = new javax.swing.JPanel();
        lblModoFormulario = new javax.swing.JLabel();
        lblIdUsuario = new javax.swing.JLabel();
        txtIdUsuario = new javax.swing.JTextField();
        lblRolFormulario = new javax.swing.JLabel();
        cmbRolFormulario = new javax.swing.JComboBox<>();
        lblNombreCompleto = new javax.swing.JLabel();
        txtNombreCompleto = new javax.swing.JTextField();
        lblNombreUsuario = new javax.swing.JLabel();
        txtNombreUsuario = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        lblEstadoFormulario = new javax.swing.JLabel();
        cmbEstadoFormulario = new javax.swing.JComboBox<>();
        chkCorreoVerificado = new javax.swing.JCheckBox();
        lblContrasena = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        lblConfirmarContrasena = new javax.swing.JLabel();
        txtConfirmarContrasena = new javax.swing.JPasswordField();
        chkMostrarContrasena = new javax.swing.JCheckBox();
        lblAyudaContrasena = new javax.swing.JLabel();
        btnNuevoUsuario = new javax.swing.JButton();
        btnRestablecerContrasena = new javax.swing.JButton();
        btnGuardarUsuario = new javax.swing.JButton();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Administración de Usuarios");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 520, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Crea cuentas, asigna roles y controla el acceso al sistema.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 720, 24);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 10, 1100, 76);

        pnlTarjetaTotal.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaTotal.setLayout(null);

        lblTotalTitulo.setText("Usuarios registrados");
        pnlTarjetaTotal.add(lblTotalTitulo);
        lblTotalTitulo.setBounds(18, 16, 180, 20);

        lblTotalValor.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTotalValor.setText("0");
        pnlTarjetaTotal.add(lblTotalValor);
        lblTotalValor.setBounds(18, 45, 110, 40);

        add(pnlTarjetaTotal);
        pnlTarjetaTotal.setBounds(28, 88, 330, 100);

        pnlTarjetaActivos.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaActivos.setLayout(null);

        lblActivosTitulo.setText("Usuarios activos");
        pnlTarjetaActivos.add(lblActivosTitulo);
        lblActivosTitulo.setBounds(18, 16, 180, 20);

        lblActivosValor.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblActivosValor.setText("0");
        pnlTarjetaActivos.add(lblActivosValor);
        lblActivosValor.setBounds(18, 45, 110, 40);

        add(pnlTarjetaActivos);
        pnlTarjetaActivos.setBounds(372, 88, 330, 100);

        pnlTarjetaBloqueados.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaBloqueados.setLayout(null);

        lblBloqueadosTitulo.setText("Usuarios bloqueados");
        pnlTarjetaBloqueados.add(lblBloqueadosTitulo);
        lblBloqueadosTitulo.setBounds(18, 16, 180, 20);

        lblBloqueadosValor.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBloqueadosValor.setText("0");
        pnlTarjetaBloqueados.add(lblBloqueadosValor);
        lblBloqueadosValor.setBounds(18, 45, 110, 40);

        add(pnlTarjetaBloqueados);
        pnlTarjetaBloqueados.setBounds(716, 88, 330, 100);

        pnlListaUsuarios.setBackground(new java.awt.Color(255, 255, 255));
        pnlListaUsuarios.setLayout(null);

        lblTituloLista.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloLista.setText("Usuarios registrados");
        pnlListaUsuarios.add(lblTituloLista);
        lblTituloLista.setBounds(16, 8, 220, 26);

        pnlListaUsuarios.add(txtBuscarUsuario);
        txtBuscarUsuario.setBounds(16, 42, 245, 34);

        pnlListaUsuarios.add(cmbRolFiltro);
        cmbRolFiltro.setBounds(273, 42, 135, 34);

        pnlListaUsuarios.add(cmbEstadoFiltro);
        cmbEstadoFiltro.setBounds(420, 42, 130, 34);

        btnActualizar.setText("Actualizar");
        pnlListaUsuarios.add(btnActualizar);
        btnActualizar.setBounds(562, 42, 100, 34);

        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre completo", "Usuario", "Correo", "Rol", "Teléfono", "Verificado", "Estado", "Último acceso"
            }
        ));
        scrollUsuarios.setViewportView(tblUsuarios);

        pnlListaUsuarios.add(scrollUsuarios);
        scrollUsuarios.setBounds(0, 90, 680, 415);

        lblCantidadUsuarios.setForeground(new java.awt.Color(98, 124, 159));
        lblCantidadUsuarios.setText("0 usuarios encontrados");
        pnlListaUsuarios.add(lblCantidadUsuarios);
        lblCantidadUsuarios.setBounds(16, 513, 260, 22);

        add(pnlListaUsuarios);
        pnlListaUsuarios.setBounds(28, 200, 680, 550);

        pnlFormularioUsuario.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormularioUsuario.setLayout(null);

        lblModoFormulario.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblModoFormulario.setText("Nuevo usuario");
        pnlFormularioUsuario.add(lblModoFormulario);
        lblModoFormulario.setBounds(16, 8, 250, 26);

        lblIdUsuario.setText("ID");
        pnlFormularioUsuario.add(lblIdUsuario);
        lblIdUsuario.setBounds(16, 42, 50, 18);

        pnlFormularioUsuario.add(txtIdUsuario);
        txtIdUsuario.setBounds(16, 62, 70, 34);

        lblRolFormulario.setText("Rol");
        pnlFormularioUsuario.add(lblRolFormulario);
        lblRolFormulario.setBounds(100, 42, 70, 18);

        pnlFormularioUsuario.add(cmbRolFormulario);
        cmbRolFormulario.setBounds(100, 62, 245, 34);

        lblNombreCompleto.setText("Nombre completo");
        pnlFormularioUsuario.add(lblNombreCompleto);
        lblNombreCompleto.setBounds(16, 106, 150, 18);

        pnlFormularioUsuario.add(txtNombreCompleto);
        txtNombreCompleto.setBounds(16, 126, 329, 34);

        lblNombreUsuario.setText("Nombre de usuario");
        pnlFormularioUsuario.add(lblNombreUsuario);
        lblNombreUsuario.setBounds(16, 170, 140, 18);

        pnlFormularioUsuario.add(txtNombreUsuario);
        txtNombreUsuario.setBounds(16, 190, 155, 34);

        lblTelefono.setText("Teléfono");
        pnlFormularioUsuario.add(lblTelefono);
        lblTelefono.setBounds(185, 170, 90, 18);

        pnlFormularioUsuario.add(txtTelefono);
        txtTelefono.setBounds(185, 190, 160, 34);

        lblCorreo.setText("Correo electrónico");
        pnlFormularioUsuario.add(lblCorreo);
        lblCorreo.setBounds(16, 234, 150, 18);

        pnlFormularioUsuario.add(txtCorreo);
        txtCorreo.setBounds(16, 254, 329, 34);

        lblEstadoFormulario.setText("Estado");
        pnlFormularioUsuario.add(lblEstadoFormulario);
        lblEstadoFormulario.setBounds(16, 298, 90, 18);

        pnlFormularioUsuario.add(cmbEstadoFormulario);
        cmbEstadoFormulario.setBounds(16, 318, 145, 34);

        chkCorreoVerificado.setText("Correo verificado");
        pnlFormularioUsuario.add(chkCorreoVerificado);
        chkCorreoVerificado.setBounds(177, 316, 170, 36);

        lblContrasena.setText("Contraseña");
        pnlFormularioUsuario.add(lblContrasena);
        lblContrasena.setBounds(16, 362, 100, 18);

        pnlFormularioUsuario.add(txtContrasena);
        txtContrasena.setBounds(16, 382, 155, 34);

        lblConfirmarContrasena.setText("Confirmar");
        pnlFormularioUsuario.add(lblConfirmarContrasena);
        lblConfirmarContrasena.setBounds(185, 362, 100, 18);

        pnlFormularioUsuario.add(txtConfirmarContrasena);
        txtConfirmarContrasena.setBounds(185, 382, 160, 34);

        chkMostrarContrasena.setText("Mostrar contraseñas");
        pnlFormularioUsuario.add(chkMostrarContrasena);
        chkMostrarContrasena.setBounds(16, 422, 180, 28);

        lblAyudaContrasena.setForeground(new java.awt.Color(98, 124, 159));
        lblAyudaContrasena.setText("<html>Mínimo 8 caracteres con mayúscula, minúscula, número y símbolo.</html>");
        pnlFormularioUsuario.add(lblAyudaContrasena);
        lblAyudaContrasena.setBounds(16, 452, 329, 38);

        btnNuevoUsuario.setText("Nuevo");
        pnlFormularioUsuario.add(btnNuevoUsuario);
        btnNuevoUsuario.setBounds(16, 500, 95, 36);

        btnRestablecerContrasena.setText("Restablecer contraseña");
        pnlFormularioUsuario.add(btnRestablecerContrasena);
        btnRestablecerContrasena.setBounds(123, 500, 135, 36);

        btnGuardarUsuario.setText("Guardar");
        pnlFormularioUsuario.add(btnGuardarUsuario);
        btnGuardarUsuario.setBounds(270, 500, 75, 36);

        add(pnlFormularioUsuario);
        pnlFormularioUsuario.setBounds(722, 200, 376, 550);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnGuardarUsuario;
    private javax.swing.JButton btnNuevoUsuario;
    private javax.swing.JButton btnRestablecerContrasena;
    private javax.swing.JCheckBox chkCorreoVerificado;
    private javax.swing.JCheckBox chkMostrarContrasena;
    private javax.swing.JComboBox<String> cmbEstadoFiltro;
    private javax.swing.JComboBox<String> cmbEstadoFormulario;
    private javax.swing.JComboBox<RolSistema> cmbRolFiltro;
    private javax.swing.JComboBox<RolSistema> cmbRolFormulario;
    private javax.swing.JLabel lblActivosTitulo;
    private javax.swing.JLabel lblActivosValor;
    private javax.swing.JLabel lblAyudaContrasena;
    private javax.swing.JLabel lblBloqueadosTitulo;
    private javax.swing.JLabel lblBloqueadosValor;
    private javax.swing.JLabel lblCantidadUsuarios;
    private javax.swing.JLabel lblConfirmarContrasena;
    private javax.swing.JLabel lblContrasena;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblEstadoFormulario;
    private javax.swing.JLabel lblIdUsuario;
    private javax.swing.JLabel lblModoFormulario;
    private javax.swing.JLabel lblNombreCompleto;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblRolFormulario;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloLista;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFormularioUsuario;
    private javax.swing.JPanel pnlListaUsuarios;
    private javax.swing.JPanel pnlTarjetaActivos;
    private javax.swing.JPanel pnlTarjetaBloqueados;
    private javax.swing.JPanel pnlTarjetaTotal;
    private javax.swing.JScrollPane scrollUsuarios;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtBuscarUsuario;
    private javax.swing.JPasswordField txtConfirmarContrasena;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtIdUsuario;
    private javax.swing.JTextField txtNombreCompleto;
    private javax.swing.JTextField txtNombreUsuario;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
