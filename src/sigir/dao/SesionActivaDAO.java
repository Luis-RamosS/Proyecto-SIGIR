package sigir.dao;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import sigir.conexion.ConexionBD;

public class SesionActivaDAO {

    private static final int MINUTOS_EXPIRACION = 2;

    public String abrirSesion(int idUsuario) throws SQLException {
        String token = UUID.randomUUID().toString();
        String equipo = obtenerEquipo();
        String direccion = obtenerDireccion();

        try (Connection cn = ConexionBD.obtenerConexion()) {
            boolean autoCommit = cn.getAutoCommit();
            cn.setAutoCommit(false);

            try {
                desactivarExpiradas(cn, idUsuario);

                String consulta = """
                        SELECT TOP 1
                            id_sesion,
                            equipo,
                            direccion_ip,
                            fecha_inicio,
                            ultimo_heartbeat
                        FROM dbo.sesiones_usuario WITH (UPDLOCK, HOLDLOCK)
                        WHERE id_usuario = ?
                          AND activa = 1;
                        """;

                try (PreparedStatement ps = cn.prepareStatement(consulta)) {
                    ps.setInt(1, idUsuario);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String equipoActivo = rs.getString("equipo");
                            throw new IllegalStateException(
                                    "Este usuario ya tiene una sesión activa"
                                    + (equipoActivo == null || equipoActivo.isBlank()
                                            ? "."
                                            : " en el equipo " + equipoActivo + ".")
                                    + "\n\nCierra la sesión anterior antes de ingresar nuevamente."
                            );
                        }
                    }
                }

                String insertar = """
                        INSERT INTO dbo.sesiones_usuario
                        (
                            id_usuario,
                            token_sesion,
                            equipo,
                            direccion_ip,
                            fecha_inicio,
                            ultimo_heartbeat,
                            activa
                        )
                        VALUES (?, ?, ?, ?, SYSDATETIME(), SYSDATETIME(), 1);
                        """;

                try (PreparedStatement ps = cn.prepareStatement(insertar)) {
                    ps.setInt(1, idUsuario);
                    ps.setString(2, token);
                    ps.setString(3, equipo);
                    ps.setString(4, direccion);
                    ps.executeUpdate();
                }

                cn.commit();
                cn.setAutoCommit(autoCommit);
                return token;

            } catch (IllegalStateException | SQLException ex) {
                cn.rollback();
                cn.setAutoCommit(autoCommit);
                throw ex;
            }
        }
    }

    public boolean actualizarHeartbeat(String token) throws SQLException {
        if (token == null || token.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE dbo.sesiones_usuario
                SET ultimo_heartbeat = SYSDATETIME()
                WHERE token_sesion = ?
                  AND activa = 1;
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeUpdate() == 1;
        }
    }

    public void cerrarSesion(String token, String motivo) throws SQLException {
        if (token == null || token.isBlank()) {
            return;
        }

        String sql = """
                UPDATE dbo.sesiones_usuario
                SET
                    activa = 0,
                    fecha_fin = SYSDATETIME(),
                    motivo_cierre = ?
                WHERE token_sesion = ?
                  AND activa = 1;
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, motivo == null ? "CIERRE" : motivo);
            ps.setString(2, token);
            ps.executeUpdate();
        }
    }

    private void desactivarExpiradas(Connection cn, int idUsuario)
            throws SQLException {

        String sql = """
                UPDATE dbo.sesiones_usuario
                SET
                    activa = 0,
                    fecha_fin = SYSDATETIME(),
                    motivo_cierre = 'EXPIRADA'
                WHERE id_usuario = ?
                  AND activa = 1
                  AND ultimo_heartbeat < DATEADD(MINUTE, ?, SYSDATETIME());
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, -MINUTOS_EXPIRACION);
            ps.executeUpdate();
        }
    }

    private String obtenerEquipo() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "EQUIPO-DESCONOCIDO";
        }
    }

    private String obtenerDireccion() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return null;
        }
    }
}
