package sigir.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.Producto;
import sigir.modelo.VentaRapida;

public class VentaRapidaDAO {

    public List<Producto> listarProductosDisponibles() throws SQLException {
        String sql = """
            SELECT p.id_producto,p.id_categoria,c.nombre AS nombre_categoria,
                   p.codigo,p.nombre,p.descripcion,p.marca,p.modelo,
                   p.precio_compra,p.precio_venta,p.stock_actual,p.stock_minimo,
                   p.maneja_numero_serie,p.estado,p.fecha_registro
            FROM dbo.productos p
            INNER JOIN dbo.categorias_producto c ON c.id_categoria=p.id_categoria
            WHERE p.estado='ACTIVO' AND p.stock_actual>0
            ORDER BY p.nombre;
            """;
        List<Producto> lista = new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion(); PreparedStatement ps=cn.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) lista.add(mapearProducto(rs));
        }
        return lista;
    }

    public List<VentaRapida> listarRecientes() throws SQLException {
        String sql = """
            SELECT TOP (100) vr.id_venta_rapida,vr.id_producto,p.codigo,p.nombre,
                   vr.id_usuario,u.nombre_completo AS nombre_usuario,
                   vr.fecha_hora_real,vr.fecha_registro,vr.cantidad,
                   vr.precio_unitario,vr.total,vr.metodo_pago,vr.numero_serie,vr.observaciones
            FROM dbo.ventas_rapidas vr
            INNER JOIN dbo.productos p ON p.id_producto=vr.id_producto
            INNER JOIN dbo.usuarios u ON u.id_usuario=vr.id_usuario
            ORDER BY vr.fecha_registro DESC, vr.id_venta_rapida DESC;
            """;
        List<VentaRapida> lista=new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion(); PreparedStatement ps=cn.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) {
                VentaRapida v=new VentaRapida();
                v.setIdVentaRapida(rs.getInt("id_venta_rapida"));
                v.setIdProducto(rs.getInt("id_producto"));
                v.setCodigoProducto(rs.getString("codigo"));
                v.setNombreProducto(rs.getString("nombre"));
                v.setIdUsuario(rs.getInt("id_usuario"));
                v.setNombreUsuario(rs.getString("nombre_usuario"));
                Timestamp fr=rs.getTimestamp("fecha_hora_real"); if(fr!=null)v.setFechaHoraReal(fr.toLocalDateTime());
                Timestamp fg=rs.getTimestamp("fecha_registro"); if(fg!=null)v.setFechaRegistro(fg.toLocalDateTime());
                v.setCantidad(rs.getInt("cantidad"));
                v.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                v.setTotal(rs.getBigDecimal("total"));
                v.setMetodoPago(rs.getString("metodo_pago"));
                v.setNumeroSerie(rs.getString("numero_serie"));
                v.setObservaciones(rs.getString("observaciones"));
                lista.add(v);
            }
        }
        return lista;
    }

    public int registrar(VentaRapida venta) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                Producto p=bloquearProducto(cn,venta.getIdProducto());
                if(!"ACTIVO".equalsIgnoreCase(p.getEstado())) throw new SQLException("El producto ya no está activo.");
                if(venta.getCantidad()<=0 || venta.getCantidad()>p.getStockActual()) throw new SQLException("Stock insuficiente. Disponible: "+p.getStockActual());

                if(p.isManejaNumeroSerie()) {
                    if(venta.getCantidad()!=1) throw new SQLException("Los productos con número de serie deben registrarse de una unidad por venta rápida.");
                    if(venta.getNumeroSerie()==null || venta.getNumeroSerie().isBlank()) throw new SQLException("Ingresa el número de serie de la unidad vendida.");
                    marcarUnidadVendida(cn,p.getIdProducto(),venta.getNumeroSerie().trim());
                } else {
                    venta.setNumeroSerie(null);
                }

                int id;
                String sql="""
                    INSERT INTO dbo.ventas_rapidas
                    (id_producto,id_usuario,fecha_hora_real,fecha_registro,cantidad,precio_unitario,total,metodo_pago,numero_serie,observaciones)
                    VALUES(?,?,?,SYSDATETIME(),?,?,?,?,?,?);
                    """;
                try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1,venta.getIdProducto());
                    ps.setInt(2,venta.getIdUsuario());
                    ps.setTimestamp(3,Timestamp.valueOf(venta.getFechaHoraReal()));
                    ps.setInt(4,venta.getCantidad());
                    ps.setBigDecimal(5,venta.getPrecioUnitario());
                    ps.setBigDecimal(6,venta.getTotal());
                    ps.setString(7,venta.getMetodoPago());
                    setNulo(ps,8,venta.getNumeroSerie());
                    setNulo(ps,9,venta.getObservaciones());
                    ps.executeUpdate();
                    try(ResultSet rs=ps.getGeneratedKeys()) { if(!rs.next()) throw new SQLException("No se generó el id de la venta rápida."); id=rs.getInt(1); }
                }

                int anterior=p.getStockActual(); int nuevo=anterior-venta.getCantidad();
                String estado=nuevo==0?"AGOTADO":"ACTIVO";
                try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.productos SET stock_actual=?,estado=? WHERE id_producto=?")) {
                    ps.setInt(1,nuevo);ps.setString(2,estado);ps.setInt(3,p.getIdProducto());ps.executeUpdate();
                }
                String mov="""
                    INSERT INTO dbo.movimientos_inventario
                    (id_producto,id_usuario,id_compra,id_venta,id_orden,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,motivo)
                    VALUES(?,?,NULL,NULL,NULL,'SALIDA_VENTA_RAPIDA',?,?,?,?);
                    """;
                try(PreparedStatement ps=cn.prepareStatement(mov)) {
                    ps.setInt(1,p.getIdProducto());ps.setInt(2,venta.getIdUsuario());ps.setInt(3,venta.getCantidad());ps.setInt(4,anterior);ps.setInt(5,nuevo);ps.setString(6,"Venta rápida #"+id);ps.executeUpdate();
                }
                cn.commit();
                return id;
            } catch(SQLException|RuntimeException ex) {
                cn.rollback(); throw ex;
            } finally { cn.setAutoCommit(true); }
        }
    }

    private void marcarUnidadVendida(Connection cn,int idProducto,String serie) throws SQLException {
        String buscar="""
            SELECT id_unidad FROM dbo.unidades_producto WITH(UPDLOCK,ROWLOCK)
            WHERE id_producto=? AND estado='DISPONIBLE'
              AND (numero_serie=? OR codigo_interno=?);
            """;
        int idUnidad;
        try(PreparedStatement ps=cn.prepareStatement(buscar)) {
            ps.setInt(1,idProducto);ps.setString(2,serie);ps.setString(3,serie);
            try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new SQLException("No existe una unidad disponible con ese número de serie."); idUnidad=rs.getInt(1); }
        }
        try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.unidades_producto SET estado='VENDIDO',fecha_salida=SYSDATETIME() WHERE id_unidad=?")) {
            ps.setInt(1,idUnidad);ps.executeUpdate();
        }
    }

    private Producto bloquearProducto(Connection cn,int idProducto) throws SQLException {
        String sql="""
            SELECT p.id_producto,p.id_categoria,c.nombre AS nombre_categoria,p.codigo,p.nombre,
                   p.descripcion,p.marca,p.modelo,p.precio_compra,p.precio_venta,p.stock_actual,
                   p.stock_minimo,p.maneja_numero_serie,p.estado,p.fecha_registro
            FROM dbo.productos p WITH(UPDLOCK,ROWLOCK)
            INNER JOIN dbo.categorias_producto c ON c.id_categoria=p.id_categoria
            WHERE p.id_producto=?;
            """;
        try(PreparedStatement ps=cn.prepareStatement(sql)) {
            ps.setInt(1,idProducto);try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new SQLException("El producto ya no existe."); return mapearProducto(rs); }
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p=new Producto(); p.setIdProducto(rs.getInt("id_producto")); p.setIdCategoria(rs.getInt("id_categoria")); p.setNombreCategoria(rs.getString("nombre_categoria")); p.setCodigo(rs.getString("codigo")); p.setNombre(rs.getString("nombre")); p.setDescripcion(rs.getString("descripcion")); p.setMarca(rs.getString("marca")); p.setModelo(rs.getString("modelo")); p.setPrecioCompra(rs.getBigDecimal("precio_compra")); p.setPrecioVenta(rs.getBigDecimal("precio_venta")); p.setStockActual(rs.getInt("stock_actual")); p.setStockMinimo(rs.getInt("stock_minimo")); p.setManejaNumeroSerie(rs.getBoolean("maneja_numero_serie")); p.setEstado(rs.getString("estado")); Timestamp f=rs.getTimestamp("fecha_registro"); if(f!=null)p.setFechaRegistro(f.toLocalDateTime()); return p;
    }

    private void setNulo(PreparedStatement ps,int pos,String v) throws SQLException {
        if(v==null || v.trim().isEmpty()) ps.setNull(pos,Types.NVARCHAR); else ps.setString(pos,v.trim());
    }
}
