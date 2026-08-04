package sigir.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import sigir.conexion.ConexionBD;
import sigir.modelo.*;

public class ReporteDAO {

    public List<UsuarioFiltro> listarUsuarios() throws SQLException {
        String sql = """
                SELECT u.id_usuario, u.nombre_completo,
                       u.nombre_usuario, r.nombre AS rol
                FROM dbo.usuarios u
                INNER JOIN dbo.roles r ON r.id_rol = u.id_rol
                WHERE u.estado = 'ACTIVO'
                ORDER BY u.nombre_completo;
                """;

        List<UsuarioFiltro> lista = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioFiltro u = new UsuarioFiltro();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setRol(rs.getString("rol"));
                lista.add(u);
            }
        }

        return lista;
    }

    public ResumenReportes obtenerResumen(
            LocalDate desde,
            LocalDate hasta) throws SQLException {

        String sql = """
                SELECT
                    COALESCE((
                        SELECT SUM(total)
                        FROM dbo.ventas
                        WHERE estado = 'COMPLETADA'
                          AND fecha_venta >= ?
                          AND fecha_venta < DATEADD(DAY, 1, ?)
                    ), 0) AS ventas_periodo,
                    COALESCE((
                        SELECT SUM(total)
                        FROM dbo.compras
                        WHERE estado = 'REGISTRADA'
                          AND fecha_compra >= ?
                          AND fecha_compra < DATEADD(DAY, 1, ?)
                    ), 0) AS compras_periodo,
                    (
                        SELECT COUNT(*)
                        FROM dbo.productos
                        WHERE estado <> 'INACTIVO'
                          AND stock_actual <= stock_minimo
                    ) AS productos_stock_bajo,
                    (
                        SELECT COUNT(*)
                        FROM dbo.ordenes_servicio
                        WHERE estado NOT IN ('ENTREGADO', 'CANCELADO')
                    ) AS reparaciones_pendientes;
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            fecha(ps, 1, desde);
            fecha(ps, 2, hasta);
            fecha(ps, 3, desde);
            fecha(ps, 4, hasta);

            try (ResultSet rs = ps.executeQuery()) {
                ResumenReportes r = new ResumenReportes();

                if (rs.next()) {
                    r.setVentasPeriodo(rs.getBigDecimal("ventas_periodo"));
                    r.setComprasPeriodo(rs.getBigDecimal("compras_periodo"));
                    r.setProductosStockBajo(
                            rs.getInt("productos_stock_bajo")
                    );
                    r.setReparacionesPendientes(
                            rs.getInt("reparaciones_pendientes")
                    );
                }

                return r;
            }
        }
    }

    public ReporteResultado consultar(
            TipoReporte tipo,
            LocalDate desde,
            LocalDate hasta,
            String estado,
            Integer idUsuario) throws SQLException {

        return switch (tipo) {
            case VENTAS ->
                ventas(desde, hasta, estado, idUsuario);
            case COMPRAS ->
                compras(desde, hasta, estado, idUsuario);
            case INVENTARIO ->
                inventario();
            case STOCK_BAJO ->
                stockBajo();
            case PRODUCTOS_MAS_VENDIDOS ->
                masVendidos(desde, hasta, idUsuario);
            case MOVIMIENTOS ->
                movimientos(desde, hasta, estado, idUsuario);
            case CREDITOS ->
                creditos(desde, hasta, estado);
            case REPARACIONES ->
                reparaciones(desde, hasta, estado, idUsuario);
            case ACTIVIDAD_USUARIOS ->
                actividad(desde, hasta, idUsuario);
        };
    }

    private ReporteResultado ventas(
            LocalDate desde,
            LocalDate hasta,
            String estado,
            Integer idUsuario) throws SQLException {

        String sql = """
                SELECT v.fecha_venta, v.numero_factura,
                       c.nombre_completo AS cliente,
                       u.nombre_completo AS usuario,
                       v.tipo_venta, v.metodo_pago,
                       v.subtotal, v.descuento, v.total, v.estado
                FROM dbo.ventas v
                INNER JOIN dbo.clientes c
                    ON c.id_cliente = v.id_cliente
                INNER JOIN dbo.usuarios u
                    ON u.id_usuario = v.id_usuario
                WHERE v.fecha_venta >= ?
                  AND v.fecha_venta < DATEADD(DAY, 1, ?)
                  AND (? IS NULL OR v.estado = ?)
                  AND (? IS NULL OR v.id_usuario = ?)
                ORDER BY v.fecha_venta DESC, v.id_venta DESC;
                """;

        ReporteResultado r = nuevo(
                "Ventas registradas",
                "Detalle de ventas realizadas en el período seleccionado.",
                List.of("Fecha", "Factura", "Cliente", "Usuario",
                        "Tipo", "Método", "Subtotal", "Descuento",
                        "Total", "Estado"),
                "Total vendido",
                true
        );

        Map<LocalDate, BigDecimal> barras = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            filtros(ps, desde, hasta, estado, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_venta");
                    LocalDateTime momento =
                            ts == null ? null : ts.toLocalDateTime();

                    BigDecimal valor = seguro(rs.getBigDecimal("total"));
                    String estadoVenta = rs.getString("estado");

                    r.agregarFila(
                            momento,
                            rs.getString("numero_factura"),
                            rs.getString("cliente"),
                            rs.getString("usuario"),
                            rs.getString("tipo_venta"),
                            rs.getString("metodo_pago"),
                            rs.getBigDecimal("subtotal"),
                            rs.getBigDecimal("descuento"),
                            valor,
                            estadoVenta
                    );

                    if ("COMPLETADA".equalsIgnoreCase(estadoVenta)) {
                        total = total.add(valor);

                        if (momento != null) {
                            barras.merge(
                                    momento.toLocalDate(),
                                    valor,
                                    BigDecimal::add
                            );
                        }
                    }
                }
            }
        }

        r.setValorResumen(total);
        r.setDatosGrafico(graficoFechas(barras));
        return r;
    }

    private ReporteResultado compras(
            LocalDate desde,
            LocalDate hasta,
            String estado,
            Integer idUsuario) throws SQLException {

        String sql = """
                SELECT c.fecha_compra, c.numero_documento,
                       p.nombre_proveedor,
                       u.nombre_completo AS usuario,
                       c.tipo_pago, c.subtotal, c.total, c.estado
                FROM dbo.compras c
                INNER JOIN dbo.proveedores p
                    ON p.id_proveedor = c.id_proveedor
                INNER JOIN dbo.usuarios u
                    ON u.id_usuario = c.id_usuario
                WHERE c.fecha_compra >= ?
                  AND c.fecha_compra < DATEADD(DAY, 1, ?)
                  AND (? IS NULL OR c.estado = ?)
                  AND (? IS NULL OR c.id_usuario = ?)
                ORDER BY c.fecha_compra DESC, c.id_compra DESC;
                """;

        ReporteResultado r = nuevo(
                "Compras registradas",
                "Compras efectuadas a proveedores en el período.",
                List.of("Fecha", "Documento", "Proveedor", "Usuario",
                        "Tipo de pago", "Subtotal", "Total", "Estado"),
                "Total comprado",
                true
        );

        Map<LocalDate, BigDecimal> barras = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            filtros(ps, desde, hasta, estado, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_compra");
                    LocalDateTime momento =
                            ts == null ? null : ts.toLocalDateTime();

                    BigDecimal valor = seguro(rs.getBigDecimal("total"));
                    String estadoCompra = rs.getString("estado");
                    String documento = rs.getString("numero_documento");

                    r.agregarFila(
                            momento,
                            documento == null || documento.isBlank()
                                    ? "Sin documento"
                                    : documento,
                            rs.getString("nombre_proveedor"),
                            rs.getString("usuario"),
                            rs.getString("tipo_pago"),
                            rs.getBigDecimal("subtotal"),
                            valor,
                            estadoCompra
                    );

                    if ("REGISTRADA".equalsIgnoreCase(estadoCompra)) {
                        total = total.add(valor);

                        if (momento != null) {
                            barras.merge(
                                    momento.toLocalDate(),
                                    valor,
                                    BigDecimal::add
                            );
                        }
                    }
                }
            }
        }

        r.setValorResumen(total);
        r.setDatosGrafico(graficoFechas(barras));
        return r;
    }

    private ReporteResultado inventario() throws SQLException {
        String sql = """
                SELECT p.codigo, p.nombre,
                       c.nombre AS categoria,
                       p.marca, p.modelo,
                       p.stock_actual, p.stock_minimo,
                       p.precio_compra, p.precio_venta,
                       CAST(p.stock_actual * p.precio_compra
                            AS DECIMAL(14,2)) AS valor_inventario,
                       p.estado
                FROM dbo.productos p
                INNER JOIN dbo.categorias_producto c
                    ON c.id_categoria = p.id_categoria
                ORDER BY valor_inventario DESC, p.nombre;
                """;

        ReporteResultado r = nuevo(
                "Inventario general",
                "Existencias, precios y valor actual de los productos.",
                List.of("Código", "Producto", "Categoría", "Marca",
                        "Modelo", "Stock", "Mínimo", "Costo",
                        "Precio venta", "Valor inventario", "Estado"),
                "Valor del inventario",
                true
        );

        List<DatoGrafico> barras = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BigDecimal valor =
                        seguro(rs.getBigDecimal("valor_inventario"));

                r.agregarFila(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("stock_actual"),
                        rs.getInt("stock_minimo"),
                        rs.getBigDecimal("precio_compra"),
                        rs.getBigDecimal("precio_venta"),
                        valor,
                        rs.getString("estado")
                );

                total = total.add(valor);

                if (barras.size() < 10) {
                    barras.add(new DatoGrafico(
                            rs.getString("nombre"),
                            valor
                    ));
                }
            }
        }

        r.setValorResumen(total);
        r.setDatosGrafico(barras);
        return r;
    }

    private ReporteResultado stockBajo() throws SQLException {
        String sql = """
                SELECT p.codigo, p.nombre,
                       c.nombre AS categoria,
                       p.stock_actual, p.stock_minimo,
                       p.stock_minimo - p.stock_actual AS faltante,
                       p.estado
                FROM dbo.productos p
                INNER JOIN dbo.categorias_producto c
                    ON c.id_categoria = p.id_categoria
                WHERE p.estado <> 'INACTIVO'
                  AND p.stock_actual <= p.stock_minimo
                ORDER BY faltante DESC, p.stock_actual, p.nombre;
                """;

        ReporteResultado r = nuevo(
                "Productos con stock bajo",
                "Productos agotados o con existencias menores al mínimo.",
                List.of("Código", "Producto", "Categoría", "Stock actual",
                        "Stock mínimo", "Faltante", "Estado"),
                "Productos afectados",
                false
        );

        List<DatoGrafico> barras = new ArrayList<>();
        int cantidad = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int faltante = Math.max(0, rs.getInt("faltante"));

                r.agregarFila(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("stock_actual"),
                        rs.getInt("stock_minimo"),
                        faltante,
                        rs.getString("estado")
                );

                cantidad++;

                if (barras.size() < 10) {
                    barras.add(new DatoGrafico(
                            rs.getString("nombre"),
                            BigDecimal.valueOf(faltante)
                    ));
                }
            }
        }

        r.setValorResumen(BigDecimal.valueOf(cantidad));
        r.setDatosGrafico(barras);
        return r;
    }

    private ReporteResultado masVendidos(
            LocalDate desde,
            LocalDate hasta,
            Integer idUsuario) throws SQLException {

        String sql = """
                SELECT p.codigo, p.nombre,
                       c.nombre AS categoria,
                       SUM(d.cantidad) AS unidades_vendidas,
                       SUM(d.subtotal) AS total_vendido,
                       AVG(d.precio_unitario) AS precio_promedio
                FROM dbo.detalle_venta d
                INNER JOIN dbo.ventas v ON v.id_venta = d.id_venta
                INNER JOIN dbo.productos p ON p.id_producto = d.id_producto
                INNER JOIN dbo.categorias_producto c
                    ON c.id_categoria = p.id_categoria
                WHERE v.estado = 'COMPLETADA'
                  AND v.fecha_venta >= ?
                  AND v.fecha_venta < DATEADD(DAY, 1, ?)
                  AND (? IS NULL OR v.id_usuario = ?)
                GROUP BY p.codigo, p.nombre, c.nombre
                ORDER BY unidades_vendidas DESC,
                         total_vendido DESC, p.nombre;
                """;

        ReporteResultado r = nuevo(
                "Productos más vendidos",
                "Productos ordenados por unidades vendidas.",
                List.of("Código", "Producto", "Categoría",
                        "Unidades vendidas", "Total vendido",
                        "Precio promedio"),
                "Unidades vendidas",
                false
        );

        List<DatoGrafico> barras = new ArrayList<>();
        long unidadesTotales = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            fecha(ps, 1, desde);
            fecha(ps, 2, hasta);
            usuario(ps, 3, 4, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int unidades = rs.getInt("unidades_vendidas");

                    r.agregarFila(
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            unidades,
                            rs.getBigDecimal("total_vendido"),
                            rs.getBigDecimal("precio_promedio")
                    );

                    unidadesTotales += unidades;

                    if (barras.size() < 10) {
                        barras.add(new DatoGrafico(
                                rs.getString("nombre"),
                                BigDecimal.valueOf(unidades)
                        ));
                    }
                }
            }
        }

        r.setValorResumen(BigDecimal.valueOf(unidadesTotales));
        r.setDatosGrafico(barras);
        return r;
    }

    private ReporteResultado movimientos(
            LocalDate desde,
            LocalDate hasta,
            String tipoMovimiento,
            Integer idUsuario) throws SQLException {

        String sql = """
                SELECT m.fecha_movimiento, p.codigo,
                       p.nombre AS producto,
                       m.tipo_movimiento, m.cantidad,
                       m.stock_anterior, m.stock_nuevo,
                       u.nombre_completo AS usuario,
                       CASE
                           WHEN m.id_compra IS NOT NULL
                               THEN 'Compra #' + CAST(m.id_compra AS VARCHAR(20))
                           WHEN m.id_venta IS NOT NULL
                               THEN 'Venta #' + CAST(m.id_venta AS VARCHAR(20))
                           WHEN m.id_orden IS NOT NULL
                               THEN 'Orden #' + CAST(m.id_orden AS VARCHAR(20))
                           ELSE 'Ajuste manual'
                       END AS origen,
                       m.motivo
                FROM dbo.movimientos_inventario m
                INNER JOIN dbo.productos p
                    ON p.id_producto = m.id_producto
                INNER JOIN dbo.usuarios u
                    ON u.id_usuario = m.id_usuario
                WHERE m.fecha_movimiento >= ?
                  AND m.fecha_movimiento < DATEADD(DAY, 1, ?)
                  AND (? IS NULL OR m.tipo_movimiento = ?)
                  AND (? IS NULL OR m.id_usuario = ?)
                ORDER BY m.fecha_movimiento DESC,
                         m.id_movimiento DESC;
                """;

        ReporteResultado r = nuevo(
                "Movimientos de inventario",
                "Entradas, salidas y ajustes realizados en el período.",
                List.of("Fecha", "Código", "Producto", "Movimiento",
                        "Cantidad", "Stock anterior", "Stock nuevo",
                        "Usuario", "Origen", "Motivo"),
                "Unidades movilizadas",
                false
        );

        Map<String, BigDecimal> barras = new HashMap<>();
        long unidades = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            filtros(
                    ps,
                    desde,
                    hasta,
                    tipoMovimiento,
                    idUsuario
            );

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_movimiento");
                    int cantidad = rs.getInt("cantidad");
                    String tipo = rs.getString("tipo_movimiento");

                    r.agregarFila(
                            ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("codigo"),
                            rs.getString("producto"),
                            tipo,
                            cantidad,
                            rs.getInt("stock_anterior"),
                            rs.getInt("stock_nuevo"),
                            rs.getString("usuario"),
                            rs.getString("origen"),
                            rs.getString("motivo")
                    );

                    unidades += cantidad;
                    barras.merge(
                            tipo,
                            BigDecimal.valueOf(cantidad),
                            BigDecimal::add
                    );
                }
            }
        }

        r.setValorResumen(BigDecimal.valueOf(unidades));
        r.setDatosGrafico(graficoTexto(barras));
        return r;
    }

    private ReporteResultado creditos(
            LocalDate desde,
            LocalDate hasta,
            String estado) throws SQLException {

        String sql = """
                SELECT cr.id_credito, v.numero_factura,
                       c.nombre_completo AS cliente,
                       c.numero_identidad,
                       cr.fecha_inicio, cr.fecha_vencimiento,
                       cr.total_credito, cr.saldo_pendiente,
                       cr.monto_cuota,
                       CASE
                           WHEN cr.estado = 'PENDIENTE'
                            AND cr.fecha_vencimiento IS NOT NULL
                            AND cr.fecha_vencimiento < CAST(GETDATE() AS DATE)
                            AND cr.saldo_pendiente > 0
                           THEN 'VENCIDO'
                           ELSE cr.estado
                       END AS estado_visual
                FROM dbo.creditos cr
                INNER JOIN dbo.ventas v ON v.id_venta = cr.id_venta
                INNER JOIN dbo.clientes c
                    ON c.id_cliente = cr.id_cliente
                WHERE cr.fecha_inicio >= ?
                  AND cr.fecha_inicio <= ?
                  AND (
                      ? IS NULL
                      OR CASE
                           WHEN cr.estado = 'PENDIENTE'
                            AND cr.fecha_vencimiento IS NOT NULL
                            AND cr.fecha_vencimiento < CAST(GETDATE() AS DATE)
                            AND cr.saldo_pendiente > 0
                           THEN 'VENCIDO'
                           ELSE cr.estado
                         END = ?
                  )
                ORDER BY cr.saldo_pendiente DESC,
                         cr.fecha_vencimiento,
                         cr.id_credito DESC;
                """;

        ReporteResultado r = nuevo(
                "Créditos",
                "Estado de los créditos otorgados a clientes.",
                List.of("Crédito", "Factura", "Cliente", "Identidad",
                        "Inicio", "Vencimiento", "Total crédito",
                        "Saldo pendiente", "Cuota", "Estado"),
                "Saldo pendiente",
                true
        );

        List<DatoGrafico> barras = new ArrayList<>();
        BigDecimal saldoTotal = BigDecimal.ZERO;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            fecha(ps, 1, desde);
            fecha(ps, 2, hasta);
            estado(ps, 3, 4, estado);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal saldo =
                            seguro(rs.getBigDecimal("saldo_pendiente"));

                    r.agregarFila(
                            "CR-" + String.format(
                                    "%05d",
                                    rs.getInt("id_credito")
                            ),
                            rs.getString("numero_factura"),
                            rs.getString("cliente"),
                            rs.getString("numero_identidad"),
                            local(rs.getDate("fecha_inicio")),
                            local(rs.getDate("fecha_vencimiento")),
                            rs.getBigDecimal("total_credito"),
                            saldo,
                            rs.getBigDecimal("monto_cuota"),
                            rs.getString("estado_visual")
                    );

                    saldoTotal = saldoTotal.add(saldo);

                    if (barras.size() < 10 && saldo.signum() > 0) {
                        barras.add(new DatoGrafico(
                                rs.getString("cliente"),
                                saldo
                        ));
                    }
                }
            }
        }

        r.setValorResumen(saldoTotal);
        r.setDatosGrafico(barras);
        return r;
    }

    private ReporteResultado reparaciones(
            LocalDate desde,
            LocalDate hasta,
            String estado,
            Integer idUsuario) throws SQLException {

        String sql = """
                SELECT o.numero_orden, o.fecha_recepcion,
                       c.nombre_completo AS cliente,
                       e.tipo_equipo, e.marca, e.modelo,
                       e.numero_serie, o.estado,
                       o.costo_estimado, o.costo_final,
                       o.fecha_prometida, o.fecha_entrega,
                       u.nombre_completo AS usuario
                FROM dbo.ordenes_servicio o
                INNER JOIN dbo.equipos_cliente e
                    ON e.id_equipo = o.id_equipo
                INNER JOIN dbo.clientes c
                    ON c.id_cliente = e.id_cliente
                INNER JOIN dbo.usuarios u
                    ON u.id_usuario = o.id_usuario_recibe
                WHERE o.fecha_recepcion >= ?
                  AND o.fecha_recepcion < DATEADD(DAY, 1, ?)
                  AND (? IS NULL OR o.estado = ?)
                  AND (? IS NULL OR o.id_usuario_recibe = ?)
                ORDER BY
                    CASE WHEN o.estado IN ('ENTREGADO', 'CANCELADO')
                         THEN 1 ELSE 0 END,
                    o.fecha_recepcion DESC;
                """;

        ReporteResultado r = nuevo(
                "Reparaciones",
                "Órdenes de servicio recibidas en el período.",
                List.of("Orden", "Recepción", "Cliente", "Equipo",
                        "Marca", "Modelo", "Serie", "Estado",
                        "Estimado", "Costo final", "Fecha prometida",
                        "Entrega", "Recibido por"),
                "Órdenes encontradas",
                false
        );

        Map<String, BigDecimal> barras = new HashMap<>();
        int cantidad = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            filtros(ps, desde, hasta, estado, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp recepcion =
                            rs.getTimestamp("fecha_recepcion");
                    Timestamp entrega =
                            rs.getTimestamp("fecha_entrega");
                    String estadoOrden = rs.getString("estado");

                    r.agregarFila(
                            rs.getString("numero_orden"),
                            recepcion == null
                                    ? null
                                    : recepcion.toLocalDateTime(),
                            rs.getString("cliente"),
                            rs.getString("tipo_equipo"),
                            rs.getString("marca"),
                            rs.getString("modelo"),
                            rs.getString("numero_serie"),
                            estadoOrden,
                            rs.getBigDecimal("costo_estimado"),
                            rs.getBigDecimal("costo_final"),
                            local(rs.getDate("fecha_prometida")),
                            entrega == null
                                    ? null
                                    : entrega.toLocalDateTime(),
                            rs.getString("usuario")
                    );

                    cantidad++;
                    barras.merge(
                            estadoOrden,
                            BigDecimal.ONE,
                            BigDecimal::add
                    );
                }
            }
        }

        r.setValorResumen(BigDecimal.valueOf(cantidad));
        r.setDatosGrafico(graficoTexto(barras));
        return r;
    }

    private ReporteResultado actividad(
            LocalDate desde,
            LocalDate hasta,
            Integer idUsuario) throws SQLException {

        String sql = """
                WITH vu AS (
                    SELECT id_usuario, COUNT(*) AS ventas,
                           COALESCE(SUM(total), 0) AS total_ventas
                    FROM dbo.ventas
                    WHERE fecha_venta >= ?
                      AND fecha_venta < DATEADD(DAY, 1, ?)
                    GROUP BY id_usuario
                ),
                cu AS (
                    SELECT id_usuario, COUNT(*) AS compras
                    FROM dbo.compras
                    WHERE fecha_compra >= ?
                      AND fecha_compra < DATEADD(DAY, 1, ?)
                    GROUP BY id_usuario
                ),
                mu AS (
                    SELECT id_usuario, COUNT(*) AS movimientos
                    FROM dbo.movimientos_inventario
                    WHERE fecha_movimiento >= ?
                      AND fecha_movimiento < DATEADD(DAY, 1, ?)
                    GROUP BY id_usuario
                ),
                su AS (
                    SELECT id_usuario, COUNT(*) AS eventos_servicio
                    FROM dbo.historial_servicio
                    WHERE fecha_evento >= ?
                      AND fecha_evento < DATEADD(DAY, 1, ?)
                    GROUP BY id_usuario
                )
                SELECT u.id_usuario, u.nombre_completo,
                       u.nombre_usuario, r.nombre AS rol,
                       COALESCE(vu.ventas, 0) AS ventas,
                       COALESCE(vu.total_ventas, 0) AS total_ventas,
                       COALESCE(cu.compras, 0) AS compras,
                       COALESCE(mu.movimientos, 0) AS movimientos,
                       COALESCE(su.eventos_servicio, 0)
                           AS eventos_servicio,
                       COALESCE(vu.ventas, 0)
                         + COALESCE(cu.compras, 0)
                         + COALESCE(mu.movimientos, 0)
                         + COALESCE(su.eventos_servicio, 0)
                           AS operaciones,
                       u.ultimo_acceso, u.estado
                FROM dbo.usuarios u
                INNER JOIN dbo.roles r ON r.id_rol = u.id_rol
                LEFT JOIN vu ON vu.id_usuario = u.id_usuario
                LEFT JOIN cu ON cu.id_usuario = u.id_usuario
                LEFT JOIN mu ON mu.id_usuario = u.id_usuario
                LEFT JOIN su ON su.id_usuario = u.id_usuario
                WHERE (? IS NULL OR u.id_usuario = ?)
                ORDER BY operaciones DESC, u.nombre_completo;
                """;

        ReporteResultado r = nuevo(
                "Actividad por usuario",
                "Resumen de operaciones realizadas por cada usuario.",
                List.of("Usuario", "Nombre de acceso", "Rol", "Ventas",
                        "Total vendido", "Compras", "Movimientos",
                        "Eventos servicio", "Operaciones",
                        "Último acceso", "Estado"),
                "Operaciones registradas",
                false
        );

        List<DatoGrafico> barras = new ArrayList<>();
        long totalOperaciones = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            fecha(ps, 1, desde);
            fecha(ps, 2, hasta);
            fecha(ps, 3, desde);
            fecha(ps, 4, hasta);
            fecha(ps, 5, desde);
            fecha(ps, 6, hasta);
            fecha(ps, 7, desde);
            fecha(ps, 8, hasta);
            usuario(ps, 9, 10, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int operaciones = rs.getInt("operaciones");
                    Timestamp ultimo = rs.getTimestamp("ultimo_acceso");
                    String rol = rs.getString("rol");

                    r.agregarFila(
                            rs.getString("nombre_completo"),
                            rs.getString("nombre_usuario"),
                            "DUENO".equalsIgnoreCase(rol)
                                    ? "DUEÑO"
                                    : rol,
                            rs.getInt("ventas"),
                            rs.getBigDecimal("total_ventas"),
                            rs.getInt("compras"),
                            rs.getInt("movimientos"),
                            rs.getInt("eventos_servicio"),
                            operaciones,
                            ultimo == null
                                    ? null
                                    : ultimo.toLocalDateTime(),
                            rs.getString("estado")
                    );

                    totalOperaciones += operaciones;

                    if (barras.size() < 10) {
                        barras.add(new DatoGrafico(
                                rs.getString("nombre_completo"),
                                BigDecimal.valueOf(operaciones)
                        ));
                    }
                }
            }
        }

        r.setValorResumen(BigDecimal.valueOf(totalOperaciones));
        r.setDatosGrafico(barras);
        return r;
    }

    private ReporteResultado nuevo(
            String titulo,
            String descripcion,
            List<String> columnas,
            String etiqueta,
            boolean monetario) {

        ReporteResultado r = new ReporteResultado();
        r.setTitulo(titulo);
        r.setDescripcion(descripcion);
        r.setColumnas(columnas);
        r.setEtiquetaResumen(etiqueta);
        r.setResumenMonetario(monetario);
        return r;
    }

    private void filtros(
            PreparedStatement ps,
            LocalDate desde,
            LocalDate hasta,
            String estado,
            Integer idUsuario) throws SQLException {

        fecha(ps, 1, desde);
        fecha(ps, 2, hasta);
        estado(ps, 3, 4, estado);
        usuario(ps, 5, 6, idUsuario);
    }

    private void estado(
            PreparedStatement ps,
            int p1,
            int p2,
            String valor) throws SQLException {

        String filtro = valor == null
                || valor.isBlank()
                || "TODOS".equalsIgnoreCase(valor)
                        ? null
                        : valor.trim();

        if (filtro == null) {
            ps.setNull(p1, Types.VARCHAR);
            ps.setNull(p2, Types.VARCHAR);
        } else {
            ps.setString(p1, filtro);
            ps.setString(p2, filtro);
        }
    }

    private void usuario(
            PreparedStatement ps,
            int p1,
            int p2,
            Integer valor) throws SQLException {

        if (valor == null || valor <= 0) {
            ps.setNull(p1, Types.INTEGER);
            ps.setNull(p2, Types.INTEGER);
        } else {
            ps.setInt(p1, valor);
            ps.setInt(p2, valor);
        }
    }

    private void fecha(
            PreparedStatement ps,
            int posicion,
            LocalDate valor) throws SQLException {

        ps.setDate(posicion, java.sql.Date.valueOf(valor));
    }

    private BigDecimal seguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private LocalDate local(java.sql.Date valor) {
        return valor == null ? null : valor.toLocalDate();
    }

    private List<DatoGrafico> graficoFechas(
            Map<LocalDate, BigDecimal> mapa) {

        List<DatoGrafico> datos = new ArrayList<>();

        mapa.forEach((fecha, valor) ->
                datos.add(new DatoGrafico(
                        fecha.format(
                                java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM")
                        ),
                        valor
                ))
        );

        if (datos.size() <= 12) return datos;

        return new ArrayList<>(
                datos.subList(
                        datos.size() - 12,
                        datos.size()
                )
        );
    }

    private List<DatoGrafico> graficoTexto(
            Map<String, BigDecimal> mapa) {

        List<DatoGrafico> datos = new ArrayList<>();

        mapa.entrySet().stream()
                .sorted(
                        Map.Entry
                                .<String, BigDecimal>
                                comparingByValue()
                                .reversed()
                )
                .limit(12)
                .forEach(e -> datos.add(
                        new DatoGrafico(
                                e.getKey(),
                                e.getValue()
                        )
                ));

        return datos;
    }
}
