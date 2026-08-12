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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.CajaChicaResumen;
import sigir.modelo.MovimientoCajaChica;

public class CajaChicaDAO {

    public CajaChicaResumen obtenerResumen() throws SQLException {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.with(DayOfWeek.MONDAY);
        LocalDate fin = inicio.plusDays(6);

        String sql = """
                SELECT
                    c.fondo_maximo,
                    COALESCE((
                        SELECT SUM(
                            CASE
                                WHEN m.tipo IN ('APERTURA','REPOSICION','AJUSTE_ENTRADA')
                                THEN m.monto
                                ELSE -m.monto
                            END
                        )
                        FROM dbo.caja_chica_movimientos AS m
                        WHERE m.estado = 'ACTIVO'
                    ), 0) AS saldo_disponible,
                    COALESCE((
                        SELECT SUM(m.monto)
                        FROM dbo.caja_chica_movimientos AS m
                        WHERE m.estado = 'ACTIVO'
                          AND m.tipo = 'EGRESO'
                          AND m.fecha_movimiento >= ?
                          AND m.fecha_movimiento < DATEADD(DAY, 1, ?)
                    ), 0) AS gastado_semana,
                    COALESCE((
                        SELECT COUNT(*)
                        FROM dbo.caja_chica_movimientos AS m
                        WHERE m.estado = 'ACTIVO'
                          AND m.fecha_movimiento >= ?
                          AND m.fecha_movimiento < DATEADD(DAY, 1, ?)
                    ), 0) AS movimientos_semana
                FROM dbo.caja_chica_configuracion AS c
                WHERE c.id_configuracion = 1;
                """;

        CajaChicaResumen resumen = new CajaChicaResumen();
        resumen.setInicioSemana(inicio);
        resumen.setFinSemana(fin);

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            Date desde = Date.valueOf(inicio);
            Date hasta = Date.valueOf(fin);
            ps.setDate(1, desde);
            ps.setDate(2, hasta);
            ps.setDate(3, desde);
            ps.setDate(4, hasta);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No existe la configuración de caja chica. Ejecuta el script de actualización."
                    );
                }

                BigDecimal fondo = seguro(rs.getBigDecimal("fondo_maximo"));
                BigDecimal saldo = seguro(rs.getBigDecimal("saldo_disponible"));

                resumen.setFondoMaximo(fondo);
                resumen.setSaldoDisponible(saldo);
                resumen.setGastadoSemana(seguro(rs.getBigDecimal("gastado_semana")));
                resumen.setMovimientosSemana(rs.getInt("movimientos_semana"));
                resumen.setReposicionSugerida(
                        fondo.subtract(saldo).max(BigDecimal.ZERO)
                );
            }
        }

        return resumen;
    }

    public List<MovimientoCajaChica> listarMovimientos(
            LocalDate desde,
            LocalDate hasta) throws SQLException {

        LocalDate fechaDesde = desde == null
                ? LocalDate.now().with(DayOfWeek.MONDAY)
                : desde;

        LocalDate fechaHasta = hasta == null
                ? LocalDate.now()
                : hasta;

        String sql = """
                WITH movimientos AS
                (
                    SELECT
                        m.id_movimiento,
                        m.fecha_movimiento,
                        m.tipo,
                        m.categoria,
                        m.concepto,
                        m.monto,
                        m.comprobante,
                        m.observaciones,
                        m.id_usuario,
                        u.nombre_completo AS nombre_usuario,
                        m.estado,
                        SUM(
                            CASE
                                WHEN m.estado <> 'ACTIVO' THEN 0
                                WHEN m.tipo IN ('APERTURA','REPOSICION','AJUSTE_ENTRADA')
                                THEN m.monto
                                ELSE -m.monto
                            END
                        ) OVER (
                            ORDER BY m.fecha_movimiento, m.id_movimiento
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS saldo_posterior
                    FROM dbo.caja_chica_movimientos AS m
                    INNER JOIN dbo.usuarios AS u
                        ON u.id_usuario = m.id_usuario
                )
                SELECT *
                FROM movimientos
                WHERE fecha_movimiento >= ?
                  AND fecha_movimiento < DATEADD(DAY, 1, ?)
                ORDER BY fecha_movimiento DESC, id_movimiento DESC;
                """;

        List<MovimientoCajaChica> lista = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaDesde));
            ps.setDate(2, Date.valueOf(fechaHasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    public int registrarMovimiento(
            int idUsuario,
            String tipo,
            String categoria,
            String concepto,
            BigDecimal monto,
            String comprobante,
            String observaciones) throws SQLException {

        try (Connection cn = ConexionBD.obtenerConexion()) {
            boolean auto = cn.getAutoCommit();
            cn.setAutoCommit(false);

            try {
                BigDecimal fondo = bloquearFondo(cn);
                BigDecimal saldo = obtenerSaldo(cn);
                BigDecimal nuevoSaldo;

                if ("EGRESO".equals(tipo)
                        || "AJUSTE_SALIDA".equals(tipo)) {
                    nuevoSaldo = saldo.subtract(monto);
                } else if ("AJUSTE_ENTRADA".equals(tipo)) {
                    nuevoSaldo = saldo.add(monto);
                } else {
                    throw new IllegalArgumentException(
                            "Tipo de movimiento no permitido."
                    );
                }

                if (nuevoSaldo.signum() < 0) {
                    throw new IllegalStateException(
                            "El movimiento supera el saldo disponible de caja chica."
                    );
                }

                if (nuevoSaldo.compareTo(fondo) > 0) {
                    throw new IllegalStateException(
                            "El movimiento haría que la caja chica supere su fondo máximo de L 2,500.00."
                    );
                }

                int id = insertarMovimiento(
                        cn,
                        idUsuario,
                        tipo,
                        categoria,
                        concepto,
                        monto,
                        comprobante,
                        observaciones
                );

                cn.commit();
                cn.setAutoCommit(auto);
                return id;

            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                cn.setAutoCommit(auto);
                throw ex;
            }
        }
    }

    public BigDecimal reponerHastaFondoMaximo(
            int idUsuario,
            String observaciones) throws SQLException {

        try (Connection cn = ConexionBD.obtenerConexion()) {
            boolean auto = cn.getAutoCommit();
            cn.setAutoCommit(false);

            try {
                BigDecimal fondo = bloquearFondo(cn);
                BigDecimal saldo = obtenerSaldo(cn);
                BigDecimal faltante = fondo.subtract(saldo);

                if (faltante.signum() <= 0) {
                    throw new IllegalStateException(
                            "La caja chica ya se encuentra en su fondo máximo."
                    );
                }

                String tipo = contarMovimientos(cn) == 0
                        ? "APERTURA"
                        : "REPOSICION";

                insertarMovimiento(
                        cn,
                        idUsuario,
                        tipo,
                        null,
                        "Reposición del fondo de caja chica",
                        faltante,
                        null,
                        observaciones
                );

                cn.commit();
                cn.setAutoCommit(auto);
                return faltante;

            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                cn.setAutoCommit(auto);
                throw ex;
            }
        }
    }

    public void registrarArqueo(
            int idUsuario,
            BigDecimal saldoFisico,
            String observaciones) throws SQLException {

        try (Connection cn = ConexionBD.obtenerConexion()) {
            boolean auto = cn.getAutoCommit();
            cn.setAutoCommit(false);

            try {
                bloquearFondo(cn);
                BigDecimal saldoSistema = obtenerSaldo(cn);
                BigDecimal diferencia = saldoFisico.subtract(saldoSistema);

                String sql = """
                        INSERT INTO dbo.caja_chica_arqueos
                        (
                            fecha_arqueo,
                            saldo_sistema,
                            saldo_fisico,
                            diferencia,
                            id_usuario,
                            observaciones
                        )
                        VALUES
                        (
                            SYSDATETIME(), ?, ?, ?, ?, ?
                        );
                        """;

                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setBigDecimal(1, saldoSistema);
                    ps.setBigDecimal(2, saldoFisico);
                    ps.setBigDecimal(3, diferencia);
                    ps.setInt(4, idUsuario);
                    establecerTexto(ps, 5, observaciones, Types.NVARCHAR);
                    ps.executeUpdate();
                }

                cn.commit();
                cn.setAutoCommit(auto);

            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                cn.setAutoCommit(auto);
                throw ex;
            }
        }
    }

    private BigDecimal bloquearFondo(Connection cn) throws SQLException {
        String sql = """
                SELECT fondo_maximo
                FROM dbo.caja_chica_configuracion WITH (UPDLOCK, HOLDLOCK)
                WHERE id_configuracion = 1;
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException(
                        "No existe la configuración de caja chica."
                );
            }
            return seguro(rs.getBigDecimal(1));
        }
    }

    private BigDecimal obtenerSaldo(Connection cn) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(
                    CASE
                        WHEN tipo IN ('APERTURA','REPOSICION','AJUSTE_ENTRADA')
                        THEN monto
                        ELSE -monto
                    END
                ), 0)
                FROM dbo.caja_chica_movimientos
                WHERE estado = 'ACTIVO';
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? seguro(rs.getBigDecimal(1)) : BigDecimal.ZERO;
        }
    }

    private int contarMovimientos(Connection cn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.caja_chica_movimientos;";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int insertarMovimiento(
            Connection cn,
            int idUsuario,
            String tipo,
            String categoria,
            String concepto,
            BigDecimal monto,
            String comprobante,
            String observaciones) throws SQLException {

        String sql = """
                INSERT INTO dbo.caja_chica_movimientos
                (
                    fecha_movimiento,
                    tipo,
                    categoria,
                    concepto,
                    monto,
                    comprobante,
                    observaciones,
                    id_usuario,
                    estado
                )
                VALUES
                (
                    SYSDATETIME(), ?, ?, ?, ?, ?, ?, ?, 'ACTIVO'
                );
                """;

        try (PreparedStatement ps = cn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, tipo);
            establecerTexto(ps, 2, categoria, Types.NVARCHAR);
            ps.setNString(3, concepto);
            ps.setBigDecimal(4, monto);
            establecerTexto(ps, 5, comprobante, Types.NVARCHAR);
            establecerTexto(ps, 6, observaciones, Types.NVARCHAR);
            ps.setInt(7, idUsuario);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "SQL Server no devolvió el identificador del movimiento."
        );
    }

    private MovimientoCajaChica mapear(ResultSet rs) throws SQLException {
        MovimientoCajaChica m = new MovimientoCajaChica();
        m.setIdMovimiento(rs.getInt("id_movimiento"));
        Timestamp f = rs.getTimestamp("fecha_movimiento");
        if (f != null) {
            m.setFechaMovimiento(f.toLocalDateTime());
        }
        m.setTipo(rs.getString("tipo"));
        m.setCategoria(rs.getString("categoria"));
        m.setConcepto(rs.getString("concepto"));
        m.setMonto(rs.getBigDecimal("monto"));
        m.setSaldoPosterior(rs.getBigDecimal("saldo_posterior"));
        m.setComprobante(rs.getString("comprobante"));
        m.setObservaciones(rs.getString("observaciones"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setNombreUsuario(rs.getString("nombre_usuario"));
        m.setEstado(rs.getString("estado"));
        return m;
    }

    private BigDecimal seguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void establecerTexto(
            PreparedStatement ps,
            int posicion,
            String valor,
            int tipoSql) throws SQLException {

        if (valor == null || valor.isBlank()) {
            ps.setNull(posicion, tipoSql);
        } else {
            ps.setNString(posicion, valor.trim());
        }
    }
}
