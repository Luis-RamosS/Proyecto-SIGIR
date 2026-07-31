package sigir.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import sigir.dao.CompraDAO;
import sigir.modelo.Compra;
import sigir.modelo.DetalleCompra;
import sigir.modelo.Producto;
import sigir.modelo.Proveedor;
import sigir.util.Sesion;
import sigir.vista.paneles.ComprasPanel;

public class CompraControlador {

    private final ComprasPanel vista;
    private final CompraDAO compraDAO;
    private final List<DetalleCompra> detalles = new ArrayList<>();
    private List<Compra> compras = new ArrayList<>();

    public CompraControlador(ComprasPanel vista) {
        this.vista = vista;
        this.compraDAO = new CompraDAO();
    }

    public void iniciar() {
        cargarCombos();
        nuevaCompra();
        buscarCompras();
    }

    public void recargar() {
        cargarCombos();
        buscarCompras();
    }

    private void cargarCombos() {
        try {
            vista.cargarProveedores(compraDAO.listarProveedoresActivos());
            vista.cargarProductos(compraDAO.listarProductosDisponibles());
        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar proveedores o productos.",
                    ex
            );
        }
    }

    public void actualizarDescuentoDetalle(
            int fila,
            String textoDescuento) {

        try {
            if (fila < 0 || fila >= detalles.size()) {
                return;
            }

            String valor = textoDescuento == null
                    ? ""
                    : textoDescuento
                            .trim()
                            .replace("L", "")
                            .replace(",", "");

            BigDecimal descuento = valor.isBlank()
                    ? BigDecimal.ZERO
                    : new BigDecimal(valor);

            descuento = descuento.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

            DetalleCompra detalle
                    = detalles.get(fila);

            if (descuento.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                throw new IllegalArgumentException(
                        "El descuento no puede ser negativo."
                );
            }

            if (descuento.compareTo(
                    detalle.getSubtotal()
            ) > 0) {

                throw new IllegalArgumentException(
                        "El descuento no puede ser mayor "
                        + "que el subtotal del producto."
                );
            }

            detalle.setDescuentoLinea(descuento);

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    "Escribe un descuento válido. "
                    + "Ejemplo: 20.00",
                    "Descuento incorrecto",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Descuento incorrecto",
                    JOptionPane.WARNING_MESSAGE
            );

        } finally {
            actualizarCarrito();
        }
    }
    
    public void nuevaCompra() {
        detalles.clear();
        vista.limpiarCompra();
        actualizarCarrito();
    }

    public void seleccionarProducto() {
        Producto producto = vista.getProductoSeleccionado();

        if (producto == null || producto.getIdProducto() <= 0) {
            vista.setCostoProducto(BigDecimal.ZERO);
            vista.mostrarStockProducto(0);
            vista.mostrarAvisoSeries(false);
            return;
        }

        vista.setCostoProducto(producto.getPrecioCompra());
        vista.mostrarStockProducto(producto.getStockActual());
        vista.mostrarAvisoSeries(producto.isManejaNumeroSerie());
    }

    public void agregarProducto() {
        try {
            Producto producto = vista.getProductoSeleccionado();

            if (producto == null || producto.getIdProducto() <= 0) {
                throw new IllegalArgumentException("Selecciona un producto.");
            }

            boolean repetido = detalles.stream().anyMatch(
                    detalle -> detalle.getIdProducto() == producto.getIdProducto()
            );

            if (repetido) {
                throw new IllegalArgumentException(
                        "El producto ya fue agregado. Elimínalo y vuelve "
                        + "a agregarlo para cambiar la cantidad."
                );
            }

            int cantidad = vista.getCantidadProducto();
            BigDecimal costo = vista.getCostoProducto();

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

            DetalleCompra detalle = new DetalleCompra();
            detalle.setIdProducto(producto.getIdProducto());
            detalle.setCodigoProducto(producto.getCodigo());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setManejaNumeroSerie(producto.isManejaNumeroSerie());
            detalle.setCantidad(cantidad);
            detalle.setCostoUnitario(costo);

            if (detalle.isManejaNumeroSerie()) {
                List<String> series = vista.solicitarNumerosSerie(
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
            actualizarCarrito();
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
        int fila = vista.getFilaDetalleSeleccionadaModelo();

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
        actualizarCarrito();
    }

    public void registrarCompra() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException("No existe una sesión activa.");
            }

            Proveedor proveedor = vista.getProveedorSeleccionado();

            if (proveedor == null || proveedor.getIdProveedor() <= 0) {
                throw new IllegalArgumentException("Selecciona un proveedor.");
            }

            if (detalles.isEmpty()) {
                throw new IllegalArgumentException(
                        "Agrega al menos un producto."
                );
            }

            Compra compra = construirCompra(proveedor);
            validarCompra(compra);

            if (compraDAO.existeDocumentoProveedor(
                    compra.getIdProveedor(),
                    compra.getNumeroDocumento()
            )) {
                throw new IllegalArgumentException(
                        "Ese número de documento ya fue registrado para "
                        + "el proveedor seleccionado."
                );
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    vista,
                    "Se registrará la compra por "
                    + vista.formatearMoneda(compra.getTotal())
                    + " y se actualizará el inventario.\n"
                    + "¿Deseas continuar?",
                    "Confirmar compra",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            vista.establecerProcesando(true);
            int idCompra = compraDAO.registrar(compra);

            JOptionPane.showMessageDialog(
                    vista,
                    "Compra #" + idCompra
                    + " registrada correctamente.\n"
                    + "El inventario fue actualizado.",
                    "Compra registrada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            nuevaCompra();
            buscarCompras();
            vista.mostrarPestanaHistorial();

        } catch (IllegalArgumentException | IllegalStateException ex) {
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
        try {
            LocalDate fechaDesde = vista.getFechaDesdeFiltro();
            LocalDate fechaHasta = vista.getFechaHastaFiltro();

            if (fechaDesde != null
                    && fechaHasta != null
                    && fechaDesde.isAfter(fechaHasta)) {
                throw new IllegalArgumentException(
                        "La fecha inicial no puede ser posterior a la final."
                );
            }

            compras = compraDAO.listarCompras(
                    vista.getTextoBusquedaHistorial(),
                    fechaDesde,
                    fechaHasta,
                    vista.getEstadoFiltro()
            );

            vista.mostrarCompras(compras);
            vista.mostrarCantidadCompras(compras.size());

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Filtro de compras",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible consultar las compras.",
                    ex
            );
        }
    }

    public void verDetalleCompra() {
        int fila = vista.getFilaCompraSeleccionadaModelo();

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
            Compra compra = compraDAO.obtenerCompraCompleta(
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
        int fila = vista.getFilaCompraSeleccionadaModelo();

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

        if (!"REGISTRADA".equalsIgnoreCase(compra.getEstado())) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Solo se pueden anular compras registradas.",
                    "Compra no anulable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "La compra #" + compra.getIdCompra()
                + " será anulada y sus existencias se restarán "
                + "del inventario.\n¿Deseas continuar?",
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
                    "La compra fue anulada y el inventario se revirtió.",
                    "Compra anulada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            buscarCompras();

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible anular la compra.",
                    ex
            );
        }
    }

    private Compra construirCompra(Proveedor proveedor) {
        Compra compra = new Compra();
        compra.setIdProveedor(proveedor.getIdProveedor());
        compra.setNombreProveedor(proveedor.getNombreProveedor());
        compra.setIdUsuario(Sesion.getIdUsuario());
        compra.setNombreUsuario(Sesion.getNombreCompleto());
        compra.setNumeroDocumento(vista.getNumeroDocumento());

        LocalDate fecha = vista.getFechaCompra();
        LocalTime hora = fecha.equals(LocalDate.now())
                ? LocalTime.now().withNano(0)
                : LocalTime.NOON;

        compra.setFechaCompra(LocalDateTime.of(fecha, hora));
        compra.setTipoPago(vista.getTipoPago());
        compra.setObservaciones(vista.getObservaciones());
        compra.setDetalles(new ArrayList<>(detalles));
        
        compra.setEstado("REGISTRADA");
        compra.recalcularTotales();
        return compra;
    }

    private void validarCompra(Compra compra) {
        if (compra.getFechaCompra().toLocalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de compra no puede estar en el futuro."
            );
        }

        if (compra.getDescuento().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede ser negativo."
            );
        }

        if (compra.getDescuento().compareTo(compra.getSubtotal()) > 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede ser mayor que el subtotal."
            );
        }

        for (DetalleCompra detalle : compra.getDetalles()) {
            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "Todas las cantidades deben ser mayores que cero."
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

    private void validarSeries(List<String> series, int cantidad) {
        if (series.size() != cantidad) {
            throw new IllegalArgumentException(
                    "Debes ingresar exactamente " + cantidad
                    + " números de serie."
            );
        }

        Set<String> unicas = new HashSet<>();

        for (String serie : series) {
            String normalizada = serie.trim().toUpperCase();

            if (normalizada.isBlank() || !unicas.add(normalizada)) {
                throw new IllegalArgumentException(
                        "Los números de serie no pueden estar vacíos "
                        + "ni repetidos."
                );
            }
        }
    }

    public void recalcularTotales() {
        actualizarCarrito();
    }

    private void actualizarCarrito() {

        vista.mostrarDetalles(detalles);

        BigDecimal subtotal = detalles.stream()
                .map(DetalleCompra::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal descuentoTotal = detalles.stream()
                .map(DetalleCompra::getDescuentoLinea)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal porcentajeDescuento
                = subtotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : descuentoTotal
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                subtotal,
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal total = subtotal
                .subtract(descuentoTotal)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        int unidades = detalles.stream()
                .mapToInt(DetalleCompra::getCantidad)
                .sum();

        vista.actualizarResumen(
                detalles.size(),
                unidades,
                subtotal,
                descuentoTotal,
                porcentajeDescuento,
                total
        );
    }

    private void mostrarErrorBaseDatos(String mensaje, SQLException ex) {
        JOptionPane.showMessageDialog(
                vista,
                mensaje + "\n\nDetalle: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
    }
}
