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
import sigir.modelo.Cliente;

public class ClienteDAO {

    public List<Cliente> listar(
            String filtro,
            Integer idTipoCliente,
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
                    c.id_cliente,
                    c.id_tipo_cliente,
                    t.nombre AS nombre_tipo_cliente,
                    c.numero_identidad,
                    c.nombre_completo,
                    c.telefono,
                    c.correo,
                    c.direccion,
                    c.fecha_registro,
                    c.estado
                FROM dbo.clientes AS c
                INNER JOIN dbo.tipos_cliente AS t
                    ON t.id_tipo_cliente = c.id_tipo_cliente
                WHERE
                    (
                        ? = ''
                        OR ISNULL(c.numero_identidad, '') LIKE '%' + ? + '%'
                        OR c.nombre_completo LIKE '%' + ? + '%'
                        OR ISNULL(c.telefono, '') LIKE '%' + ? + '%'
                        OR ISNULL(c.correo, '') LIKE '%' + ? + '%'
                    )
                    AND (? IS NULL OR c.id_tipo_cliente = ?)
                    AND (? IS NULL OR c.estado = ?)
                ORDER BY
                    CASE WHEN c.estado = 'ACTIVO' THEN 0 ELSE 1 END,
                    c.nombre_completo;
                """;

        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, texto);
            sentencia.setString(2, texto);
            sentencia.setString(3, texto);
            sentencia.setString(4, texto);
            sentencia.setString(5, texto);

            if (idTipoCliente == null || idTipoCliente <= 0) {
                sentencia.setNull(6, Types.INTEGER);
                sentencia.setNull(7, Types.INTEGER);
            } else {
                sentencia.setInt(6, idTipoCliente);
                sentencia.setInt(7, idTipoCliente);
            }

            if (estadoFiltro == null) {
                sentencia.setNull(8, Types.VARCHAR);
                sentencia.setNull(9, Types.VARCHAR);
            } else {
                sentencia.setString(8, estadoFiltro);
                sentencia.setString(9, estadoFiltro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    clientes.add(mapear(resultado));
                }
            }
        }

        return clientes;
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {

        String sql = """
                SELECT
                    c.id_cliente,
                    c.id_tipo_cliente,
                    t.nombre AS nombre_tipo_cliente,
                    c.numero_identidad,
                    c.nombre_completo,
                    c.telefono,
                    c.correo,
                    c.direccion,
                    c.fecha_registro,
                    c.estado
                FROM dbo.clientes AS c
                INNER JOIN dbo.tipos_cliente AS t
                    ON t.id_tipo_cliente = c.id_tipo_cliente
                WHERE c.id_cliente = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idCliente);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public boolean existeIdentidad(
            String numeroIdentidad,
            Integer idClienteExcluir) throws SQLException {

        String identidad = textoNulo(numeroIdentidad);

        if (identidad == null) {
            return false;
        }

        String sql = """
                SELECT COUNT(*)
                FROM dbo.clientes
                WHERE UPPER(numero_identidad) = UPPER(?)
                  AND (? IS NULL OR id_cliente <> ?);
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, identidad);

            if (idClienteExcluir == null || idClienteExcluir <= 0) {
                sentencia.setNull(2, Types.INTEGER);
                sentencia.setNull(3, Types.INTEGER);
            } else {
                sentencia.setInt(2, idClienteExcluir);
                sentencia.setInt(3, idClienteExcluir);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    public int insertar(Cliente cliente) throws SQLException {

        String sql = """
                INSERT INTO dbo.clientes
                (
                    id_tipo_cliente,
                    numero_identidad,
                    nombre_completo,
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

            asignarDatos(sentencia, cliente, false);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException(
                "SQL Server no devolvió el id del cliente registrado."
        );
    }

    public void actualizar(Cliente cliente) throws SQLException {

        String sql = """
                UPDATE dbo.clientes
                SET
                    id_tipo_cliente = ?,
                    numero_identidad = ?,
                    nombre_completo = ?,
                    telefono = ?,
                    correo = ?,
                    direccion = ?,
                    estado = ?
                WHERE id_cliente = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            asignarDatos(sentencia, cliente, true);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "El cliente ya no existe en la base de datos."
                );
            }
        }
    }

    public void cambiarEstado(
            int idCliente,
            String estado) throws SQLException {

        String sql = """
                UPDATE dbo.clientes
                SET estado = ?
                WHERE id_cliente = ?;
                """;

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, idCliente);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "El cliente ya no existe en la base de datos."
                );
            }
        }
    }

    private void asignarDatos(
            PreparedStatement sentencia,
            Cliente cliente,
            boolean incluirId) throws SQLException {

        sentencia.setInt(1, cliente.getIdTipoCliente());

        establecerTextoNulo(
                sentencia,
                2,
                cliente.getNumeroIdentidad()
        );

        sentencia.setString(
                3,
                cliente.getNombreCompleto()
        );

        establecerTextoNulo(
                sentencia,
                4,
                cliente.getTelefono()
        );

        establecerTextoNulo(
                sentencia,
                5,
                cliente.getCorreo()
        );

        establecerTextoNulo(
                sentencia,
                6,
                cliente.getDireccion()
        );

        sentencia.setString(7, cliente.getEstado());

        if (incluirId) {
            sentencia.setInt(8, cliente.getIdCliente());
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

    private Cliente mapear(ResultSet resultado) throws SQLException {

        Cliente cliente = new Cliente();

        cliente.setIdCliente(resultado.getInt("id_cliente"));
        cliente.setIdTipoCliente(
                resultado.getInt("id_tipo_cliente")
        );
        cliente.setNombreTipoCliente(
                resultado.getString("nombre_tipo_cliente")
        );
        cliente.setNumeroIdentidad(
                resultado.getString("numero_identidad")
        );
        cliente.setNombreCompleto(
                resultado.getString("nombre_completo")
        );
        cliente.setTelefono(resultado.getString("telefono"));
        cliente.setCorreo(resultado.getString("correo"));
        cliente.setDireccion(resultado.getString("direccion"));
        cliente.setEstado(resultado.getString("estado"));

        Timestamp fecha =
                resultado.getTimestamp("fecha_registro");

        if (fecha != null) {
            cliente.setFechaRegistro(
                    fecha.toLocalDateTime()
            );
        }

        return cliente;
    }
}
