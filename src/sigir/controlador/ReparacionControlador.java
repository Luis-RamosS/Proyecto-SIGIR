package sigir.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JOptionPane;
import sigir.dao.ReparacionDAO;
import sigir.modelo.*;
import sigir.util.Sesion;
import sigir.vista.paneles.ReparacionesPanel;

public class ReparacionControlador {
    private final ReparacionesPanel vista;
    private final ReparacionDAO dao = new ReparacionDAO();
    private List<Cliente> clientes = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private List<OrdenServicio> ordenes = new ArrayList<>();
    private OrdenServicio ordenActual;

    public ReparacionControlador(ReparacionesPanel vista) { this.vista = vista; }

    public void iniciar() { cargarCatalogos(); nuevaOrden(); buscarOrdenes(); buscarHistorial(); actualizarIndicadores(); }
    public void recargar() {
        cargarCatalogos(); buscarOrdenes(); buscarHistorial(); actualizarIndicadores();
        if (ordenActual != null) cargarOrdenPorId(ordenActual.getIdOrden());
    }

    private void cargarCatalogos() {
        try {
            clientes = dao.listarClientesActivos();
            productos = dao.listarRepuestosDisponibles();
            vista.cargarClientes(clientes);
            vista.cargarProductos(productos);
        } catch (SQLException ex) { mostrarError("No fue posible cargar clientes o productos.", ex); }
    }

    public void nuevaOrden() { ordenActual = null; vista.limpiarNuevaOrden(); vista.setNumeroOrden(generarNumeroOrden()); }

    public void seleccionarClienteNuevaOrden(Cliente cliente) {
        try { vista.cargarEquiposCliente(cliente == null ? List.of() : dao.listarEquiposCliente(cliente.getIdCliente())); }
        catch (SQLException ex) { mostrarError("No fue posible cargar los equipos del cliente.", ex); }
    }

    public void seleccionarEquipoExistente() { vista.mostrarEquipoExistente(vista.getEquipoExistenteSeleccionado()); }

    public void registrarOrden() {
        try {
            if (!Sesion.haySesionActiva()) throw new IllegalStateException("No existe una sesión activa.");
            Cliente cliente = vista.getClienteNuevaOrden();
            if (cliente == null) throw new IllegalArgumentException("Busca y selecciona un cliente.");
            EquipoCliente existente = vista.getEquipoExistenteSeleccionado();
            boolean nuevo = existente == null;
            EquipoCliente equipo = nuevo ? vista.construirEquipoNuevo() : existente;
            validarEquipo(equipo);
            equipo.setIdCliente(cliente.getIdCliente());
            OrdenServicio orden = vista.construirNuevaOrden();
            orden.setIdUsuarioRecibe(Sesion.getIdUsuario());
            orden.setNombreUsuarioRecibe(Sesion.getNombreCompleto());
            orden.setFechaRecepcion(LocalDateTime.now());
            orden.setEstado("RECIBIDO");
            orden.setIdCliente(cliente.getIdCliente());
            validarNuevaOrden(orden);
            if (dao.existeNumeroOrden(orden.getNumeroOrden())) { orden.setNumeroOrden(generarNumeroOrden()); vista.setNumeroOrden(orden.getNumeroOrden()); }
            int r = JOptionPane.showConfirmDialog(vista,"Se registrará la orden "+orden.getNumeroOrden()+" para "+cliente.getNombreCompleto()+".\n\n¿Deseas continuar?","Confirmar orden",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
            vista.establecerProcesando(true);
            int id = dao.registrarOrden(orden,equipo,nuevo);
            JOptionPane.showMessageDialog(vista,"Orden registrada correctamente.\nNúmero: "+orden.getNumeroOrden()+"\nID interno: "+id,"Orden creada",JOptionPane.INFORMATION_MESSAGE);
            nuevaOrden(); recargar(); vista.mostrarPestanaSeguimiento();
        } catch (IllegalArgumentException|IllegalStateException ex) {
            JOptionPane.showMessageDialog(vista,ex.getMessage(),"Orden no registrada",JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) { mostrarError("No fue posible registrar la orden.",ex); }
        finally { vista.establecerProcesando(false); }
    }

    public void buscarOrdenes() {
        try { ordenes=dao.listarOrdenes(vista.getTextoBusquedaOrden(),vista.getEstadoOrdenFiltro()); vista.mostrarOrdenes(ordenes); }
        catch(SQLException ex){mostrarError("No fue posible consultar las órdenes.",ex);}
    }

    public void cargarOrdenSeleccionada() {
        int fila=vista.getFilaOrdenSeleccionadaModelo();
        if(fila<0||fila>=ordenes.size()){JOptionPane.showMessageDialog(vista,"Selecciona una orden de la tabla.","Orden no seleccionada",JOptionPane.WARNING_MESSAGE);return;}
        cargarOrdenPorId(ordenes.get(fila).getIdOrden());
    }

    private void cargarOrdenPorId(int id) {
        try {
            OrdenServicio completa=dao.obtenerOrdenCompleta(id);
            if(completa==null)throw new SQLException("La orden seleccionada ya no existe.");
            ordenActual=completa;vista.mostrarOrdenActual(completa);
        }catch(SQLException ex){mostrarError("No fue posible cargar la orden.",ex);}
    }

    public void guardarSeguimiento() {
        try {
            validarOrdenActual();
            OrdenServicio cambios=vista.construirCambiosOrden(ordenActual);
            validarCambios(cambios);
            dao.actualizarOrden(cambios,Sesion.getIdUsuario(),vista.getDescripcionCambio());
            JOptionPane.showMessageDialog(vista,"La orden fue actualizada correctamente.","Cambios guardados",JOptionPane.INFORMATION_MESSAGE);
            ordenActual=cambios;vista.limpiarDescripcionCambio();recargar();
        }catch(IllegalArgumentException|IllegalStateException ex){JOptionPane.showMessageDialog(vista,ex.getMessage(),"Cambios no guardados",JOptionPane.WARNING_MESSAGE);}
        catch(SQLException ex){mostrarError("No fue posible actualizar la orden.",ex);}
    }

    public void seleccionarProductoRepuesto(Producto p){vista.mostrarProductoRepuesto(p);}

    public void agregarRepuesto() {
        try {
            validarOrdenActual();
            if("ENTREGADO".equals(ordenActual.getEstado())||"CANCELADO".equals(ordenActual.getEstado()))throw new IllegalArgumentException("No puedes agregar repuestos a una orden finalizada.");
            Producto p=vista.getProductoRepuestoSeleccionado();
            if(p==null)throw new IllegalArgumentException("Busca y selecciona un repuesto.");
            int cantidad=vista.getCantidadRepuesto();BigDecimal precio=vista.getPrecioRepuesto();
            if(cantidad<=0)throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            if(cantidad>p.getStockActual())throw new IllegalArgumentException("Stock insuficiente. Disponible: "+p.getStockActual()+".");
            if(precio.signum()<0)throw new IllegalArgumentException("El precio no puede ser negativo.");
            RepuestoOrden repuesto=new RepuestoOrden();repuesto.setIdOrden(ordenActual.getIdOrden());repuesto.setIdProducto(p.getIdProducto());repuesto.setCodigoProducto(p.getCodigo());repuesto.setNombreProducto(p.getNombre());repuesto.setCantidad(cantidad);repuesto.setPrecioUnitario(precio);
            dao.agregarRepuesto(ordenActual.getIdOrden(),Sesion.getIdUsuario(),repuesto);
            JOptionPane.showMessageDialog(vista,"Repuesto asignado y descontado del inventario.","Repuesto agregado",JOptionPane.INFORMATION_MESSAGE);
            vista.limpiarRepuesto();cargarCatalogos();cargarOrdenPorId(ordenActual.getIdOrden());buscarHistorial();
        }catch(IllegalArgumentException|IllegalStateException ex){JOptionPane.showMessageDialog(vista,ex.getMessage(),"Repuesto no agregado",JOptionPane.WARNING_MESSAGE);}
        catch(SQLException ex){mostrarError("No fue posible agregar el repuesto.",ex);}
    }

    public void cancelarOrden() {
        try {
            validarOrdenActual();
            if("ENTREGADO".equals(ordenActual.getEstado()))throw new IllegalArgumentException("Una orden entregada no puede cancelarse.");
            String motivo=JOptionPane.showInputDialog(vista,"Escribe el motivo de la cancelación:","Cancelar orden",JOptionPane.WARNING_MESSAGE);
            if(motivo==null)return;
            if(motivo.trim().length()<5)throw new IllegalArgumentException("El motivo debe contener al menos 5 caracteres.");
            int r=JOptionPane.showConfirmDialog(vista,"La orden "+ordenActual.getNumeroOrden()+" será cancelada.\nLos repuestos asignados volverán al inventario.\n\n¿Deseas continuar?","Confirmar cancelación",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(r!=JOptionPane.YES_OPTION)return;
            dao.cancelarOrden(ordenActual.getIdOrden(),Sesion.getIdUsuario(),motivo.trim());
            JOptionPane.showMessageDialog(vista,"La orden fue cancelada correctamente.","Orden cancelada",JOptionPane.INFORMATION_MESSAGE);
            ordenActual=null;vista.limpiarOrdenActual();recargar();
        }catch(IllegalArgumentException|IllegalStateException ex){JOptionPane.showMessageDialog(vista,ex.getMessage(),"Orden no cancelada",JOptionPane.WARNING_MESSAGE);}
        catch(SQLException ex){mostrarError("No fue posible cancelar la orden.",ex);}
    }

    public void buscarHistorial() {
        try { vista.mostrarHistorialGeneral(dao.listarHistorialGeneral(vista.getTextoBusquedaHistorial())); }
        catch(SQLException ex){mostrarError("No fue posible consultar el historial.",ex);}
    }

    private void actualizarIndicadores(){try{vista.actualizarIndicadores(dao.contarEstado("RECIBIDO"),dao.contarEstado("EN_REPARACION"),dao.contarEstado("LISTO"));}catch(SQLException ex){mostrarError("No fue posible actualizar los indicadores.",ex);}}
    private void validarEquipo(EquipoCliente e){if(e==null)throw new IllegalArgumentException("Selecciona o registra un equipo.");if(e.getTipoEquipo()==null||e.getTipoEquipo().trim().length()<3)throw new IllegalArgumentException("Escribe el tipo de equipo.");}
    private void validarNuevaOrden(OrdenServicio o){if(o.getProblemaReportado()==null||o.getProblemaReportado().trim().length()<5)throw new IllegalArgumentException("Describe el problema reportado con al menos 5 caracteres.");if(o.getCostoEstimado().signum()<0)throw new IllegalArgumentException("El costo estimado no puede ser negativo.");if(o.getFechaPrometida()!=null&&o.getFechaPrometida().isBefore(LocalDate.now()))throw new IllegalArgumentException("La fecha prometida no puede estar en el pasado.");}
    private void validarOrdenActual(){if(ordenActual==null)throw new IllegalStateException("Primero carga una orden del listado.");if(!Sesion.haySesionActiva())throw new IllegalStateException("No existe una sesión activa.");}
    private void validarCambios(OrdenServicio o){if(o.getCostoEstimado().signum()<0||o.getCostoFinal().signum()<0)throw new IllegalArgumentException("Los costos no pueden ser negativos.");if(("LISTO".equals(o.getEstado())||"ENTREGADO".equals(o.getEstado()))&&(o.getDiagnostico()==null||o.getDiagnostico().trim().length()<5))throw new IllegalArgumentException("Antes de marcar la orden como "+o.getEstado()+", registra el diagnóstico.");if(("LISTO".equals(o.getEstado())||"ENTREGADO".equals(o.getEstado()))&&(o.getTrabajoRealizado()==null||o.getTrabajoRealizado().trim().length()<5))throw new IllegalArgumentException("Antes de marcar la orden como "+o.getEstado()+", registra el trabajo realizado.");}
    private String generarNumeroOrden(){return "SRV-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))+"-"+ThreadLocalRandom.current().nextInt(100,1000);}
    private void mostrarError(String m,SQLException ex){JOptionPane.showMessageDialog(vista,m+"\n\nDetalle: "+ex.getMessage(),"Error de base de datos",JOptionPane.ERROR_MESSAGE);ex.printStackTrace();}
}
