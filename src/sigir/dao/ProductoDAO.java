package sigir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.Producto;

public class ProductoDAO {

    public List<Producto> listar(
            String filtro,
            Integer idCategoria) throws SQLException {

        String texto = filtro == null ? "" : filtro.trim();

        String sql = """
                SELECT
                    p.id_producto,
                    p.id_categoria,
                    c.nombre AS nombre_categoria,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.marca,
                    p.modelo,
                    p.precio_compra,
                    p.precio_venta,
                    p.stock_actual,
                    p.stock_minimo,
                    p.maneja_numero_serie,
                    p.estado,
                    p.fecha_registro
                FROM dbo.productos AS p
                INNER JOIN dbo.categorias_producto AS c
                    ON c.id_categoria = p.id_categoria
                WHERE
                    (
                        ? = ''
                        OR p.codigo LIKE '%' + ? + '%'
                        OR p.nombre LIKE '%' + ? + '%'
                        OR ISNULL(p.marca, '') LIKE '%' + ? + '%'
                        OR ISNULL(p.modelo, '') LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR p.id_categoria = ?)
                ORDER BY
                    CASE WHEN p.estado = 'ACTIVO' THEN 0 ELSE 1 END,
                    p.nombre;
                """;

        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, texto);
            sentencia.setString(2, texto);
            sentencia.setString(3, texto);
            sentencia.setString(4, texto);
            sentencia.setString(5, texto);

            if (idCategoria == null || idCategoria <= 0) {
                sentencia.setNull(6, java.sql.Types.INTEGER);
                sentencia.setNull(7, java.sql.Types.INTEGER);
            } else {
                sentencia.setInt(6, idCategoria);
                sentencia.setInt(7, idCategoria);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    productos.add(mapear(resultado));
                }
            }
        }

        return productos;
    }

    public Producto buscarPorId(int idProducto) throws SQLException {

        String sql = """
                SELECT
                    p.id_producto,
                    p.id_categoria,
                    c.nombre AS nombre_categoria,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.marca,
                    p.modelo,
                    p.precio_compra,
                    p.precio_venta,
                    p.stock_actual,
                    p.stock_minimo,
                    p.maneja_numero_serie,
                    p.estado,
                    p.fecha_registro
                FROM dbo.productos AS p
                INNER JOIN dbo.categorias_producto AS c
                    ON c.id_categoria = p.id_categoria
                WHERE p.id_producto = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public boolean existeCodigo(
            String codigo,
            Integer idProductoExcluir) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM dbo.productos
                WHERE UPPER(codigo) = UPPER(?)
                  AND (? IS NULL OR id_producto <> ?);
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, codigo);

            if (idProductoExcluir == null || idProductoExcluir <= 0) {
                sentencia.setNull(2, java.sql.Types.INTEGER);
                sentencia.setNull(3, java.sql.Types.INTEGER);
            } else {
                sentencia.setInt(2, idProductoExcluir);
                sentencia.setInt(3, idProductoExcluir);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    public int insertar(Producto producto) throws SQLException {

        String sql = """
                INSERT INTO dbo.productos
                (
                    id_categoria,
                    codigo,
                    nombre,
                    descripcion,
                    marca,
                    modelo,
                    precio_compra,
                    precio_venta,
                    stock_actual,
                    stock_minimo,
                    maneja_numero_serie,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?);
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            asignarDatos(sentencia, producto, false);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException(
                "SQL Server no devolvió el id del producto registrado."
        );
    }

    public void actualizar(Producto producto) throws SQLException {

        String sql = """
                UPDATE dbo.productos
                SET
                    id_categoria = ?,
                    codigo = ?,
                    nombre = ?,
                    descripcion = ?,
                    marca = ?,
                    modelo = ?,
                    precio_compra = ?,
                    precio_venta = ?,
                    stock_minimo = ?,
                    maneja_numero_serie = ?,
                    estado = ?
                WHERE id_producto = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            asignarDatos(sentencia, producto, true);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "El producto ya no existe en la base de datos."
                );
            }
        }
    }

    public void cambiarEstado(
            int idProducto,
            String estado) throws SQLException {

        String sql = """
                UPDATE dbo.productos
                SET estado = ?
                WHERE id_producto = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, idProducto);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "El producto ya no existe en la base de datos."
                );
            }
        }
    }

    private void asignarDatos(
            PreparedStatement sentencia,
            Producto producto,
            boolean incluirId) throws SQLException {

        sentencia.setInt(1, producto.getIdCategoria());
        sentencia.setString(2, producto.getCodigo());
        sentencia.setString(3, producto.getNombre());
        sentencia.setString(4, textoNulo(producto.getDescripcion()));
        sentencia.setString(5, textoNulo(producto.getMarca()));
        sentencia.setString(6, textoNulo(producto.getModelo()));
        sentencia.setBigDecimal(7, producto.getPrecioCompra());
        sentencia.setBigDecimal(8, producto.getPrecioVenta());
        sentencia.setInt(9, producto.getStockMinimo());
        sentencia.setBoolean(10, producto.isManejaNumeroSerie());
        sentencia.setString(11, producto.getEstado());

        if (incluirId) {
            sentencia.setInt(12, producto.getIdProducto());
        }
    }

    private String textoNulo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }

    private Producto mapear(ResultSet resultado) throws SQLException {

        Producto producto = new Producto();

        producto.setIdProducto(resultado.getInt("id_producto"));
        producto.setIdCategoria(resultado.getInt("id_categoria"));
        producto.setNombreCategoria(
                resultado.getString("nombre_categoria")
        );
        producto.setCodigo(resultado.getString("codigo"));
        producto.setNombre(resultado.getString("nombre"));
        producto.setDescripcion(resultado.getString("descripcion"));
        producto.setMarca(resultado.getString("marca"));
        producto.setModelo(resultado.getString("modelo"));
        producto.setPrecioCompra(
                resultado.getBigDecimal("precio_compra")
        );
        producto.setPrecioVenta(
                resultado.getBigDecimal("precio_venta")
        );
        producto.setStockActual(
                resultado.getInt("stock_actual")
        );
        producto.setStockMinimo(
                resultado.getInt("stock_minimo")
        );
        producto.setManejaNumeroSerie(
                resultado.getBoolean("maneja_numero_serie")
        );
        producto.setEstado(resultado.getString("estado"));

        Timestamp fecha = resultado.getTimestamp("fecha_registro");

        if (fecha != null) {
            producto.setFechaRegistro(fecha.toLocalDateTime());
        }

        return producto;
    }
}
