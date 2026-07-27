package sigir.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import sigir.conexion.ConexionBD;
import sigir.modelo.SolicitudRecuperacion;

public class RecuperacionContrasenaDAO {

    public Optional<SolicitudRecuperacion> crearSolicitud(
            String correo,
            String codigoHash,
            int minutosVigencia) throws SQLException {

        String sql = "{call dbo.sp_crear_recuperacion_contrasena(?, ?, ?)}";

        try (Connection conexion = ConexionBD.obtenerConexion();
             CallableStatement sentencia = conexion.prepareCall(sql)) {

            sentencia.setString(1, correo);
            sentencia.setString(2, codigoHash);
            sentencia.setInt(3, minutosVigencia);

            boolean tieneResultado = sentencia.execute();

            while (true) {
                if (tieneResultado) {
                    try (ResultSet resultado = sentencia.getResultSet()) {
                        if (resultado != null && resultado.next()) {
                            SolicitudRecuperacion solicitud =
                                    new SolicitudRecuperacion(
                                            resultado.getInt("id_usuario"),
                                            resultado.getString("nombre_completo"),
                                            resultado.getString("nombre_usuario"),
                                            resultado.getString("correo")
                                    );

                            return Optional.of(solicitud);
                        }
                    }
                } else if (sentencia.getUpdateCount() == -1) {
                    break;
                }

                tieneResultado = sentencia.getMoreResults();
            }

            return Optional.empty();
        }
    }

    public long validarCodigo(
            String correo,
            String codigoHash) throws SQLException {

        String sql = "{call dbo.sp_validar_codigo_recuperacion(?, ?)}";

        try (Connection conexion = ConexionBD.obtenerConexion();
             CallableStatement sentencia = conexion.prepareCall(sql)) {

            sentencia.setString(1, correo);
            sentencia.setString(2, codigoHash);

            boolean tieneResultado = sentencia.execute();

            while (true) {
                if (tieneResultado) {
                    try (ResultSet resultado = sentencia.getResultSet()) {
                        if (resultado != null && resultado.next()) {
                            return resultado.getLong("id_recuperacion");
                        }
                    }
                } else if (sentencia.getUpdateCount() == -1) {
                    break;
                }

                tieneResultado = sentencia.getMoreResults();
            }
        }

        throw new SQLException(
                "SQL Server no devolvió una recuperación válida."
        );
    }

    public void cambiarContrasena(
            long idRecuperacion,
            String nuevoHash) throws SQLException {

        String sql = "{call dbo.sp_cambiar_contrasena_recuperacion(?, ?)}";

        try (Connection conexion = ConexionBD.obtenerConexion();
             CallableStatement sentencia = conexion.prepareCall(sql)) {

            sentencia.setLong(1, idRecuperacion);
            sentencia.setString(2, nuevoHash);
            sentencia.execute();
        }
    }
}
