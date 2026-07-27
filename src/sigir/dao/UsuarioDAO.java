package sigir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import sigir.conexion.ConexionBD;
import sigir.modelo.Usuario;
import sigir.util.PasswordUtil;

public class UsuarioDAO {

    private static final int MAXIMO_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    public Usuario iniciarSesion(
            String nombreUsuario,
            char[] contrasena) throws SQLException {

        String usuarioNormalizado = nombreUsuario == null
                ? ""
                : nombreUsuario.trim();

        if (usuarioNormalizado.isBlank()
                || contrasena == null
                || contrasena.length == 0) {

            return null;
        }

        String sql = """
                SELECT
                    u.id_usuario,
                    u.id_rol,
                    u.nombre_completo,
                    u.nombre_usuario,
                    u.correo,
                    u.contrasena_hash,
                    u.telefono,
                    u.estado,
                    u.intentos_fallidos,
                    u.bloqueado_hasta,
                    u.ultimo_acceso,
                    r.nombre AS nombre_rol
                FROM dbo.usuarios AS u
                INNER JOIN dbo.roles AS r
                    ON r.id_rol = u.id_rol
                WHERE u.nombre_usuario = ?
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();

             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, usuarioNormalizado);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                if (!resultado.next()) {
                    return null;
                }

                int idUsuario =
                        resultado.getInt("id_usuario");

                String estado =
                        resultado.getString("estado");

                int intentosFallidos =
                        resultado.getInt("intentos_fallidos");

                Timestamp bloqueoTimestamp =
                        resultado.getTimestamp("bloqueado_hasta");

                LocalDateTime bloqueadoHasta =
                        bloqueoTimestamp == null
                                ? null
                                : bloqueoTimestamp.toLocalDateTime();

                comprobarDisponibilidad(
                        conexion,
                        idUsuario,
                        estado,
                        bloqueadoHasta
                );

                String hashGuardado =
                        resultado.getString("contrasena_hash");

                boolean claveCorrecta =
                        PasswordUtil.verificar(
                                contrasena,
                                hashGuardado
                        );

                if (!claveCorrecta) {
                    registrarIntentoFallido(
                            conexion,
                            idUsuario,
                            intentosFallidos + 1
                    );

                    return null;
                }

                registrarAccesoExitoso(
                        conexion,
                        idUsuario
                );

                Timestamp ultimoAccesoTimestamp =
                        resultado.getTimestamp("ultimo_acceso");

                LocalDateTime ultimoAcceso =
                        ultimoAccesoTimestamp == null
                                ? null
                                : ultimoAccesoTimestamp
                                        .toLocalDateTime();

                return new Usuario(
                        idUsuario,
                        resultado.getInt("id_rol"),
                        resultado.getString(
                                "nombre_completo"
                        ),
                        resultado.getString(
                                "nombre_usuario"
                        ),
                        resultado.getString("correo"),
                        resultado.getString("telefono"),
                        "ACTIVO",
                        resultado.getString("nombre_rol"),
                        ultimoAcceso
                );
            }
        }
    }

    private void comprobarDisponibilidad(
            Connection conexion,
            int idUsuario,
            String estado,
            LocalDateTime bloqueadoHasta)
            throws SQLException {

        if ("INACTIVO".equalsIgnoreCase(estado)) {
            throw new IllegalStateException(
                    "La cuenta se encuentra inactiva."
            );
        }

        if (bloqueadoHasta != null) {

            if (bloqueadoHasta.isAfter(
                    LocalDateTime.now())) {

                throw new IllegalStateException(
                        "La cuenta está bloqueada hasta "
                        + bloqueadoHasta
                                .withNano(0)
                                .toString()
                                .replace("T", " ")
                );
            }

            desbloquearCuenta(
                    conexion,
                    idUsuario
            );
        }
    }

    private void registrarIntentoFallido(
            Connection conexion,
            int idUsuario,
            int nuevosIntentos) throws SQLException {

        if (nuevosIntentos >= MAXIMO_INTENTOS) {

            String sql = """
                    UPDATE dbo.usuarios
                    SET
                        intentos_fallidos = ?,
                        estado = 'BLOQUEADO',
                        bloqueado_hasta =
                            DATEADD(MINUTE, ?, SYSDATETIME())
                    WHERE id_usuario = ?
                    """;

            try (PreparedStatement sentencia =
                         conexion.prepareStatement(sql)) {

                sentencia.setInt(1, nuevosIntentos);
                sentencia.setInt(2, MINUTOS_BLOQUEO);
                sentencia.setInt(3, idUsuario);
                sentencia.executeUpdate();
            }

        } else {

            String sql = """
                    UPDATE dbo.usuarios
                    SET intentos_fallidos = ?
                    WHERE id_usuario = ?
                    """;

            try (PreparedStatement sentencia =
                         conexion.prepareStatement(sql)) {

                sentencia.setInt(1, nuevosIntentos);
                sentencia.setInt(2, idUsuario);
                sentencia.executeUpdate();
            }
        }
    }

    private void registrarAccesoExitoso(
            Connection conexion,
            int idUsuario) throws SQLException {

        String sql = """
                UPDATE dbo.usuarios
                SET
                    intentos_fallidos = 0,
                    bloqueado_hasta = NULL,
                    estado = 'ACTIVO',
                    ultimo_acceso = SYSDATETIME()
                WHERE id_usuario = ?
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idUsuario);
            sentencia.executeUpdate();
        }
    }

    private void desbloquearCuenta(
            Connection conexion,
            int idUsuario) throws SQLException {

        String sql = """
                UPDATE dbo.usuarios
                SET
                    intentos_fallidos = 0,
                    bloqueado_hasta = NULL,
                    estado = 'ACTIVO'
                WHERE id_usuario = ?
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idUsuario);
            sentencia.executeUpdate();
        }
    }
}