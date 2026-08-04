package sigir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.ResultadoBusquedaGlobal;

public class BusquedaGlobalDAO {

    public List<ResultadoBusquedaGlobal> buscar(
            String texto) throws SQLException {

        String filtro = texto == null
                ? ""
                : texto.trim();

        if (filtro.isBlank()) {
            return List.of();
        }

        String sql = """
                SELECT TOP (15)
                    r.tipo,
                    r.id,
                    r.titulo,
                    r.detalle
                FROM
                (
                    SELECT
                        'PRODUCTO' AS tipo,
                        p.id_producto AS id,
                        CONCAT(
                            p.codigo,
                            ' - ',
                            p.nombre
                        ) AS titulo,
                        CONCAT(
                            'Stock: ',
                            p.stock_actual,
                            ' | Precio: L ',
                            FORMAT(p.precio_venta, 'N2')
                        ) AS detalle
                    FROM dbo.productos AS p
                    WHERE p.estado <> 'INACTIVO'
                      AND
                      (
                          p.codigo LIKE '%' + ? + '%'
                          OR p.nombre LIKE '%' + ? + '%'
                          OR p.marca LIKE '%' + ? + '%'
                      )

                    UNION ALL

                    SELECT
                        'CLIENTE',
                        c.id_cliente,
                        c.nombre_completo,
                        CONCAT(
                            'Identidad: ',
                            COALESCE(
                                c.numero_identidad,
                                'Sin identidad'
                            ),
                            ' | Tel: ',
                            COALESCE(
                                c.telefono,
                                'Sin teléfono'
                            )
                        )
                    FROM dbo.clientes AS c
                    WHERE c.estado = 'ACTIVO'
                      AND
                      (
                          c.nombre_completo
                              LIKE '%' + ? + '%'
                          OR c.numero_identidad
                              LIKE '%' + ? + '%'
                          OR c.telefono
                              LIKE '%' + ? + '%'
                      )

                    UNION ALL

                    SELECT
                        'VENTA',
                        v.id_venta,
                        CONCAT(
                            v.numero_factura,
                            ' - ',
                            c.nombre_completo
                        ),
                        CONCAT(
                            'Total: L ',
                            FORMAT(v.total, 'N2'),
                            ' | ',
                            v.estado
                        )
                    FROM dbo.ventas AS v
                    INNER JOIN dbo.clientes AS c
                        ON c.id_cliente = v.id_cliente
                    WHERE
                        v.numero_factura
                            LIKE '%' + ? + '%'
                        OR c.nombre_completo
                            LIKE '%' + ? + '%'
                        OR v.estado
                            LIKE '%' + ? + '%'

                    UNION ALL

                    SELECT
                        'COMPRA',
                        co.id_compra,
                        CONCAT(
                            COALESCE(
                                co.numero_documento,
                                CONCAT(
                                    'Compra #',
                                    co.id_compra
                                )
                            ),
                            ' - ',
                            pr.nombre_proveedor
                        ),
                        CONCAT(
                            'Total: L ',
                            FORMAT(co.total, 'N2'),
                            ' | ',
                            co.estado
                        )
                    FROM dbo.compras AS co
                    INNER JOIN dbo.proveedores AS pr
                        ON pr.id_proveedor = co.id_proveedor
                    WHERE
                        co.numero_documento
                            LIKE '%' + ? + '%'
                        OR pr.nombre_proveedor
                            LIKE '%' + ? + '%'
                        OR co.estado
                            LIKE '%' + ? + '%'

                    UNION ALL

                    SELECT
                        'REPARACION',
                        o.id_orden,
                        CONCAT(
                            o.numero_orden,
                            ' - ',
                            c.nombre_completo
                        ),
                        CONCAT(
                            e.tipo_equipo,
                            ' ',
                            COALESCE(e.marca, ''),
                            ' | ',
                            o.estado
                        )
                    FROM dbo.ordenes_servicio AS o
                    INNER JOIN dbo.equipos_cliente AS e
                        ON e.id_equipo = o.id_equipo
                    INNER JOIN dbo.clientes AS c
                        ON c.id_cliente = e.id_cliente
                    WHERE
                        o.numero_orden
                            LIKE '%' + ? + '%'
                        OR c.nombre_completo
                            LIKE '%' + ? + '%'
                        OR o.estado
                            LIKE '%' + ? + '%'
                ) AS r
                ORDER BY
                    CASE r.tipo
                        WHEN 'PRODUCTO' THEN 1
                        WHEN 'CLIENTE' THEN 2
                        WHEN 'VENTA' THEN 3
                        WHEN 'COMPRA' THEN 4
                        ELSE 5
                    END,
                    r.titulo;
                """;

        List<ResultadoBusquedaGlobal> resultados =
                new ArrayList<>();

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            for (int posicion = 1;
                    posicion <= 15;
                    posicion++) {

                sentencia.setString(
                        posicion,
                        filtro
                );
            }

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                while (resultado.next()) {
                    ResultadoBusquedaGlobal item =
                            new ResultadoBusquedaGlobal();

                    item.setTipo(
                            resultado.getString("tipo")
                    );

                    item.setId(
                            resultado.getInt("id")
                    );

                    item.setTitulo(
                            resultado.getString("titulo")
                    );

                    item.setDetalle(
                            resultado.getString("detalle")
                    );

                    resultados.add(item);
                }
            }
        }

        return resultados;
    }
}
