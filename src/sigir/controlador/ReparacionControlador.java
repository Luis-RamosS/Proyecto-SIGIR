package sigir.controlador;

import java.awt.Cursor;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.ReparacionDAO;
import sigir.modelo.Cliente;
import sigir.modelo.EquipoCliente;
import sigir.modelo.HistorialServicio;
import sigir.modelo.OrdenServicio;
import sigir.modelo.Producto;
import sigir.modelo.RepuestoOrden;
import sigir.util.Sesion;
import sigir.vista.paneles.ReparacionesPanel;

public class ReparacionControlador {

    private final ReparacionesPanel vista;
    private final ReparacionDAO dao =
            new ReparacionDAO();

    private List<Cliente> clientes =
            new ArrayList<>();

    private List<Producto> productos =
            new ArrayList<>();

    private List<OrdenServicio> ordenes =
            new ArrayList<>();

    private OrdenServicio ordenActual;

    private SwingWorker<DatosCarga, Void>
            trabajadorCarga;

    private SwingWorker<List<OrdenServicio>, Void>
            trabajadorOrdenes;

    private SwingWorker<List<HistorialServicio>, Void>
            trabajadorHistorial;

    private SwingWorker<List<EquipoCliente>, Void>
            trabajadorEquipos;

    private SwingWorker<OrdenServicio, Void>
            trabajadorDetalle;

    private long ultimaCarga;
    private long versionOrdenes;
    private long versionHistorial;
    private long versionEquipos;
    private long versionDetalle;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record Indicadores(
            int recibidos,
            int enReparacion,
            int listos
    ) {
    }

    private record DatosCarga(
            List<Cliente> clientes,
            List<Producto> productos,
            List<OrdenServicio> ordenes,
            List<HistorialServicio> historial,
            Indicadores indicadores,
            OrdenServicio ordenActual
    ) {
    }

    private record FiltroOrdenes(
            String texto,
            String estado
    ) {
    }

    public ReparacionControlador(
            ReparacionesPanel vista) {

        this.vista = vista;
    }

    public void iniciarAsync() {
        nuevaOrden();
        cargarTodoAsync();
    }

    public void recargarAsync() {
        cargarTodoAsync();
    }

    public void iniciar() {
        iniciarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void recargarSiNecesario() {
        long transcurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (transcurrido
                >= VIGENCIA_DATOS_MS) {

            cargarTodoAsync();
        }
    }

    private void cargarTodoAsync() {
        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            recargaPendiente = true;
            return;
        }

        final FiltroOrdenes filtroOrdenes =
                capturarFiltroOrdenes();

        final String filtroHistorial =
                vista.getTextoBusquedaHistorial();

        final Integer idOrdenActual =
                ordenActual == null
                        ? null
                        : ordenActual.getIdOrden();

        recargaPendiente = false;

        cancelarBusquedasSecundarias();

        final long versionOrdenesCarga =
                ++versionOrdenes;

        final long versionHistorialCarga =
                ++versionHistorial;

        final long versionDetalleCarga =
                ++versionDetalle;

        vista.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR
                )
        );

        trabajadorCarga =
                new SwingWorker<>() {

            @Override
            protected DatosCarga doInBackground()
                    throws Exception {

                List<Cliente> clientesCargados =
                        dao.listarClientesActivos();

                List<Producto> productosCargados =
                        dao.listarRepuestosDisponibles();

                List<OrdenServicio> ordenesCargadas =
                        dao.listarOrdenes(
                                filtroOrdenes.texto(),
                                filtroOrdenes.estado()
                        );

                List<HistorialServicio> historial =
                        dao.listarHistorialGeneral(
                                filtroHistorial
                        );

                int[] conteos =
                        dao.contarIndicadores();

                OrdenServicio ordenCompleta =
                        idOrdenActual == null
                                ? null
                                : dao.obtenerOrdenCompleta(
                                        idOrdenActual
                                );

                return new DatosCarga(
                        clientesCargados,
                        productosCargados,
                        ordenesCargadas,
                        historial,
                        new Indicadores(
                                conteos[0],
                                conteos[1],
                                conteos[2]
                        ),
                        ordenCompleta
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    clientes =
                            new ArrayList<>(
                                    datos.clientes()
                            );

                    productos =
                            new ArrayList<>(
                                    datos.productos()
                            );

                    vista.cargarClientes(clientes);
                    vista.cargarProductos(productos);

                    if (versionOrdenesCarga
                            == versionOrdenes) {

                        ordenes =
                                new ArrayList<>(
                                        datos.ordenes()
                                );

                        vista.mostrarOrdenes(
                                ordenes
                        );
                    }

                    if (versionHistorialCarga
                            == versionHistorial) {

                        vista.mostrarHistorialGeneral(
                                datos.historial()
                        );
                    }

                    vista.actualizarIndicadores(
                            datos.indicadores()
                                    .recibidos(),
                            datos.indicadores()
                                    .enReparacion(),
                            datos.indicadores()
                                    .listos()
                    );

                    if (versionDetalleCarga
                            == versionDetalle
                            && datos.ordenActual() != null) {

                        ordenActual =
                                datos.ordenActual();

                        vista.mostrarOrdenActual(
                                ordenActual
                        );
                    }

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La carga fue cancelada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible cargar "
                            + "el módulo de reparaciones.",
                            causa
                    );

                } finally {
                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );

                    if (recargaPendiente) {
                        cargarTodoAsync();
                    }
                }
            }
        };

        trabajadorCarga.execute();
    }

    private void cancelarBusquedasSecundarias() {
        if (trabajadorOrdenes != null
                && !trabajadorOrdenes.isDone()) {

            trabajadorOrdenes.cancel(true);
        }

        if (trabajadorHistorial != null
                && !trabajadorHistorial.isDone()) {

            trabajadorHistorial.cancel(true);
        }

        if (trabajadorDetalle != null
                && !trabajadorDetalle.isDone()) {

            trabajadorDetalle.cancel(true);
        }
    }

    public void nuevaOrden() {
        ordenActual = null;
        ++versionDetalle;

        vista.limpiarNuevaOrden();

        vista.setNumeroOrden(
                generarNumeroOrden()
        );
    }

    public void seleccionarClienteNuevaOrden(
            Cliente cliente) {

        final long versionActual =
                ++versionEquipos;

        if (trabajadorEquipos != null
                && !trabajadorEquipos.isDone()) {

            trabajadorEquipos.cancel(true);
        }

        if (cliente == null
                || cliente.getIdCliente() <= 0) {

            vista.cargarEquiposCliente(
                    List.of()
            );
            return;
        }

        final int idCliente =
                cliente.getIdCliente();

        trabajadorEquipos =
                new SwingWorker<>() {

            @Override
            protected List<EquipoCliente>
                    doInBackground()
                    throws Exception {

                return dao.listarEquiposCliente(
                        idCliente
                );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionEquipos) {

                    return;
                }

                try {
                    vista.cargarEquiposCliente(
                            get()
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // Se eligió otro cliente.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible cargar "
                            + "los equipos del cliente.",
                            causa
                    );
                }
            }
        };

        trabajadorEquipos.execute();
    }

    public void seleccionarEquipoExistente() {
        vista.mostrarEquipoExistente(
                vista.getEquipoExistenteSeleccionado()
        );
    }

    public void registrarOrden() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException(
                        "No existe una sesión activa."
                );
            }

            Cliente cliente =
                    vista.getClienteNuevaOrden();

            if (cliente == null) {
                throw new IllegalArgumentException(
                        "Busca y selecciona un cliente."
                );
            }

            EquipoCliente existente =
                    vista.getEquipoExistenteSeleccionado();

            boolean nuevo =
                    existente == null;

            EquipoCliente equipo =
                    nuevo
                            ? vista.construirEquipoNuevo()
                            : existente;

            validarEquipo(equipo);

            equipo.setIdCliente(
                    cliente.getIdCliente()
            );

            OrdenServicio orden =
                    vista.construirNuevaOrden();

            orden.setIdUsuarioRecibe(
                    Sesion.getIdUsuario()
            );

            orden.setNombreUsuarioRecibe(
                    Sesion.getNombreCompleto()
            );

            orden.setFechaRecepcion(
                    LocalDateTime.now()
            );

            orden.setEstado("RECIBIDO");

            orden.setIdCliente(
                    cliente.getIdCliente()
            );

            validarNuevaOrden(orden);

            if (dao.existeNumeroOrden(
                    orden.getNumeroOrden()
            )) {

                orden.setNumeroOrden(
                        generarNumeroOrden()
                );

                vista.setNumeroOrden(
                        orden.getNumeroOrden()
                );
            }

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "Se registrará la orden "
                            + orden.getNumeroOrden()
                            + " para "
                            + cliente.getNombreCompleto()
                            + ".\n\n¿Deseas continuar?",
                            "Confirmar orden",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

            if (respuesta
                    != JOptionPane.YES_OPTION) {

                return;
            }

            vista.establecerProcesando(true);

            int id =
                    dao.registrarOrden(
                            orden,
                            equipo,
                            nuevo
                    );

            JOptionPane.showMessageDialog(
                    vista,
                    "Orden registrada correctamente.\n"
                    + "Número: "
                    + orden.getNumeroOrden()
                    + "\nID interno: "
                    + id,
                    "Orden creada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            nuevaOrden();
            vista.mostrarPestanaSeguimiento();
            recargarAsync();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Orden no registrada",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible registrar "
                    + "la orden.",
                    ex
            );

        } finally {
            vista.establecerProcesando(false);
        }
    }

    public void buscarOrdenes() {
        final FiltroOrdenes filtro =
                capturarFiltroOrdenes();

        final long versionActual =
                ++versionOrdenes;

        if (trabajadorOrdenes != null
                && !trabajadorOrdenes.isDone()) {

            trabajadorOrdenes.cancel(true);
        }

        trabajadorOrdenes =
                new SwingWorker<>() {

            @Override
            protected List<OrdenServicio>
                    doInBackground()
                    throws Exception {

                return dao.listarOrdenes(
                        filtro.texto(),
                        filtro.estado()
                );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionOrdenes) {

                    return;
                }

                try {
                    List<OrdenServicio> resultado =
                            get();

                    ordenes =
                            new ArrayList<>(
                                    resultado
                            );

                    vista.mostrarOrdenes(
                            ordenes
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La búsqueda fue reemplazada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible consultar "
                            + "las órdenes.",
                            causa
                    );
                }
            }
        };

        trabajadorOrdenes.execute();
    }

    public void cargarOrdenSeleccionada() {
        int fila =
                vista.getFilaOrdenSeleccionadaModelo();

        if (fila < 0
                || fila >= ordenes.size()) {

            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona una orden de la tabla.",
                    "Orden no seleccionada",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        cargarOrdenPorId(
                ordenes.get(fila).getIdOrden()
        );
    }

    private void cargarOrdenPorId(int id) {
        final long versionActual =
                ++versionDetalle;

        if (trabajadorDetalle != null
                && !trabajadorDetalle.isDone()) {

            trabajadorDetalle.cancel(true);
        }

        trabajadorDetalle =
                new SwingWorker<>() {

            @Override
            protected OrdenServicio doInBackground()
                    throws Exception {

                OrdenServicio completa =
                        dao.obtenerOrdenCompleta(id);

                if (completa == null) {
                    throw new SQLException(
                            "La orden seleccionada "
                            + "ya no existe."
                    );
                }

                return completa;
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionDetalle) {

                    return;
                }

                try {
                    ordenActual = get();

                    vista.mostrarOrdenActual(
                            ordenActual
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // Se cargó otra orden.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible cargar "
                            + "la orden.",
                            causa
                    );
                }
            }
        };

        trabajadorDetalle.execute();
    }

    public void guardarSeguimiento() {
        try {
            validarOrdenActual();

            OrdenServicio cambios =
                    vista.construirCambiosOrden(
                            ordenActual
                    );

            validarCambios(cambios);

            dao.actualizarOrden(
                    cambios,
                    Sesion.getIdUsuario(),
                    vista.getDescripcionCambio()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "La orden fue actualizada "
                    + "correctamente.",
                    "Cambios guardados",
                    JOptionPane.INFORMATION_MESSAGE
            );

            ordenActual = cambios;
            vista.limpiarDescripcionCambio();
            recargarAsync();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Cambios no guardados",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible actualizar "
                    + "la orden.",
                    ex
            );
        }
    }

    public void seleccionarProductoRepuesto(
            Producto producto) {

        vista.mostrarProductoRepuesto(
                producto
        );
    }

    public void agregarRepuesto() {
        try {
            validarOrdenActual();

            if ("ENTREGADO".equals(
                    ordenActual.getEstado()
            )
                    || "CANCELADO".equals(
                            ordenActual.getEstado()
                    )) {

                throw new IllegalArgumentException(
                        "No puedes agregar repuestos "
                        + "a una orden finalizada."
                );
            }

            Producto producto =
                    vista.getProductoRepuestoSeleccionado();

            if (producto == null) {
                throw new IllegalArgumentException(
                        "Busca y selecciona un repuesto."
                );
            }

            int cantidad =
                    vista.getCantidadRepuesto();

            BigDecimal precio =
                    vista.getPrecioRepuesto();

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser "
                        + "mayor que cero."
                );
            }

            if (cantidad
                    > producto.getStockActual()) {

                throw new IllegalArgumentException(
                        "Stock insuficiente. Disponible: "
                        + producto.getStockActual()
                        + "."
                );
            }

            if (precio.signum() < 0) {
                throw new IllegalArgumentException(
                        "El precio no puede ser negativo."
                );
            }

            RepuestoOrden repuesto =
                    new RepuestoOrden();

            repuesto.setIdOrden(
                    ordenActual.getIdOrden()
            );

            repuesto.setIdProducto(
                    producto.getIdProducto()
            );

            repuesto.setCodigoProducto(
                    producto.getCodigo()
            );

            repuesto.setNombreProducto(
                    producto.getNombre()
            );

            repuesto.setCantidad(cantidad);
            repuesto.setPrecioUnitario(precio);

            dao.agregarRepuesto(
                    ordenActual.getIdOrden(),
                    Sesion.getIdUsuario(),
                    repuesto
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "Repuesto asignado y descontado "
                    + "del inventario.",
                    "Repuesto agregado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            vista.limpiarRepuesto();
            recargarAsync();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Repuesto no agregado",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible agregar "
                    + "el repuesto.",
                    ex
            );
        }
    }

    public void cancelarOrden() {
        try {
            validarOrdenActual();

            if ("ENTREGADO".equals(
                    ordenActual.getEstado()
            )) {

                throw new IllegalArgumentException(
                        "Una orden entregada "
                        + "no puede cancelarse."
                );
            }

            String motivo =
                    JOptionPane.showInputDialog(
                            vista,
                            "Escribe el motivo "
                            + "de la cancelación:",
                            "Cancelar orden",
                            JOptionPane.WARNING_MESSAGE
                    );

            if (motivo == null) {
                return;
            }

            if (motivo.trim().length() < 5) {
                throw new IllegalArgumentException(
                        "El motivo debe contener "
                        + "al menos 5 caracteres."
                );
            }

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "La orden "
                            + ordenActual.getNumeroOrden()
                            + " será cancelada.\n"
                            + "Los repuestos asignados "
                            + "volverán al inventario."
                            + "\n\n¿Deseas continuar?",
                            "Confirmar cancelación",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

            if (respuesta
                    != JOptionPane.YES_OPTION) {

                return;
            }

            dao.cancelarOrden(
                    ordenActual.getIdOrden(),
                    Sesion.getIdUsuario(),
                    motivo.trim()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "La orden fue cancelada "
                    + "correctamente.",
                    "Orden cancelada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            ordenActual = null;
            ++versionDetalle;

            vista.limpiarOrdenActual();
            recargarAsync();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Orden no cancelada",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible cancelar "
                    + "la orden.",
                    ex
            );
        }
    }

    public void buscarHistorial() {
        final String filtro =
                vista.getTextoBusquedaHistorial();

        final long versionActual =
                ++versionHistorial;

        if (trabajadorHistorial != null
                && !trabajadorHistorial.isDone()) {

            trabajadorHistorial.cancel(true);
        }

        trabajadorHistorial =
                new SwingWorker<>() {

            @Override
            protected List<HistorialServicio>
                    doInBackground()
                    throws Exception {

                return dao.listarHistorialGeneral(
                        filtro
                );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionHistorial) {

                    return;
                }

                try {
                    vista.mostrarHistorialGeneral(
                            get()
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La búsqueda fue reemplazada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible consultar "
                            + "el historial.",
                            causa
                    );
                }
            }
        };

        trabajadorHistorial.execute();
    }

    private FiltroOrdenes capturarFiltroOrdenes() {
        return new FiltroOrdenes(
                vista.getTextoBusquedaOrden(),
                vista.getEstadoOrdenFiltro()
        );
    }

    private void validarEquipo(
            EquipoCliente equipo) {

        if (equipo == null) {
            throw new IllegalArgumentException(
                    "Selecciona o registra un equipo."
            );
        }

        if (equipo.getTipoEquipo() == null
                || equipo.getTipoEquipo()
                        .trim().length() < 3) {

            throw new IllegalArgumentException(
                    "Escribe el tipo de equipo."
            );
        }
    }

    private void validarNuevaOrden(
            OrdenServicio orden) {

        if (orden.getProblemaReportado() == null
                || orden.getProblemaReportado()
                        .trim().length() < 5) {

            throw new IllegalArgumentException(
                    "Describe el problema reportado "
                    + "con al menos 5 caracteres."
            );
        }

        if (orden.getCostoEstimado()
                .signum() < 0) {

            throw new IllegalArgumentException(
                    "El costo estimado "
                    + "no puede ser negativo."
            );
        }

        if (orden.getFechaPrometida() != null
                && orden.getFechaPrometida()
                        .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha prometida "
                    + "no puede estar en el pasado."
            );
        }
    }

    private void validarOrdenActual() {
        if (ordenActual == null) {
            throw new IllegalStateException(
                    "Primero carga una orden "
                    + "del listado."
            );
        }

        if (!Sesion.haySesionActiva()) {
            throw new IllegalStateException(
                    "No existe una sesión activa."
            );
        }
    }

    private void validarCambios(
            OrdenServicio orden) {

        if (orden.getCostoEstimado().signum() < 0
                || orden.getCostoFinal().signum() < 0) {

            throw new IllegalArgumentException(
                    "Los costos no pueden ser negativos."
            );
        }

        boolean finalizando =
                "LISTO".equals(orden.getEstado())
                || "ENTREGADO".equals(
                        orden.getEstado()
                );

        if (finalizando
                && (orden.getDiagnostico() == null
                || orden.getDiagnostico()
                        .trim().length() < 5)) {

            throw new IllegalArgumentException(
                    "Antes de marcar la orden como "
                    + orden.getEstado()
                    + ", registra el diagnóstico."
            );
        }

        if (finalizando
                && (orden.getTrabajoRealizado() == null
                || orden.getTrabajoRealizado()
                        .trim().length() < 5)) {

            throw new IllegalArgumentException(
                    "Antes de marcar la orden como "
                    + orden.getEstado()
                    + ", registra el trabajo realizado."
            );
        }
    }

    private String generarNumeroOrden() {
        return "SRV-"
                + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd-HHmmss"
                        )
                )
                + "-"
                + ThreadLocalRandom.current()
                        .nextInt(100, 1000);
    }

    private void mostrarError(
            String mensaje,
            Throwable ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje
                + "\n\nDetalle: "
                + (
                    ex == null
                            ? "Error desconocido"
                            : ex.getMessage()
                ),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
