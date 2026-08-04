package sigir.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.AbonoCredito;
import sigir.modelo.Credito;

public class CreditoDAO {

    public List<Credito> listar(String filtro, String estado) throws SQLException {
        String texto = filtro == null ? "" : filtro.trim();
        String estadoFiltro = estado == null || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado)
                ? null : estado.trim().toUpperCase();

        String sql = """
                SELECT
                    cr.id_credito,
                    cr.id_venta,
                    cr.id_cliente,
                    v.numero_factura,
                    c.nombre_completo AS nombre_cliente,
                    c.numero_identidad,
                    cr.fecha_inicio,
                    cr.fecha_vencimiento,
                    cr.total_credito,
                    cr.saldo_pendiente,
                    cr.monto_cuota,
                    CASE
                        WHEN cr.estado = 'PENDIENTE'
                         AND cr.fecha_vencimiento IS NOT NULL
                         AND cr.fecha_vencimiento < CONVERT(date, GETDATE())
                         AND cr.saldo_pendiente > 0
                        THEN 'VENCIDO'
                        ELSE cr.estado
                    END AS estado_visual,
                    cr.observaciones
                FROM dbo.creditos AS cr
                INNER JOIN dbo.ventas AS v ON v.id_venta = cr.id_venta
                INNER JOIN dbo.clientes AS c ON c.id_cliente = cr.id_cliente
                WHERE
                    (
                        ? = ''
                        OR c.nombre_completo LIKE '%' + ? + '%'
                        OR c.numero_identidad LIKE '%' + ? + '%'
                        OR v.numero_factura LIKE '%' + ? + '%'
                        OR CAST(cr.id_credito AS VARCHAR(20)) LIKE '%' + ? + '%'
                    )
                    AND
                    (
                        ? IS NULL
                        OR
                        CASE
                            WHEN cr.estado = 'PENDIENTE'
                             AND cr.fecha_vencimiento IS NOT NULL
                             AND cr.fecha_vencimiento < CONVERT(date, GETDATE())
                             AND cr.saldo_pendiente > 0
                            THEN 'VENCIDO'
                            ELSE cr.estado
                        END = ?
                    )
                ORDER BY
                    CASE WHEN cr.saldo_pendiente > 0 THEN 0 ELSE 1 END,
                    cr.fecha_vencimiento,
                    cr.id_credito DESC;
                """;

        List<Credito> lista = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, texto);
            }

            if (estadoFiltro == null) {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            } else {
                ps.setString(6, estadoFiltro);
                ps.setString(7, estadoFiltro);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCredito(rs));
                }
            }
        }

        return lista;
    }

    public List<Credito> listarDisponiblesParaAbono() throws SQLException {
        String sql = """
                SELECT
                    cr.id_credito,
                    cr.id_venta,
                    cr.id_cliente,
                    v.numero_factura,
                    c.nombre_completo AS nombre_cliente,
                    c.numero_identidad,
                    cr.fecha_inicio,
                    cr.fecha_vencimiento,
                    cr.total_credito,
                    cr.saldo_pendiente,
                    cr.monto_cuota,
                    CASE
                        WHEN cr.fecha_vencimiento IS NOT NULL
                         AND cr.fecha_vencimiento < CONVERT(date, GETDATE())
                        THEN 'VENCIDO'
                        ELSE 'PENDIENTE'
                    END AS estado_visual,
                    cr.observaciones
                FROM dbo.creditos AS cr
                INNER JOIN dbo.ventas AS v ON v.id_venta = cr.id_venta
                INNER JOIN dbo.clientes AS c ON c.id_cliente = cr.id_cliente
                WHERE cr.estado = 'PENDIENTE'
                  AND cr.saldo_pendiente > 0
                ORDER BY cr.fecha_vencimiento, c.nombre_completo, cr.id_credito;
                """;

        List<Credito> lista = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCredito(rs));
            }
        }

        return lista;
    }

    public List<AbonoCredito> listarAbonos(String filtro) throws SQLException {
        String texto = filtro == null ? "" : filtro.trim();

        String sql = """
                SELECT
                    a.id_abono,
                    a.id_credito,
                    a.id_usuario,
                    u.nombre_completo AS nombre_usuario,
                    c.nombre_completo AS nombre_cliente,
                    v.numero_factura,
                    a.fecha_abono,
                    a.monto,
                    a.metodo_pago,
                    a.referencia,
                    a.observaciones
                FROM dbo.abonos_credito AS a
                INNER JOIN dbo.creditos AS cr ON cr.id_credito = a.id_credito
                INNER JOIN dbo.ventas AS v ON v.id_venta = cr.id_venta
                INNER JOIN dbo.clientes AS c ON c.id_cliente = cr.id_cliente
                INNER JOIN dbo.usuarios AS u ON u.id_usuario = a.id_usuario
                WHERE
                    ? = ''
                    OR c.nombre_completo LIKE '%' + ? + '%'
                    OR v.numero_factura LIKE '%' + ? + '%'
                    OR ISNULL(a.referencia, '') LIKE '%' + ? + '%'
                ORDER BY a.fecha_abono DESC, a.id_abono DESC;
                """;

        List<AbonoCredito> lista = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, texto);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AbonoCredito a = new AbonoCredito();
                    a.setIdAbono(rs.getInt("id_abono"));
                    a.setIdCredito(rs.getInt("id_credito"));
                    a.setIdUsuario(rs.getInt("id_usuario"));
                    a.setNombreUsuario(rs.getString("nombre_usuario"));
                    a.setNombreCliente(rs.getString("nombre_cliente"));
                    a.setNumeroFactura(rs.getString("numero_factura"));

                    Timestamp fecha = rs.getTimestamp("fecha_abono");
                    if (fecha != null) {
                        a.setFechaAbono(fecha.toLocalDateTime());
                    }

                    a.setMonto(rs.getBigDecimal("monto"));
                    a.setMetodoPago(rs.getString("metodo_pago"));
                    a.setReferencia(rs.getString("referencia"));
                    a.setObservaciones(rs.getString("observaciones"));
                    lista.add(a);
                }
            }
        }

        return lista;
    }

    public void registrarAbono(
            int idCredito,
            int idUsuario,
            BigDecimal monto,
            String metodoPago,
            String referencia,
            String observaciones) throws SQLException {

        try (Connection cn = ConexionBD.obtenerConexion();
             CallableStatement cs = cn.prepareCall(
                     "{call dbo.sp_registrar_abono_credito(?,?,?,?,?,?)}"
             )) {

            cs.setInt(1, idCredito);
            cs.setInt(2, idUsuario);
            cs.setBigDecimal(3, monto);
            cs.setString(4, metodoPago);

            if (referencia == null || referencia.isBlank()) {
                cs.setNull(5, Types.VARCHAR);
            } else {
                cs.setString(5, referencia.trim());
            }

            if (observaciones == null || observaciones.isBlank()) {
                cs.setNull(6, Types.NVARCHAR);
            } else {
                cs.setString(6, observaciones.trim());
            }

            cs.execute();
        }
    }

    public int[] contarIndicadores() throws SQLException {
        String sql = """
                SELECT
                    SUM(CASE
                        WHEN estado = 'PENDIENTE' AND saldo_pendiente > 0
                        THEN 1 ELSE 0 END) AS pendientes,
                    SUM(CASE
                        WHEN estado = 'PENDIENTE'
                         AND saldo_pendiente > 0
                         AND fecha_vencimiento IS NOT NULL
                         AND fecha_vencimiento < CONVERT(date, GETDATE())
                        THEN 1 ELSE 0 END) AS vencidos,
                    SUM(CASE
                        WHEN estado = 'PAGADO'
                        THEN 1 ELSE 0 END) AS pagados
                FROM dbo.creditos;
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return new int[]{0, 0, 0};
            }

            return new int[]{
                rs.getInt("pendientes"),
                rs.getInt("vencidos"),
                rs.getInt("pagados")
            };
        }
    }

    public int contarPendientes() throws SQLException {
        return contarIndicadores()[0];
    }

    public int contarVencidos() throws SQLException {
        return contarIndicadores()[1];
    }

    public int contarPagados() throws SQLException {
        return contarIndicadores()[2];
    }

    private Credito mapearCredito(ResultSet rs) throws SQLException {
        Credito c = new Credito();
        c.setIdCredito(rs.getInt("id_credito"));
        c.setIdVenta(rs.getInt("id_venta"));
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNumeroFactura(rs.getString("numero_factura"));
        c.setNombreCliente(rs.getString("nombre_cliente"));
        c.setNumeroIdentidad(rs.getString("numero_identidad"));

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            c.setFechaInicio(fechaInicio.toLocalDate());
        }

        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        if (fechaVencimiento != null) {
            c.setFechaVencimiento(fechaVencimiento.toLocalDate());
        }

        c.setTotalCredito(rs.getBigDecimal("total_credito"));
        c.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
        c.setMontoCuota(rs.getBigDecimal("monto_cuota"));
        c.setEstado(rs.getString("estado_visual"));
        c.setObservaciones(rs.getString("observaciones"));
        return c;
    }
}
