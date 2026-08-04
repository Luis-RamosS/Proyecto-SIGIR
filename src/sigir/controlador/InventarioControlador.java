package sigir.controlador;

import java.awt.Cursor;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.InventarioDAO;
import sigir.modelo.Categoria;
import sigir.modelo.MovimientoInventario;
import sigir.modelo.Producto;
import sigir.modelo.ResumenInventario;
import sigir.util.Sesion;
import sigir.vista.paneles.InventarioPanel;

public class InventarioControlador {

    private final InventarioPanel vista;
    private final InventarioDAO inventarioDAO;

    private List<Producto> productos =
            new ArrayList<>();

    private List<MovimientoInventario> movimientos =
            new ArrayList<>();

    private SwingWorker<DatosCarga, Void> trabajadorCarga;

    private SwingWorker<List<Producto>, Void>
            trabajadorExistencias;

    private SwingWorker<List<MovimientoInventario>, Void>
            trabajadorMovimientos;

    private long ultimaCarga;
    private long versionExistencias;
    private long versionMovimientos;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record DatosCarga(
            ResumenInventario resumen,
            List<Categoria> categorias,
            List<Producto> productosAjustables,
            List<Producto> existencias,
            List<MovimientoInventario> movimientos
    ) {
    }

    private record FiltroExistencias(
            String texto,
            Integer idCategoria,
            String nivelStock
    ) {
    }

    private record FiltroMovimientos(
            String texto,
            String tipoMovimiento,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
    }

    public InventarioControlador(
            InventarioPanel vista) {

        this.vista = vista;
        this.inventarioDAO =
                new InventarioDAO();
    }

    public void iniciarAsync() {
        vista.configurarPermisoAjustes(
                Sesion.haySesionActiva()
                && Sesion.esDueno()
        );

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
        long tiempoTranscurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (tiempoTranscurrido
                >= VIGENCIA_DATOS_MS) {

            cargarTodoAsync();
        }
    }

    public void actualizarTodo() {
        recargarAsync();
    }

    public void cargarResumen() {
        recargarAsync();
    }

    public void cargarCategorias() {
        recargarAsync();
    }

    public void cargarProductosAjustables() {
        recargarAsync();
    }

    private void cargarTodoAsync() {
        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            recargaPendiente = true;
            return;
        }

        final FiltroExistencias filtroExistencias;

        final FiltroMovimientos filtroMovimientos;

        try {
            filtroExistencias =
                    capturarFiltroExistencias();

            filtroMovimientos =
                    capturarFiltroMovimientos();

        } catch (IllegalArgumentException ex) {
            mostrarAvisoFechas(
                    ex.getMessage()
            );
            return;
        }

        recargaPendiente = false;

        if (trabajadorExistencias != null
                && !trabajadorExistencias.isDone()) {

            trabajadorExistencias.cancel(true);
        }

        if (trabajadorMovimientos != null
                && !trabajadorMovimientos.isDone()) {

            trabajadorMovimientos.cancel(true);
        }

        final long versionExistenciasCarga =
                ++versionExistencias;

        final long versionMovimientosCarga =
                ++versionMovimientos;

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

                ResumenInventario resumen =
                        inventarioDAO.obtenerResumen();

                List<Categoria> categorias =
                        inventarioDAO
                                .listarCategoriasActivas();

                List<Producto> ajustables =
                        inventarioDAO
                                .listarProductosAjustables();

                List<Producto> existencias =
                        inventarioDAO.listarExistencias(
                                filtroExistencias.texto(),
                                filtroExistencias.idCategoria(),
                                filtroExistencias.nivelStock()
                        );

                List<MovimientoInventario>
                        movimientosCargados =
                        inventarioDAO.listarMovimientos(
                                filtroMovimientos.texto(),
                                filtroMovimientos
                                        .tipoMovimiento(),
                                filtroMovimientos.fechaDesde(),
                                filtroMovimientos.fechaHasta()
                        );

                return new DatosCarga(
                        resumen,
                        categorias,
                        ajustables,
                        existencias,
                        movimientosCargados
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    vista.mostrarResumen(
                            datos.resumen()
                    );

                    vista.cargarCategorias(
                            datos.categorias()
                    );

                    vista.cargarProductosAjustables(
                            datos.productosAjustables()
                    );

                    if (versionExistenciasCarga
                            == versionExistencias) {

                        productos =
                                new ArrayList<>(
                                        datos.existencias()
                                );

                        vista.mostrarExistencias(
                                productos
                        );

                        vista.mostrarCantidadProductos(
                                productos.size()
                        );
                    }

                    if (versionMovimientosCarga
                            == versionMovimientos) {

                        movimientos =
                                new ArrayList<>(
                                        datos.movimientos()
                                );

                        vista.mostrarMovimientos(
                                movimientos
                        );

                        vista.mostrarCantidadMovimientos(
                                movimientos.size()
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

                    mostrarErrorBaseDatos(
                            "No fue posible cargar "
                            + "el módulo de inventario.",
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

    public void buscarExistencias() {
        final FiltroExistencias filtro =
                capturarFiltroExistencias();

        final long versionActual =
                ++versionExistencias;

        if (trabajadorExistencias != null
                && !trabajadorExistencias.isDone()) {

            trabajadorExistencias.cancel(true);
        }

        trabajadorExistencias =
                new SwingWorker<>() {

            @Override
            protected List<Producto>
                    doInBackground()
                    throws Exception {

                return inventarioDAO
                        .listarExistencias(
                                filtro.texto(),
                                filtro.idCategoria(),
                                filtro.nivelStock()
                        );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionExistencias) {

                    return;
                }

                try {
                    List<Producto> resultado =
                            get();

                    productos =
                            new ArrayList<>(
                                    resultado
                            );

                    vista.mostrarExistencias(
                            productos
                    );

                    vista.mostrarCantidadProductos(
                            productos.size()
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

                    mostrarErrorBaseDatos(
                            "No fue posible consultar "
                            + "las existencias.",
                            causa
                    );
                }
            }
        };

        trabajadorExistencias.execute();
    }

    public void buscarMovimientos() {
        final FiltroMovimientos filtro;

        try {
            filtro =
                    capturarFiltroMovimientos();

        } catch (IllegalArgumentException ex) {
            mostrarAvisoFechas(
                    ex.getMessage()
            );
            return;
        }

        final long versionActual =
                ++versionMovimientos;

        if (trabajadorMovimientos != null
                && !trabajadorMovimientos.isDone()) {

            trabajadorMovimientos.cancel(true);
        }

        trabajadorMovimientos =
                new SwingWorker<>() {

            @Override
            protected List<MovimientoInventario>
                    doInBackground()
                    throws Exception {

                return inventarioDAO
                        .listarMovimientos(
                                filtro.texto(),
                                filtro.tipoMovimiento(),
                                filtro.fechaDesde(),
                                filtro.fechaHasta()
                        );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionMovimientos) {

                    return;
                }

                try {
                    List<MovimientoInventario>
                            resultado = get();

                    movimientos =
                            new ArrayList<>(
                                    resultado
                            );

                    vista.mostrarMovimientos(
                            movimientos
                    );

                    vista.mostrarCantidadMovimientos(
                            movimientos.size()
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

                    mostrarErrorBaseDatos(
                            "No fue posible consultar "
                            + "los movimientos.",
                            causa
                    );
                }
            }
        };

        trabajadorMovimientos.execute();
    }

    private FiltroExistencias
            capturarFiltroExistencias() {

        Categoria categoria =
                vista.getCategoriaFiltro();

        Integer idCategoria =
                categoria == null
                || categoria.getIdCategoria() <= 0
                        ? null
                        : categoria.getIdCategoria();

        return new FiltroExistencias(
                vista.getTextoBusquedaExistencias(),
                idCategoria,
                vista.getNivelStockFiltro()
        );
    }

    private FiltroMovimientos
            capturarFiltroMovimientos() {

        LocalDate fechaDesde =
                vista.getFechaDesdeFiltro();

        LocalDate fechaHasta =
                vista.getFechaHastaFiltro();

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser "
                    + "posterior a la fecha final."
            );
        }

        return new FiltroMovimientos(
                vista.getTextoBusquedaMovimientos(),
                vista.getTipoMovimientoFiltro(),
                fechaDesde,
                fechaHasta
        );
    }

    public void seleccionarProductoAjuste() {
        Producto producto =
                vista.getProductoAjusteSeleccionado();

        if (producto == null
                || producto.getIdProducto() <= 0) {

            vista.mostrarDatosProductoAjuste(
                    0,
                    false
            );
            return;
        }

        vista.mostrarDatosProductoAjuste(
                producto.getStockActual(),
                producto.isManejaNumeroSerie()
        );
    }

    public void registrarAjuste() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException(
                        "No existe una sesión activa."
                );
            }

            if (!Sesion.esDueno()) {
                throw new IllegalStateException(
                        "Solo el dueño puede realizar "
                        + "ajustes manuales."
                );
            }

            Producto producto =
                    vista.getProductoAjusteSeleccionado();

            if (producto == null
                    || producto.getIdProducto() <= 0) {

                throw new IllegalArgumentException(
                        "Selecciona un producto."
                );
            }

            if (producto.isManejaNumeroSerie()) {
                throw new IllegalArgumentException(
                        "Este producto maneja números de serie. "
                        + "Su stock debe cambiarse mediante compras, "
                        + "ventas o reparaciones."
                );
            }

            String tipo =
                    vista.getTipoAjuste();

            int cantidad =
                    vista.getCantidadAjuste();

            String motivo =
                    vista.getMotivoAjuste();

            if (!"AJUSTE_ENTRADA".equals(tipo)
                    && !"AJUSTE_SALIDA".equals(tipo)) {

                throw new IllegalArgumentException(
                        "Selecciona un tipo de ajuste válido."
                );
            }

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            if (motivo == null
                    || motivo.isBlank()
                    || motivo.trim().length() < 5) {

                throw new IllegalArgumentException(
                        "Escribe un motivo de al menos "
                        + "5 caracteres."
                );
            }

            String accion =
                    tipo.equals("AJUSTE_ENTRADA")
                            ? "aumentará"
                            : "disminuirá";

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "El stock de "
                            + producto.getNombre()
                            + " " + accion + " en "
                            + cantidad + " unidades.\n"
                            + "¿Deseas continuar?",
                            "Confirmar ajuste",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

            if (respuesta
                    != JOptionPane.YES_OPTION) {

                return;
            }

            inventarioDAO.registrarAjuste(
                    producto.getIdProducto(),
                    Sesion.getIdUsuario(),
                    tipo,
                    cantidad,
                    motivo.trim()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "El ajuste se registró correctamente.",
                    "Inventario actualizado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            vista.limpiarAjuste();
            vista.mostrarPestanaMovimientos();
            recargarAsync();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Ajuste no realizado",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible registrar el ajuste.",
                    ex
            );
        }
    }

    private void mostrarAvisoFechas(
            String mensaje) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje,
                "Fechas no válidas",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarErrorBaseDatos(
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
