package sigir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.RolSistema;
import sigir.modelo.UsuarioGestion;

public class GestionUsuarioDAO {

    public List<RolSistema> listarRolesActivos()
            throws SQLException {

        String sql = """
                SELECT
                    id_rol,
                    nombre,
                    descripcion,
                    estado
                FROM dbo.roles
                WHERE estado = 1
                ORDER BY
                    CASE
                        WHEN nombre = 'DUENO' THEN 0
                        ELSE 1
                    END,
                    nombre;
                """;

        List<RolSistema> roles =
                new ArrayList<>();

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {
                RolSistema rol =
                        new RolSistema();

                rol.setIdRol(
                        resultado.getInt("id_rol")
                );

                rol.setNombre(
                        resultado.getString("nombre")
                );

                rol.setDescripcion(
                        resultado.getString(
                                "descripcion"
                        )
                );

                rol.setActivo(
                        resultado.getBoolean("estado")
                );

                roles.add(rol);
            }
        }

        return roles;
    }

    public List<UsuarioGestion> listarUsuarios(
            String filtro,
            Integer idRol,
            String estado) throws SQLException {

        String texto = filtro == null
                ? ""
                : filtro.trim();

        Integer rolFiltro =
                idRol == null || idRol <= 0
                        ? null
                        : idRol;

        String estadoFiltro =
                estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado)
                        ? null
                        : estado.trim().toUpperCase();

        String sql = """
                SELECT
                    u.id_usuario,
                    u.id_rol,
                    r.nombre AS nombre_rol,
                    r.descripcion AS descripcion_rol,
                    u.nombre_completo,
                    u.nombre_usuario,
                    u.correo,
                    u.correo_verificado,
                    u.telefono,
                    u.estado,
                    u.intentos_fallidos,
                    u.bloqueado_hasta,
                    u.ultimo_acceso,
                    u.fecha_cambio_contrasena,
                    u.fecha_creacion
                FROM dbo.usuarios AS u
                INNER JOIN dbo.roles AS r
                    ON r.id_rol = u.id_rol
                WHERE
                    (
                        ? = ''
                        OR u.nombre_completo
                            LIKE '%' + ? + '%'
                        OR u.nombre_usuario
                            LIKE '%' + ? + '%'
                        OR u.correo
                            LIKE '%' + ? + '%'
                        OR u.telefono
                            LIKE '%' + ? + '%'
                        OR r.nombre
                            LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR u.id_rol = ?)
                    AND (? IS NULL OR u.estado = ?)
                ORDER BY
                    CASE
                        WHEN u.estado = 'ACTIVO'
                        THEN 0
                        ELSE 1
                    END,
                    u.nombre_completo;
                """;

        List<UsuarioGestion> usuarios =
                new ArrayList<>();

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            for (int i = 1; i <= 6; i++) {
                sentencia.setString(i, texto);
            }

            if (rolFiltro == null) {
                sentencia.setNull(
                        7,
                        Types.INTEGER
                );

                sentencia.setNull(
                        8,
                        Types.INTEGER
                );
            } else {
                sentencia.setInt(7, rolFiltro);
                sentencia.setInt(8, rolFiltro);
            }

            if (estadoFiltro == null) {
                sentencia.setNull(
                        9,
                        Types.VARCHAR
                );

                sentencia.setNull(
                        10,
                        Types.VARCHAR
                );
            } else {
                sentencia.setString(
                        9,
                        estadoFiltro
                );

                sentencia.setString(
                        10,
                        estadoFiltro
                );
            }

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                while (resultado.next()) {
                    usuarios.add(
                            mapearUsuario(resultado)
                    );
                }
            }
        }

        return usuarios;
    }

    public int insertar(
            UsuarioGestion usuario,
            String contrasenaHash)
            throws SQLException {

        String sql = """
                INSERT INTO dbo.usuarios
                (
                    id_rol,
                    nombre_completo,
                    nombre_usuario,
                    correo,
                    correo_verificado,
                    contrasena_hash,
                    telefono,
                    estado,
                    intentos_fallidos,
                    bloqueado_hasta,
                    fecha_cambio_contrasena,
                    fecha_creacion
                )
                VALUES
                (
                    ?, ?, ?, ?, ?, ?, ?, ?,
                    0, NULL, SYSDATETIME(),
                    SYSDATETIME()
                );
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            sentencia.setInt(
                    1,
                    usuario.getIdRol()
            );

            sentencia.setNString(
                    2,
                    usuario.getNombreCompleto()
            );

            sentencia.setNString(
                    3,
                    usuario.getNombreUsuario()
            );

            sentencia.setString(
                    4,
                    usuario.getCorreo()
            );

            sentencia.setBoolean(
                    5,
                    usuario.isCorreoVerificado()
            );

            sentencia.setString(
                    6,
                    contrasenaHash
            );

            establecerTextoNulo(
                    sentencia,
                    7,
                    usuario.getTelefono(),
                    Types.VARCHAR
            );

            sentencia.setString(
                    8,
                    usuario.getEstado()
            );

            sentencia.executeUpdate();

            try (ResultSet claves =
                         sentencia.getGeneratedKeys()) {

                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException(
                "SQL Server no devolvió el id del usuario."
        );
    }

    public void actualizar(
            UsuarioGestion usuario)
            throws SQLException {

        String sql = """
                UPDATE dbo.usuarios
                SET
                    id_rol = ?,
                    nombre_completo = ?,
                    nombre_usuario = ?,
                    correo = ?,
                    correo_verificado = ?,
                    telefono = ?,
                    estado = ?,
                    intentos_fallidos =
                        CASE
                            WHEN ? = 'ACTIVO'
                            THEN 0
                            ELSE intentos_fallidos
                        END,
                    bloqueado_hasta =
                        CASE
                            WHEN ? = 'ACTIVO'
                            THEN NULL
                            ELSE bloqueado_hasta
                        END
                WHERE id_usuario = ?;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    usuario.getIdRol()
            );

            sentencia.setNString(
                    2,
                    usuario.getNombreCompleto()
            );

            sentencia.setNString(
                    3,
                    usuario.getNombreUsuario()
            );

            sentencia.setString(
                    4,
                    usuario.getCorreo()
            );

            sentencia.setBoolean(
                    5,
                    usuario.isCorreoVerificado()
            );

            establecerTextoNulo(
                    sentencia,
                    6,
                    usuario.getTelefono(),
                    Types.VARCHAR
            );

            sentencia.setString(
                    7,
                    usuario.getEstado()
            );

            sentencia.setString(
                    8,
                    usuario.getEstado()
            );

            sentencia.setString(
                    9,
                    usuario.getEstado()
            );

            sentencia.setInt(
                    10,
                    usuario.getIdUsuario()
            );

            if (sentencia.executeUpdate() == 0) {
                throw new SQLException(
                        "El usuario seleccionado ya no existe."
                );
            }
        }
    }

    public void restablecerContrasena(
            int idUsuario,
            String contrasenaHash)
            throws SQLException {

        String sql = """
                UPDATE dbo.usuarios
                SET
                    contrasena_hash = ?,
                    fecha_cambio_contrasena =
                        SYSDATETIME(),
                    intentos_fallidos = 0,
                    bloqueado_hasta = NULL,
                    estado =
                        CASE
                            WHEN estado = 'BLOQUEADO'
                            THEN 'ACTIVO'
                            ELSE estado
                        END
                WHERE id_usuario = ?;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    contrasenaHash
            );

            sentencia.setInt(
                    2,
                    idUsuario
            );

            if (sentencia.executeUpdate() == 0) {
                throw new SQLException(
                        "El usuario seleccionado ya no existe."
                );
            }
        }
    }

    public boolean existeNombreUsuario(
            String nombreUsuario,
            int excluirId) throws SQLException {

        return existeValorUnico(
                "nombre_usuario",
                nombreUsuario,
                excluirId
        );
    }

    public boolean existeCorreo(
            String correo,
            int excluirId) throws SQLException {

        return existeValorUnico(
                "correo",
                correo,
                excluirId
        );
    }

    public int[] contarIndicadores()
            throws SQLException {

        String sql = """
                SELECT
                    COUNT(*) AS total,
                    SUM(
                        CASE
                            WHEN estado = 'ACTIVO'
                            THEN 1
                            ELSE 0
                        END
                    ) AS activos,
                    SUM(
                        CASE
                            WHEN estado = 'BLOQUEADO'
                            THEN 1
                            ELSE 0
                        END
                    ) AS bloqueados
                FROM dbo.usuarios;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            if (!resultado.next()) {
                return new int[]{0, 0, 0};
            }

            return new int[]{
                resultado.getInt("total"),
                resultado.getInt("activos"),
                resultado.getInt("bloqueados")
            };
        }
    }

    public int contarEstado(String estado)
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM dbo.usuarios
                WHERE estado = ?;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                return resultado.next()
                        ? resultado.getInt(1)
                        : 0;
            }
        }
    }

    public int contarTodos() throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM dbo.usuarios;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            return resultado.next()
                    ? resultado.getInt(1)
                    : 0;
        }
    }

    public int contarDuenosActivos()
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM dbo.usuarios AS u
                INNER JOIN dbo.roles AS r
                    ON r.id_rol = u.id_rol
                WHERE r.nombre = 'DUENO'
                  AND u.estado = 'ACTIVO';
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            return resultado.next()
                    ? resultado.getInt(1)
                    : 0;
        }
    }

    private boolean existeValorUnico(
            String columna,
            String valor,
            int excluirId) throws SQLException {

        if (!"nombre_usuario".equals(columna)
                && !"correo".equals(columna)) {

            throw new IllegalArgumentException(
                    "Columna no permitida."
            );
        }

        String sql = """
                SELECT COUNT(*)
                FROM dbo.usuarios
                WHERE LOWER(%s) =
                    LOWER(LTRIM(RTRIM(?)))
                  AND id_usuario <> ?;
                """.formatted(columna);

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, valor);
            sentencia.setInt(2, excluirId);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                return resultado.next()
                        && resultado.getInt(1) > 0;
            }
        }
    }

    private UsuarioGestion mapearUsuario(
            ResultSet resultado) throws SQLException {

        UsuarioGestion usuario =
                new UsuarioGestion();

        usuario.setIdUsuario(
                resultado.getInt("id_usuario")
        );

        usuario.setIdRol(
                resultado.getInt("id_rol")
        );

        usuario.setNombreRol(
                resultado.getString("nombre_rol")
        );

        usuario.setDescripcionRol(
                resultado.getString(
                        "descripcion_rol"
                )
        );

        usuario.setNombreCompleto(
                resultado.getString(
                        "nombre_completo"
                )
        );

        usuario.setNombreUsuario(
                resultado.getString(
                        "nombre_usuario"
                )
        );

        usuario.setCorreo(
                resultado.getString("correo")
        );

        usuario.setCorreoVerificado(
                resultado.getBoolean(
                        "correo_verificado"
                )
        );

        usuario.setTelefono(
                resultado.getString("telefono")
        );

        usuario.setEstado(
                resultado.getString("estado")
        );

        usuario.setIntentosFallidos(
                resultado.getInt(
                        "intentos_fallidos"
                )
        );

        Timestamp bloqueado =
                resultado.getTimestamp(
                        "bloqueado_hasta"
                );

        if (bloqueado != null) {
            usuario.setBloqueadoHasta(
                    bloqueado.toLocalDateTime()
            );
        }

        Timestamp ultimo =
                resultado.getTimestamp(
                        "ultimo_acceso"
                );

        if (ultimo != null) {
            usuario.setUltimoAcceso(
                    ultimo.toLocalDateTime()
            );
        }

        Timestamp cambio =
                resultado.getTimestamp(
                        "fecha_cambio_contrasena"
                );

        if (cambio != null) {
            usuario.setFechaCambioContrasena(
                    cambio.toLocalDateTime()
            );
        }

        Timestamp creacion =
                resultado.getTimestamp(
                        "fecha_creacion"
                );

        if (creacion != null) {
            usuario.setFechaCreacion(
                    creacion.toLocalDateTime()
            );
        }

        return usuario;
    }

    private void establecerTextoNulo(
            PreparedStatement sentencia,
            int posicion,
            String valor,
            int tipoSql) throws SQLException {

        if (valor == null
                || valor.trim().isEmpty()) {

            sentencia.setNull(
                    posicion,
                    tipoSql
            );
        } else {
            sentencia.setString(
                    posicion,
                    valor.trim()
            );
        }
    }
}
