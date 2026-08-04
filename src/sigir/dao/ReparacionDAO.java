package sigir.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import sigir.conexion.ConexionBD;
import sigir.modelo.*;

public class ReparacionDAO {

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
        try (Connection cn=ConexionBD.obtenerConexion(); PreparedStatement ps=cn.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while (rs.next()) {
                Cliente c=new Cliente();
                c.setIdCliente(rs.getInt("id_cliente"));
                c.setIdTipoCliente(rs.getInt("id_tipo_cliente"));
                c.setNombreTipoCliente(rs.getString("nombre_tipo_cliente"));
                c.setNumeroIdentidad(rs.getString("numero_identidad"));
                c.setNombreCompleto(rs.getString("nombre_completo"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setDireccion(rs.getString("direccion"));
                c.setEstado(rs.getString("estado"));
                Timestamp f=rs.getTimestamp("fecha_registro");
                if(f!=null)c.setFechaRegistro(f.toLocalDateTime());
                lista.add(c);
            }
        }
        return lista;
    }

    public List<EquipoCliente> listarEquiposCliente(int idCliente) throws SQLException {
        String sql="""
                SELECT e.id_equipo,e.id_cliente,c.nombre_completo AS nombre_cliente,
                       e.tipo_equipo,e.marca,e.modelo,e.numero_serie,e.color,
                       e.accesorios_recibidos,e.observaciones,e.fecha_registro
                FROM dbo.equipos_cliente e
                INNER JOIN dbo.clientes c ON c.id_cliente=e.id_cliente
                WHERE e.id_cliente=? ORDER BY e.fecha_registro DESC;
                """;
        List<EquipoCliente> lista=new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setInt(1,idCliente);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())lista.add(mapearEquipo(rs));}
        }
        return lista;
    }

    public List<Producto> listarRepuestosDisponibles() throws SQLException {
        String sql="""
                SELECT p.id_producto,p.id_categoria,c.nombre AS nombre_categoria,
                       p.codigo,p.nombre,p.descripcion,p.marca,p.modelo,
                       p.precio_compra,p.precio_venta,p.stock_actual,p.stock_minimo,
                       p.maneja_numero_serie,p.estado,p.fecha_registro
                FROM dbo.productos p
                INNER JOIN dbo.categorias_producto c ON c.id_categoria=p.id_categoria
                WHERE p.estado='ACTIVO' AND p.stock_actual>0 AND p.maneja_numero_serie=0
                ORDER BY p.nombre;
                """;
        List<Producto> lista=new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next())lista.add(mapearProducto(rs));
        }
        return lista;
    }

    public boolean existeNumeroOrden(String numero) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement("SELECT COUNT(*) FROM dbo.ordenes_servicio WHERE numero_orden=?")){
            ps.setString(1,numero);
            try(ResultSet rs=ps.executeQuery()){return rs.next()&&rs.getInt(1)>0;}
        }
    }

    public int registrarOrden(OrdenServicio orden, EquipoCliente equipo, boolean nuevo) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion()){
            cn.setAutoCommit(false);
            try{
                int idEquipo=nuevo?insertarEquipo(cn,equipo):equipo.getIdEquipo();
                orden.setIdEquipo(idEquipo);
                int idOrden=insertarOrden(cn,orden);
                insertarHistorial(cn,idOrden,orden.getIdUsuarioRecibe(),null,"RECIBIDO","Orden recibida y registrada en el sistema.");
                cn.commit();return idOrden;
            }catch(SQLException|RuntimeException ex){cn.rollback();throw ex;}
            finally{cn.setAutoCommit(true);}
        }
    }

    public List<OrdenServicio> listarOrdenes(String filtro,String estado) throws SQLException {
        String q=filtro==null?"":filtro.trim();
        String e=estado==null||estado.isBlank()||"TODOS".equalsIgnoreCase(estado)?null:estado.trim().toUpperCase();
        String sql="""
                SELECT o.id_orden,o.id_equipo,o.id_usuario_recibe,u.nombre_completo AS nombre_usuario_recibe,
                       o.numero_orden,o.fecha_recepcion,o.problema_reportado,o.diagnostico,o.trabajo_realizado,
                       o.costo_estimado,o.costo_final,o.estado,o.fecha_prometida,o.fecha_entrega,o.garantia_hasta,o.observaciones,
                       ec.id_cliente,c.nombre_completo AS nombre_cliente,c.numero_identidad,c.telefono AS telefono_cliente,
                       ec.tipo_equipo,ec.marca AS marca_equipo,ec.modelo AS modelo_equipo,
                       ec.numero_serie AS numero_serie_equipo,ec.color AS color_equipo,ec.accesorios_recibidos
                FROM dbo.ordenes_servicio o
                INNER JOIN dbo.equipos_cliente ec ON ec.id_equipo=o.id_equipo
                INNER JOIN dbo.clientes c ON c.id_cliente=ec.id_cliente
                INNER JOIN dbo.usuarios u ON u.id_usuario=o.id_usuario_recibe
                WHERE (?='' OR o.numero_orden LIKE '%'+?+'%' OR c.nombre_completo LIKE '%'+?+'%'
                       OR c.numero_identidad LIKE '%'+?+'%' OR ec.tipo_equipo LIKE '%'+?+'%'
                       OR ec.marca LIKE '%'+?+'%' OR ec.modelo LIKE '%'+?+'%' OR ec.numero_serie LIKE '%'+?+'%')
                  AND (? IS NULL OR o.estado=?)
                ORDER BY CASE WHEN o.estado IN('ENTREGADO','CANCELADO') THEN 1 ELSE 0 END,
                         o.fecha_recepcion DESC,o.id_orden DESC;
                """;
        List<OrdenServicio> lista=new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=1;i<=8;i++)ps.setString(i,q);
            if(e==null){ps.setNull(9,Types.VARCHAR);ps.setNull(10,Types.VARCHAR);}else{ps.setString(9,e);ps.setString(10,e);}
            try(ResultSet rs=ps.executeQuery()){while(rs.next())lista.add(mapearOrden(rs));}
        }
        return lista;
    }

    public OrdenServicio obtenerOrdenCompleta(int idOrden) throws SQLException {
        String sql="""
                SELECT o.id_orden,o.id_equipo,o.id_usuario_recibe,u.nombre_completo AS nombre_usuario_recibe,
                       o.numero_orden,o.fecha_recepcion,o.problema_reportado,o.diagnostico,o.trabajo_realizado,
                       o.costo_estimado,o.costo_final,o.estado,o.fecha_prometida,o.fecha_entrega,o.garantia_hasta,o.observaciones,
                       ec.id_cliente,c.nombre_completo AS nombre_cliente,c.numero_identidad,c.telefono AS telefono_cliente,
                       ec.tipo_equipo,ec.marca AS marca_equipo,ec.modelo AS modelo_equipo,
                       ec.numero_serie AS numero_serie_equipo,ec.color AS color_equipo,ec.accesorios_recibidos
                FROM dbo.ordenes_servicio o
                INNER JOIN dbo.equipos_cliente ec ON ec.id_equipo=o.id_equipo
                INNER JOIN dbo.clientes c ON c.id_cliente=ec.id_cliente
                INNER JOIN dbo.usuarios u ON u.id_usuario=o.id_usuario_recibe
                WHERE o.id_orden=?;
                """;
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setInt(1,idOrden);
            try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())return null;
                OrdenServicio o=mapearOrden(rs);
                o.setRepuestos(listarRepuestos(cn,idOrden));
                o.setHistorial(listarHistorialOrden(cn,idOrden));
                return o;
            }
        }
    }

    public void actualizarOrden(OrdenServicio orden,int idUsuario,String descripcion) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion()){
            cn.setAutoCommit(false);
            try{
                String anterior=bloquearEstado(cn,orden.getIdOrden());
                if("ENTREGADO".equals(anterior)||"CANCELADO".equals(anterior))throw new SQLException("La orden finalizada no puede modificarse.");
                String sql="""
                        UPDATE dbo.ordenes_servicio SET diagnostico=?,trabajo_realizado=?,costo_estimado=?,costo_final=?,
                            estado=?,fecha_prometida=?,fecha_entrega=CASE WHEN ?='ENTREGADO' THEN COALESCE(fecha_entrega,SYSDATETIME()) ELSE fecha_entrega END,
                            garantia_hasta=?,observaciones=? WHERE id_orden=?;
                        """;
                try(PreparedStatement ps=cn.prepareStatement(sql)){
                    setText(ps,1,orden.getDiagnostico());setText(ps,2,orden.getTrabajoRealizado());
                    ps.setBigDecimal(3,orden.getCostoEstimado());ps.setBigDecimal(4,orden.getCostoFinal());ps.setString(5,orden.getEstado());
                    setDate(ps,6,orden.getFechaPrometida());ps.setString(7,orden.getEstado());setDate(ps,8,orden.getGarantiaHasta());
                    setText(ps,9,orden.getObservaciones());ps.setInt(10,orden.getIdOrden());ps.executeUpdate();
                }
                if(!anterior.equals(orden.getEstado())||(descripcion!=null&&!descripcion.isBlank())){
                    String d=descripcion==null||descripcion.isBlank()?"Estado actualizado de "+anterior+" a "+orden.getEstado()+".":descripcion.trim();
                    insertarHistorial(cn,orden.getIdOrden(),idUsuario,anterior,orden.getEstado(),d);
                }
                cn.commit();
            }catch(SQLException|RuntimeException ex){cn.rollback();throw ex;}
            finally{cn.setAutoCommit(true);}
        }
    }

    public void agregarRepuesto(int idOrden,int idUsuario,RepuestoOrden r) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion()){
            cn.setAutoCommit(false);
            try{
                String estado=bloquearEstado(cn,idOrden);
                if("ENTREGADO".equals(estado)||"CANCELADO".equals(estado))throw new SQLException("No se pueden agregar repuestos a una orden finalizada.");
                Producto p=bloquearProducto(cn,r.getIdProducto());
                if(p.isManejaNumeroSerie())throw new SQLException("Los productos con número de serie no pueden utilizarse como repuesto desde este módulo.");
                if(p.getStockActual()<r.getCantidad())throw new SQLException("Stock insuficiente de "+p.getNombre()+". Disponible: "+p.getStockActual()+".");
                r.recalcularSubtotal();
                try(PreparedStatement ps=cn.prepareStatement("INSERT INTO dbo.repuestos_orden(id_orden,id_producto,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)")){
                    ps.setInt(1,idOrden);ps.setInt(2,r.getIdProducto());ps.setInt(3,r.getCantidad());ps.setBigDecimal(4,r.getPrecioUnitario());ps.setBigDecimal(5,r.getSubtotal());ps.executeUpdate();
                }
                int anterior=p.getStockActual(),nuevo=anterior-r.getCantidad();
                actualizarStock(cn,p,nuevo);
                try(PreparedStatement ps=cn.prepareStatement("""
                        INSERT INTO dbo.movimientos_inventario(id_producto,id_usuario,id_compra,id_venta,id_orden,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,motivo)
                        VALUES(?,?,NULL,NULL,?,'SALIDA_REPARACION',?,?,?,?)
                        """)){
                    ps.setInt(1,r.getIdProducto());ps.setInt(2,idUsuario);ps.setInt(3,idOrden);ps.setInt(4,r.getCantidad());ps.setInt(5,anterior);ps.setInt(6,nuevo);ps.setString(7,"Repuesto utilizado en orden "+idOrden);ps.executeUpdate();
                }
                insertarHistorial(cn,idOrden,idUsuario,estado,estado,"Se asignó el repuesto "+p.getCodigo()+" - "+p.getNombre()+", cantidad "+r.getCantidad()+".");
                cn.commit();
            }catch(SQLException|RuntimeException ex){cn.rollback();throw ex;}
            finally{cn.setAutoCommit(true);}
        }
    }

    public void cancelarOrden(int idOrden,int idUsuario,String motivo) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion()){
            cn.setAutoCommit(false);
            try{
                String anterior=bloquearEstado(cn,idOrden);
                if("ENTREGADO".equals(anterior))throw new SQLException("Una orden entregada no puede cancelarse.");
                if("CANCELADO".equals(anterior))throw new SQLException("La orden ya está cancelada.");
                for(RepuestoOrden r:listarRepuestos(cn,idOrden)){
                    Producto p=bloquearProducto(cn,r.getIdProducto());
                    int stockAnterior=p.getStockActual(),stockNuevo=stockAnterior+r.getCantidad();
                    actualizarStock(cn,p,stockNuevo);
                    try(PreparedStatement ps=cn.prepareStatement("""
                            INSERT INTO dbo.movimientos_inventario(id_producto,id_usuario,id_compra,id_venta,id_orden,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,motivo)
                            VALUES(?,?,NULL,NULL,?,'AJUSTE_ENTRADA',?,?,?,?)
                            """)){
                        ps.setInt(1,r.getIdProducto());ps.setInt(2,idUsuario);ps.setInt(3,idOrden);ps.setInt(4,r.getCantidad());ps.setInt(5,stockAnterior);ps.setInt(6,stockNuevo);ps.setString(7,"Reversión de repuesto por cancelación de orden "+idOrden);ps.executeUpdate();
                    }
                }
                try(PreparedStatement ps=cn.prepareStatement("DELETE FROM dbo.repuestos_orden WHERE id_orden=?")){ps.setInt(1,idOrden);ps.executeUpdate();}
                try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.ordenes_servicio SET estado='CANCELADO',fecha_entrega=NULL WHERE id_orden=?")){ps.setInt(1,idOrden);ps.executeUpdate();}
                insertarHistorial(cn,idOrden,idUsuario,anterior,"CANCELADO",motivo);
                cn.commit();
            }catch(SQLException|RuntimeException ex){cn.rollback();throw ex;}
            finally{cn.setAutoCommit(true);}
        }
    }

    public List<HistorialServicio> listarHistorialGeneral(String filtro) throws SQLException {
        String q=filtro==null?"":filtro.trim();
        String sql="""
                SELECT h.id_historial,h.id_orden,h.id_usuario,o.numero_orden,c.nombre_completo AS nombre_cliente,
                       u.nombre_completo AS nombre_usuario,h.fecha_evento,h.estado_anterior,h.estado_nuevo,h.descripcion
                FROM dbo.historial_servicio h
                INNER JOIN dbo.ordenes_servicio o ON o.id_orden=h.id_orden
                INNER JOIN dbo.equipos_cliente e ON e.id_equipo=o.id_equipo
                INNER JOIN dbo.clientes c ON c.id_cliente=e.id_cliente
                INNER JOIN dbo.usuarios u ON u.id_usuario=h.id_usuario
                WHERE ?='' OR o.numero_orden LIKE '%'+?+'%' OR c.nombre_completo LIKE '%'+?+'%'
                       OR u.nombre_completo LIKE '%'+?+'%' OR h.descripcion LIKE '%'+?+'%'
                ORDER BY h.fecha_evento DESC,h.id_historial DESC;
                """;
        List<HistorialServicio> lista=new ArrayList<>();
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=1;i<=5;i++)ps.setString(i,q);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())lista.add(mapearHistorial(rs));}
        }
        return lista;
    }

    public int[] contarIndicadores() throws SQLException {
        String sql = """
                SELECT
                    SUM(CASE WHEN estado='RECIBIDO' THEN 1 ELSE 0 END)
                        AS recibidos,
                    SUM(CASE WHEN estado='EN_REPARACION' THEN 1 ELSE 0 END)
                        AS en_reparacion,
                    SUM(CASE WHEN estado='LISTO' THEN 1 ELSE 0 END)
                        AS listos
                FROM dbo.ordenes_servicio;
                """;

        try(Connection cn=ConexionBD.obtenerConexion();
            PreparedStatement ps=cn.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()){

            if(!rs.next()){
                return new int[]{0,0,0};
            }

            return new int[]{
                rs.getInt("recibidos"),
                rs.getInt("en_reparacion"),
                rs.getInt("listos")
            };
        }
    }

    public int contarEstado(String estado) throws SQLException {
        try(Connection cn=ConexionBD.obtenerConexion();PreparedStatement ps=cn.prepareStatement("SELECT COUNT(*) FROM dbo.ordenes_servicio WHERE estado=?")){
            ps.setString(1,estado);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}
        }
    }

    private int insertarEquipo(Connection cn,EquipoCliente e) throws SQLException {
        String sql="INSERT INTO dbo.equipos_cliente(id_cliente,tipo_equipo,marca,modelo,numero_serie,color,accesorios_recibidos,observaciones) VALUES(?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,e.getIdCliente());ps.setString(2,e.getTipoEquipo());setText(ps,3,e.getMarca());setText(ps,4,e.getModelo());setText(ps,5,e.getNumeroSerie());setText(ps,6,e.getColor());setText(ps,7,e.getAccesoriosRecibidos());setText(ps,8,e.getObservaciones());ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())return rs.getInt(1);}
        }
        throw new SQLException("SQL Server no devolvió el id del equipo.");
    }

    private int insertarOrden(Connection cn,OrdenServicio o) throws SQLException {
        String sql="""
                INSERT INTO dbo.ordenes_servicio(id_equipo,id_usuario_recibe,numero_orden,fecha_recepcion,problema_reportado,
                    diagnostico,trabajo_realizado,costo_estimado,costo_final,estado,fecha_prometida,fecha_entrega,garantia_hasta,observaciones)
                VALUES(?,?,?,?,?,NULL,NULL,?,0,'RECIBIDO',?,NULL,NULL,?)
                """;
        try(PreparedStatement ps=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,o.getIdEquipo());ps.setInt(2,o.getIdUsuarioRecibe());ps.setString(3,o.getNumeroOrden());ps.setTimestamp(4,Timestamp.valueOf(o.getFechaRecepcion()));ps.setString(5,o.getProblemaReportado());ps.setBigDecimal(6,o.getCostoEstimado());setDate(ps,7,o.getFechaPrometida());setText(ps,8,o.getObservaciones());ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())return rs.getInt(1);}
        }
        throw new SQLException("SQL Server no devolvió el id de la orden.");
    }

    private String bloquearEstado(Connection cn,int idOrden) throws SQLException {
        try(PreparedStatement ps=cn.prepareStatement("SELECT estado FROM dbo.ordenes_servicio WITH(UPDLOCK,ROWLOCK) WHERE id_orden=?")){
            ps.setInt(1,idOrden);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("La orden seleccionada ya no existe.");return rs.getString(1);}
        }
    }

    private Producto bloquearProducto(Connection cn,int idProducto) throws SQLException {
        String sql="""
                SELECT p.id_producto,p.id_categoria,c.nombre AS nombre_categoria,p.codigo,p.nombre,p.descripcion,p.marca,p.modelo,
                       p.precio_compra,p.precio_venta,p.stock_actual,p.stock_minimo,p.maneja_numero_serie,p.estado,p.fecha_registro
                FROM dbo.productos p WITH(UPDLOCK,ROWLOCK)
                INNER JOIN dbo.categorias_producto c ON c.id_categoria=p.id_categoria WHERE p.id_producto=?
                """;
        try(PreparedStatement ps=cn.prepareStatement(sql)){ps.setInt(1,idProducto);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("El producto ya no existe.");return mapearProducto(rs);}}
    }

    private void actualizarStock(Connection cn,Producto p,int nuevo) throws SQLException {
        String estado="INACTIVO".equalsIgnoreCase(p.getEstado())?"INACTIVO":nuevo==0?"AGOTADO":"ACTIVO";
        try(PreparedStatement ps=cn.prepareStatement("UPDATE dbo.productos SET stock_actual=?,estado=? WHERE id_producto=?")){ps.setInt(1,nuevo);ps.setString(2,estado);ps.setInt(3,p.getIdProducto());ps.executeUpdate();}
    }

    private void insertarHistorial(Connection cn,int idOrden,int idUsuario,String anterior,String nuevo,String descripcion) throws SQLException {
        try(PreparedStatement ps=cn.prepareStatement("INSERT INTO dbo.historial_servicio(id_orden,id_usuario,estado_anterior,estado_nuevo,descripcion) VALUES(?,?,?,?,?)")){
            ps.setInt(1,idOrden);ps.setInt(2,idUsuario);if(anterior==null)ps.setNull(3,Types.VARCHAR);else ps.setString(3,anterior);ps.setString(4,nuevo);ps.setString(5,descripcion);ps.executeUpdate();
        }
    }

    private List<RepuestoOrden> listarRepuestos(Connection cn,int idOrden) throws SQLException {
        String sql="""
                SELECT r.id_repuesto_orden,r.id_orden,r.id_producto,p.codigo,p.nombre,r.cantidad,r.precio_unitario,r.subtotal,r.fecha_asignacion
                FROM dbo.repuestos_orden r INNER JOIN dbo.productos p ON p.id_producto=r.id_producto
                WHERE r.id_orden=? ORDER BY r.fecha_asignacion,r.id_repuesto_orden
                """;
        List<RepuestoOrden> lista=new ArrayList<>();
        try(PreparedStatement ps=cn.prepareStatement(sql)){ps.setInt(1,idOrden);try(ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                RepuestoOrden r=new RepuestoOrden();r.setIdRepuestoOrden(rs.getInt("id_repuesto_orden"));r.setIdOrden(rs.getInt("id_orden"));r.setIdProducto(rs.getInt("id_producto"));r.setCodigoProducto(rs.getString("codigo"));r.setNombreProducto(rs.getString("nombre"));r.setCantidad(rs.getInt("cantidad"));r.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));r.setSubtotal(rs.getBigDecimal("subtotal"));Timestamp f=rs.getTimestamp("fecha_asignacion");if(f!=null)r.setFechaAsignacion(f.toLocalDateTime());lista.add(r);
            }}}
        return lista;
    }

    private List<HistorialServicio> listarHistorialOrden(Connection cn,int idOrden) throws SQLException {
        String sql="""
                SELECT h.id_historial,h.id_orden,h.id_usuario,o.numero_orden,c.nombre_completo AS nombre_cliente,
                       u.nombre_completo AS nombre_usuario,h.fecha_evento,h.estado_anterior,h.estado_nuevo,h.descripcion
                FROM dbo.historial_servicio h
                INNER JOIN dbo.ordenes_servicio o ON o.id_orden=h.id_orden
                INNER JOIN dbo.equipos_cliente e ON e.id_equipo=o.id_equipo
                INNER JOIN dbo.clientes c ON c.id_cliente=e.id_cliente
                INNER JOIN dbo.usuarios u ON u.id_usuario=h.id_usuario
                WHERE h.id_orden=? ORDER BY h.fecha_evento DESC,h.id_historial DESC
                """;
        List<HistorialServicio> lista=new ArrayList<>();
        try(PreparedStatement ps=cn.prepareStatement(sql)){ps.setInt(1,idOrden);try(ResultSet rs=ps.executeQuery()){while(rs.next())lista.add(mapearHistorial(rs));}}
        return lista;
    }

    private EquipoCliente mapearEquipo(ResultSet rs) throws SQLException {
        EquipoCliente e=new EquipoCliente();e.setIdEquipo(rs.getInt("id_equipo"));e.setIdCliente(rs.getInt("id_cliente"));e.setNombreCliente(rs.getString("nombre_cliente"));e.setTipoEquipo(rs.getString("tipo_equipo"));e.setMarca(rs.getString("marca"));e.setModelo(rs.getString("modelo"));e.setNumeroSerie(rs.getString("numero_serie"));e.setColor(rs.getString("color"));e.setAccesoriosRecibidos(rs.getString("accesorios_recibidos"));e.setObservaciones(rs.getString("observaciones"));Timestamp f=rs.getTimestamp("fecha_registro");if(f!=null)e.setFechaRegistro(f.toLocalDateTime());return e;
    }

    private OrdenServicio mapearOrden(ResultSet rs) throws SQLException {
        OrdenServicio o=new OrdenServicio();o.setIdOrden(rs.getInt("id_orden"));o.setIdEquipo(rs.getInt("id_equipo"));o.setIdUsuarioRecibe(rs.getInt("id_usuario_recibe"));o.setNombreUsuarioRecibe(rs.getString("nombre_usuario_recibe"));o.setNumeroOrden(rs.getString("numero_orden"));Timestamp fr=rs.getTimestamp("fecha_recepcion");if(fr!=null)o.setFechaRecepcion(fr.toLocalDateTime());o.setProblemaReportado(rs.getString("problema_reportado"));o.setDiagnostico(rs.getString("diagnostico"));o.setTrabajoRealizado(rs.getString("trabajo_realizado"));o.setCostoEstimado(rs.getBigDecimal("costo_estimado"));o.setCostoFinal(rs.getBigDecimal("costo_final"));o.setEstado(rs.getString("estado"));Date fp=rs.getDate("fecha_prometida");if(fp!=null)o.setFechaPrometida(fp.toLocalDate());Timestamp fe=rs.getTimestamp("fecha_entrega");if(fe!=null)o.setFechaEntrega(fe.toLocalDateTime());Date gh=rs.getDate("garantia_hasta");if(gh!=null)o.setGarantiaHasta(gh.toLocalDate());o.setObservaciones(rs.getString("observaciones"));o.setIdCliente(rs.getInt("id_cliente"));o.setNombreCliente(rs.getString("nombre_cliente"));o.setNumeroIdentidad(rs.getString("numero_identidad"));o.setTelefonoCliente(rs.getString("telefono_cliente"));o.setTipoEquipo(rs.getString("tipo_equipo"));o.setMarcaEquipo(rs.getString("marca_equipo"));o.setModeloEquipo(rs.getString("modelo_equipo"));o.setNumeroSerieEquipo(rs.getString("numero_serie_equipo"));o.setColorEquipo(rs.getString("color_equipo"));o.setAccesoriosRecibidos(rs.getString("accesorios_recibidos"));return o;
    }

    private HistorialServicio mapearHistorial(ResultSet rs) throws SQLException {
        HistorialServicio h=new HistorialServicio();h.setIdHistorial(rs.getInt("id_historial"));h.setIdOrden(rs.getInt("id_orden"));h.setIdUsuario(rs.getInt("id_usuario"));h.setNumeroOrden(rs.getString("numero_orden"));h.setNombreCliente(rs.getString("nombre_cliente"));h.setNombreUsuario(rs.getString("nombre_usuario"));Timestamp f=rs.getTimestamp("fecha_evento");if(f!=null)h.setFechaEvento(f.toLocalDateTime());h.setEstadoAnterior(rs.getString("estado_anterior"));h.setEstadoNuevo(rs.getString("estado_nuevo"));h.setDescripcion(rs.getString("descripcion"));return h;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p=new Producto();p.setIdProducto(rs.getInt("id_producto"));p.setIdCategoria(rs.getInt("id_categoria"));p.setNombreCategoria(rs.getString("nombre_categoria"));p.setCodigo(rs.getString("codigo"));p.setNombre(rs.getString("nombre"));p.setDescripcion(rs.getString("descripcion"));p.setMarca(rs.getString("marca"));p.setModelo(rs.getString("modelo"));p.setPrecioCompra(rs.getBigDecimal("precio_compra"));p.setPrecioVenta(rs.getBigDecimal("precio_venta"));p.setStockActual(rs.getInt("stock_actual"));p.setStockMinimo(rs.getInt("stock_minimo"));p.setManejaNumeroSerie(rs.getBoolean("maneja_numero_serie"));p.setEstado(rs.getString("estado"));Timestamp f=rs.getTimestamp("fecha_registro");if(f!=null)p.setFechaRegistro(f.toLocalDateTime());return p;
    }

    private void setDate(PreparedStatement ps,int i,java.time.LocalDate d) throws SQLException {if(d==null)ps.setNull(i,Types.DATE);else ps.setDate(i,Date.valueOf(d));}
    private void setText(PreparedStatement ps,int i,String s) throws SQLException {if(s==null||s.trim().isEmpty())ps.setNull(i,Types.NVARCHAR);else ps.setString(i,s.trim());}
}
