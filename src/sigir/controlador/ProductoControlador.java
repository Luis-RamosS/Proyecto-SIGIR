package sigir.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import sigir.dao.CategoriaDAO;
import sigir.dao.ProductoDAO;
import sigir.modelo.Categoria;
import sigir.modelo.Producto;
import sigir.vista.paneles.ProductosPanel;

public class ProductoControlador {

    private final ProductosPanel vista;
    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    private List<Producto> productos = new ArrayList<>();
    private Integer idProductoSeleccionado;

    private String firmaFormularioBase;
    private boolean ignorandoSeleccion;

    public ProductoControlador(ProductosPanel vista) {
        this.vista = vista;
        this.productoDAO = new ProductoDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public void iniciar() {
        cargarCategorias();
        buscar();
        nuevo();
    }

    public void recargar() {
        cargarCategorias();
        buscar();
    }

    public void buscar() {
        try {
            Categoria categoria = vista.getCategoriaFiltro();
            Integer idCategoria = categoria == null
                    || categoria.getIdCategoria() <= 0
                    ? null
                    : categoria.getIdCategoria();

            productos = productoDAO.listar(
                    vista.getTextoBusqueda(),
                    idCategoria
            );

            vista.mostrarProductos(productos);
            vista.mostrarCantidad(productos.size());

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar los productos.",
                    ex
            );
        }
    }

    public boolean nuevo() {
        if (!confirmarDescartarCambios()) {
            restaurarSeleccionActual();
            return false;
        }

        idProductoSeleccionado = null;

        vista.limpiarFormulario();
        vista.setModoEdicion(false);

        actualizarFirmaFormularioBase();
        return true;
    }

    public boolean prepararNuevoDesdeCompras() {
        return nuevo();
    }

    public void seleccionarFila() {
        if (ignorandoSeleccion) {
            return;
        }

        int filaModelo =
                vista.getFilaSeleccionadaModelo();

        if (filaModelo < 0
                || filaModelo >= productos.size()) {

            return;
        }

        Producto producto =
                productos.get(filaModelo);

        if (!confirmarDescartarCambios()) {
            restaurarSeleccionActual();
            return;
        }

        idProductoSeleccionado =
                producto.getIdProducto();

        vista.mostrarProducto(producto);
        vista.setModoEdicion(true);

        actualizarFirmaFormularioBase();
    }

    public void guardar() {
        try {
            Producto producto = vista.obtenerProductoFormulario();
            validar(producto);

            Integer idExcluir = idProductoSeleccionado;
            boolean creando =
                    idProductoSeleccionado == null;

            if (productoDAO.existeCodigo(
                    producto.getCodigo(),
                    idExcluir
            )) {
                vista.enfocarCodigo();

                JOptionPane.showMessageDialog(
                        vista,
                        "Ya existe un producto con ese código.",
                        "Código duplicado",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (creando) {
                int idGenerado = productoDAO.insertar(producto);
                idProductoSeleccionado = idGenerado;
                producto.setIdProducto(idGenerado);

                if (!vista.tieneProductoRegistradoListener()) {
                    JOptionPane.showMessageDialog(
                            vista,
                            "Producto registrado correctamente.",
                            "SIGIR",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

            } else {
                producto.setIdProducto(idProductoSeleccionado);
                productoDAO.actualizar(producto);

                JOptionPane.showMessageDialog(
                        vista,
                        "Producto actualizado correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            actualizarFirmaFormularioBase();

            buscar();
            seleccionarProductoEnTabla(idProductoSeleccionado);

            if (creando) {
                Producto productoNotificar =
                        productos.stream()
                                .filter(item ->
                                        item.getIdProducto()
                                        == idProductoSeleccionado
                                )
                                .findFirst()
                                .orElse(producto);

                vista.notificarProductoRegistrado(
                        productoNotificar
                );
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible guardar el producto.",
                    ex
            );
        }
    }

    public void desactivar() {
        if (idProductoSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un producto de la tabla.",
                    "Producto no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "El producto dejará de estar disponible para nuevas "
                + "operaciones.\n¿Deseas desactivarlo?",
                "Desactivar producto",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            productoDAO.cambiarEstado(
                    idProductoSeleccionado,
                    "INACTIVO"
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "Producto desactivado correctamente.",
                    "SIGIR",
                    JOptionPane.INFORMATION_MESSAGE
            );

            nuevo();
            buscar();

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible desactivar el producto.",
                    ex
            );
        }
    }

    private void cargarCategorias() {
        try {
            List<Categoria> categorias =
                    categoriaDAO.listarActivas();

            vista.cargarCategorias(categorias);

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar las categorías.",
                    ex
            );
        }
    }

    private void validar(Producto producto) {

        if (producto.getCodigo() == null
                || producto.getCodigo().isBlank()) {

            vista.enfocarCodigo();
            throw new IllegalArgumentException(
                    "Ingresa el código del producto."
            );
        }

        if (producto.getCodigo().length() > 30) {
            vista.enfocarCodigo();
            throw new IllegalArgumentException(
                    "El código no puede superar 30 caracteres."
            );
        }

        if (producto.getNombre() == null
                || producto.getNombre().isBlank()) {

            vista.enfocarNombre();
            throw new IllegalArgumentException(
                    "Ingresa el nombre del producto."
            );
        }

        if (producto.getNombre().length() > 120) {
            vista.enfocarNombre();
            throw new IllegalArgumentException(
                    "El nombre no puede superar 120 caracteres."
            );
        }

        if (producto.getIdCategoria() <= 0) {
            vista.enfocarCategoria();
            throw new IllegalArgumentException(
                    "Selecciona una categoría."
            );
        }

        if (producto.getPrecioCompra()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El precio de compra no puede ser negativo."
            );
        }

        if (producto.getPrecioVenta()
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

    private void actualizarFirmaFormularioBase() {
        firmaFormularioBase =
                vista.firmaFormulario();
    }

    private boolean hayCambiosSinGuardar() {
        if (firmaFormularioBase == null) {
            return false;
        }

        return !firmaFormularioBase.equals(
                vista.firmaFormulario()
        );
    }

    private boolean confirmarDescartarCambios() {
        if (!hayCambiosSinGuardar()) {
            return true;
        }

        int respuesta =
                JOptionPane.showConfirmDialog(
                        vista,
                        "Hay cambios sin guardar.\\n\\n"
                        + "¿Deseas descartar los cambios?",
                        "Descartar cambios",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        return respuesta
                == JOptionPane.YES_OPTION;
    }

    private void restaurarSeleccionActual() {
        ignorandoSeleccion = true;

        try {
            if (idProductoSeleccionado == null) {
                vista.limpiarSeleccionTabla();
                return;
            }

            for (int i = 0;
                    i < productos.size();
                    i++) {

                if (productos.get(i)
                        .getIdProducto()
                        == idProductoSeleccionado) {

                    vista.seleccionarFilaModelo(i);
                    return;
                }
            }

            vista.limpiarSeleccionTabla();

        } finally {
            ignorandoSeleccion = false;
        }
    }

    private void seleccionarProductoEnTabla(
            int idProducto) {

        for (int i = 0;
                i < productos.size();
                i++) {

            if (productos.get(i)
                    .getIdProducto()
                    == idProducto) {

                ignorandoSeleccion = true;

                try {
                    vista.seleccionarFilaModelo(i);
                } finally {
                    ignorandoSeleccion = false;
                }

                Producto producto =
                        productos.get(i);

                idProductoSeleccionado =
                        producto.getIdProducto();

                vista.mostrarProducto(producto);
                vista.setModoEdicion(true);

                actualizarFirmaFormularioBase();
                break;
            }
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
