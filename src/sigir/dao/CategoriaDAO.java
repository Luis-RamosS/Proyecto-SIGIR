package sigir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.Categoria;

public class CategoriaDAO {

    public List<Categoria> listarActivas() throws SQLException {

        String sql = """
                SELECT
                    id_categoria,
                    nombre,
                    descripcion,
                    estado
                FROM dbo.categorias_producto
                WHERE estado = 1
                ORDER BY nombre;
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                categorias.add(new Categoria(
                        resultado.getInt("id_categoria"),
                        resultado.getString("nombre"),
                        resultado.getString("descripcion"),
                        resultado.getBoolean("estado")
                ));
            }
        }

        return categorias;
    }
}
