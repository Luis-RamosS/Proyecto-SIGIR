package sigir.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.Cursor;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.CompraDAO;
import sigir.dao.CategoriaDAO;
import sigir.dao.ProductoDAO;
import sigir.modelo.Categoria;
import sigir.modelo.Compra;
import sigir.modelo.DetalleCompra;
import sigir.modelo.Producto;
import sigir.modelo.Proveedor;
import sigir.util.Sesion;
import sigir.vista.paneles.ComprasPanel;

public class CompraControlador {

    private final ComprasPanel vista;
    private final CompraDAO compraDAO;
    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    private final List<DetalleCompra> detalles =
            new ArrayList<>();

    private List<Producto> productosDisponibles =
            new ArrayList<>();

    private List<Compra> compras =
            new ArrayList<>();

    private SwingWorker<DatosCarga, Void> trabajadorCarga;
    private SwingWorker<List<Compra>, Void> trabajadorBusqueda;

    private long ultimaCarga;
    private long versionBusqueda;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record DatosCarga(
            List<Proveedor> proveedores,
            List<Producto> productos,
            List<Compra> compras
    ) {
    }

    private record FiltrosHistorial(
            String texto,
            LocalDate desde,
            LocalDate hasta,
            String estado
    ) {
    }

    public CompraControlador(ComprasPanel vista) {
        this.vista = vista;
        this.compraDAO = new CompraDAO();
        this.productoDAO = new ProductoDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public void iniciarAsync() {
        nuevaCompra();
        cargarAsync();
    }

    public void recargarAsync() {
        cargarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void recargarSiNecesario() {
        long transcurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (transcurrido >= VIGENCIA_DATOS_MS) {
            cargarAsync();
        }
    }

    private void cargarAsync() {
        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            recargaPendiente = true;
            return;
        }

        final FiltrosHistorial filtros;

        try {
            filtros = capturarFiltrosHistorial();
        } catch (IllegalArgumentException ex) {
            mostrarAvisoFiltro(ex.getMessage());
            return;
        }

        recargaPendiente = false;

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

                List<Proveedor> proveedores =
                        compraDAO.listarProveedoresActivos();

                List<Producto> productos =
                        compraDAO.listarProductosDisponibles();

                List<Compra> comprasCargadas =
                        compraDAO.listarCompras(
                                filtros.texto(),
                                filtros.desde(),
                                filtros.hasta(),
                                filtros.estado()
                        );

                return new DatosCarga(
                        proveedores,
                        productos,
                        comprasCargadas
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    productosDisponibles =
                            new ArrayList<>(
                                    datos.productos()
                            );

                    compras =
                            new ArrayList<>(
                                    datos.compras()
                            );

                    vista.cargarProveedores(
                            datos.proveedores()
                    );

                    vista.mostrarCompras(compras);

                    vista.mostrarCantidadCompras(
                            compras.size()
                    );

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
                            + "el módulo de compras.",
                            causa
                    );

                } finally {
                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );

                    if (recargaPendiente) {
                        cargarAsync();
                    }
                }
            }
        };

        trabajadorCarga.execute();
    }

    private FiltrosHistorial capturarFiltrosHistorial() {
        LocalDate fechaDesde =
                vista.getFechaDesdeFiltro();

        LocalDate fechaHasta =
                vista.getFechaHastaFiltro();

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser "
                    + "posterior a la final."
            );
        }

        return new FiltrosHistorial(
                vista.getTextoBusquedaHistorial(),
                fechaDesde,
                fechaHasta,
                vista.getEstadoFiltro()
        );
    }

    public void nuevaCompra() {
        detalles.clear();
        vista.limpiarCompra();
        actualizarDetalle();
    }

    public void buscarProductoAvanzado() {
        if (productosDisponibles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    vista,
                    "No existen productos disponibles para seleccionar.",
                    "Catálogo vacío",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Producto seleccionado =
                vista.solicitarProductoAvanzado(
                        productosDisponibles
                );

        if (seleccionado == null) {
            return;
        }

        vista.establecerProductoSeleccionado(
                seleccionado
        );

        seleccionarProducto();
    }

    public void registrarProductoNuevo() {
        try {
            List<Categoria> categorias =
                    categoriaDAO.listarActivas();

            if (categorias.isEmpty()) {
                JOptionPane.showMessageDialog(
                        vista,
                        "No existen categorías activas para registrar el producto.",
                        "Categorías no disponibles",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Producto producto =
                    vista.solicitarNuevoProducto(categorias);

            if (producto == null) {
                return;
            }

            validarProductoNuevo(producto);

            if (productoDAO.existeCodigo(
                    producto.getCodigo(),
                    null
            )) {
                throw new IllegalArgumentException(
                        "Ya existe un producto con el código "
                        + producto.getCodigo() + "."
                );
            }

            /*
             * El producto se crea sin existencias. La cantidad real entra
             * al inventario únicamente cuando se guarda esta compra.
             */
            producto.setEstado("AGOTADO");
            producto.setStockActual(0);

            int idProducto =
                    productoDAO.insertar(producto);

            Producto guardado =
                    productoDAO.buscarPorId(idProducto);

            if (guardado == null) {
                throw new SQLException(
                        "El producto se registró, pero no pudo volver a cargarse."
                );
            }

            productosDisponibles =
                    new ArrayList<>(
                            compraDAO.listarProductosDisponibles()
                    );

            vista.establecerProductoSeleccionado(guardado);
            seleccionarProducto();

            JOptionPane.showMessageDialog(
                    vista,
                    "Producto registrado correctamente.\n\n"
                    + "Ahora indica la cantidad comprada y presiona Agregar.",
                    "Producto listo para la compra",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Producto no registrado",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible registrar el producto nuevo.",
                    ex
            );
        }
    }

    public void usarProductoRegistrado(
            Producto producto) {

        if (producto == null
                || producto.getIdProducto() <= 0) {

            return;
        }

        productosDisponibles.removeIf(
                existente ->
                        existente.getIdProducto()
                        == producto.getIdProducto()
        );

        productosDisponibles.add(producto);

        vista.establecerProductoSeleccionado(
                producto
        );

        seleccionarProducto();
    }

    public void seleccionarProducto() {
        Producto producto =
                vista.getProductoSeleccionado();

        if (producto == null
                || producto.getIdProducto() <= 0) {

            vista.setCostoProducto(BigDecimal.ZERO);
            vista.mostrarStockProducto(0);
            vista.mostrarAvisoSeries(false);
            return;
        }

        vista.setCostoProducto(
                producto.getPrecioCompra()
        );

        vista.mostrarStockProducto(
                producto.getStockActual()
        );

        vista.mostrarAvisoSeries(
                producto.isManejaNumeroSerie()
        );
    }

    public void agregarProducto() {
        try {
            Producto producto =
                    vista.getProductoSeleccionado();

            if (producto == null
                    || producto.getIdProducto() <= 0) {

                throw new IllegalArgumentException(
                        "Primero busca y selecciona un producto."
                );
            }

            boolean repetido =
                    detalles.stream().anyMatch(
                            detalle ->
                                    detalle.getIdProducto()
                                    == producto.getIdProducto()
                    );

            if (repetido) {
                throw new IllegalArgumentException(
                        "El producto ya fue agregado. "
                        + "Elimínalo y vuelve a agregarlo "
                        + "para cambiar la cantidad o el costo."
                );
            }

            int cantidad =
                    vista.getCantidadProducto();

            BigDecimal costo =
                    vista.getCostoProducto();

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            if (costo.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "El costo unitario no puede ser negativo."
                );
            }

            DetalleCompra detalle =
                    new DetalleCompra();

            detalle.setIdProducto(
                    producto.getIdProducto()
            );

            detalle.setCodigoProducto(
                    producto.getCodigo()
            );

            detalle.setNombreProducto(
                    producto.getNombre()
            );

            detalle.setManejaNumeroSerie(
                    producto.isManejaNumeroSerie()
            );

            detalle.setCantidad(cantidad);
            detalle.setCostoUnitario(costo);

            if (detalle.isManejaNumeroSerie()) {
                List<String> series =
                        vista.solicitarNumerosSerie(
                                producto,
                                cantidad
                        );

                if (series == null) {
                    return;
                }

                validarSeries(series, cantidad);
                detalle.setNumerosSerie(series);
            }

            detalles.add(detalle);

            actualizarDetalle();
            vista.limpiarProductoSeleccionado();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Producto no agregado",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public void eliminarProducto() {
        int fila =
                vista.getFilaDetalleSeleccionadaModelo();

        if (fila < 0 || fila >= detalles.size()) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un producto del detalle.",
                    "Producto no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        detalles.remove(fila);
        actualizarDetalle();
    }

    public void registrarCompra() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException(
                        "No existe una sesión activa."
                );
            }

            Proveedor proveedor =
                    vista.getProveedorSeleccionado();

            if (proveedor == null
                    || proveedor.getIdProveedor() <= 0) {

                throw new IllegalArgumentException(
                        "Selecciona un proveedor."
                );
            }

            if (detalles.isEmpty()) {
                throw new IllegalArgumentException(
                        "Agrega al menos un producto."
                );
            }

            Compra compra =
                    construirCompra(proveedor);

            validarCompra(compra);

            if (compraDAO.existeDocumentoProveedor(
                    compra.getIdProveedor(),
                    compra.getNumeroDocumento()
            )) {
                throw new IllegalArgumentException(
                        "Ese número de documento ya fue registrado "
                        + "para el proveedor seleccionado."
                );
            }

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "Se registrará la compra por "
                            + vista.formatearMoneda(
                                    compra.getTotal()
                            )
                            + ".\n"
                            + "El inventario se actualizará "
                            + "automáticamente.\n\n"
                            + "¿Deseas continuar?",
                            "Confirmar compra",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            vista.establecerProcesando(true);

            int idCompra =
                    compraDAO.registrar(compra);

            JOptionPane.showMessageDialog(
                    vista,
                    "Compra #" + idCompra
                    + " registrada correctamente.\n"
                    + "El inventario fue actualizado.",
                    "Compra registrada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            nuevaCompra();
            recargarAsync();
            vista.mostrarPestanaHistorial();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "No se puede registrar la compra",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible registrar la compra.",
                    ex
            );

        } finally {
            vista.establecerProcesando(false);
        }
    }

    public void buscarCompras() {
        final FiltrosHistorial filtros;

        try {
            filtros = capturarFiltrosHistorial();
        } catch (IllegalArgumentException ex) {
            mostrarAvisoFiltro(ex.getMessage());
            return;
        }

        long versionActual =
                ++versionBusqueda;

        if (trabajadorBusqueda != null
                && !trabajadorBusqueda.isDone()) {

            trabajadorBusqueda.cancel(true);
        }

        trabajadorBusqueda =
                new SwingWorker<>() {

            @Override
            protected List<Compra> doInBackground()
                    throws Exception {

                return compraDAO.listarCompras(
                        filtros.texto(),
                        filtros.desde(),
                        filtros.hasta(),
                        filtros.estado()
                );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionBusqueda) {

                    return;
                }

                try {
                    List<Compra> resultado = get();

                    compras =
                            new ArrayList<>(
                                    resultado
                            );

                    vista.mostrarCompras(compras);

                    vista.mostrarCantidadCompras(
                            compras.size()
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
                            + "las compras.",
                            causa
                    );
                }
            }
        };

        trabajadorBusqueda.execute();
    }

    public void verDetalleCompra() {
        int fila =
                vista.getFilaCompraSeleccionadaModelo();

        if (fila < 0 || fila >= compras.size()) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona una compra del historial.",
                    "Compra no seleccionada",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Compra compra =
                    compraDAO.obtenerCompraCompleta(
                            compras.get(fila).getIdCompra()
                    );

            if (compra == null) {
                throw new SQLException(
                        "La compra seleccionada ya no existe."
                );
            }

            vista.mostrarDetalleCompra(compra);

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar el detalle de la compra.",
                    ex
            );
        }
    }

    public void anularCompra() {
        int fila =
                vista.getFilaCompraSeleccionadaModelo();

        if (fila < 0 || fila >= compras.size()) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona una compra del historial.",
                    "Compra no seleccionada",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Compra compra = compras.get(fila);

        if (!"REGISTRADA".equalsIgnoreCase(
                compra.getEstado())) {

            JOptionPane.showMessageDialog(
                    vista,
                    "Solo se pueden anular compras registradas.",
                    "Compra no anulable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int respuesta =
                JOptionPane.showConfirmDialog(
                        vista,
                        "La compra #" + compra.getIdCompra()
                        + " será anulada y sus existencias "
                        + "se restarán del inventario.\n"
                        + "¿Deseas continuar?",
                        "Anular compra",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            compraDAO.anular(
                    compra.getIdCompra(),
                    Sesion.getIdUsuario()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "La compra fue anulada y el inventario "
                    + "se revirtió.",
                    "Compra anulada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            recargarAsync();

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible anular la compra.",
                    ex
            );
        }
    }

    private Compra construirCompra(
            Proveedor proveedor) {

        Compra compra = new Compra();

        compra.setIdProveedor(
                proveedor.getIdProveedor()
        );

        compra.setNombreProveedor(
                proveedor.getNombreProveedor()
        );

        compra.setIdUsuario(
                Sesion.getIdUsuario()
        );

        compra.setNombreUsuario(
                Sesion.getNombreCompleto()
        );

        compra.setNumeroDocumento(
                vista.getNumeroDocumento()
        );

        LocalDate fecha =
                vista.getFechaCompra();

        LocalTime hora =
                fecha.equals(LocalDate.now())
                        ? LocalTime.now().withNano(0)
                        : LocalTime.NOON;

        compra.setFechaCompra(
                LocalDateTime.of(fecha, hora)
        );

        compra.setTipoPago(
                vista.getTipoPago()
        );

        compra.setObservaciones(
                vista.getObservaciones()
        );

        compra.setDetalles(
                new ArrayList<>(detalles)
        );

        /*
         * El descuento fue eliminado de Compras.
         * Toda compra nueva se registra con descuento 0.
         */
        compra.setDescuento(BigDecimal.ZERO);

        compra.setEstado("REGISTRADA");
        compra.recalcularTotales();

        return compra;
    }

    private void validarProductoNuevo(Producto producto) {
        if (producto.getCodigo() == null
                || producto.getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresa el código del producto."
            );
        }

        if (producto.getCodigo().trim().length() > 30) {
            throw new IllegalArgumentException(
                    "El código no puede superar 30 caracteres."
            );
        }

        if (producto.getNombre() == null
                || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresa el nombre del producto."
            );
        }

        if (producto.getNombre().trim().length() > 120) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar 120 caracteres."
            );
        }

        if (producto.getIdCategoria() <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona una categoría."
            );
        }

        if (producto.getPrecioCompra() == null
                || producto.getPrecioCompra()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio de compra no puede ser negativo."
            );
        }

        if (producto.getPrecioVenta() == null
                || producto.getPrecioVenta()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio de venta no puede ser negativo."
            );
        }

        if (producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser negativo."
            );
        }

        if (producto.getDescripcion() != null
                && producto.getDescripcion().length() > 2000) {
            throw new IllegalArgumentException(
                    "La descripción es demasiado extensa."
            );
        }
    }

    private void validarCompra(Compra compra) {
        if (compra.getFechaCompra()
                .toLocalDate()
                .isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de compra no puede estar en el futuro."
            );
        }

        for (DetalleCompra detalle
                : compra.getDetalles()) {

            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "Todas las cantidades deben ser "
                        + "mayores que cero."
                );
            }

            if (detalle.getCostoUnitario()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Los costos unitarios no pueden ser negativos."
                );
            }

            if (detalle.isManejaNumeroSerie()
                    && detalle.getNumerosSerie().size()
                    != detalle.getCantidad()) {

                throw new IllegalArgumentException(
                        "Faltan números de serie para "
                        + detalle.getNombreProducto() + "."
                );
            }
        }
    }

    private void validarSeries(
            List<String> series,
            int cantidad) {

        if (series.size() != cantidad) {
            throw new IllegalArgumentException(
                    "Debes ingresar exactamente "
                    + cantidad
                    + " números de serie."
            );
        }

        Set<String> unicas = new HashSet<>();

        for (String serie : series) {
            String normalizada =
                    serie.trim().toUpperCase();

            if (normalizada.isBlank()
                    || !unicas.add(normalizada)) {

                throw new IllegalArgumentException(
                        "Los números de serie no pueden "
                        + "estar vacíos ni repetidos."
                );
            }
        }
    }

    private void actualizarDetalle() {
        vista.mostrarDetalles(detalles);
    }

    private void mostrarAvisoFiltro(
            String mensaje) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje,
                "Filtro de compras",
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
