package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.controlador.InicioControlador;
import sigir.controlador.NavegacionInicioListener;
import sigir.modelo.ActividadDiariaInicio;
import sigir.modelo.DatosInicio;
import sigir.modelo.ModuloInicio;
import sigir.modelo.ProductoStockInicio;
import sigir.modelo.ResumenInicio;
import sigir.util.Sesion;

public class InicioPanel
        extends javax.swing.JPanel {

    private static final Locale LOCALE_HONDURAS =
            new Locale("es", "HN");

    private static final DateTimeFormatter
            FORMATO_FECHA_COMPLETA =
                    DateTimeFormatter.ofPattern(
                            "EEEE, d 'de' MMMM 'de' yyyy",
                            LOCALE_HONDURAS
                    );

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    LOCALE_HONDURAS
            );

    private final NavegacionInicioListener navegacion;
    private final InicioControlador controlador;

    public InicioPanel() {
        this(modulo -> {
        });
    }

    public InicioPanel(
            NavegacionInicioListener navegacion) {

        this.navegacion = navegacion == null
                ? modulo -> {
                }
                : navegacion;

        initComponents();
        configurarComponentes();
        aplicarEstilos();
        configurarEventos();
        actualizarEncabezado();

        controlador =
                new InicioControlador(this);

        controlador.iniciar();
    }

    public void recargar() {
        actualizarEncabezado();
        controlador.recargar();
    }

    private void configurarComponentes() {
        formatoMoneda.setMinimumFractionDigits(2);
        formatoMoneda.setMaximumFractionDigits(2);

        tblStockBajo.setFillsViewportHeight(true);
        tblStockBajo.setAutoCreateRowSorter(true);

        configurarModeloStock(List.of());

        graficoActividad.setDatos(List.of());
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);

        javax.swing.JPanel[] paneles = {
            pnlVentasHoy,
            pnlProductos,
            pnlStockBajo,
            pnlCreditos,
            pnlReparaciones,
            pnlProductosStock,
            pnlActividad
        };

        for (javax.swing.JPanel panel : paneles) {
            panel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    borde
                            ),
                            BorderFactory.createEmptyBorder(
                                    6, 6, 6, 6
                            )
                    )
            );
        }

        javax.swing.JButton[] enlaces = {
            btnVerVentasTarjeta,
            btnVerProductos,
            btnVerInventarioTarjeta,
            btnVerCreditos,
            btnVerReparaciones,
            btnVerInventario
        };

        for (JButton boton : enlaces) {
            boton.setBackground(Color.WHITE);
            boton.setForeground(azul);
            boton.setBorderPainted(false);
            boton.setContentAreaFilled(false);
            boton.setFocusPainted(false);
            boton.setHorizontalAlignment(
                    SwingConstants.LEFT
            );
            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        btnActualizar.setBackground(azul);
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        lblFecha.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                borde
                        ),
                        BorderFactory.createEmptyBorder(
                                0, 12, 0, 12
                        )
                )
        );

        lblFecha.setForeground(texto);

        estilizarTabla(tblStockBajo);
    }

    private void estilizarTabla(
            javax.swing.JTable tabla) {

        tabla.setRowHeight(38);
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

        DefaultTableCellRenderer centro =
                new DefaultTableCellRenderer();

        centro.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        tabla.setDefaultRenderer(
                Integer.class,
                centro
        );
    }

    private void configurarEventos() {
        btnActualizar.addActionListener(
                e -> controlador.recargar()
        );

        btnVerVentasTarjeta.addActionListener(
                e -> abrir(ModuloInicio.VENTAS)
        );

        btnVerProductos.addActionListener(
                e -> abrir(ModuloInicio.PRODUCTOS)
        );

        btnVerInventarioTarjeta.addActionListener(
                e -> abrir(ModuloInicio.INVENTARIO)
        );

        btnVerCreditos.addActionListener(
                e -> abrir(ModuloInicio.CREDITOS)
        );

        btnVerReparaciones.addActionListener(
                e -> abrir(ModuloInicio.REPARACIONES)
        );

        btnVerInventario.addActionListener(
                e -> abrir(ModuloInicio.INVENTARIO)
        );
    }

    private void abrir(ModuloInicio modulo) {
        navegacion.abrirModulo(modulo);
    }

    private void actualizarEncabezado() {
        String nombre = "Usuario";

        if (Sesion.haySesionActiva()) {
            String sesion =
                    Sesion.getNombreCompleto();

            if (sesion != null
                    && !sesion.isBlank()) {
                nombre = sesion.trim();
            }
        }

        lblBienvenida.setText(
                "¡Bienvenido, " + nombre + "!"
        );

        String fecha = LocalDate.now()
                .format(FORMATO_FECHA_COMPLETA);

        fecha = Character.toUpperCase(
                fecha.charAt(0)
        ) + fecha.substring(1);

        lblFecha.setText(fecha);
    }

    public void mostrarDatos(DatosInicio datos) {
        ResumenInicio resumen = datos.getResumen();

        lblVentasValor.setText(
                String.valueOf(
                        resumen.getVentasHoy()
                )
        );

        lblProductosValor.setText(
                String.valueOf(
                        resumen.getProductosRegistrados()
                )
        );

        lblProductosDetalle.setText(
                "productos activos"
        );

        lblStockValor.setText(
                String.valueOf(
                        resumen.getProductosStockBajo()
                )
        );

        lblStockDetalle.setText(
                resumen.getProductosStockBajo() == 1
                        ? "producto requiere atención"
                        : "productos requieren atención"
        );

        lblCreditosValor.setText(
                String.valueOf(
                        resumen.getCreditosPendientes()
                )
        );

        lblCreditosDetalle.setText(
                "Saldo: "
                + formatoMoneda.format(
                        resumen
                        .getSaldoCreditosPendientes()
                )
        );

        lblReparacionesValor.setText(
                String.valueOf(
                        resumen.getReparacionesPendientes()
                )
        );

        lblReparacionesDetalle.setText(
                "órdenes sin finalizar"
        );

        configurarModeloStock(
                datos.getProductosStockBajo()
        );

        mostrarActividad(
                datos.getActividadSemanal()
        );
    }

    private void configurarModeloStock(
            List<ProductoStockInicio> productos) {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new String[]{
                            "Código",
                            "Producto",
                            "Stock actual",
                            "Stock mínimo",
                            "Nivel"
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

        for (ProductoStockInicio producto
                : productos) {

            modelo.addRow(new Object[]{
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                textoEstado(producto.getNivel())
            });
        }

        tblStockBajo.setModel(modelo);
        estilizarTabla(tblStockBajo);

        if (tblStockBajo.getColumnCount() >= 5) {
            tblStockBajo.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(80);

            tblStockBajo.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(190);

            tblStockBajo.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(90);

            tblStockBajo.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(90);

            tblStockBajo.getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(85);
        }
    }

    private void mostrarActividad(
            List<ActividadDiariaInicio> actividad) {

        graficoActividad.setDatos(actividad);

        int total = actividad.stream()
                .mapToInt(
                        ActividadDiariaInicio
                                ::getOperaciones
                )
                .sum();

        int promedio = actividad.isEmpty()
                ? 0
                : (int) Math.round(
                        (double) total
                        / actividad.size()
                );

        ActividadDiariaInicio mejor =
                actividad.stream()
                        .max(
                                Comparator.comparingInt(
                                        ActividadDiariaInicio
                                                ::getOperaciones
                                )
                        )
                        .orElse(null);

        lblTotalSemanaValor.setText(
                String.valueOf(total)
        );

        lblPromedioValor.setText(
                String.valueOf(promedio)
        );

        if (mejor == null) {
            lblMejorDiaValor.setText(
                    "Sin actividad"
            );
        } else {
            String dia = mejor.getFecha()
                    .getDayOfWeek()
                    .getDisplayName(
                            TextStyle.FULL,
                            LOCALE_HONDURAS
                    );

            dia = Character.toUpperCase(
                    dia.charAt(0)
            ) + dia.substring(1);

            lblMejorDiaValor.setText(
                    dia
                    + " ("
                    + mejor.getOperaciones()
                    + " operaciones)"
            );
        }
    }

    public void mostrarCargando(boolean cargando) {
        btnActualizar.setEnabled(!cargando);

        btnActualizar.setText(
                cargando
                        ? "Actualizando..."
                        : "Actualizar"
        );

        if (cargando) {
            lblEstadoCarga.setText(
                    "Consultando la base de datos..."
            );

            lblEstadoCarga.setForeground(
                    new Color(98, 124, 159)
            );
        }
    }

    public void mostrarEstadoCarga(
            String mensaje,
            boolean correcto) {

        lblEstadoCarga.setText(mensaje);

        lblEstadoCarga.setForeground(
                correcto
                        ? new Color(34, 155, 85)
                        : new Color(192, 52, 52)
        );
    }

    private String textoEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "";
        }

        String texto = estado
                .trim()
                .toLowerCase()
                .replace('_', ' ');

        return Character.toUpperCase(
                texto.charAt(0)
        ) + texto.substring(1);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblBienvenida = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        btnActualizar = new javax.swing.JButton();
        lblEstadoCarga = new javax.swing.JLabel();
        pnlVentasHoy = new javax.swing.JPanel();
        lblVentasTitulo = new javax.swing.JLabel();
        lblVentasValor = new javax.swing.JLabel();
        btnVerVentasTarjeta = new javax.swing.JButton();
        pnlProductos = new javax.swing.JPanel();
        lblProductosTitulo = new javax.swing.JLabel();
        lblProductosValor = new javax.swing.JLabel();
        lblProductosDetalle = new javax.swing.JLabel();
        btnVerProductos = new javax.swing.JButton();
        pnlStockBajo = new javax.swing.JPanel();
        lblStockTitulo = new javax.swing.JLabel();
        lblStockValor = new javax.swing.JLabel();
        lblStockDetalle = new javax.swing.JLabel();
        btnVerInventarioTarjeta = new javax.swing.JButton();
        pnlCreditos = new javax.swing.JPanel();
        lblCreditosTitulo = new javax.swing.JLabel();
        lblCreditosValor = new javax.swing.JLabel();
        lblCreditosDetalle = new javax.swing.JLabel();
        btnVerCreditos = new javax.swing.JButton();
        pnlReparaciones = new javax.swing.JPanel();
        lblReparacionesTitulo = new javax.swing.JLabel();
        lblReparacionesValor = new javax.swing.JLabel();
        lblReparacionesDetalle = new javax.swing.JLabel();
        btnVerReparaciones = new javax.swing.JButton();
        pnlProductosStock = new javax.swing.JPanel();
        lblStockTablaTitulo = new javax.swing.JLabel();
        btnVerInventario = new javax.swing.JButton();
        scrollStock = new javax.swing.JScrollPane();
        tblStockBajo = new javax.swing.JTable();
        pnlActividad = new javax.swing.JPanel();
        lblActividadTitulo = new javax.swing.JLabel();
        lblActividadSubtitulo = new javax.swing.JLabel();
        graficoActividad = new sigir.componentes.GraficoActividadInicioPanel();
        lblTotalSemanaTitulo = new javax.swing.JLabel();
        lblTotalSemanaValor = new javax.swing.JLabel();
        lblPromedioTitulo = new javax.swing.JLabel();
        lblPromedioValor = new javax.swing.JLabel();
        lblMejorDiaTitulo = new javax.swing.JLabel();
        lblMejorDiaValor = new javax.swing.JLabel();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblBienvenida.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBienvenida.setForeground(new java.awt.Color(24, 50, 87));
        lblBienvenida.setText("¡Bienvenido!");
        pnlEncabezado.add(lblBienvenida);
        lblBienvenida.setBounds(0, 0, 760, 40);

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblDescripcion.setForeground(new java.awt.Color(98, 124, 159));
        lblDescripcion.setText("Resumen real de tu inventario y operaciones.");
        pnlEncabezado.add(lblDescripcion);
        lblDescripcion.setBounds(0, 42, 650, 24);

        lblFecha.setBackground(new java.awt.Color(255, 255, 255));
        lblFecha.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFecha.setOpaque(true);
        lblFecha.setText("Fecha actual");
        pnlEncabezado.add(lblFecha);
        lblFecha.setBounds(760, 4, 250, 38);

        btnActualizar.setText("Actualizar");
        pnlEncabezado.add(btnActualizar);
        btnActualizar.setBounds(1022, 4, 100, 38);

        lblEstadoCarga.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEstadoCarga.setText("Listo");
        pnlEncabezado.add(lblEstadoCarga);
        lblEstadoCarga.setBounds(760, 48, 362, 20);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 10, 1122, 72);

        pnlVentasHoy.setBackground(new java.awt.Color(255, 255, 255));
        pnlVentasHoy.setLayout(null);
        lblVentasTitulo.setText("Ventas de hoy");
        pnlVentasHoy.add(lblVentasTitulo);
        lblVentasTitulo.setBounds(16, 12, 180, 20);
        lblVentasValor.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblVentasValor.setForeground(new java.awt.Color(24, 50, 87));
        lblVentasValor.setText("0");
        pnlVentasHoy.add(lblVentasValor);
        lblVentasValor.setBounds(16, 36, 180, 38);
        btnVerVentasTarjeta.setText("Ver ventas");
        pnlVentasHoy.add(btnVerVentasTarjeta);
        btnVerVentasTarjeta.setBounds(10, 84, 185, 25);
        add(pnlVentasHoy);
        pnlVentasHoy.setBounds(28, 92, 210, 124);

        pnlProductos.setBackground(new java.awt.Color(255, 255, 255));
        pnlProductos.setLayout(null);
        lblProductosTitulo.setText("Productos registrados");
        pnlProductos.add(lblProductosTitulo);
        lblProductosTitulo.setBounds(16, 12, 180, 20);
        lblProductosValor.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblProductosValor.setForeground(new java.awt.Color(24, 50, 87));
        lblProductosValor.setText("0");
        pnlProductos.add(lblProductosValor);
        lblProductosValor.setBounds(16, 36, 180, 38);
        lblProductosDetalle.setForeground(new java.awt.Color(98, 124, 159));
        lblProductosDetalle.setText("productos activos");
        pnlProductos.add(lblProductosDetalle);
        lblProductosDetalle.setBounds(16, 73, 185, 18);
        btnVerProductos.setText("Ver productos");
        pnlProductos.add(btnVerProductos);
        btnVerProductos.setBounds(10, 94, 185, 25);
        add(pnlProductos);
        pnlProductos.setBounds(250, 92, 210, 124);

        pnlStockBajo.setBackground(new java.awt.Color(255, 255, 255));
        pnlStockBajo.setLayout(null);
        lblStockTitulo.setText("Stock bajo");
        pnlStockBajo.add(lblStockTitulo);
        lblStockTitulo.setBounds(16, 12, 180, 20);
        lblStockValor.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblStockValor.setForeground(new java.awt.Color(216, 126, 25));
        lblStockValor.setText("0");
        pnlStockBajo.add(lblStockValor);
        lblStockValor.setBounds(16, 36, 180, 38);
        lblStockDetalle.setForeground(new java.awt.Color(98, 124, 159));
        lblStockDetalle.setText("productos requieren atención");
        pnlStockBajo.add(lblStockDetalle);
        lblStockDetalle.setBounds(16, 73, 185, 18);
        btnVerInventarioTarjeta.setText("Ver inventario");
        pnlStockBajo.add(btnVerInventarioTarjeta);
        btnVerInventarioTarjeta.setBounds(10, 94, 185, 25);
        add(pnlStockBajo);
        pnlStockBajo.setBounds(472, 92, 210, 124);

        pnlCreditos.setBackground(new java.awt.Color(255, 255, 255));
        pnlCreditos.setLayout(null);
        lblCreditosTitulo.setText("Créditos pendientes");
        pnlCreditos.add(lblCreditosTitulo);
        lblCreditosTitulo.setBounds(16, 12, 180, 20);
        lblCreditosValor.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblCreditosValor.setForeground(new java.awt.Color(192, 52, 52));
        lblCreditosValor.setText("0");
        pnlCreditos.add(lblCreditosValor);
        lblCreditosValor.setBounds(16, 36, 180, 38);
        lblCreditosDetalle.setForeground(new java.awt.Color(98, 124, 159));
        lblCreditosDetalle.setText("Saldo: L 0.00");
        pnlCreditos.add(lblCreditosDetalle);
        lblCreditosDetalle.setBounds(16, 73, 185, 18);
        btnVerCreditos.setText("Ver créditos");
        pnlCreditos.add(btnVerCreditos);
        btnVerCreditos.setBounds(10, 94, 185, 25);
        add(pnlCreditos);
        pnlCreditos.setBounds(694, 92, 210, 124);

        pnlReparaciones.setBackground(new java.awt.Color(255, 255, 255));
        pnlReparaciones.setLayout(null);
        lblReparacionesTitulo.setText("Reparaciones pendientes");
        pnlReparaciones.add(lblReparacionesTitulo);
        lblReparacionesTitulo.setBounds(16, 12, 180, 20);
        lblReparacionesValor.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblReparacionesValor.setForeground(new java.awt.Color(122, 73, 196));
        lblReparacionesValor.setText("0");
        pnlReparaciones.add(lblReparacionesValor);
        lblReparacionesValor.setBounds(16, 36, 180, 38);
        lblReparacionesDetalle.setForeground(new java.awt.Color(98, 124, 159));
        lblReparacionesDetalle.setText("órdenes sin finalizar");
        pnlReparaciones.add(lblReparacionesDetalle);
        lblReparacionesDetalle.setBounds(16, 73, 185, 18);
        btnVerReparaciones.setText("Ver reparaciones");
        pnlReparaciones.add(btnVerReparaciones);
        btnVerReparaciones.setBounds(10, 94, 185, 25);
        add(pnlReparaciones);
        pnlReparaciones.setBounds(916, 92, 210, 124);

        pnlProductosStock.setBackground(new java.awt.Color(255, 255, 255));
        pnlProductosStock.setLayout(null);
        lblStockTablaTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblStockTablaTitulo.setText("Productos con poco inventario");
        pnlProductosStock.add(lblStockTablaTitulo);
        lblStockTablaTitulo.setBounds(16, 9, 300, 26);
        btnVerInventario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        btnVerInventario.setText("Ver todos");
        pnlProductosStock.add(btnVerInventario);
        btnVerInventario.setBounds(970, 9, 105, 26);
        scrollStock.setViewportView(tblStockBajo);
        pnlProductosStock.add(scrollStock);
        scrollStock.setBounds(0, 44, 1102, 238);
        add(pnlProductosStock);
        pnlProductosStock.setBounds(28, 230, 1102, 290);

        pnlActividad.setBackground(new java.awt.Color(255, 255, 255));
        pnlActividad.setLayout(null);
        lblActividadTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblActividadTitulo.setText("Actividad de los últimos 7 días");
        pnlActividad.add(lblActividadTitulo);
        lblActividadTitulo.setBounds(16, 10, 310, 24);
        lblActividadSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblActividadSubtitulo.setText("Ventas, compras, inventario, abonos y reparaciones");
        pnlActividad.add(lblActividadSubtitulo);
        lblActividadSubtitulo.setBounds(16, 34, 430, 20);
        pnlActividad.add(graficoActividad);
        graficoActividad.setBounds(16, 57, 720, 145);
        lblTotalSemanaTitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblTotalSemanaTitulo.setText("Total de operaciones");
        pnlActividad.add(lblTotalSemanaTitulo);
        lblTotalSemanaTitulo.setBounds(770, 30, 180, 20);
        lblTotalSemanaValor.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblTotalSemanaValor.setForeground(new java.awt.Color(24, 50, 87));
        lblTotalSemanaValor.setText("0");
        pnlActividad.add(lblTotalSemanaValor);
        lblTotalSemanaValor.setBounds(770, 52, 180, 34);
        lblPromedioTitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblPromedioTitulo.setText("Promedio diario");
        pnlActividad.add(lblPromedioTitulo);
        lblPromedioTitulo.setBounds(770, 96, 160, 20);
        lblPromedioValor.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblPromedioValor.setForeground(new java.awt.Color(24, 50, 87));
        lblPromedioValor.setText("0");
        pnlActividad.add(lblPromedioValor);
        lblPromedioValor.setBounds(770, 118, 160, 28);
        lblMejorDiaTitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblMejorDiaTitulo.setText("Día con más actividad");
        pnlActividad.add(lblMejorDiaTitulo);
        lblMejorDiaTitulo.setBounds(940, 96, 170, 20);
        lblMejorDiaValor.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblMejorDiaValor.setForeground(new java.awt.Color(49, 105, 181));
        lblMejorDiaValor.setText("Sin actividad");
        pnlActividad.add(lblMejorDiaValor);
        lblMejorDiaValor.setBounds(940, 118, 175, 48);
        add(pnlActividad);
        pnlActividad.setBounds(28, 534, 1102, 218);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnVerCreditos;
    private javax.swing.JButton btnVerInventario;
    private javax.swing.JButton btnVerInventarioTarjeta;
    private javax.swing.JButton btnVerProductos;
    private javax.swing.JButton btnVerReparaciones;
    private javax.swing.JButton btnVerVentasTarjeta;
    private sigir.componentes.GraficoActividadInicioPanel graficoActividad;
    private javax.swing.JLabel lblActividadSubtitulo;
    private javax.swing.JLabel lblActividadTitulo;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblCreditosDetalle;
    private javax.swing.JLabel lblCreditosTitulo;
    private javax.swing.JLabel lblCreditosValor;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblEstadoCarga;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblMejorDiaTitulo;
    private javax.swing.JLabel lblMejorDiaValor;
    private javax.swing.JLabel lblProductosDetalle;
    private javax.swing.JLabel lblProductosTitulo;
    private javax.swing.JLabel lblProductosValor;
    private javax.swing.JLabel lblPromedioTitulo;
    private javax.swing.JLabel lblPromedioValor;
    private javax.swing.JLabel lblReparacionesDetalle;
    private javax.swing.JLabel lblReparacionesTitulo;
    private javax.swing.JLabel lblReparacionesValor;
    private javax.swing.JLabel lblStockDetalle;
    private javax.swing.JLabel lblStockTablaTitulo;
    private javax.swing.JLabel lblStockTitulo;
    private javax.swing.JLabel lblStockValor;
    private javax.swing.JLabel lblTotalSemanaTitulo;
    private javax.swing.JLabel lblTotalSemanaValor;
    private javax.swing.JLabel lblVentasTitulo;
    private javax.swing.JLabel lblVentasValor;
    private javax.swing.JPanel pnlActividad;
    private javax.swing.JPanel pnlCreditos;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlProductos;
    private javax.swing.JPanel pnlProductosStock;
    private javax.swing.JPanel pnlReparaciones;
    private javax.swing.JPanel pnlStockBajo;
    private javax.swing.JPanel pnlVentasHoy;
    private javax.swing.JScrollPane scrollStock;
    private javax.swing.JTable tblStockBajo;
    // End of variables declaration//GEN-END:variables
}
