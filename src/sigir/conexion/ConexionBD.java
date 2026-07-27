package sigir.conexion;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=SIGIR;"
            + "integratedSecurity=true;"
            + "authenticationScheme=NativeAuthentication;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}