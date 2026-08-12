package sigir.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.JComponent;
import sigir.vista.paneles.ProductosPanel;
import sigir.vista.paneles.ClientesPanel;
import sigir.vista.paneles.ProveedoresPanel;
import sigir.vista.paneles.ComprasPanel;
import sigir.vista.paneles.InventarioPanel;
import sigir.vista.paneles.VentasPanel;
import sigir.vista.paneles.VentaRapidaPanel;
import sigir.vista.paneles.CreditosPanel;
import sigir.vista.paneles.ReparacionesPanel;
import sigir.vista.paneles.UsuariosPanel;
import sigir.vista.paneles.ReportesPanel;
import javax.swing.JOptionPane;
import sigir.util.HorarioVentaRapidaUtil;
import sigir.util.Sesion;
import sigir.util.SesionRemota;
import sigir.vista.paneles.ConfiguracionPanel;
import sigir.componentes.BuscadorGlobal;
import sigir.modelo.ModuloInicio;
import sigir.modelo.Cliente;
import sigir.modelo.Proveedor;
import sigir.vista.paneles.InicioPanel;

/**
 * Pantalla de inicio de SIGIR creada como JFrame Form de NetBeans.
 * El diseño puede abrirse y modificarse desde la pestaña Design.
 */
public class FrmInicio extends javax.swing.JFrame {

    
    private javax.swing.JButton botonMenuActivo;
    private JComponent panelActual;
    private InicioPanel inicioPanel;
    private ProductosPanel productosPanel;
    private ClientesPanel clientesPanel;
    private ProveedoresPanel proveedoresPanel;
    private ComprasPanel comprasPanel;
    private InventarioPanel inventarioPanel;
    private VentasPanel ventasPanel;
    private VentaRapidaPanel ventaRapidaPanel;
    private CreditosPanel creditosPanel;
    private ReparacionesPanel reparacionesPanel;
    private UsuariosPanel usuariosPanel;
    private ReportesPanel reportesPanel;
    private ConfiguracionPanel configuracionPanel;
    private javax.swing.Timer timerHorarioVentaRapida;
    private javax.swing.Timer timerHeartbeatSesion;
    
    private void limpiarCuadrosDelMenu() {

        lblMarca.setText("SIGIR");
        lblNotificacion.setText("");

        btnInicio.setText("Inicio");
        btnVentas.setText("Ventas");
        btnVentaRapida.setText("Venta rápida");
        btnCompras.setText("Compras");
        btnProductos.setText("Productos");
        btnInventario.setText("Inventario");
        btnClientes.setText("Clientes");
        btnProveedores.setText("Proveedores");
        btnCreditos.setText("Créditos");
        btnReparaciones.setText("Reparaciones");
        btnUsuarios.setText("Usuarios");
        btnReportes.setText("Reportes");
        btnConfiguracion.setText("Configuración");

        lblVersion.setText("SIGIR v1.0.0");
    }
    
    public FrmInicio() {

        initComponents();

        panelActual = pnlContenido;

        if (Sesion.haySesionActiva()) {

            lblNombreUsuario.setText(
                    Sesion.getNombreCompleto()
            );

            lblRolUsuario.setText(
                    Sesion.getRol()
            );

            lblBienvenida.setText(
                    "¡Bienvenido, "
                    + Sesion.getUsuarioActual()
                            .getNombreCompleto()
                    + "!"
            );
        }

        configurarVentana();
        aplicarEstilos();
        limpiarCuadrosDelMenu();
        configurarBotonCerrarSesion();

        configurarNavegacion();
        configurarPermisos();
        configurarHorarioVentaRapida();
        configurarControlSesionRemota();
        configurarBusquedaGlobal();

        javax.swing.SwingUtilities.invokeLater(() -> {
            btnInicio.doClick();
            preguntarVentaRapida();
        });
    }

    public FrmInicio(String usuario) {
        this();
    }
    
    private InicioPanel obtenerInicioPanel() {

    if (inicioPanel == null) {
        inicioPanel = new InicioPanel(modulo -> {

            switch (modulo) {
                case VENTAS ->
                    btnVentas.doClick();

                case PRODUCTOS ->
                    btnProductos.doClick();

                case INVENTARIO ->
                    btnInventario.doClick();

                case CREDITOS ->
                    btnCreditos.doClick();

                case REPARACIONES ->
                    btnReparaciones.doClick();
            }
        });
    }

    return inicioPanel;
}
    


    private void preguntarVentaRapida() {
        if (!Sesion.haySesionActiva()
                || !Sesion.esDueno()
                || !HorarioVentaRapidaUtil.estaHabilitadaAhora()) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Se realizó alguna venta rápida fuera del horario de atención?",
                "Venta rápida pendiente",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            btnVentaRapida.doClick();
        }
    }

    private void configurarVentana() {
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void configurarHorarioVentaRapida() {
        actualizarDisponibilidadVentaRapida();

        timerHorarioVentaRapida = new javax.swing.Timer(
                5000,
                e -> actualizarDisponibilidadVentaRapida()
        );
        timerHorarioVentaRapida.start();
    }

    private void actualizarDisponibilidadVentaRapida() {
        boolean dueno = Sesion.haySesionActiva() && Sesion.esDueno();
        boolean horario = HorarioVentaRapidaUtil.estaHabilitadaAhora();

        btnVentaRapida.setVisible(dueno);
        btnVentaRapida.setEnabled(dueno && horario);
        btnVentaRapida.setToolTipText(
                horario
                        ? "Registrar ventas realizadas fuera del horario normal"
                        : "Disponible únicamente de "
                        + HorarioVentaRapidaUtil.descripcionHorario()
        );

        if (ventaRapidaPanel != null) {
            ventaRapidaPanel.actualizarEstadoHorario();
        }
    }

    private void configurarControlSesionRemota() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salirAplicacion();
            }
        });

        if (!SesionRemota.haySesion()) {
            return;
        }

        timerHeartbeatSesion = new javax.swing.Timer(30000, e -> {
            try {
                if (!SesionRemota.refrescar()) {
                    cerrarPorSesionNoValida();
                }
            } catch (SQLException ex) {
                System.err.println(
                        "No fue posible actualizar el heartbeat de sesión: "
                        + ex.getMessage()
                );
            }
        });
        timerHeartbeatSesion.start();
    }

    private void cerrarPorSesionNoValida() {
        detenerTimers();
        JOptionPane.showMessageDialog(
                this,
                "La sesión de este usuario dejó de estar activa en el servidor.\n"
                + "Por seguridad, SIGIR volverá al inicio de sesión.",
                "Sesión finalizada",
                JOptionPane.WARNING_MESSAGE
        );

        SesionRemota.cerrarSilenciosamente("SESION_REEMPLAZADA");
        Sesion.cerrar();
        LoginFrame login = new LoginFrame();
        login.setVisible(true);
        dispose();
    }

    private void salirAplicacion() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas salir de SIGIR?",
                "Salir",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        detenerTimers();
        SesionRemota.cerrarSilenciosamente("CIERRE_APLICACION");
        Sesion.cerrar();
        dispose();
        System.exit(0);
    }

    private void detenerTimers() {
        if (timerHorarioVentaRapida != null) {
            timerHorarioVentaRapida.stop();
        }
        if (timerHeartbeatSesion != null) {
            timerHeartbeatSesion.stop();
        }
    }
    private void configurarBotonCerrarSesion() {

        final String textoNormal = "Cerrar sesión";
        final String textoSubrayado
                = "<html><u>Cerrar sesión</u></html>";

        final java.awt.Color rojoNormal
                = new java.awt.Color(210, 45, 45);

        final java.awt.Color rojoHover
                = new java.awt.Color(160, 25, 25);

        btnCerrarSesion.setText(textoNormal);
        btnCerrarSesion.setForeground(rojoNormal);

        // Fondo transparente
        btnCerrarSesion.setOpaque(false);
        btnCerrarSesion.setContentAreaFilled(false);

        // Sin borde ni cuadro al presionarlo
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setFocusPainted(false);

        btnCerrarSesion.setCursor(
                java.awt.Cursor.getPredefinedCursor(
                        java.awt.Cursor.HAND_CURSOR
                )
        );

        btnCerrarSesion.addMouseListener(
                new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(
                    java.awt.event.MouseEvent evt) {

                btnCerrarSesion.setForeground(rojoHover);
                btnCerrarSesion.setText(textoSubrayado);
            }

            @Override
            public void mouseExited(
                    java.awt.event.MouseEvent evt) {

                btnCerrarSesion.setForeground(rojoNormal);
                btnCerrarSesion.setText(textoNormal);
            }

            @Override
            public void mousePressed(
                    java.awt.event.MouseEvent evt) {

                btnCerrarSesion.setForeground(
                        new java.awt.Color(120, 15, 15)
                );
            }

            @Override
            public void mouseReleased(
                    java.awt.event.MouseEvent evt) {

                if (btnCerrarSesion.contains(evt.getPoint())) {
                    btnCerrarSesion.setForeground(rojoHover);
                } else {
                    btnCerrarSesion.setForeground(rojoNormal);
                }
            }
        });
    }
    
    private javax.swing.JButton[] obtenerBotonesMenu() {

        return new javax.swing.JButton[]{
            btnInicio,
            btnVentas,
            btnVentaRapida,
            btnCompras,
            btnProductos,
            btnInventario,
            btnClientes,
            btnProveedores,
            btnCreditos,
            btnReparaciones,
            btnUsuarios,
            btnReportes,
            btnConfiguracion
        };
    }
    private void configurarBusquedaGlobal() {

        BuscadorGlobal.instalar(
                txtBuscar,
                resultado -> {

                    switch (resultado.getTipo()) {

                        case "PRODUCTO" ->
                            btnProductos.doClick();

                        case "CLIENTE" ->
                            btnClientes.doClick();

                        case "VENTA" ->
                            btnVentas.doClick();

                        case "COMPRA" ->
                            btnCompras.doClick();

                        case "REPARACION" ->
                            btnReparaciones.doClick();

                        default -> {
                        }
                    }
                }
        );
    }
    
    
    private void marcarBotonActivo(
        javax.swing.JButton botonSeleccionado) {

    java.awt.Color azulActivo =
            new java.awt.Color(70, 119, 177);

    java.awt.Color textoInactivo =
            new java.awt.Color(30, 73, 125);

    java.awt.Color borde =
            new java.awt.Color(230, 233, 238);

    for (javax.swing.JButton boton
            : obtenerBotonesMenu()) {

        boolean activo =
                boton == botonSeleccionado;

        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);

        boton.setBackground(
                activo
                        ? azulActivo
                        : java.awt.Color.WHITE
        );

        boton.setForeground(
                activo
                        ? java.awt.Color.WHITE
                        : textoInactivo
        );

        boton.setFont(
                new java.awt.Font(
                        "Segoe UI",
                        activo
                                ? java.awt.Font.BOLD
                                : java.awt.Font.PLAIN,
                        14
                )
        );

        boton.setBorder(
                javax.swing.BorderFactory.createMatteBorder(
                        0,
                        0,
                        1,
                        0,
                        borde
                )
        );
    }

    botonMenuActivo = botonSeleccionado;
}
    
    private void mostrarModulo(
            javax.swing.JButton boton,
            javax.swing.JComponent panel) {

        marcarBotonActivo(boton);
        mostrarPanel(panel);
    }
    
    private void configurarPermisos() {

        if (!Sesion.haySesionActiva()) {
            return;
        }

        boolean esDueno = Sesion.esDueno();

        /*
     * Cambia btnUsuarios por el nombre real del botón
     * que abre el módulo de usuarios.
         */
        btnUsuarios.setVisible(esDueno);
        btnVentaRapida.setVisible(esDueno);
    }
    private void mostrarPanel(
            javax.swing.JComponent panel) {

        pnlContenido.removeAll();

        pnlContenido.setLayout(
                new java.awt.BorderLayout()
        );

        pnlContenido.add(
                panel,
                java.awt.BorderLayout.CENTER
        );

        pnlContenido.revalidate();
        pnlContenido.repaint();
    }
    private void aplicarEstilos() {
        Color borde = new Color(222, 228, 236);
        Color azul = new Color(64, 108, 163);

        javax.swing.JPanel[] tarjetas = {
            pnlVentasHoy,
            pnlProductosRegistrados,
            pnlStockBajo,
            pnlCreditosPendientes,
            pnlReparacionesPendientes,
            pnlVentasRecientes,
            pnlPocoInventario,
            pnlActividadSemanal
        };

        for (javax.swing.JPanel tarjeta : tarjetas) {
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borde),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }

        javax.swing.JButton[] botonesMenu = {
            btnInicio, btnVentas, btnVentaRapida, btnCompras, btnProductos, btnInventario,
            btnClientes, btnProveedores, btnCreditos, btnReparaciones,
            btnUsuarios, btnReportes, btnConfiguracion
        };

        for (javax.swing.JButton boton : botonesMenu) {
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setHorizontalAlignment(SwingConstants.LEFT);
        }

        btnInicio.setBackground(azul);
        btnInicio.setForeground(Color.WHITE);
        btnInicio.setOpaque(true);

        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 234)),
                BorderFactory.createEmptyBorder(0, 14, 0, 10)
        ));

        btnFecha.setBorder(BorderFactory.createLineBorder(
                new Color(214, 223, 234)
        ));

        estilizarTabla(tblVentasRecientes);
        estilizarTabla(tblPocoInventario);
    }

    private void estilizarTabla(javax.swing.JTable tabla) {
        tabla.setRowHeight(38);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(232, 237, 243));
        tabla.setSelectionBackground(new Color(232, 241, 252));
        tabla.setSelectionForeground(new Color(30, 55, 90));

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(new Color(248, 250, 253));
        cabecera.setForeground(new Color(34, 59, 94));
        cabecera.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cabecera.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(renderer);
        }
    }

    private void cargarDatos() {
        tblVentasRecientes.setModel(new DefaultTableModel(
                new Object[][]{
                    {"1", "María González", "24/05/2025 10:45", "Completada"},
                    {"2", "Carlos Ramírez", "24/05/2025 10:15", "Completada"},
                    {"3", "Distribuidora del Sur", "24/05/2025 09:50", "Completada"},
                    {"4", "Laura Pérez", "24/05/2025 09:20", "Pendiente"},
                    {"5", "ElectroHogar S.A.", "24/05/2025 08:55", "Pendiente"}
                },
                new String[]{"#", "Cliente", "Fecha", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tblPocoInventario.setModel(new DefaultTableModel(
                new Object[][]{
                    {"Cable HDMI 2m", "3", "10", "Crítico"},
                    {"Mouse inalámbrico", "4", "10", "Crítico"},
                    {"Teclado USB", "6", "10", "Bajo"},
                    {"Parlante Bluetooth", "7", "15", "Bajo"},
                    {"Cargador USB-C", "8", "15", "Bajo"}
                },
                new String[]{"Producto", "Stock actual", "Stock mínimo", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        estilizarTabla(tblVentasRecientes);
        estilizarTabla(tblPocoInventario);
    }

    public void abrirVentaConCliente(
            Cliente cliente) {

        if (cliente == null
                || cliente.getIdCliente() <= 0) {

            return;
        }

        if (ventasPanel == null) {
            ventasPanel = new VentasPanel();
        }

        mostrarModulo(
                btnVentas,
                ventasPanel
        );

        ventasPanel.activar();
        ventasPanel.prepararNuevaVentaParaCliente(
                cliente
        );
    }

    public void abrirCompraConProveedor(
            Proveedor proveedor) {

        if (proveedor == null
                || proveedor.getIdProveedor() <= 0) {

            return;
        }

        if (comprasPanel == null) {
            comprasPanel = new ComprasPanel();
        }

        mostrarModulo(
                btnCompras,
                comprasPanel
        );

        comprasPanel.activar();
        comprasPanel.prepararNuevaCompraParaProveedor(
                proveedor
        );
    }

    private void configurarNavegacion() {

        btnInicio.addActionListener(e -> {

            InicioPanel panel = obtenerInicioPanel();
            panel.recargar();

            mostrarModulo(
                    btnInicio,
                    obtenerInicioPanel() );
        });

        btnVentaRapida.addActionListener(e -> {
            if (!HorarioVentaRapidaUtil.estaHabilitadaAhora()) {
                JOptionPane.showMessageDialog(
                        this,
                        "La venta rápida está disponible únicamente de "
                        + HorarioVentaRapidaUtil.descripcionHorario()
                        + ".",
                        "Venta rápida no disponible",
                        JOptionPane.WARNING_MESSAGE
                );
                actualizarDisponibilidadVentaRapida();
                return;
            }

            if (!Sesion.esDueno()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo el dueño puede registrar ventas rápidas.",
                        "Acceso restringido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (ventaRapidaPanel == null) {
                ventaRapidaPanel = new VentaRapidaPanel();
            }

            mostrarModulo(btnVentaRapida, ventaRapidaPanel);
            ventaRapidaPanel.activar();
        });

        btnCompras.addActionListener(e -> {

            if (comprasPanel == null) {
                comprasPanel = new ComprasPanel();
            }

            mostrarModulo(
                    btnCompras,
                    comprasPanel
            );

            comprasPanel.activar();
        });

        btnProductos.addActionListener(e -> {

            if (productosPanel == null) {
                productosPanel = new ProductosPanel();
            }

            mostrarModulo(
                    btnProductos,
                    productosPanel
            );

            productosPanel.activar();
        });

        btnInventario.addActionListener(e -> {

            if (inventarioPanel == null) {
                inventarioPanel = new InventarioPanel();
            }

            mostrarModulo(
                    btnInventario,
                    inventarioPanel
            );

            inventarioPanel.activar();
        });
        
        btnClientes.addActionListener(e -> {

            if (clientesPanel == null) {
                clientesPanel = new ClientesPanel();
            }

            mostrarModulo(
                    btnClientes,
                    clientesPanel
            );

            clientesPanel.activar();
        });

        btnProveedores.addActionListener(e -> {

            if (proveedoresPanel == null) {
                proveedoresPanel
                        = new ProveedoresPanel();
            } 

            mostrarModulo(
                    btnProveedores,
                    proveedoresPanel
            );
            proveedoresPanel.activar();
        });

        btnVentas.addActionListener(e -> {

            if (ventasPanel == null) {
                ventasPanel = new VentasPanel();
            } 

            mostrarModulo(
                    btnVentas,
                    ventasPanel
            );
            ventasPanel.activar();
        });

        btnCreditos.addActionListener(e -> {

            if (creditosPanel == null) {
                creditosPanel = new CreditosPanel();
            }

            mostrarModulo(
                    btnCreditos,
                    creditosPanel
            );

            creditosPanel.activar();
        });

        btnReparaciones.addActionListener(e -> {

            if (reparacionesPanel == null) {
                reparacionesPanel = new ReparacionesPanel();
            }

            mostrarModulo(
                    btnReparaciones,
                    reparacionesPanel
            );

            reparacionesPanel.activar();
        });

        btnUsuarios.addActionListener(e -> {

            if (!Sesion.esDueno()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo el dueño puede administrar usuarios.",
                        "Acceso restringido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (usuariosPanel == null) {
                usuariosPanel = new UsuariosPanel();
            }

            mostrarModulo(
                    btnUsuarios,
                    usuariosPanel
            );
            usuariosPanel.activar();
        });
        
        btnReportes.addActionListener(e -> {

            if (reportesPanel == null) {
                reportesPanel = new ReportesPanel();
            }

            mostrarModulo(
                    btnReportes,
                    reportesPanel
            );

            reportesPanel.activar();
        });

        btnConfiguracion.addActionListener(e -> {

            if (!Sesion.esDueno()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo el dueño puede modificar la configuración.",
                        "Acceso restringido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (configuracionPanel == null) {
                configuracionPanel = new ConfiguracionPanel();
            }

            mostrarModulo(
                    btnConfiguracion,
                    configuracionPanel
            );

            configuracionPanel.activar();
        });
    }

    private void abrirModulo(String modulo) {
        JOptionPane.showMessageDialog(
                this,
                "El JFrame Form de " + modulo + " se agregará después.",
                "SIGIR",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    
    private void cerrarSesion() {

    int respuesta =
            javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "¿Deseas cerrar la sesión actual?",
                    "Cerrar sesión",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );

    if (respuesta
            != javax.swing.JOptionPane.YES_OPTION) {

        return;
    }

    detenerTimers();
    SesionRemota.cerrarSilenciosamente("CIERRE_USUARIO");
    Sesion.cerrar();

    LoginFrame login = new LoginFrame();
    login.setVisible(true);

    dispose();
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMenu = new javax.swing.JPanel();
        lblMarca = new javax.swing.JLabel();
        btnCerrarSesion = new javax.swing.JButton();
        lblSubMarca = new javax.swing.JLabel();
        btnInicio = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnVentaRapida = new javax.swing.JButton();
        btnCompras = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        btnProveedores = new javax.swing.JButton();
        btnCreditos = new javax.swing.JButton();
        btnReparaciones = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        btnReportes = new javax.swing.JButton();
        lblVersion = new javax.swing.JLabel();
        btnConfiguracion = new javax.swing.JButton();
        pnlDerecha = new javax.swing.JPanel();
        pnlBarraSuperior = new javax.swing.JPanel();
        txtBuscar = new javax.swing.JTextField();
        lblNotificacion = new javax.swing.JLabel();
        lblAvatar = new javax.swing.JLabel();
        lblNombreUsuario = new javax.swing.JLabel();
        lblRolUsuario = new javax.swing.JLabel();
        pnlContenido = new javax.swing.JPanel();
        lblBienvenida = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        btnFecha = new javax.swing.JButton();
        pnlVentasHoy = new javax.swing.JPanel();
        lblVentasTitulo = new javax.swing.JLabel();
        lblVentasValor = new javax.swing.JLabel();
        lblVentasEnlace = new javax.swing.JLabel();
        pnlProductosRegistrados = new javax.swing.JPanel();
        lblProductosTitulo = new javax.swing.JLabel();
        lblProductosValor = new javax.swing.JLabel();
        lblProductosEnlace = new javax.swing.JLabel();
        pnlStockBajo = new javax.swing.JPanel();
        lblStockTitulo = new javax.swing.JLabel();
        lblStockValor = new javax.swing.JLabel();
        lblStockEnlace = new javax.swing.JLabel();
        pnlCreditosPendientes = new javax.swing.JPanel();
        lblCreditosTitulo = new javax.swing.JLabel();
        lblCreditosValor = new javax.swing.JLabel();
        lblCreditosEnlace = new javax.swing.JLabel();
        pnlReparacionesPendientes = new javax.swing.JPanel();
        lblReparacionesTitulo = new javax.swing.JLabel();
        lblReparacionesValor = new javax.swing.JLabel();
        lblReparacionesEnlace = new javax.swing.JLabel();
        pnlVentasRecientes = new javax.swing.JPanel();
        lblVentasRecientesTitulo = new javax.swing.JLabel();
        btnVerVentas = new javax.swing.JButton();
        scrollVentas = new javax.swing.JScrollPane();
        tblVentasRecientes = new javax.swing.JTable();
        pnlPocoInventario = new javax.swing.JPanel();
        lblPocoInventarioTitulo = new javax.swing.JLabel();
        btnVerInventario = new javax.swing.JButton();
        scrollInventario = new javax.swing.JScrollPane();
        tblPocoInventario = new javax.swing.JTable();
        pnlActividadSemanal = new javax.swing.JPanel();
        lblActividadTitulo = new javax.swing.JLabel();
        lblActividadSubtitulo = new javax.swing.JLabel();
        pnlBarraLun = new javax.swing.JPanel();
        pnlBarraMar = new javax.swing.JPanel();
        pnlBarraMie = new javax.swing.JPanel();
        pnlBarraJue = new javax.swing.JPanel();
        pnlBarraVie = new javax.swing.JPanel();
        pnlBarraSab = new javax.swing.JPanel();
        pnlBarraDom = new javax.swing.JPanel();
        lblLun = new javax.swing.JLabel();
        lblMar = new javax.swing.JLabel();
        lblMie = new javax.swing.JLabel();
        lblJue = new javax.swing.JLabel();
        lblVie = new javax.swing.JLabel();
        lblSab = new javax.swing.JLabel();
        lblDom = new javax.swing.JLabel();
        lblTotalTexto = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        lblPromedioTexto = new javax.swing.JLabel();
        lblPromedioValor = new javax.swing.JLabel();
        lblMejorDiaTexto = new javax.swing.JLabel();
        lblMejorDiaValor = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SIGIR - Inicio");
        setMinimumSize(new java.awt.Dimension(1200, 760));

        pnlMenu.setBackground(new java.awt.Color(255, 255, 255));
        pnlMenu.setPreferredSize(new java.awt.Dimension(250, 800));
        pnlMenu.setLayout(null);

        lblMarca.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        lblMarca.setForeground(new java.awt.Color(21, 50, 91));
        lblMarca.setText("⬡ SIGIR");
        pnlMenu.add(lblMarca);
        lblMarca.setBounds(28, 24, 180, 44);

        btnCerrarSesion.setBackground(new java.awt.Color(204, 0, 0));
        btnCerrarSesion.setForeground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.setToolTipText("Cerrar la sesión actual");
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });
        pnlMenu.add(btnCerrarSesion);
        btnCerrarSesion.setBounds(20, 676, 210, 42);

        lblSubMarca.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblSubMarca.setForeground(new java.awt.Color(75, 108, 151));
        lblSubMarca.setText("Sistema de Gestión de Inventario");
        pnlMenu.add(lblSubMarca);
        lblSubMarca.setBounds(28, 70, 200, 20);

        btnInicio.setBackground(new java.awt.Color(64, 108, 163));
        btnInicio.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnInicio.setForeground(new java.awt.Color(255, 255, 255));
        btnInicio.setText("⌂   Inicio");
        pnlMenu.add(btnInicio);
        btnInicio.setBounds(16, 104, 218, 42);

        btnVentas.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnVentas.setForeground(new java.awt.Color(49, 85, 132));
        btnVentas.setText("🛒  Ventas");
        pnlMenu.add(btnVentas);
        btnVentas.setBounds(16, 147, 218, 42);

        btnVentaRapida.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnVentaRapida.setForeground(new java.awt.Color(49, 85, 132));
        btnVentaRapida.setText("Venta rápida");
        pnlMenu.add(btnVentaRapida);
        btnVentaRapida.setBounds(16, 190, 218, 42);

        btnCompras.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnCompras.setForeground(new java.awt.Color(49, 85, 132));
        btnCompras.setText("▣   Compras");
        pnlMenu.add(btnCompras);
        btnCompras.setBounds(16, 233, 218, 42);

        btnProductos.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnProductos.setForeground(new java.awt.Color(49, 85, 132));
        btnProductos.setText("◇   Productos");
        pnlMenu.add(btnProductos);
        btnProductos.setBounds(16, 276, 218, 42);

        btnInventario.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnInventario.setForeground(new java.awt.Color(49, 85, 132));
        btnInventario.setText("▤   Inventario");
        pnlMenu.add(btnInventario);
        btnInventario.setBounds(16, 319, 218, 42);

        btnClientes.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnClientes.setForeground(new java.awt.Color(49, 85, 132));
        btnClientes.setText("♙   Clientes");
        pnlMenu.add(btnClientes);
        btnClientes.setBounds(16, 362, 218, 42);

        btnProveedores.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnProveedores.setForeground(new java.awt.Color(49, 85, 132));
        btnProveedores.setText("▱   Proveedores");
        pnlMenu.add(btnProveedores);
        btnProveedores.setBounds(16, 405, 218, 42);

        btnCreditos.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnCreditos.setForeground(new java.awt.Color(49, 85, 132));
        btnCreditos.setText("▧   Créditos");
        pnlMenu.add(btnCreditos);
        btnCreditos.setBounds(16, 448, 218, 42);

        btnReparaciones.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnReparaciones.setForeground(new java.awt.Color(49, 85, 132));
        btnReparaciones.setText("🔧  Reparaciones");
        pnlMenu.add(btnReparaciones);
        btnReparaciones.setBounds(16, 491, 218, 42);

        btnUsuarios.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnUsuarios.setForeground(new java.awt.Color(49, 85, 132));
        btnUsuarios.setText("♙   Usuarios");
        pnlMenu.add(btnUsuarios);
        btnUsuarios.setBounds(16, 534, 218, 42);

        btnReportes.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnReportes.setForeground(new java.awt.Color(49, 85, 132));
        btnReportes.setText("▥   Reportes");
        pnlMenu.add(btnReportes);
        btnReportes.setBounds(16, 577, 218, 42);

        lblVersion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblVersion.setForeground(new java.awt.Color(65, 98, 143));
        lblVersion.setText("◉   SIGIR v1.0.0");
        pnlMenu.add(lblVersion);
        lblVersion.setBounds(30, 735, 150, 24);

        btnConfiguracion.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        btnConfiguracion.setForeground(new java.awt.Color(49, 85, 132));
        btnConfiguracion.setText("⚙   Configuración ");
        pnlMenu.add(btnConfiguracion);
        btnConfiguracion.setBounds(16, 620, 218, 42);

        getContentPane().add(pnlMenu, java.awt.BorderLayout.WEST);

        pnlDerecha.setBackground(new java.awt.Color(248, 250, 253));
        pnlDerecha.setLayout(new java.awt.BorderLayout());

        pnlBarraSuperior.setBackground(new java.awt.Color(255, 255, 255));
        pnlBarraSuperior.setPreferredSize(new java.awt.Dimension(1000, 82));
        pnlBarraSuperior.setLayout(null);

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBuscar.setForeground(new java.awt.Color(104, 128, 162));
        txtBuscar.setText("Buscar productos, clientes, documentos...");
        pnlBarraSuperior.add(txtBuscar);
        txtBuscar.setBounds(30, 18, 490, 46);

        lblNotificacion.setFont(new java.awt.Font("Segoe UI Symbol", 0, 28)); // NOI18N
        lblNotificacion.setForeground(new java.awt.Color(50, 87, 137));
        lblNotificacion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNotificacion.setText("♧");
        pnlBarraSuperior.add(lblNotificacion);
        lblNotificacion.setBounds(820, 18, 50, 46);

        lblAvatar.setBackground(new java.awt.Color(241, 244, 248));
        lblAvatar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblAvatar.setForeground(new java.awt.Color(67, 94, 134));
        lblAvatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAvatar.setText("AD");
        lblAvatar.setOpaque(true);
        pnlBarraSuperior.add(lblAvatar);
        lblAvatar.setBounds(900, 18, 46, 46);

        lblNombreUsuario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblNombreUsuario.setForeground(new java.awt.Color(27, 50, 84));
        lblNombreUsuario.setText("Admin Demo");
        pnlBarraSuperior.add(lblNombreUsuario);
        lblNombreUsuario.setBounds(960, 20, 140, 20);

        lblRolUsuario.setForeground(new java.awt.Color(76, 105, 146));
        lblRolUsuario.setText("Administrador");
        pnlBarraSuperior.add(lblRolUsuario);
        lblRolUsuario.setBounds(960, 42, 120, 18);

        pnlDerecha.add(pnlBarraSuperior, java.awt.BorderLayout.NORTH);

        pnlContenido.setBackground(new java.awt.Color(248, 250, 253));
        pnlContenido.setPreferredSize(new java.awt.Dimension(1116, 810));
        pnlContenido.setLayout(null);

        lblBienvenida.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblBienvenida.setForeground(new java.awt.Color(20, 43, 78));
        lblBienvenida.setText("¡Bienvenido, Admin!");
        pnlContenido.add(lblBienvenida);
        lblBienvenida.setBounds(34, 24, 370, 36);

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDescripcion.setForeground(new java.awt.Color(79, 109, 151));
        lblDescripcion.setText("Resumen general de tu inventario y operaciones.");
        pnlContenido.add(lblDescripcion);
        lblDescripcion.setBounds(34, 58, 420, 24);

        btnFecha.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnFecha.setForeground(new java.awt.Color(76, 105, 146));
        btnFecha.setText("▣   24 de mayo, 2025   ⌄");
        pnlContenido.add(btnFecha);
        btnFecha.setBounds(900, 28, 185, 44);

        pnlVentasHoy.setBackground(new java.awt.Color(255, 255, 255));
        pnlVentasHoy.setLayout(null);

        lblVentasTitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblVentasTitulo.setForeground(new java.awt.Color(67, 93, 130));
        lblVentasTitulo.setText("Ventas de hoy");
        pnlVentasHoy.add(lblVentasTitulo);
        lblVentasTitulo.setBounds(22, 18, 175, 22);

        lblVentasValor.setFont(new java.awt.Font("Segoe UI", 1, 27)); // NOI18N
        lblVentasValor.setForeground(new java.awt.Color(20, 43, 78));
        lblVentasValor.setText("24");
        pnlVentasHoy.add(lblVentasValor);
        lblVentasValor.setBounds(22, 44, 100, 38);

        lblVentasEnlace.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblVentasEnlace.setForeground(new java.awt.Color(31, 102, 220));
        lblVentasEnlace.setText("Ver detalles  →");
        pnlVentasHoy.add(lblVentasEnlace);
        lblVentasEnlace.setBounds(22, 96, 160, 20);

        pnlContenido.add(pnlVentasHoy);
        pnlVentasHoy.setBounds(34, 100, 195, 130);

        pnlProductosRegistrados.setBackground(new java.awt.Color(255, 255, 255));
        pnlProductosRegistrados.setLayout(null);

        lblProductosTitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblProductosTitulo.setForeground(new java.awt.Color(67, 93, 130));
        lblProductosTitulo.setText("Productos registrados");
        pnlProductosRegistrados.add(lblProductosTitulo);
        lblProductosTitulo.setBounds(22, 18, 175, 22);

        lblProductosValor.setFont(new java.awt.Font("Segoe UI", 1, 27)); // NOI18N
        lblProductosValor.setForeground(new java.awt.Color(20, 43, 78));
        lblProductosValor.setText("256");
        pnlProductosRegistrados.add(lblProductosValor);
        lblProductosValor.setBounds(22, 44, 100, 38);

        lblProductosEnlace.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblProductosEnlace.setForeground(new java.awt.Color(31, 102, 220));
        lblProductosEnlace.setText("Ver productos  →");
        pnlProductosRegistrados.add(lblProductosEnlace);
        lblProductosEnlace.setBounds(22, 96, 160, 20);

        pnlContenido.add(pnlProductosRegistrados);
        pnlProductosRegistrados.setBounds(244, 100, 195, 130);

        pnlStockBajo.setBackground(new java.awt.Color(255, 255, 255));
        pnlStockBajo.setLayout(null);

        lblStockTitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStockTitulo.setForeground(new java.awt.Color(67, 93, 130));
        lblStockTitulo.setText("Stock bajo");
        pnlStockBajo.add(lblStockTitulo);
        lblStockTitulo.setBounds(22, 18, 175, 22);

        lblStockValor.setFont(new java.awt.Font("Segoe UI", 1, 27)); // NOI18N
        lblStockValor.setForeground(new java.awt.Color(20, 43, 78));
        lblStockValor.setText("18");
        pnlStockBajo.add(lblStockValor);
        lblStockValor.setBounds(22, 44, 100, 38);

        lblStockEnlace.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblStockEnlace.setForeground(new java.awt.Color(31, 102, 220));
        lblStockEnlace.setText("Ver inventario  →");
        pnlStockBajo.add(lblStockEnlace);
        lblStockEnlace.setBounds(22, 96, 160, 20);

        pnlContenido.add(pnlStockBajo);
        pnlStockBajo.setBounds(454, 100, 195, 130);

        pnlCreditosPendientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlCreditosPendientes.setLayout(null);

        lblCreditosTitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCreditosTitulo.setForeground(new java.awt.Color(67, 93, 130));
        lblCreditosTitulo.setText("Créditos pendientes");
        pnlCreditosPendientes.add(lblCreditosTitulo);
        lblCreditosTitulo.setBounds(22, 18, 175, 22);

        lblCreditosValor.setFont(new java.awt.Font("Segoe UI", 1, 27)); // NOI18N
        lblCreditosValor.setForeground(new java.awt.Color(20, 43, 78));
        lblCreditosValor.setText("12");
        pnlCreditosPendientes.add(lblCreditosValor);
        lblCreditosValor.setBounds(22, 44, 100, 38);

        lblCreditosEnlace.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCreditosEnlace.setForeground(new java.awt.Color(31, 102, 220));
        lblCreditosEnlace.setText("Ver créditos  →");
        pnlCreditosPendientes.add(lblCreditosEnlace);
        lblCreditosEnlace.setBounds(22, 96, 160, 20);

        pnlContenido.add(pnlCreditosPendientes);
        pnlCreditosPendientes.setBounds(664, 100, 195, 130);

        pnlReparacionesPendientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlReparacionesPendientes.setLayout(null);

        lblReparacionesTitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblReparacionesTitulo.setForeground(new java.awt.Color(67, 93, 130));
        lblReparacionesTitulo.setText("Reparaciones pendientes");
        pnlReparacionesPendientes.add(lblReparacionesTitulo);
        lblReparacionesTitulo.setBounds(18, 18, 175, 22);

        lblReparacionesValor.setFont(new java.awt.Font("Segoe UI", 1, 27)); // NOI18N
        lblReparacionesValor.setForeground(new java.awt.Color(20, 43, 78));
        lblReparacionesValor.setText("7");
        pnlReparacionesPendientes.add(lblReparacionesValor);
        lblReparacionesValor.setBounds(18, 44, 100, 38);

        lblReparacionesEnlace.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblReparacionesEnlace.setForeground(new java.awt.Color(31, 102, 220));
        lblReparacionesEnlace.setText("Ver reparaciones  →");
        pnlReparacionesPendientes.add(lblReparacionesEnlace);
        lblReparacionesEnlace.setBounds(18, 96, 160, 20);

        pnlContenido.add(pnlReparacionesPendientes);
        pnlReparacionesPendientes.setBounds(874, 100, 195, 130);

        pnlVentasRecientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlVentasRecientes.setLayout(null);

        lblVentasRecientesTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblVentasRecientesTitulo.setForeground(new java.awt.Color(27, 52, 88));
        lblVentasRecientesTitulo.setText("Ventas recientes");
        pnlVentasRecientes.add(lblVentasRecientesTitulo);
        lblVentasRecientesTitulo.setBounds(18, 12, 180, 26);

        btnVerVentas.setForeground(new java.awt.Color(75, 102, 142));
        btnVerVentas.setText("Ver todas");
        pnlVentasRecientes.add(btnVerVentas);
        btnVerVentas.setBounds(420, 10, 80, 30);

        scrollVentas.setViewportView(tblVentasRecientes);

        pnlVentasRecientes.add(scrollVentas);
        scrollVentas.setBounds(12, 48, 492, 208);

        pnlContenido.add(pnlVentasRecientes);
        pnlVentasRecientes.setBounds(34, 248, 520, 270);

        pnlPocoInventario.setBackground(new java.awt.Color(255, 255, 255));
        pnlPocoInventario.setLayout(null);

        lblPocoInventarioTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblPocoInventarioTitulo.setForeground(new java.awt.Color(27, 52, 88));
        lblPocoInventarioTitulo.setText("Productos con poco inventario");
        pnlPocoInventario.add(lblPocoInventarioTitulo);
        lblPocoInventarioTitulo.setBounds(18, 12, 250, 26);

        btnVerInventario.setForeground(new java.awt.Color(75, 102, 142));
        btnVerInventario.setText("Ver todos");
        pnlPocoInventario.add(btnVerInventario);
        btnVerInventario.setBounds(420, 10, 80, 30);

        scrollInventario.setViewportView(tblPocoInventario);

        pnlPocoInventario.add(scrollInventario);
        scrollInventario.setBounds(12, 48, 492, 208);

        pnlContenido.add(pnlPocoInventario);
        pnlPocoInventario.setBounds(570, 248, 520, 270);

        pnlActividadSemanal.setBackground(new java.awt.Color(255, 255, 255));
        pnlActividadSemanal.setLayout(null);

        lblActividadTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblActividadTitulo.setForeground(new java.awt.Color(27, 52, 88));
        lblActividadTitulo.setText("Actividad semanal");
        pnlActividadSemanal.add(lblActividadTitulo);
        lblActividadTitulo.setBounds(18, 12, 180, 26);

        lblActividadSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblActividadSubtitulo.setForeground(new java.awt.Color(97, 123, 159));
        lblActividadSubtitulo.setText("Operaciones");
        pnlActividadSemanal.add(lblActividadSubtitulo);
        lblActividadSubtitulo.setBounds(18, 38, 100, 18);

        pnlBarraLun.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraLun.setLayout(null);
        pnlActividadSemanal.add(pnlBarraLun);
        pnlBarraLun.setBounds(90, 112, 42, 78);

        pnlBarraMar.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraMar.setLayout(null);
        pnlActividadSemanal.add(pnlBarraMar);
        pnlBarraMar.setBounds(164, 92, 42, 98);

        pnlBarraMie.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraMie.setLayout(null);
        pnlActividadSemanal.add(pnlBarraMie);
        pnlBarraMie.setBounds(238, 72, 42, 118);

        pnlBarraJue.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraJue.setLayout(null);
        pnlActividadSemanal.add(pnlBarraJue);
        pnlBarraJue.setBounds(312, 82, 42, 108);

        pnlBarraVie.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraVie.setLayout(null);
        pnlActividadSemanal.add(pnlBarraVie);
        pnlBarraVie.setBounds(386, 46, 42, 144);

        pnlBarraSab.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraSab.setLayout(null);
        pnlActividadSemanal.add(pnlBarraSab);
        pnlBarraSab.setBounds(460, 96, 42, 94);

        pnlBarraDom.setBackground(new java.awt.Color(74, 113, 165));
        pnlBarraDom.setLayout(null);
        pnlActividadSemanal.add(pnlBarraDom);
        pnlBarraDom.setBounds(534, 122, 42, 68);

        lblLun.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLun.setText("Lun");
        pnlActividadSemanal.add(lblLun);
        lblLun.setBounds(84, 195, 54, 20);

        lblMar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMar.setText("Mar");
        pnlActividadSemanal.add(lblMar);
        lblMar.setBounds(158, 195, 54, 20);

        lblMie.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMie.setText("Mié");
        pnlActividadSemanal.add(lblMie);
        lblMie.setBounds(232, 195, 54, 20);

        lblJue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblJue.setText("Jue");
        pnlActividadSemanal.add(lblJue);
        lblJue.setBounds(306, 195, 54, 20);

        lblVie.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVie.setText("Vie");
        pnlActividadSemanal.add(lblVie);
        lblVie.setBounds(380, 195, 54, 20);

        lblSab.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSab.setText("Sáb");
        pnlActividadSemanal.add(lblSab);
        lblSab.setBounds(454, 195, 54, 20);

        lblDom.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDom.setText("Dom");
        pnlActividadSemanal.add(lblDom);
        lblDom.setBounds(528, 195, 54, 20);

        lblTotalTexto.setForeground(new java.awt.Color(98, 124, 159));
        lblTotalTexto.setText("Total de operaciones de la semana");
        pnlActividadSemanal.add(lblTotalTexto);
        lblTotalTexto.setBounds(680, 40, 250, 20);

        lblTotalValor.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTotalValor.setForeground(new java.awt.Color(24, 50, 87));
        lblTotalValor.setText("203");
        pnlActividadSemanal.add(lblTotalValor);
        lblTotalValor.setBounds(680, 62, 120, 32);

        lblPromedioTexto.setForeground(new java.awt.Color(98, 124, 159));
        lblPromedioTexto.setText("Promedio diario");
        pnlActividadSemanal.add(lblPromedioTexto);
        lblPromedioTexto.setBounds(680, 104, 150, 20);

        lblPromedioValor.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblPromedioValor.setForeground(new java.awt.Color(24, 50, 87));
        lblPromedioValor.setText("29");
        pnlActividadSemanal.add(lblPromedioValor);
        lblPromedioValor.setBounds(680, 126, 100, 32);

        lblMejorDiaTexto.setForeground(new java.awt.Color(98, 124, 159));
        lblMejorDiaTexto.setText("Mejor día");
        pnlActividadSemanal.add(lblMejorDiaTexto);
        lblMejorDiaTexto.setBounds(850, 104, 150, 20);

        lblMejorDiaValor.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblMejorDiaValor.setForeground(new java.awt.Color(28, 103, 215));
        lblMejorDiaValor.setText("Viernes (42 operaciones)");
        pnlActividadSemanal.add(lblMejorDiaValor);
        lblMejorDiaValor.setBounds(850, 128, 190, 26);

        pnlContenido.add(pnlActividadSemanal);
        pnlActividadSemanal.setBounds(34, 536, 1056, 230);

        pnlDerecha.add(pnlContenido, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlDerecha, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
        cerrarSesion();
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmInicio("admin").setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnCompras;
    private javax.swing.JButton btnConfiguracion;
    private javax.swing.JButton btnCreditos;
    private javax.swing.JButton btnFecha;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnProveedores;
    private javax.swing.JButton btnReparaciones;
    private javax.swing.JButton btnReportes;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVentas;
    private javax.swing.JButton btnVentaRapida;
    private javax.swing.JButton btnVerInventario;
    private javax.swing.JButton btnVerVentas;
    private javax.swing.JLabel lblActividadSubtitulo;
    private javax.swing.JLabel lblActividadTitulo;
    private javax.swing.JLabel lblAvatar;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblCreditosEnlace;
    private javax.swing.JLabel lblCreditosTitulo;
    private javax.swing.JLabel lblCreditosValor;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblDom;
    private javax.swing.JLabel lblJue;
    private javax.swing.JLabel lblLun;
    private javax.swing.JLabel lblMar;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblMejorDiaTexto;
    private javax.swing.JLabel lblMejorDiaValor;
    private javax.swing.JLabel lblMie;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblNotificacion;
    private javax.swing.JLabel lblPocoInventarioTitulo;
    private javax.swing.JLabel lblProductosEnlace;
    private javax.swing.JLabel lblProductosTitulo;
    private javax.swing.JLabel lblProductosValor;
    private javax.swing.JLabel lblPromedioTexto;
    private javax.swing.JLabel lblPromedioValor;
    private javax.swing.JLabel lblReparacionesEnlace;
    private javax.swing.JLabel lblReparacionesTitulo;
    private javax.swing.JLabel lblReparacionesValor;
    private javax.swing.JLabel lblRolUsuario;
    private javax.swing.JLabel lblSab;
    private javax.swing.JLabel lblStockEnlace;
    private javax.swing.JLabel lblStockTitulo;
    private javax.swing.JLabel lblStockValor;
    private javax.swing.JLabel lblSubMarca;
    private javax.swing.JLabel lblTotalTexto;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JLabel lblVentasEnlace;
    private javax.swing.JLabel lblVentasRecientesTitulo;
    private javax.swing.JLabel lblVentasTitulo;
    private javax.swing.JLabel lblVentasValor;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JLabel lblVie;
    private javax.swing.JPanel pnlActividadSemanal;
    private javax.swing.JPanel pnlBarraDom;
    private javax.swing.JPanel pnlBarraJue;
    private javax.swing.JPanel pnlBarraLun;
    private javax.swing.JPanel pnlBarraMar;
    private javax.swing.JPanel pnlBarraMie;
    private javax.swing.JPanel pnlBarraSab;
    private javax.swing.JPanel pnlBarraSuperior;
    private javax.swing.JPanel pnlBarraVie;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlCreditosPendientes;
    private javax.swing.JPanel pnlDerecha;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JPanel pnlPocoInventario;
    private javax.swing.JPanel pnlProductosRegistrados;
    private javax.swing.JPanel pnlReparacionesPendientes;
    private javax.swing.JPanel pnlStockBajo;
    private javax.swing.JPanel pnlVentasHoy;
    private javax.swing.JPanel pnlVentasRecientes;
    private javax.swing.JScrollPane scrollInventario;
    private javax.swing.JScrollPane scrollVentas;
    private javax.swing.JTable tblPocoInventario;
    private javax.swing.JTable tblVentasRecientes;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
