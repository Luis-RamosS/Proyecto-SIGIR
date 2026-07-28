package sigir.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.Categoria;
import sigir.modelo.MovimientoInventario;
import sigir.modelo.Producto;
import sigir.modelo.ResumenInventario;

public class InventarioDAO {

    public ResumenInventario obtenerResumen() throws SQLException {

        String sql = """
                SELECT
                    COUNT(*) AS total_productos,
                    SUM(
                        CASE
                            WHEN estado <> 'INACTIVO'
                             AND stock_actual > 0
                             AND stock_actual <= stock_minimo
                                THEN 1
                            ELSE 0
                        END
                    ) AS stock_bajo,
                    SUM(
                        CASE
                            WHEN estado <> 'INACTIVO'
                             AND stock_actual = 0
                                THEN 1
                            ELSE 0
                        END
                    ) AS agotados,
                    ISNULL(
                        SUM(
                            CASE
                                WHEN estado <> 'INACTIVO'
                                    THEN stock_actual * precio_compra
                                ELSE 0
                            END
                        ),
                        0
                    ) AS valor_inventario
                FROM dbo.productos;

                SELECT COUNT(*) AS movimientos_hoy
                FROM dbo.movimientos_inventario
                WHERE CAST(fecha_movimiento AS DATE)
                    = CAST(SYSDATETIME() AS DATE);
                """;

        ResumenInventario resumen = new ResumenInventario();

        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {

            boolean tieneResultado = sentencia.execute(sql);

            if (tieneResultado) {
                try (ResultSet resultado = sentencia.getResultSet()) {
                    if (resultado.next()) {
                        resumen.setTotalProductos(
                                resultado.getInt("total_productos")
                        );
                        resumen.setStockBajo(
                                resultado.getInt("stock_bajo")
                        );
                        resumen.setAgotados(
                                resultado.getInt("agotados")
                        );
                        resumen.setValorInventario(
                                resultado.getBigDecimal(
                                        "valor_inventario"
                                )
                        );
                    }
                }
            }

            if (sentencia.getMoreResults()) {
                try (ResultSet resultado = sentencia.getResultSet()) {
                    if (resultado.next()) {
                        resumen.setMovimientosHoy(
                                resultado.getInt("movimientos_hoy")
                        );
                    }
                }
            }
        }

        return resumen;
    }

    public List<Categoria> listarCategoriasActivas()
            throws SQLException {

        String sql = """
                SELECT
                    id_categoria,
                    nombre,
                    descripcion,
                    estado
                FROM dbo.categorias_producto
                WHERE estado = 1
                ORDER BY nombre;
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                categorias.add(new Categoria(
                        resultado.getInt("id_categoria"),
                        resultado.getString("nombre"),
                        resultado.getString("descripcion"),
                        resultado.getBoolean("estado")
                ));
            }
        }

        return categorias;
    }

    public List<Producto> listarExistencias(
            String filtro,
            Integer idCategoria,
            String nivelStock) throws SQLException {

        String texto = filtro == null ? "" : filtro.trim();

        String nivel = nivelStock == null
                || nivelStock.isBlank()
                ? "TODOS"
                : nivelStock.trim().toUpperCase();

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
                        OR c.nombre LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR p.id_categoria = ?)
                    AND
                    (
                        ? = 'TODOS'
                        OR (
                            ? = 'DISPONIBLE'
                            AND p.estado <> 'INACTIVO'
                            AND p.stock_actual > p.stock_minimo
                        )
                        OR (
                            ? = 'STOCK_BAJO'
                            AND p.estado <> 'INACTIVO'
                            AND p.stock_actual > 0
                            AND p.stock_actual <= p.stock_minimo
                        )
                        OR (
                            ? = 'AGOTADO'
                            AND p.estado <> 'INACTIVO'
                            AND p.stock_actual = 0
                        )
                        OR (
                            ? = 'INACTIVO'
                            AND p.estado = 'INACTIVO'
                        )
                    )
                ORDER BY
                    CASE
                        WHEN p.estado = 'INACTIVO' THEN 3
                        WHEN p.stock_actual = 0 THEN 0
                        WHEN p.stock_actual <= p.stock_minimo THEN 1
                        ELSE 2
                    END,
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
            sentencia.setString(6, texto);

            if (idCategoria == null || idCategoria <= 0) {
                sentencia.setNull(7, Types.INTEGER);
                sentencia.setNull(8, Types.INTEGER);
            } else {
                sentencia.setInt(7, idCategoria);
                sentencia.setInt(8, idCategoria);
            }

            sentencia.setString(9, nivel);
            sentencia.setString(10, nivel);
            sentencia.setString(11, nivel);
            sentencia.setString(12, nivel);
            sentencia.setString(13, nivel);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    productos.add(mapearProducto(resultado));
                }
            }
        }

        return productos;
    }

    public List<Producto> listarProductosAjustables()
            throws SQLException {

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
                WHERE p.estado IN ('ACTIVO', 'AGOTADO')
                ORDER BY p.nombre;
                """;

        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                productos.add(mapearProducto(resultado));
            }
        }

        return productos;
    }

    public List<MovimientoInventario> listarMovimientos(
            String filtro,
            String tipoMovimiento,
            LocalDate fechaDesde,
            LocalDate fechaHasta) throws SQLException {

        String texto = filtro == null ? "" : filtro.trim();

        String tipo = tipoMovimiento == null
                || tipoMovimiento.isBlank()
                || "TODOS".equalsIgnoreCase(tipoMovimiento)
                        ? null
                        : tipoMovimiento.trim().toUpperCase();

        String sql = """
                SELECT
                    mi.id_movimiento,
                    mi.id_producto,
                    p.codigo AS codigo_producto,
                    p.nombre AS nombre_producto,
                    mi.id_usuario,
                    u.nombre_completo AS nombre_usuario,
                    mi.id_compra,
                    mi.id_venta,
                    mi.id_orden,
                    mi.tipo_movimiento,
                    mi.cantidad,
                    mi.stock_anterior,
                    mi.stock_nuevo,
                    mi.fecha_movimiento,
                    mi.motivo
                FROM dbo.movimientos_inventario AS mi
                INNER JOIN dbo.productos AS p
                    ON p.id_producto = mi.id_producto
                INNER JOIN dbo.usuarios AS u
                    ON u.id_usuario = mi.id_usuario
                WHERE
                    (
                        ? = ''
                        OR p.codigo LIKE '%' + ? + '%'
                        OR p.nombre LIKE '%' + ? + '%'
                        OR u.nombre_completo LIKE '%' + ? + '%'
                        OR ISNULL(mi.motivo, '') LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR mi.tipo_movimiento = ?)
                    AND (? IS NULL OR CAST(mi.fecha_movimiento AS DATE) >= ?)
                    AND (? IS NULL OR CAST(mi.fecha_movimiento AS DATE) <= ?)
                ORDER BY
                    mi.fecha_movimiento DESC,
                    mi.id_movimiento DESC;
                """;

        List<MovimientoInventario> movimientos =
                new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, texto);
            sentencia.setString(2, texto);
            sentencia.setString(3, texto);
            sentencia.setString(4, texto);
            sentencia.setString(5, texto);

            if (tipo == null) {
                sentencia.setNull(6, Types.VARCHAR);
                sentencia.setNull(7, Types.VARCHAR);
            } else {
                sentencia.setString(6, tipo);
                sentencia.setString(7, tipo);
            }

            establecerFecha(
                    sentencia,
                    8,
                    9,
                    fechaDesde
            );

            establecerFecha(
                    sentencia,
                    10,
                    11,
                    fechaHasta
            );

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    movimientos.add(
                            mapearMovimiento(resultado)
                    );
                }
            }
        }

        return movimientos;
    }

    public void registrarAjuste(
            int idProducto,
            int idUsuario,
            String tipoMovimiento,
            int cantidad,
            String motivo) throws SQLException {

        String tipo = tipoMovimiento == null
                ? ""
                : tipoMovimiento.trim().toUpperCase();

        if (!tipo.equals("AJUSTE_ENTRADA")
                && !tipo.equals("AJUSTE_SALIDA")) {

            throw new SQLException(
                    "El tipo de ajuste no es válido."
            );
        }

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                Producto producto = bloquearProducto(
                        conexion,
                        idProducto
                );

                if (producto.isManejaNumeroSerie()) {
                    throw new SQLException(
                            "Los ajustes manuales para productos "
                            + "con número de serie están bloqueados. "
                            + "Deben gestionarse mediante compras, "
                            + "ventas o reparaciones."
                    );
                }

                int stockAnterior =
                        producto.getStockActual();

                int stockNuevo = tipo.equals("AJUSTE_ENTRADA")
                        ? stockAnterior + cantidad
                        : stockAnterior - cantidad;

                if (stockNuevo < 0) {
                    throw new SQLException(
                            "El ajuste dejaría existencias negativas. "
                            + "Stock disponible: " + stockAnterior + "."
                    );
                }

                actualizarStock(
                        conexion,
                        producto,
                        stockNuevo
                );

                insertarMovimientoAjuste(
                        conexion,
                        idProducto,
                        idUsuario,
                        tipo,
                        cantidad,
                        stockAnterior,
                        stockNuevo,
                        motivo
                );

                conexion.commit();

            } catch (SQLException | RuntimeException ex) {
                conexion.rollback();
                throw ex;

            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    private Producto bloquearProducto(
            Connection conexion,
            int idProducto) throws SQLException {

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
                FROM dbo.productos AS p WITH (UPDLOCK, ROWLOCK)
                INNER JOIN dbo.categorias_producto AS c
                    ON c.id_categoria = p.id_categoria
                WHERE p.id_producto = ?;
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException(
                            "El producto seleccionado ya no existe."
                    );
                }

                return mapearProducto(resultado);
            }
        }
    }

    private void actualizarStock(
            Connection conexion,
            Producto producto,
            int stockNuevo) throws SQLException {

        String estadoNuevo = producto.getEstado();

        if (!"INACTIVO".equalsIgnoreCase(estadoNuevo)) {
            estadoNuevo = stockNuevo == 0
                    ? "AGOTADO"
                    : "ACTIVO";
        }

        String sql = """
                UPDATE dbo.productos
                SET
                    stock_actual = ?,
                    estado = ?
                WHERE id_producto = ?;
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, stockNuevo);
            sentencia.setString(2, estadoNuevo);
            sentencia.setInt(3, producto.getIdProducto());

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No fue posible actualizar el producto."
                );
            }
        }
    }

    private void insertarMovimientoAjuste(
            Connection conexion,
            int idProducto,
            int idUsuario,
            String tipoMovimiento,
            int cantidad,
            int stockAnterior,
            int stockNuevo,
            String motivo) throws SQLException {

        String sql = """
                INSERT INTO dbo.movimientos_inventario
                (
                    id_producto,
                    id_usuario,
                    id_compra,
                    id_venta,
                    id_orden,
                    tipo_movimiento,
                    cantidad,
                    stock_anterior,
                    stock_nuevo,
                    motivo
                )
                VALUES
                (
                    ?,
                    ?,
                    NULL,
                    NULL,
                    NULL,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                );
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProducto);
            sentencia.setInt(2, idUsuario);
            sentencia.setString(3, tipoMovimiento);
            sentencia.setInt(4, cantidad);
            sentencia.setInt(5, stockAnterior);
            sentencia.setInt(6, stockNuevo);
            sentencia.setString(7, motivo);

            sentencia.executeUpdate();
        }
    }

    private Producto mapearProducto(
            ResultSet resultado) throws SQLException {

        Producto producto = new Producto();

        producto.setIdProducto(
                resultado.getInt("id_producto")
        );
        producto.setIdCategoria(
                resultado.getInt("id_categoria")
        );
        producto.setNombreCategoria(
                resultado.getString("nombre_categoria")
        );
        producto.setCodigo(
                resultado.getString("codigo")
        );
        producto.setNombre(
                resultado.getString("nombre")
        );
        producto.setDescripcion(
                resultado.getString("descripcion")
        );
        producto.setMarca(
                resultado.getString("marca")
        );
        producto.setModelo(
                resultado.getString("modelo")
        );
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
                resultado.getBoolean(
                        "maneja_numero_serie"
                )
        );
        producto.setEstado(
                resultado.getString("estado")
        );

        Timestamp fecha =
                resultado.getTimestamp("fecha_registro");

        if (fecha != null) {
            producto.setFechaRegistro(
                    fecha.toLocalDateTime()
            );
        }

        return producto;
    }

    private MovimientoInventario mapearMovimiento(
            ResultSet resultado) throws SQLException {

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setIdMovimiento(
                resultado.getInt("id_movimiento")
        );
        movimiento.setIdProducto(
                resultado.getInt("id_producto")
        );
        movimiento.setCodigoProducto(
                resultado.getString("codigo_producto")
        );
        movimiento.setNombreProducto(
                resultado.getString("nombre_producto")
        );
        movimiento.setIdUsuario(
                resultado.getInt("id_usuario")
        );
        movimiento.setNombreUsuario(
                resultado.getString("nombre_usuario")
        );

        movimiento.setIdCompra(
                obtenerEnteroNulo(
                        resultado,
                        "id_compra"
                )
        );
        movimiento.setIdVenta(
                obtenerEnteroNulo(
                        resultado,
                        "id_venta"
                )
        );
        movimiento.setIdOrden(
                obtenerEnteroNulo(
                        resultado,
                        "id_orden"
                )
        );

        movimiento.setTipoMovimiento(
                resultado.getString("tipo_movimiento")
        );
        movimiento.setCantidad(
                resultado.getInt("cantidad")
        );
        movimiento.setStockAnterior(
                resultado.getInt("stock_anterior")
        );
        movimiento.setStockNuevo(
                resultado.getInt("stock_nuevo")
        );

        Timestamp fecha =
                resultado.getTimestamp("fecha_movimiento");

        if (fecha != null) {
            movimiento.setFechaMovimiento(
                    fecha.toLocalDateTime()
            );
        }

        movimiento.setMotivo(
                resultado.getString("motivo")
        );

        return movimiento;
    }

    private Integer obtenerEnteroNulo(
            ResultSet resultado,
            String columna) throws SQLException {

        int valor = resultado.getInt(columna);

        return resultado.wasNull() ? null : valor;
    }

    private void establecerFecha(
            PreparedStatement sentencia,
            int posicionNulo,
            int posicionValor,
            LocalDate fecha) throws SQLException {

        if (fecha == null) {
            sentencia.setNull(posicionNulo, Types.DATE);
            sentencia.setNull(posicionValor, Types.DATE);
        } else {
            Date fechaSql = Date.valueOf(fecha);
            sentencia.setDate(posicionNulo, fechaSql);
            sentencia.setDate(posicionValor, fechaSql);
        }
    }
}
