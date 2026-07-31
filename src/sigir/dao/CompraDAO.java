package sigir.dao;

import java.sql.CallableStatement;
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
import sigir.modelo.Compra;
import sigir.modelo.DetalleCompra;
import sigir.modelo.Producto;
import sigir.modelo.Proveedor;

public class CompraDAO {

    public List<Proveedor> listarProveedoresActivos() throws SQLException {
        String sql = """
                SELECT id_proveedor, rtn, nombre_proveedor, nombre_contacto,
                       telefono, correo, direccion, estado, fecha_registro
                FROM dbo.proveedores
                WHERE estado = 'ACTIVO'
                ORDER BY nombre_proveedor;
                """;

        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setIdProveedor(resultado.getInt("id_proveedor"));
                proveedor.setRtn(resultado.getString("rtn"));
                proveedor.setNombreProveedor(resultado.getString("nombre_proveedor"));
                proveedor.setNombreContacto(resultado.getString("nombre_contacto"));
                proveedor.setTelefono(resultado.getString("telefono"));
                proveedor.setCorreo(resultado.getString("correo"));
                proveedor.setDireccion(resultado.getString("direccion"));
                proveedor.setEstado(resultado.getString("estado"));

                Timestamp fecha = resultado.getTimestamp("fecha_registro");
                if (fecha != null) {
                    proveedor.setFechaRegistro(fecha.toLocalDateTime());
                }

                proveedores.add(proveedor);
            }
        }

        return proveedores;
    }

    public List<Producto> listarProductosDisponibles() throws SQLException {
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
                producto.setPrecioCompra(resultado.getBigDecimal("precio_compra"));
                producto.setPrecioVenta(resultado.getBigDecimal("precio_venta"));
                producto.setStockActual(resultado.getInt("stock_actual"));
                producto.setStockMinimo(resultado.getInt("stock_minimo"));
                producto.setManejaNumeroSerie(resultado.getBoolean("maneja_numero_serie"));
                producto.setEstado(resultado.getString("estado"));

                Timestamp fecha = resultado.getTimestamp("fecha_registro");
                if (fecha != null) {
                    producto.setFechaRegistro(fecha.toLocalDateTime());
                }

                productos.add(producto);
            }
        }

        return productos;
    }

    public boolean existeDocumentoProveedor(
            int idProveedor,
            String numeroDocumento) throws SQLException {

        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            return false;
        }

        String sql = """
                SELECT COUNT(*)
                FROM dbo.compras
                WHERE id_proveedor = ?
                  AND UPPER(numero_documento) = UPPER(?)
                  AND estado <> 'ANULADA';
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProveedor);
            sentencia.setString(2, numeroDocumento.trim());

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    public int registrar(Compra compra) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                int idCompra = insertarEncabezado(conexion, compra);

                for (DetalleCompra detalle : compra.getDetalles()) {
                    insertarDetalle(conexion, idCompra, detalle);

                    int stockAnterior = bloquearYObtenerStock(
                            conexion,
                            detalle.getIdProducto()
                    );

                    int stockNuevo = stockAnterior + detalle.getCantidad();

                    actualizarProductoPorCompra(conexion, detalle, stockNuevo);
                    insertarMovimientoEntrada(
                            conexion,
                            compra.getIdUsuario(),
                            idCompra,
                            detalle,
                            stockAnterior,
                            stockNuevo
                    );

                    if (detalle.isManejaNumeroSerie()) {
                        insertarUnidadesSerie(conexion, idCompra, detalle);
                    }
                }

                validarTotales(conexion, idCompra);
                conexion.commit();
                return idCompra;

            } catch (SQLException | RuntimeException ex) {
                conexion.rollback();
                throw ex;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    public List<Compra> listarCompras(
            String filtro,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String estado) throws SQLException {

        String texto = filtro == null ? "" : filtro.trim();
        String estadoFiltro = estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado)
                ? null
                : estado.trim().toUpperCase();

        String sql = """
                SELECT c.id_compra, c.id_proveedor, p.nombre_proveedor,
                       c.id_usuario, u.nombre_completo AS nombre_usuario,
                       c.numero_documento, c.fecha_compra, c.subtotal,
                       c.descuento, c.total, c.tipo_pago, c.estado,
                       c.observaciones
                FROM dbo.compras AS c
                INNER JOIN dbo.proveedores AS p
                    ON p.id_proveedor = c.id_proveedor
                INNER JOIN dbo.usuarios AS u
                    ON u.id_usuario = c.id_usuario
                WHERE
                    (
                        ? = ''
                        OR ISNULL(c.numero_documento, '') LIKE '%' + ? + '%'
                        OR p.nombre_proveedor LIKE '%' + ? + '%'
                        OR u.nombre_completo LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR CAST(c.fecha_compra AS DATE) >= ?)
                    AND (? IS NULL OR CAST(c.fecha_compra AS DATE) <= ?)
                    AND (? IS NULL OR c.estado = ?)
                ORDER BY c.fecha_compra DESC, c.id_compra DESC;
                """;

        List<Compra> compras = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, texto);
            sentencia.setString(2, texto);
            sentencia.setString(3, texto);
            sentencia.setString(4, texto);
            establecerFecha(sentencia, 5, 6, fechaDesde);
            establecerFecha(sentencia, 7, 8, fechaHasta);

            if (estadoFiltro == null) {
                sentencia.setNull(9, Types.VARCHAR);
                sentencia.setNull(10, Types.VARCHAR);
            } else {
                sentencia.setString(9, estadoFiltro);
                sentencia.setString(10, estadoFiltro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    compras.add(mapearCompra(resultado));
                }
            }
        }

        return compras;
    }

    public Compra obtenerCompraCompleta(int idCompra) throws SQLException {
        String sqlCompra = """
                SELECT c.id_compra, c.id_proveedor, p.nombre_proveedor,
                       c.id_usuario, u.nombre_completo AS nombre_usuario,
                       c.numero_documento, c.fecha_compra, c.subtotal,
                       c.descuento, c.total, c.tipo_pago, c.estado,
                       c.observaciones
                FROM dbo.compras AS c
                INNER JOIN dbo.proveedores AS p
                    ON p.id_proveedor = c.id_proveedor
                INNER JOIN dbo.usuarios AS u
                    ON u.id_usuario = c.id_usuario
                WHERE c.id_compra = ?;
                """;

        String sqlDetalle = """
                SELECT dc.id_detalle_compra, dc.id_producto,
                       p.codigo, p.nombre, p.maneja_numero_serie,
                       dc.cantidad, dc.costo_unitario, dc.subtotal
                FROM dbo.detalle_compra AS dc
                INNER JOIN dbo.productos AS p
                    ON p.id_producto = dc.id_producto
                WHERE dc.id_compra = ?
                ORDER BY dc.id_detalle_compra;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            Compra compra;

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlCompra)) {
                sentencia.setInt(1, idCompra);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        return null;
                    }
                    compra = mapearCompra(resultado);
                }
            }

            List<DetalleCompra> detalles = new ArrayList<>();

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlDetalle)) {
                sentencia.setInt(1, idCompra);

                try (ResultSet resultado = sentencia.executeQuery()) {
                    while (resultado.next()) {
                        DetalleCompra detalle = new DetalleCompra();
                        detalle.setIdDetalleCompra(resultado.getInt("id_detalle_compra"));
                        detalle.setIdProducto(resultado.getInt("id_producto"));
                        detalle.setCodigoProducto(resultado.getString("codigo"));
                        detalle.setNombreProducto(resultado.getString("nombre"));
                        detalle.setManejaNumeroSerie(resultado.getBoolean("maneja_numero_serie"));
                        detalle.setCantidad(resultado.getInt("cantidad"));
                        detalle.setCostoUnitario(resultado.getBigDecimal("costo_unitario"));
                        detalle.setSubtotal(resultado.getBigDecimal("subtotal"));

                        if (detalle.isManejaNumeroSerie()) {
                            detalle.setNumerosSerie(
                                    listarSeriesCompra(
                                            conexion,
                                            idCompra,
                                            detalle.getIdProducto()
                                    )
                            );
                        }

                        detalles.add(detalle);
                    }
                }
            }

            compra.setDetalles(detalles);
            return compra;
        }
    }

    public void anular(int idCompra, int idUsuario) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                String estado = bloquearCompra(conexion, idCompra);

                if (!"REGISTRADA".equalsIgnoreCase(estado)) {
                    throw new SQLException(
                            "Solo se pueden anular compras registradas."
                    );
                }

                List<DetalleCompra> detalles =
                        listarDetallesParaAnulacion(conexion, idCompra);

                if (detalles.isEmpty()) {
                    throw new SQLException("La compra no contiene detalles.");
                }

                for (DetalleCompra detalle : detalles) {
                    int stockAnterior = bloquearYObtenerStock(
                            conexion,
                            detalle.getIdProducto()
                    );

                    if (stockAnterior < detalle.getCantidad()) {
                        throw new SQLException(
                                "No se puede anular la compra porque el producto "
                                + detalle.getNombreProducto()
                                + " ya no tiene suficientes existencias."
                        );
                    }

                    if (detalle.isManejaNumeroSerie()) {
                        eliminarUnidadesDeCompra(conexion, idCompra, detalle);
                    }

                    int stockNuevo = stockAnterior - detalle.getCantidad();
                    actualizarStockPorAnulacion(
                            conexion,
                            detalle.getIdProducto(),
                            stockNuevo
                    );

                    insertarMovimientoDevolucion(
                            conexion,
                            idUsuario,
                            idCompra,
                            detalle,
                            stockAnterior,
                            stockNuevo
                    );
                }

                try (PreparedStatement sentencia = conexion.prepareStatement(
                        "UPDATE dbo.compras SET estado = 'ANULADA' WHERE id_compra = ?;"
                )) {
                    sentencia.setInt(1, idCompra);
                    sentencia.executeUpdate();
                }

                conexion.commit();

            } catch (SQLException | RuntimeException ex) {
                conexion.rollback();
                throw ex;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    private int insertarEncabezado(Connection conexion, Compra compra)
            throws SQLException {

        String sql = """
                INSERT INTO dbo.compras
                (
                    id_proveedor, id_usuario, numero_documento,
                    fecha_compra, subtotal, descuento, total,
                    tipo_pago, estado, observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'REGISTRADA', ?);
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            sentencia.setInt(1, compra.getIdProveedor());
            sentencia.setInt(2, compra.getIdUsuario());
            establecerTextoNulo(sentencia, 3, compra.getNumeroDocumento());
            sentencia.setTimestamp(4, Timestamp.valueOf(compra.getFechaCompra()));
            sentencia.setBigDecimal(5, compra.getSubtotal());
            sentencia.setBigDecimal(6, compra.getDescuento());
            sentencia.setBigDecimal(7, compra.getTotal());
            sentencia.setString(8, compra.getTipoPago());
            establecerTextoNulo(sentencia, 9, compra.getObservaciones());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException("SQL Server no devolvió el id de la compra.");
    }

    private void insertarDetalle(
            Connection conexion,
            int idCompra,
            DetalleCompra detalle) throws SQLException {

        String sql = """
                INSERT INTO dbo.detalle_compra
                (id_compra, id_producto, cantidad, costo_unitario, subtotal)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCompra);
            sentencia.setInt(2, detalle.getIdProducto());
            sentencia.setInt(3, detalle.getCantidad());
            sentencia.setBigDecimal(4, detalle.getCostoUnitario());
            sentencia.setBigDecimal(5, detalle.getSubtotal());
            sentencia.executeUpdate();
        }
    }

    private int bloquearYObtenerStock(Connection conexion, int idProducto)
            throws SQLException {

        String sql = """
                SELECT stock_actual
                FROM dbo.productos WITH (UPDLOCK, ROWLOCK)
                WHERE id_producto = ?;
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException(
                            "El producto seleccionado ya no existe."
                    );
                }
                return resultado.getInt("stock_actual");
            }
        }
    }

    private void actualizarProductoPorCompra(
            Connection conexion,
            DetalleCompra detalle,
            int stockNuevo) throws SQLException {

        String sql = """
                UPDATE dbo.productos
                SET stock_actual = ?,
                    precio_compra = ?,
                    estado = CASE
                        WHEN estado = 'AGOTADO' THEN 'ACTIVO'
                        ELSE estado
                    END
                WHERE id_producto = ?;
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, stockNuevo);
            sentencia.setBigDecimal(2, detalle.getCostoUnitario());
            sentencia.setInt(3, detalle.getIdProducto());
            sentencia.executeUpdate();
        }
    }

    private void insertarMovimientoEntrada(
            Connection conexion,
            int idUsuario,
            int idCompra,
            DetalleCompra detalle,
            int stockAnterior,
            int stockNuevo) throws SQLException {

        String sql = """
                INSERT INTO dbo.movimientos_inventario
                (
                    id_producto, id_usuario, id_compra, id_venta, id_orden,
                    tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    motivo
                )
                VALUES (?, ?, ?, NULL, NULL, 'ENTRADA_COMPRA', ?, ?, ?, ?);
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, detalle.getIdProducto());
            sentencia.setInt(2, idUsuario);
            sentencia.setInt(3, idCompra);
            sentencia.setInt(4, detalle.getCantidad());
            sentencia.setInt(5, stockAnterior);
            sentencia.setInt(6, stockNuevo);
            sentencia.setString(7, "Entrada por compra #" + idCompra);
            sentencia.executeUpdate();
        }
    }

    private void insertarUnidadesSerie(
            Connection conexion,
            int idCompra,
            DetalleCompra detalle) throws SQLException {

        if (detalle.getNumerosSerie().size() != detalle.getCantidad()) {
            throw new SQLException(
                    "La cantidad de números de serie no coincide con la "
                    + "cantidad comprada de " + detalle.getNombreProducto() + "."
            );
        }

        String sql = """
                INSERT INTO dbo.unidades_producto
                (id_producto, numero_serie, codigo_interno, estado, observaciones)
                VALUES (?, ?, ?, 'DISPONIBLE', ?);
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            int secuencia = 1;

            for (String numeroSerie : detalle.getNumerosSerie()) {
                String codigoInterno = String.format(
                        "CMP%08d-P%05d-%03d",
                        idCompra,
                        detalle.getIdProducto(),
                        secuencia
                );

                sentencia.setInt(1, detalle.getIdProducto());
                sentencia.setString(2, numeroSerie);
                sentencia.setString(3, codigoInterno);
                sentencia.setString(4, "COMPRA_ID:" + idCompra);
                sentencia.addBatch();
                secuencia++;
            }

            sentencia.executeBatch();
        }
    }

    private void validarTotales(Connection conexion, int idCompra)
            throws SQLException {

        try (CallableStatement sentencia = conexion.prepareCall(
                "{call dbo.sp_validar_totales_compra(?)}"
        )) {
            sentencia.setInt(1, idCompra);
            sentencia.execute();
        }
    }

    private Compra mapearCompra(ResultSet resultado) throws SQLException {
        Compra compra = new Compra();
        compra.setIdCompra(resultado.getInt("id_compra"));
        compra.setIdProveedor(resultado.getInt("id_proveedor"));
        compra.setNombreProveedor(resultado.getString("nombre_proveedor"));
        compra.setIdUsuario(resultado.getInt("id_usuario"));
        compra.setNombreUsuario(resultado.getString("nombre_usuario"));
        compra.setNumeroDocumento(resultado.getString("numero_documento"));

        Timestamp fecha = resultado.getTimestamp("fecha_compra");
        if (fecha != null) {
            compra.setFechaCompra(fecha.toLocalDateTime());
        }

        compra.setSubtotal(resultado.getBigDecimal("subtotal"));
        compra.setDescuento(resultado.getBigDecimal("descuento"));
        compra.setTotal(resultado.getBigDecimal("total"));
        compra.setTipoPago(resultado.getString("tipo_pago"));
        compra.setEstado(resultado.getString("estado"));
        compra.setObservaciones(resultado.getString("observaciones"));
        return compra;
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

    private List<String> listarSeriesCompra(
            Connection conexion,
            int idCompra,
            int idProducto) throws SQLException {

        String sql = """
                SELECT numero_serie
                FROM dbo.unidades_producto
                WHERE id_producto = ?
                  AND observaciones = ?
                ORDER BY id_unidad;
                """;

        List<String> series = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idProducto);
            sentencia.setString(2, "COMPRA_ID:" + idCompra);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    series.add(resultado.getString("numero_serie"));
                }
            }
        }

        return series;
    }

    private String bloquearCompra(Connection conexion, int idCompra)
            throws SQLException {

        String sql = """
                SELECT estado
                FROM dbo.compras WITH (UPDLOCK, ROWLOCK)
                WHERE id_compra = ?;
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCompra);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("La compra seleccionada no existe.");
                }
                return resultado.getString("estado");
            }
        }
    }

    private List<DetalleCompra> listarDetallesParaAnulacion(
            Connection conexion,
            int idCompra) throws SQLException {

        String sql = """
                SELECT dc.id_producto, p.codigo, p.nombre,
                       p.maneja_numero_serie, dc.cantidad,
                       dc.costo_unitario, dc.subtotal
                FROM dbo.detalle_compra AS dc
                INNER JOIN dbo.productos AS p
                    ON p.id_producto = dc.id_producto
                WHERE dc.id_compra = ?;
                """;

        List<DetalleCompra> detalles = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCompra);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    DetalleCompra detalle = new DetalleCompra();
                    detalle.setIdProducto(resultado.getInt("id_producto"));
                    detalle.setCodigoProducto(resultado.getString("codigo"));
                    detalle.setNombreProducto(resultado.getString("nombre"));
                    detalle.setManejaNumeroSerie(
                            resultado.getBoolean("maneja_numero_serie")
                    );
                    detalle.setCantidad(resultado.getInt("cantidad"));
                    detalle.setCostoUnitario(
                            resultado.getBigDecimal("costo_unitario")
                    );
                    detalle.setSubtotal(resultado.getBigDecimal("subtotal"));
                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    private void eliminarUnidadesDeCompra(
            Connection conexion,
            int idCompra,
            DetalleCompra detalle) throws SQLException {

        String consulta = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN estado = 'DISPONIBLE' THEN 1 ELSE 0 END)
                           AS disponibles
                FROM dbo.unidades_producto WITH (UPDLOCK, ROWLOCK)
                WHERE id_producto = ?
                  AND observaciones = ?;
                """;

        String referencia = "COMPRA_ID:" + idCompra;

        try (PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
            sentencia.setInt(1, detalle.getIdProducto());
            sentencia.setString(2, referencia);

            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                int total = resultado.getInt("total");
                int disponibles = resultado.getInt("disponibles");

                if (total != detalle.getCantidad() || disponibles != total) {
                    throw new SQLException(
                            "No se puede anular la compra porque una unidad "
                            + "con serie de " + detalle.getNombreProducto()
                            + " ya fue utilizada o modificada."
                    );
                }
            }
        }

        try (PreparedStatement sentencia = conexion.prepareStatement(
                """
                DELETE FROM dbo.unidades_producto
                WHERE id_producto = ? AND observaciones = ?;
                """
        )) {
            sentencia.setInt(1, detalle.getIdProducto());
            sentencia.setString(2, referencia);
            sentencia.executeUpdate();
        }
    }

    private void actualizarStockPorAnulacion(
            Connection conexion,
            int idProducto,
            int stockNuevo) throws SQLException {

        String sql = """
                UPDATE dbo.productos
                SET stock_actual = ?,
                    estado = CASE
                        WHEN ? = 0 AND estado <> 'INACTIVO' THEN 'AGOTADO'
                        ELSE estado
                    END
                WHERE id_producto = ?;
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, stockNuevo);
            sentencia.setInt(2, stockNuevo);
            sentencia.setInt(3, idProducto);
            sentencia.executeUpdate();
        }
    }

    private void insertarMovimientoDevolucion(
            Connection conexion,
            int idUsuario,
            int idCompra,
            DetalleCompra detalle,
            int stockAnterior,
            int stockNuevo) throws SQLException {

        String sql = """
                INSERT INTO dbo.movimientos_inventario
                (
                    id_producto, id_usuario, id_compra, id_venta, id_orden,
                    tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    motivo
                )
                VALUES (?, ?, ?, NULL, NULL, 'DEVOLUCION_PROVEEDOR',
                        ?, ?, ?, ?);
                """;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, detalle.getIdProducto());
            sentencia.setInt(2, idUsuario);
            sentencia.setInt(3, idCompra);
            sentencia.setInt(4, detalle.getCantidad());
            sentencia.setInt(5, stockAnterior);
            sentencia.setInt(6, stockNuevo);
            sentencia.setString(
                    7,
                    "Reversión por anulación de compra #" + idCompra
            );
            sentencia.executeUpdate();
        }
    }

    private void establecerTextoNulo(
            PreparedStatement sentencia,
            int posicion,
            String texto) throws SQLException {

        if (texto == null || texto.trim().isEmpty()) {
            sentencia.setNull(posicion, Types.NVARCHAR);
        } else {
            sentencia.setString(posicion, texto.trim());
        }
    }
}
