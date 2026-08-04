package sigir.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.*;

public class VentaDAO {

    public List<Cliente> listarClientesActivos() throws SQLException {
        String sql = """
            SELECT c.id_cliente,c.id_tipo_cliente,t.nombre AS nombre_tipo_cliente,
                   c.numero_identidad,c.nombre_completo,c.telefono,c.correo,
                   c.direccion,c.fecha_registro,c.estado
            FROM dbo.clientes c
            INNER JOIN dbo.tipos_cliente t ON t.id_tipo_cliente=c.id_tipo_cliente
            WHERE c.estado='ACTIVO'
            ORDER BY c.nombre_completo;
            """;
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("id_cliente"));
                c.setIdTipoCliente(rs.getInt("id_tipo_cliente"));
                c.setNombreTipoCliente(rs.getString("nombre_tipo_cliente"));
                c.setNumeroIdentidad(rs.getString("numero_identidad"));
                c.setNombreCompleto(rs.getString("nombre_completo"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setDireccion(rs.getString("direccion"));
                c.setEstado(rs.getString("estado"));
                Timestamp f = rs.getTimestamp("fecha_registro");
                if (f != null) c.setFechaRegistro(f.toLocalDateTime());
                lista.add(c);
            }
        }
        return lista;
    }

    public List<Producto> listarProductosDisponibles() throws SQLException {
        String sql = """
            SELECT p.id_producto,p.id_categoria,c.nombre AS nombre_categoria,p.codigo,p.nombre,
                   p.descripcion,p.marca,p.modelo,p.precio_compra,p.precio_venta,
                   p.stock_actual,p.stock_minimo,p.maneja_numero_serie,p.estado,p.fecha_registro
            FROM dbo.productos p
            INNER JOIN dbo.categorias_producto c ON c.id_categoria=p.id_categoria
            WHERE p.estado='ACTIVO' AND p.stock_actual>0
            ORDER BY p.nombre;
            """;
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearProducto(rs));
        }
        return lista;
    }

    public List<UnidadProducto> listarUnidadesDisponibles(int idProducto) throws SQLException {
        String sql = """
            SELECT id_unidad,id_producto,numero_serie,codigo_interno,estado
            FROM dbo.unidades_producto
            WHERE id_producto=? AND estado='DISPONIBLE'
            ORDER BY numero_serie,codigo_interno;
            """;
        List<UnidadProducto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1,idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UnidadProducto u = new UnidadProducto();
                    u.setIdUnidad(rs.getInt("id_unidad"));
                    u.setIdProducto(rs.getInt("id_producto"));
                    u.setNumeroSerie(rs.getString("numero_serie"));
                    u.setCodigoInterno(rs.getString("codigo_interno"));
                    u.setEstado(rs.getString("estado"));
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    public boolean existeNumeroFactura(String numero) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM dbo.ventas WHERE numero_factura=?")) {
            ps.setString(1,numero);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1)>0; }
        }
    }

    public int registrar(Venta venta) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                int idVenta = insertarVenta(cn,venta);
                for (DetalleVenta d : venta.getDetalles()) {
                    Producto p = bloquearProducto(cn,d.getIdProducto());
                    if (!"ACTIVO".equalsIgnoreCase(p.getEstado())) throw new SQLException("El producto "+p.getNombre()+" no está activo.");
                    if (p.getStockActual()<d.getCantidad()) throw new SQLException("Stock insuficiente de "+p.getNombre()+". Disponible: "+p.getStockActual());
                    if (p.getPrecioVenta().compareTo(d.getPrecioLista())!=0) throw new SQLException("El precio de "+p.getNombre()+" cambió. Actualiza el módulo.");
                    int idDetalle = insertarDetalle(cn,idVenta,d);
                    if (d.isManejaNumeroSerie()) registrarUnidades(cn,idDetalle,d);
                    int anterior=p.getStockActual(), nuevo=anterior-d.getCantidad();
                    actualizarStock(cn,p,nuevo);
                    insertarMovimiento(cn,d.getIdProducto(),venta.getIdUsuario(),idVenta,"SALIDA_VENTA",d.getCantidad(),anterior,nuevo,"Salida por venta #"+idVenta);
                }
                if ("CREDITO".equals(venta.getTipoVenta())) insertarCredito(cn,idVenta,venta);
                try (CallableStatement cs=cn.prepareCall("{call dbo.sp_validar_totales_venta(?)}")) { cs.setInt(1,idVenta); cs.execute(); }
                cn.commit();
                return idVenta;
            } catch (SQLException|RuntimeException ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        }
    }

    public List<Venta> listarVentas(String filtro, LocalDate desde, LocalDate hasta, String metodo, String estado) throws SQLException {
        String texto=filtro==null?"":filtro.trim();
        String met=normalizar(metodo), est=normalizar(estado);
        String sql="""
            SELECT v.id_venta,v.id_cliente,c.nombre_completo AS nombre_cliente,
                   v.id_usuario,u.nombre_completo AS nombre_usuario,
                   v.id_usuario_autoriza_descuento,v.numero_factura,v.fecha_venta,
                   v.tipo_venta,v.metodo_pago,v.subtotal,v.descuento,v.tipo_descuento,
                   v.motivo_descuento,v.total,v.monto_pagado,v.cambio,v.estado,v.observaciones
            FROM dbo.ventas v
            INNER JOIN dbo.clientes c ON c.id_cliente=v.id_cliente
            INNER JOIN dbo.usuarios u ON u.id_usuario=v.id_usuario
            WHERE (?='' OR v.numero_factura LIKE '%'+?+'%' OR c.nombre_completo LIKE '%'+?+'%' OR u.nombre_completo LIKE '%'+?+'%')
              AND (? IS NULL OR v.fecha_venta >= ?)
              AND (? IS NULL OR v.fecha_venta < DATEADD(DAY,1,?))
              AND (? IS NULL OR v.metodo_pago=?)
              AND (? IS NULL OR v.estado=?)
            ORDER BY v.fecha_venta DESC,v.id_venta DESC;
            """;
        List<Venta> lista=new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion(); PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setString(1,texto); ps.setString(2,texto); ps.setString(3,texto); ps.setString(4,texto);
            setFecha(ps,5,6,desde); setFecha(ps,7,8,hasta); setFiltro(ps,9,10,met); setFiltro(ps,11,12,est);
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) lista.add(mapearVenta(rs)); }
        }
        return lista;
    }

    public Venta obtenerVentaCompleta(int idVenta) throws SQLException {
        String cab="""
            SELECT v.id_venta,v.id_cliente,c.nombre_completo AS nombre_cliente,
                   v.id_usuario,u.nombre_completo AS nombre_usuario,
                   v.id_usuario_autoriza_descuento,v.numero_factura,v.fecha_venta,
                   v.tipo_venta,v.metodo_pago,v.subtotal,v.descuento,v.tipo_descuento,
                   v.motivo_descuento,v.total,v.monto_pagado,v.cambio,v.estado,v.observaciones
            FROM dbo.ventas v
            INNER JOIN dbo.clientes c ON c.id_cliente=v.id_cliente
            INNER JOIN dbo.usuarios u ON u.id_usuario=v.id_usuario
            WHERE v.id_venta=?;
            """;
        String det="""
            SELECT dv.id_detalle_venta,dv.id_producto,p.codigo,p.nombre,p.maneja_numero_serie,
                   dv.cantidad,dv.precio_lista,dv.descuento_unitario,dv.precio_unitario,
                   dv.subtotal,dv.dias_garantia
            FROM dbo.detalle_venta dv
            INNER JOIN dbo.productos p ON p.id_producto=dv.id_producto
            WHERE dv.id_venta=? ORDER BY dv.id_detalle_venta;
            """;
        try(Connection cn=ConexionBD.obtenerConexion()){
            Venta v;
            try(PreparedStatement ps=cn.prepareStatement(cab)){ ps.setInt(1,idVenta); try(ResultSet rs=ps.executeQuery()){ if(!rs.next()) return null; v=mapearVenta(rs); } }
            List<DetalleVenta> detalles=new ArrayList<>();
            try(PreparedStatement ps=cn.prepareStatement(det)){ ps.setInt(1,idVenta); try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    DetalleVenta d=mapearDetalle(rs);
                    if(d.isManejaNumeroSerie()) d.setUnidades(listarUnidadesVendidas(cn,d.getIdDetalleVenta()));
                    detalles.add(d);
                }
            }}
            v.setDetalles(detalles);
            return v;
        }
    }

    public void anular(int idVenta,int idUsuario) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion()){
            cn.setAutoCommit(false);
            try{
                String estado;
                try(PreparedStatement ps=cn.prepareStatement("SELECT estado FROM dbo.ventas WITH(UPDLOCK,ROWLOCK) WHERE id_venta=?")){ ps.setInt(1,idVenta); try(ResultSet rs=ps.executeQuery()){ if(!rs.next()) throw new SQLException("La venta ya no existe."); estado=rs.getString(1); }}
                if("ANULADA".equalsIgnoreCase(estado)) throw new SQLException("La venta ya está anulada.");
                for(DetalleVenta d:listarDetallesAnulacion(cn,idVenta)){
                    Producto p=bloquearProducto(cn,d.getIdProducto());
                    int anterior=p.getStockActual(), nuevo=anterior+d.getCantidad();
                    if(d.isManejaNumeroSerie()) restaurarUnidades(cn,d.getIdDetalleVenta());
                    actualizarStock(cn,p,nuevo);
                    insertarMovimiento(cn,d.getIdProducto(),idUsuario,idVenta,"DEVOLUCION_CLIENTE",d.getCantidad(),anterior,nuevo,"Reversión por anulación de venta #"+idVenta);
                }
                try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.ventas SET estado='ANULADA' WHERE id_venta=?")){ ps.setInt(1,idVenta); ps.executeUpdate(); }
                try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.creditos SET estado='ANULADO' WHERE id_venta=? AND estado<>'PAGADO'")){ ps.setInt(1,idVenta); ps.executeUpdate(); }
                cn.commit();
            }catch(SQLException|RuntimeException ex){ cn.rollback(); throw ex; }
            finally{ cn.setAutoCommit(true); }
        }
    }

    private int insertarVenta(Connection cn,Venta v)throws SQLException{
        String sql="""
            INSERT INTO dbo.ventas(id_cliente,id_usuario,id_usuario_autoriza_descuento,numero_factura,fecha_venta,tipo_venta,metodo_pago,subtotal,descuento,tipo_descuento,motivo_descuento,total,monto_pagado,cambio,estado,observaciones)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);
            """;
        try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,v.getIdCliente()); ps.setInt(2,v.getIdUsuario());
            if(v.getIdUsuarioAutorizaDescuento()==null) ps.setNull(3,Types.INTEGER); else ps.setInt(3,v.getIdUsuarioAutorizaDescuento());
            ps.setString(4,v.getNumeroFactura()); ps.setTimestamp(5,Timestamp.valueOf(v.getFechaVenta())); ps.setString(6,v.getTipoVenta()); ps.setString(7,v.getMetodoPago());
            ps.setBigDecimal(8,v.getSubtotal()); ps.setBigDecimal(9,v.getDescuento()); setNulo(ps,10,v.getTipoDescuento()); setNulo(ps,11,v.getMotivoDescuento());
            ps.setBigDecimal(12,v.getTotal()); ps.setBigDecimal(13,v.getMontoPagado()); ps.setBigDecimal(14,v.getCambio()); ps.setString(15,v.getEstado()); setNulo(ps,16,v.getObservaciones());
            ps.executeUpdate(); try(ResultSet rs=ps.getGeneratedKeys()){ if(rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("SQL Server no devolvió el id de la venta.");
    }

    private int insertarDetalle(Connection cn,int idVenta,DetalleVenta d)throws SQLException{
        String sql="INSERT INTO dbo.detalle_venta(id_venta,id_producto,cantidad,precio_lista,descuento_unitario,precio_unitario,subtotal,dias_garantia) VALUES(?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,idVenta); ps.setInt(2,d.getIdProducto()); ps.setInt(3,d.getCantidad()); ps.setBigDecimal(4,d.getPrecioLista()); ps.setBigDecimal(5,d.getDescuentoUnitario()); ps.setBigDecimal(6,d.getPrecioUnitario()); ps.setBigDecimal(7,d.getSubtotal()); ps.setInt(8,d.getDiasGarantia());
            ps.executeUpdate(); try(ResultSet rs=ps.getGeneratedKeys()){ if(rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("No se generó el detalle de venta.");
    }

    private void registrarUnidades(Connection cn,int idDetalle,DetalleVenta d)throws SQLException{
        if(d.getUnidades().size()!=d.getCantidad()) throw new SQLException("Faltan números de serie de "+d.getNombreProducto());
        String validar="SELECT id_unidad FROM dbo.unidades_producto WITH(UPDLOCK,ROWLOCK) WHERE id_unidad=? AND id_producto=? AND estado='DISPONIBLE'";
        try(PreparedStatement pv=cn.prepareStatement(validar); PreparedStatement pr=cn.prepareStatement("INSERT INTO dbo.detalle_venta_unidades(id_detalle_venta,id_unidad) VALUES(?,?)"); PreparedStatement pu=cn.prepareStatement("UPDATE dbo.unidades_producto SET estado='VENDIDO',fecha_salida=SYSDATETIME() WHERE id_unidad=?")){
            for(UnidadProducto u:d.getUnidades()){
                pv.setInt(1,u.getIdUnidad()); pv.setInt(2,d.getIdProducto()); try(ResultSet rs=pv.executeQuery()){ if(!rs.next()) throw new SQLException("Una unidad seleccionada ya no está disponible."); }
                pr.setInt(1,idDetalle); pr.setInt(2,u.getIdUnidad()); pr.executeUpdate(); pu.setInt(1,u.getIdUnidad()); pu.executeUpdate();
            }
        }
    }

    private Producto bloquearProducto(Connection cn,int idProducto)throws SQLException{
        String sql="""
            SELECT p.id_producto,p.id_categoria,c.nombre AS nombre_categoria,p.codigo,p.nombre,p.descripcion,p.marca,p.modelo,p.precio_compra,p.precio_venta,p.stock_actual,p.stock_minimo,p.maneja_numero_serie,p.estado,p.fecha_registro
            FROM dbo.productos p WITH(UPDLOCK,ROWLOCK)
            INNER JOIN dbo.categorias_producto c ON c.id_categoria=p.id_categoria
            WHERE p.id_producto=?;
            """;
        try(PreparedStatement ps=cn.prepareStatement(sql)){ ps.setInt(1,idProducto); try(ResultSet rs=ps.executeQuery()){ if(!rs.next()) throw new SQLException("El producto ya no existe."); return mapearProducto(rs); } }
    }

    private void actualizarStock(Connection cn,Producto p,int nuevo)throws SQLException{
        String estado="INACTIVO".equalsIgnoreCase(p.getEstado())?"INACTIVO":nuevo==0?"AGOTADO":"ACTIVO";
        try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.productos SET stock_actual=?,estado=? WHERE id_producto=?")){ ps.setInt(1,nuevo); ps.setString(2,estado); ps.setInt(3,p.getIdProducto()); ps.executeUpdate(); }
    }

    private void insertarMovimiento(Connection cn,int idProducto,int idUsuario,int idVenta,String tipo,int cantidad,int anterior,int nuevo,String motivo)throws SQLException{
        String sql="INSERT INTO dbo.movimientos_inventario(id_producto,id_usuario,id_compra,id_venta,id_orden,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,motivo) VALUES(?,?,NULL,?,NULL,?,?,?,?,?)";
        try(PreparedStatement ps=cn.prepareStatement(sql)){ ps.setInt(1,idProducto); ps.setInt(2,idUsuario); ps.setInt(3,idVenta); ps.setString(4,tipo); ps.setInt(5,cantidad); ps.setInt(6,anterior); ps.setInt(7,nuevo); ps.setString(8,motivo); ps.executeUpdate(); }
    }

    private void insertarCredito(Connection cn,int idVenta,Venta v)throws SQLException{
        String sql="INSERT INTO dbo.creditos(id_venta,id_cliente,fecha_inicio,fecha_vencimiento,total_credito,saldo_pendiente,monto_cuota,estado,observaciones) VALUES(?,?,?,?,?,?,?,'PENDIENTE',?)";
        try(PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setInt(1,idVenta); ps.setInt(2,v.getIdCliente()); ps.setDate(3,Date.valueOf(v.getFechaVenta().toLocalDate()));
            if(v.getFechaVencimientoCredito()==null) ps.setNull(4,Types.DATE); else ps.setDate(4,Date.valueOf(v.getFechaVencimientoCredito()));
            ps.setBigDecimal(5,v.getTotal()); ps.setBigDecimal(6,v.getTotal()); if(v.getMontoCuotaCredito()==null) ps.setNull(7,Types.DECIMAL); else ps.setBigDecimal(7,v.getMontoCuotaCredito()); setNulo(ps,8,v.getObservaciones()); ps.executeUpdate();
        }
    }

    private List<UnidadProducto> listarUnidadesVendidas(Connection cn,int idDetalle)throws SQLException{
        List<UnidadProducto> lista=new ArrayList<>();
        String sql="SELECT u.id_unidad,u.id_producto,u.numero_serie,u.codigo_interno,u.estado FROM dbo.detalle_venta_unidades d INNER JOIN dbo.unidades_producto u ON u.id_unidad=d.id_unidad WHERE d.id_detalle_venta=?";
        try(PreparedStatement ps=cn.prepareStatement(sql)){ ps.setInt(1,idDetalle); try(ResultSet rs=ps.executeQuery()){ while(rs.next()){ UnidadProducto u=new UnidadProducto(); u.setIdUnidad(rs.getInt(1)); u.setIdProducto(rs.getInt(2)); u.setNumeroSerie(rs.getString(3)); u.setCodigoInterno(rs.getString(4)); u.setEstado(rs.getString(5)); lista.add(u); } } }
        return lista;
    }

    private List<DetalleVenta> listarDetallesAnulacion(Connection cn,int idVenta)throws SQLException{
        List<DetalleVenta> lista=new ArrayList<>();
        String sql="SELECT dv.id_detalle_venta,dv.id_producto,p.codigo,p.nombre,p.maneja_numero_serie,dv.cantidad,dv.precio_lista,dv.descuento_unitario,dv.precio_unitario,dv.subtotal,dv.dias_garantia FROM dbo.detalle_venta dv INNER JOIN dbo.productos p ON p.id_producto=dv.id_producto WHERE dv.id_venta=?";
        try(PreparedStatement ps=cn.prepareStatement(sql)){ ps.setInt(1,idVenta); try(ResultSet rs=ps.executeQuery()){ while(rs.next()) lista.add(mapearDetalle(rs)); } }
        return lista;
    }

    private void restaurarUnidades(Connection cn,int idDetalle)throws SQLException{
        List<Integer> ids=new ArrayList<>();
        try(PreparedStatement ps=cn.prepareStatement("SELECT u.id_unidad FROM dbo.detalle_venta_unidades d INNER JOIN dbo.unidades_producto u WITH(UPDLOCK,ROWLOCK) ON u.id_unidad=d.id_unidad WHERE d.id_detalle_venta=?")){ ps.setInt(1,idDetalle); try(ResultSet rs=ps.executeQuery()){ while(rs.next()) ids.add(rs.getInt(1)); } }
        try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.unidades_producto SET estado='DISPONIBLE',fecha_salida=NULL WHERE id_unidad=?")){ for(Integer id:ids){ ps.setInt(1,id); ps.addBatch(); } ps.executeBatch(); }
        try(PreparedStatement ps=cn.prepareStatement("DELETE FROM dbo.detalle_venta_unidades WHERE id_detalle_venta=?")){ ps.setInt(1,idDetalle); ps.executeUpdate(); }
    }

    private Producto mapearProducto(ResultSet rs)throws SQLException{
        Producto p=new Producto(); p.setIdProducto(rs.getInt("id_producto")); p.setIdCategoria(rs.getInt("id_categoria")); p.setNombreCategoria(rs.getString("nombre_categoria")); p.setCodigo(rs.getString("codigo")); p.setNombre(rs.getString("nombre")); p.setDescripcion(rs.getString("descripcion")); p.setMarca(rs.getString("marca")); p.setModelo(rs.getString("modelo")); p.setPrecioCompra(rs.getBigDecimal("precio_compra")); p.setPrecioVenta(rs.getBigDecimal("precio_venta")); p.setStockActual(rs.getInt("stock_actual")); p.setStockMinimo(rs.getInt("stock_minimo")); p.setManejaNumeroSerie(rs.getBoolean("maneja_numero_serie")); p.setEstado(rs.getString("estado")); Timestamp f=rs.getTimestamp("fecha_registro"); if(f!=null)p.setFechaRegistro(f.toLocalDateTime()); return p;
    }

    private DetalleVenta mapearDetalle(ResultSet rs)throws SQLException{
        DetalleVenta d=new DetalleVenta(); d.setIdDetalleVenta(rs.getInt("id_detalle_venta")); d.setIdProducto(rs.getInt("id_producto")); d.setCodigoProducto(rs.getString("codigo")); d.setNombreProducto(rs.getString("nombre")); d.setManejaNumeroSerie(rs.getBoolean("maneja_numero_serie")); d.setCantidad(rs.getInt("cantidad")); d.setPrecioLista(rs.getBigDecimal("precio_lista")); d.setDescuentoUnitario(rs.getBigDecimal("descuento_unitario")); d.setPrecioUnitario(rs.getBigDecimal("precio_unitario")); d.setSubtotal(rs.getBigDecimal("subtotal")); d.setDiasGarantia(rs.getInt("dias_garantia")); return d;
    }

    private Venta mapearVenta(ResultSet rs)throws SQLException{
        Venta v=new Venta(); v.setIdVenta(rs.getInt("id_venta")); v.setIdCliente(rs.getInt("id_cliente")); v.setNombreCliente(rs.getString("nombre_cliente")); v.setIdUsuario(rs.getInt("id_usuario")); v.setNombreUsuario(rs.getString("nombre_usuario")); int a=rs.getInt("id_usuario_autoriza_descuento"); if(!rs.wasNull())v.setIdUsuarioAutorizaDescuento(a); v.setNumeroFactura(rs.getString("numero_factura")); Timestamp f=rs.getTimestamp("fecha_venta"); if(f!=null)v.setFechaVenta(f.toLocalDateTime()); v.setTipoVenta(rs.getString("tipo_venta")); v.setMetodoPago(rs.getString("metodo_pago")); v.setSubtotal(rs.getBigDecimal("subtotal")); v.setDescuento(rs.getBigDecimal("descuento")); v.setTipoDescuento(rs.getString("tipo_descuento")); v.setMotivoDescuento(rs.getString("motivo_descuento")); v.setTotal(rs.getBigDecimal("total")); v.setMontoPagado(rs.getBigDecimal("monto_pagado")); v.setCambio(rs.getBigDecimal("cambio")); v.setEstado(rs.getString("estado")); v.setObservaciones(rs.getString("observaciones")); return v;
    }

    private void setFecha(PreparedStatement ps,int p1,int p2,LocalDate f)throws SQLException{ if(f==null){ps.setNull(p1,Types.DATE);ps.setNull(p2,Types.DATE);}else{Date d=Date.valueOf(f);ps.setDate(p1,d);ps.setDate(p2,d);} }
    private void setFiltro(PreparedStatement ps,int p1,int p2,String v)throws SQLException{ if(v==null){ps.setNull(p1,Types.VARCHAR);ps.setNull(p2,Types.VARCHAR);}else{ps.setString(p1,v);ps.setString(p2,v);} }
    private String normalizar(String v){ return v==null||v.isBlank()||"TODOS".equalsIgnoreCase(v)?null:v.trim().toUpperCase(); }
    private void setNulo(PreparedStatement ps,int pos,String v)throws SQLException{ if(v==null||v.trim().isEmpty())ps.setNull(pos,Types.NVARCHAR);else ps.setString(pos,v.trim()); }
}
