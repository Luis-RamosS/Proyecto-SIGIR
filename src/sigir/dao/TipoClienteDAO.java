package sigir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.TipoCliente;

public class TipoClienteDAO {

    public List<TipoCliente> listarActivos() throws SQLException {

        String sql = """
                SELECT
                    id_tipo_cliente,
                    nombre,
                    descripcion,
                    estado
                FROM dbo.tipos_cliente
                WHERE estado = 1
                ORDER BY nombre;
                """;

        List<TipoCliente> tipos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                tipos.add(new TipoCliente(
                        resultado.getInt("id_tipo_cliente"),
                        resultado.getString("nombre"),
                        resultado.getString("descripcion"),
                        resultado.getBoolean("estado")
                ));
            }
        }

        return tipos;
    }
}
