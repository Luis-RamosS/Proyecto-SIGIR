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
import sigir.modelo.Proveedor;

public class ProveedorDAO {

    public List<Proveedor> listar(
            String filtro,
            String estado) throws SQLException {

        String texto = filtro == null ? "" : filtro.trim();

        String estadoFiltro =
                estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado)
                        ? null
                        : estado.trim().toUpperCase();

        String sql = """
                SELECT
                    id_proveedor,
                    rtn,
                    nombre_proveedor,
                    nombre_contacto,
                    telefono,
                    correo,
                    direccion,
                    estado,
                    fecha_registro
                FROM dbo.proveedores
                WHERE
                    (
                        ? = ''
                        OR ISNULL(rtn, '') LIKE '%' + ? + '%'
                        OR nombre_proveedor LIKE '%' + ? + '%'
                        OR ISNULL(nombre_contacto, '') LIKE '%' + ? + '%'
                        OR ISNULL(telefono, '') LIKE '%' + ? + '%'
                        OR ISNULL(correo, '') LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR estado = ?)
                ORDER BY
                    CASE WHEN estado = 'ACTIVO' THEN 0 ELSE 1 END,
                    nombre_proveedor;
                """;

        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, texto);
            sentencia.setString(2, texto);
            sentencia.setString(3, texto);
            sentencia.setString(4, texto);
            sentencia.setString(5, texto);
            sentencia.setString(6, texto);

            if (estadoFiltro == null) {
                sentencia.setNull(7, Types.VARCHAR);
                sentencia.setNull(8, Types.VARCHAR);
            } else {
                sentencia.setString(7, estadoFiltro);
                sentencia.setString(8, estadoFiltro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    proveedores.add(mapear(resultado));
                }
            }
        }

        return proveedores;
    }

    public Proveedor buscarPorId(int idProveedor) throws SQLException {

        String sql = """
                SELECT
                    id_proveedor,
                    rtn,
                    nombre_proveedor,
                    nombre_contacto,
                    telefono,
                    correo,
                    direccion,
                    estado,
                    fecha_registro
                FROM dbo.proveedores
                WHERE id_proveedor = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProveedor);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public boolean existeRtn(
            String rtn,
            Integer idProveedorExcluir) throws SQLException {

        String valorRtn = textoNulo(rtn);

        if (valorRtn == null) {
            return false;
        }

        String sql = """
                SELECT COUNT(*)
                FROM dbo.proveedores
                WHERE UPPER(rtn) = UPPER(?)
                  AND (? IS NULL OR id_proveedor <> ?);
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, valorRtn);

            if (idProveedorExcluir == null
                    || idProveedorExcluir <= 0) {

                sentencia.setNull(2, Types.INTEGER);
                sentencia.setNull(3, Types.INTEGER);

            } else {
                sentencia.setInt(2, idProveedorExcluir);
                sentencia.setInt(3, idProveedorExcluir);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    public int insertar(Proveedor proveedor) throws SQLException {

        String sql = """
                INSERT INTO dbo.proveedores
                (
                    rtn,
                    nombre_proveedor,
                    nombre_contacto,
                    telefono,
                    correo,
                    direccion,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            asignarDatos(sentencia, proveedor, false);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException(
                "SQL Server no devolvió el id del proveedor registrado."
        );
    }

    public void actualizar(Proveedor proveedor) throws SQLException {

        String sql = """
                UPDATE dbo.proveedores
                SET
                    rtn = ?,
                    nombre_proveedor = ?,
                    nombre_contacto = ?,
                    telefono = ?,
                    correo = ?,
                    direccion = ?,
                    estado = ?
                WHERE id_proveedor = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            asignarDatos(sentencia, proveedor, true);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "El proveedor ya no existe en la base de datos."
                );
            }
        }
    }

    public void cambiarEstado(
            int idProveedor,
            String estado) throws SQLException {

        String sql = """
                UPDATE dbo.proveedores
                SET estado = ?
                WHERE id_proveedor = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, idProveedor);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "El proveedor ya no existe en la base de datos."
                );
            }
        }
    }

    private void asignarDatos(
            PreparedStatement sentencia,
            Proveedor proveedor,
            boolean incluirId) throws SQLException {

        establecerTextoNulo(
                sentencia,
                1,
                proveedor.getRtn()
        );

        sentencia.setString(
                2,
                proveedor.getNombreProveedor()
        );

        establecerTextoNulo(
                sentencia,
                3,
                proveedor.getNombreContacto()
        );

        establecerTextoNulo(
                sentencia,
                4,
                proveedor.getTelefono()
        );

        establecerTextoNulo(
                sentencia,
                5,
                proveedor.getCorreo()
        );

        establecerTextoNulo(
                sentencia,
                6,
                proveedor.getDireccion()
        );

        sentencia.setString(7, proveedor.getEstado());

        if (incluirId) {
            sentencia.setInt(8, proveedor.getIdProveedor());
        }
    }

    private void establecerTextoNulo(
            PreparedStatement sentencia,
            int posicion,
            String texto) throws SQLException {

        String valor = textoNulo(texto);

        if (valor == null) {
            sentencia.setNull(posicion, Types.NVARCHAR);
        } else {
            sentencia.setString(posicion, valor);
        }
    }

    private String textoNulo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }

    private Proveedor mapear(ResultSet resultado) throws SQLException {

        Proveedor proveedor = new Proveedor();

        proveedor.setIdProveedor(
                resultado.getInt("id_proveedor")
        );
        proveedor.setRtn(resultado.getString("rtn"));
        proveedor.setNombreProveedor(
                resultado.getString("nombre_proveedor")
        );
        proveedor.setNombreContacto(
                resultado.getString("nombre_contacto")
        );
        proveedor.setTelefono(
                resultado.getString("telefono")
        );
        proveedor.setCorreo(
                resultado.getString("correo")
        );
        proveedor.setDireccion(
                resultado.getString("direccion")
        );
        proveedor.setEstado(
                resultado.getString("estado")
        );

        Timestamp fecha =
                resultado.getTimestamp("fecha_registro");

        if (fecha != null) {
            proveedor.setFechaRegistro(
                    fecha.toLocalDateTime()
            );
        }

        return proveedor;
    }
}
