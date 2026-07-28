package sigir.controlador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
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

    private List<Producto> productos = new ArrayList<>();
    private List<MovimientoInventario> movimientos =
            new ArrayList<>();

    public InventarioControlador(InventarioPanel vista) {
        this.vista = vista;
        this.inventarioDAO = new InventarioDAO();
    }

    public void iniciar() {
        vista.configurarPermisoAjustes(
                Sesion.haySesionActiva()
                && Sesion.esDueno()
        );

        cargarCategorias();
        cargarProductosAjustables();
        actualizarTodo();
    }

    public void recargar() {
        cargarCategorias();
        cargarProductosAjustables();
        actualizarTodo();
    }

    public void actualizarTodo() {
        cargarResumen();
        buscarExistencias();
        buscarMovimientos();
    }

    public void cargarResumen() {
        try {
            ResumenInventario resumen =
                    inventarioDAO.obtenerResumen();

            vista.mostrarResumen(resumen);

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar el resumen del inventario.",
                    ex
            );
        }
    }

    public void cargarCategorias() {
        try {
            vista.cargarCategorias(
                    inventarioDAO.listarCategoriasActivas()
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar las categorías.",
                    ex
            );
        }
    }

    public void cargarProductosAjustables() {
        try {
            vista.cargarProductosAjustables(
                    inventarioDAO.listarProductosAjustables()
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar los productos ajustables.",
                    ex
            );
        }
    }

    public void buscarExistencias() {
        try {
            Categoria categoria =
                    vista.getCategoriaFiltro();

            Integer idCategoria =
                    categoria == null
                    || categoria.getIdCategoria() <= 0
                            ? null
                            : categoria.getIdCategoria();

            productos = inventarioDAO.listarExistencias(
                    vista.getTextoBusquedaExistencias(),
                    idCategoria,
                    vista.getNivelStockFiltro()
            );

            vista.mostrarExistencias(productos);
            vista.mostrarCantidadProductos(
                    productos.size()
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible consultar las existencias.",
                    ex
            );
        }
    }

    public void buscarMovimientos() {
        try {
            LocalDate fechaDesde =
                    vista.getFechaDesdeFiltro();

            LocalDate fechaHasta =
                    vista.getFechaHastaFiltro();

            if (fechaDesde != null
                    && fechaHasta != null
                    && fechaDesde.isAfter(fechaHasta)) {

                throw new IllegalArgumentException(
                        "La fecha inicial no puede ser posterior "
                        + "a la fecha final."
                );
            }

            movimientos =
                    inventarioDAO.listarMovimientos(
                            vista.getTextoBusquedaMovimientos(),
                            vista.getTipoMovimientoFiltro(),
                            fechaDesde,
                            fechaHasta
                    );

            vista.mostrarMovimientos(movimientos);
            vista.mostrarCantidadMovimientos(
                    movimientos.size()
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Fechas no válidas",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible consultar los movimientos.",
                    ex
            );
        }
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
                        "Solo el dueño puede realizar ajustes manuales."
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

            String tipo = vista.getTipoAjuste();
            int cantidad = vista.getCantidadAjuste();
            String motivo = vista.getMotivoAjuste();

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
                        "Escribe un motivo de al menos 5 caracteres."
                );
            }

            String accion = tipo.equals("AJUSTE_ENTRADA")
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

            if (respuesta != JOptionPane.YES_OPTION) {
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
            cargarProductosAjustables();
            actualizarTodo();
            vista.mostrarPestanaMovimientos();

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

    private void mostrarErrorBaseDatos(
            String mensaje,
            SQLException ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje + "\n\nDetalle: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}
