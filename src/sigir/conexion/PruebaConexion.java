package sigir.conexion;

import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {

        try (Connection conexion = ConexionBD.obtenerConexion()) {

            if (conexion != null && !conexion.isClosed()) {
                System.out.println(
                        "Conexión exitosa con la base de datos SIGIR."
                );
            }

        } catch (SQLException ex) {
            System.err.println("No fue posible conectar con SQL Server.");
            System.err.println("Mensaje: " + ex.getMessage());
            System.err.println("Código: " + ex.getErrorCode());
            System.err.println("Estado SQL: " + ex.getSQLState());
        }
    }
}