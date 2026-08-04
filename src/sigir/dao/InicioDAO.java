package sigir.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sigir.conexion.ConexionBD;
import sigir.modelo.ActividadDiariaInicio;
import sigir.modelo.DatosInicio;
import sigir.modelo.ProductoStockInicio;
import sigir.modelo.ResumenInicio;
import sigir.modelo.VentaRecienteInicio;

public class InicioDAO {

    public DatosInicio cargarDatos()
            throws SQLException {

        try (Connection conexion =
                     ConexionBD.obtenerConexion()) {

            DatosInicio datos = new DatosInicio();

            datos.setResumen(
                    obtenerResumen(conexion)
            );

            datos.setVentasRecientes(
                    listarVentasRecientes(conexion)
            );

            datos.setProductosStockBajo(
                    listarProductosStockBajo(conexion)
            );

            datos.setActividadSemanal(
                    listarActividadSemanal(conexion)
            );

            return datos;
        }
    }

    private ResumenInicio obtenerResumen(
            Connection conexion)
            throws SQLException {

        String sql = """
                SELECT
                    (
                        SELECT COUNT(*)
                        FROM dbo.ventas
                        WHERE estado = 'COMPLETADA'
                          AND CONVERT(date, fecha_venta)
                              = CONVERT(date, SYSDATETIME())
                    ) AS ventas_hoy,

                    COALESCE
                    (
                        (
                            SELECT SUM(total)
                            FROM dbo.ventas
                            WHERE estado = 'COMPLETADA'
                              AND CONVERT(date, fecha_venta)
                                  = CONVERT(date, SYSDATETIME())
                        ),
                        0
                    ) AS total_vendido_hoy,

                    (
                        SELECT COUNT(*)
                        FROM dbo.productos
                        WHERE estado <> 'INACTIVO'
                    ) AS productos_registrados,

                    (
                        SELECT COUNT(*)
                        FROM dbo.productos
                        WHERE estado <> 'INACTIVO'
                          AND stock_actual <= stock_minimo
                    ) AS productos_stock_bajo,

                    (
                        SELECT COUNT(*)
                        FROM dbo.creditos
                        WHERE estado IN ('PENDIENTE', 'VENCIDO')
                          AND saldo_pendiente > 0
                    ) AS creditos_pendientes,

                    COALESCE
                    (
                        (
                            SELECT SUM(saldo_pendiente)
                            FROM dbo.creditos
                            WHERE estado IN ('PENDIENTE', 'VENCIDO')
                              AND saldo_pendiente > 0
                        ),
                        0
                    ) AS saldo_creditos_pendientes,

                    (
                        SELECT COUNT(*)
                        FROM dbo.ordenes_servicio
                        WHERE estado NOT IN
                        (
                            'ENTREGADO',
                            'CANCELADO'
                        )
                    ) AS reparaciones_pendientes;
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            ResumenInicio resumen =
                    new ResumenInicio();

            if (resultado.next()) {
                resumen.setVentasHoy(
                        resultado.getInt(
                                "ventas_hoy"
                        )
                );

                resumen.setTotalVendidoHoy(
                        valorSeguro(
                                resultado.getBigDecimal(
                                        "total_vendido_hoy"
                                )
                        )
                );

                resumen.setProductosRegistrados(
                        resultado.getInt(
                                "productos_registrados"
                        )
                );

                resumen.setProductosStockBajo(
                        resultado.getInt(
                                "productos_stock_bajo"
                        )
                );

                resumen.setCreditosPendientes(
                        resultado.getInt(
                                "creditos_pendientes"
                        )
                );

                resumen.setSaldoCreditosPendientes(
                        valorSeguro(
                                resultado.getBigDecimal(
                                        "saldo_creditos_pendientes"
                                )
                        )
                );

                resumen.setReparacionesPendientes(
                        resultado.getInt(
                                "reparaciones_pendientes"
                        )
                );
            }

            return resumen;
        }
    }

    private List<VentaRecienteInicio>
            listarVentasRecientes(
                    Connection conexion)
                    throws SQLException {

        String sql = """
                SELECT TOP (6)
                    v.id_venta,
                    v.numero_factura,
                    c.nombre_completo AS cliente,
                    v.fecha_venta,
                    v.total,
                    v.estado
                FROM dbo.ventas AS v
                INNER JOIN dbo.clientes AS c
                    ON c.id_cliente = v.id_cliente
                ORDER BY
                    v.fecha_venta DESC,
                    v.id_venta DESC;
                """;

        List<VentaRecienteInicio> ventas =
                new ArrayList<>();

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {
                VentaRecienteInicio venta =
                        new VentaRecienteInicio();

                venta.setIdVenta(
                        resultado.getInt("id_venta")
                );

                venta.setNumeroFactura(
                        resultado.getString(
                                "numero_factura"
                        )
                );

                venta.setCliente(
                        resultado.getString("cliente")
                );

                Timestamp fecha =
                        resultado.getTimestamp(
                                "fecha_venta"
                        );

                if (fecha != null) {
                    venta.setFechaVenta(
                            fecha.toLocalDateTime()
                    );
                }

                venta.setTotal(
                        valorSeguro(
                                resultado.getBigDecimal(
                                        "total"
                                )
                        )
                );

                venta.setEstado(
                        resultado.getString("estado")
                );

                ventas.add(venta);
            }
        }

        return ventas;
    }

    private List<ProductoStockInicio>
            listarProductosStockBajo(
                    Connection conexion)
                    throws SQLException {

        String sql = """
                SELECT TOP (6)
                    p.id_producto,
                    p.codigo,
                    p.nombre,
                    p.stock_actual,
                    p.stock_minimo,
                    CASE
                        WHEN p.stock_actual = 0
                        THEN 'AGOTADO'

                        WHEN p.stock_actual
                             <= CEILING(
                                    p.stock_minimo * 0.50
                                )
                        THEN 'CRITICO'

                        ELSE 'BAJO'
                    END AS nivel
                FROM dbo.productos AS p
                WHERE p.estado <> 'INACTIVO'
                  AND p.stock_actual <= p.stock_minimo
                ORDER BY
                    CASE
                        WHEN p.stock_actual = 0 THEN 0
                        ELSE 1
                    END,
                    p.stock_actual ASC,
                    p.nombre ASC;
                """;

        List<ProductoStockInicio> productos =
                new ArrayList<>();

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {
                ProductoStockInicio producto =
                        new ProductoStockInicio();

                producto.setIdProducto(
                        resultado.getInt(
                                "id_producto"
                        )
                );

                producto.setCodigo(
                        resultado.getString("codigo")
                );

                producto.setNombre(
                        resultado.getString("nombre")
                );

                producto.setStockActual(
                        resultado.getInt(
                                "stock_actual"
                        )
                );

                producto.setStockMinimo(
                        resultado.getInt(
                                "stock_minimo"
                        )
                );

                producto.setNivel(
                        resultado.getString("nivel")
                );

                productos.add(producto);
            }
        }

        return productos;
    }

    private List<ActividadDiariaInicio>
            listarActividadSemanal(
                    Connection conexion)
                    throws SQLException {

        String sql = """
                DECLARE @desde date =
                    DATEADD
                    (
                        DAY,
                        -6,
                        CONVERT(date, SYSDATETIME())
                    );

                SELECT
                    CONVERT(date, operaciones.fecha) AS fecha,
                    COUNT_BIG(*) AS cantidad
                FROM
                (
                    SELECT fecha_venta AS fecha
                    FROM dbo.ventas
                    WHERE fecha_venta >= @desde

                    UNION ALL

                    SELECT fecha_compra
                    FROM dbo.compras
                    WHERE fecha_compra >= @desde

                    UNION ALL

                    SELECT fecha_movimiento
                    FROM dbo.movimientos_inventario
                    WHERE fecha_movimiento >= @desde

                    UNION ALL

                    SELECT fecha_abono
                    FROM dbo.abonos_credito
                    WHERE fecha_abono >= @desde

                    UNION ALL

                    SELECT fecha_evento
                    FROM dbo.historial_servicio
                    WHERE fecha_evento >= @desde
                ) AS operaciones
                GROUP BY CONVERT(date, operaciones.fecha)
                ORDER BY fecha;
                """;

        Map<LocalDate, Integer> cantidades =
                new HashMap<>();

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {
                cantidades.put(
                        resultado.getDate("fecha")
                                .toLocalDate(),
                        resultado.getInt("cantidad")
                );
            }
        }

        List<ActividadDiariaInicio> actividad =
                new ArrayList<>();

        LocalDate inicio =
                LocalDate.now().minusDays(6);

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = inicio.plusDays(i);

            actividad.add(
                    new ActividadDiariaInicio(
                            fecha,
                            cantidades.getOrDefault(
                                    fecha,
                                    0
                            )
                    )
            );
        }

        return actividad;
    }

    private BigDecimal valorSeguro(
            BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }
}
