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
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

public class ProductoControlador {

    private final ProductosPanel vista;
    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    private List<Producto> productos = new ArrayList<>();
    private Integer idProductoSeleccionado;

    private SwingWorker<DatosCarga, Void> trabajador;

    private long ultimaCarga;

    private static final long VIGENCIA_DATOS_MS
            = 30_000;

    private record DatosCarga(
            List<Categoria> categorias,
            List<Producto> productos
            ) {

    }

    public ProductoControlador(ProductosPanel vista) {
        this.vista = vista;
        this.productoDAO = new ProductoDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public void iniciarAsync() {
        cargarAsync(true);
    }

    public void recargarAsync() {
        cargarAsync(true);
    }

    public void recargarSiNecesario() {

        long tiempoTranscurrido
                = System.currentTimeMillis()
                - ultimaCarga;

        if (tiempoTranscurrido
                >= VIGENCIA_DATOS_MS) {

            cargarAsync(false);
        }
    }

    
    private void cargarAsync(
            boolean cargarCategorias) {

        if (trabajador != null
                && !trabajador.isDone()) {

            return;
        }

        Categoria categoria
                = vista.getCategoriaFiltro();

        Integer idCategoria
                = categoria == null
                || categoria.getIdCategoria() <= 0
                ? null
                : categoria.getIdCategoria();

        String textoBusqueda
                = vista.getTextoBusqueda();

        vista.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR
                )
        );

        trabajador
                = new SwingWorker<>() {

            @Override
            protected DatosCarga doInBackground()
                    throws Exception {

                List<Categoria> categorias
                        = cargarCategorias
                                ? categoriaDAO
                                        .listarActivas()
                                : List.of();

                List<Producto> productosCargados
                        = productoDAO.listar(
                                textoBusqueda,
                                idCategoria
                        );

                return new DatosCarga(
                        categorias,
                        productosCargados
                );
            }

            @Override
            protected void done() {

                try {
                    DatosCarga datos = get();

                    if (cargarCategorias) {
                        vista.cargarCategorias(
                                datos.categorias()
                        );
                    }

                    productos
                            = datos.productos();

                    vista.mostrarProductos(productos);

                    vista.mostrarCantidad(
                            productos.size()
                    );

                    ultimaCarga
                            = System.currentTimeMillis();

                } catch (InterruptedException ex) {

                    Thread.currentThread()
                            .interrupt();

                } catch (ExecutionException ex) {

                    Throwable causa
                            = ex.getCause();

                    JOptionPane.showMessageDialog(
                            vista,
                            "No fue posible cargar "
                            + "los productos.\n\n"
                            + (causa == null
                                    ? ex.getMessage()
                                    : causa.getMessage()),
                            "Error de base de datos",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );
                }
            }
        };

        trabajador.execute();
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

    public void nuevo() {
        idProductoSeleccionado = null;
        vista.limpiarFormulario();
        vista.setModoEdicion(false);
    }

    public void seleccionarFila() {
        int filaModelo = vista.getFilaSeleccionadaModelo();

        if (filaModelo < 0 || filaModelo >= productos.size()) {
            return;
        }

        Producto producto = productos.get(filaModelo);
        idProductoSeleccionado = producto.getIdProducto();

        vista.mostrarProducto(producto);
        vista.setModoEdicion(true);
    }

    public void guardar() {
        try {
            Producto producto = vista.obtenerProductoFormulario();
            validar(producto);

            Integer idExcluir = idProductoSeleccionado;

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

            if (idProductoSeleccionado == null) {
                int idGenerado = productoDAO.insertar(producto);
                idProductoSeleccionado = idGenerado;

                JOptionPane.showMessageDialog(
                        vista,
                        "Producto registrado correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );

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

            buscar();
            seleccionarProductoEnTabla(idProductoSeleccionado);

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

    private void seleccionarProductoEnTabla(int idProducto) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getIdProducto() == idProducto) {
                vista.seleccionarFilaModelo(i);
                seleccionarFila();
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
