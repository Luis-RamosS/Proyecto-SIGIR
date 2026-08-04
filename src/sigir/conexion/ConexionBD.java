package sigir.conexion;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    private static final String URL
            = "jdbc:sqlserver://192.168.0.8:1433;"
            + "databaseName=SIGIR;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USUARIO
            = "sigir_app";

    private static final String CONTRASENA
            = "SigirApp#2026_Segura";

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL,USUARIO,CONTRASENA);
    }
}