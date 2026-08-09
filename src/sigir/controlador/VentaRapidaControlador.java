package sigir.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.VentaRapidaDAO;
import sigir.modelo.Producto;
import sigir.modelo.VentaRapida;
import sigir.util.Sesion;
import sigir.vista.paneles.VentaRapidaPanel;

public class VentaRapidaControlador {

    private final VentaRapidaPanel vista;
    private final VentaRapidaDAO dao = new VentaRapidaDAO();
    private List<Producto> productos = new ArrayList<>();
    private SwingWorker<Datos, Void> trabajador;

    private record Datos(List<Producto> productos, List<VentaRapida> ventas) {}

    public VentaRapidaControlador(VentaRapidaPanel vista) {
        this.vista = vista;
    }

    public void iniciarAsync() {
        recargarAsync();
    }

    public void recargarAsync() {
        if (trabajador != null && !trabajador.isDone()) {
            trabajador.cancel(true);
        }
        vista.establecerProcesando(true);
        trabajador = new SwingWorker<>() {
            @Override protected Datos doInBackground() throws Exception {
                return new Datos(dao.listarProductosDisponibles(), dao.listarRecientes());
            }
            @Override protected void done() {
                try {
                    if (!isCancelled()) {
                        Datos d=get();
                        productos=new ArrayList<>(d.productos());
                        vista.cargarProductos(productos);
                        vista.mostrarHistorial(d.ventas());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa=ex.getCause()==null?ex:ex.getCause();
                    error("No fue posible cargar las ventas rápidas.", causa);
                } finally {
                    vista.establecerProcesando(false);
                }
            }
        };
        trabajador.execute();
    }

    public void seleccionarProducto() {
        Producto p=vista.getProductoSeleccionado();
        if(p==null) {
            vista.mostrarDatosProducto(BigDecimal.ZERO,0,false);
            return;
        }
        vista.mostrarDatosProducto(p.getPrecioVenta(),p.getStockActual(),p.isManejaNumeroSerie());
    }

    public void actualizarTotal() {
        try {
            BigDecimal precio=vista.getPrecioUnitario();
            int cantidad=vista.getCantidad();
            vista.setTotal(precio.multiply(BigDecimal.valueOf(Math.max(cantidad,0))));
        } catch (IllegalArgumentException ex) {
            vista.setTotal(BigDecimal.ZERO);
        }
    }

    public void registrar() {
        try {
            if(!Sesion.haySesionActiva()) throw new IllegalStateException("No existe una sesión activa.");
            if(!Sesion.esDueno()) throw new IllegalStateException("Solo el dueño puede registrar ventas rápidas.");

            Producto p=vista.getProductoSeleccionado();
            if(p==null || p.getIdProducto()<=0) throw new IllegalArgumentException("Selecciona el producto vendido.");

            int cantidad=vista.getCantidad();
            if(cantidad<=0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            if(cantidad>p.getStockActual()) throw new IllegalArgumentException("Stock insuficiente. Disponible: "+p.getStockActual());

            BigDecimal precio=vista.getPrecioUnitario();
            if(precio.signum()<0) throw new IllegalArgumentException("El precio no puede ser negativo.");

            LocalDate fecha=vista.getFechaReal();
            LocalTime hora=vista.getHoraReal();
            LocalDateTime real=LocalDateTime.of(fecha,hora);
            if(real.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("La fecha y hora de la venta rápida no pueden estar en el futuro.");

            VentaRapida v=new VentaRapida();
            v.setIdProducto(p.getIdProducto());
            v.setIdUsuario(Sesion.getIdUsuario());
            v.setFechaHoraReal(real);
            v.setCantidad(cantidad);
            v.setPrecioUnitario(precio);
            v.setTotal(precio.multiply(BigDecimal.valueOf(cantidad)));
            v.setMetodoPago(vista.getMetodoPago());
            v.setNumeroSerie(vista.getNumeroSerie());
            v.setObservaciones(vista.getObservaciones());

            if(p.isManejaNumeroSerie() && (v.getNumeroSerie()==null || v.getNumeroSerie().isBlank())) {
                throw new IllegalArgumentException("Este producto maneja número de serie. Ingresa la serie de la unidad vendida.");
            }
            if(p.isManejaNumeroSerie() && cantidad!=1) {
                throw new IllegalArgumentException("Para un producto con número de serie registra una unidad por venta rápida.");
            }

            int r=JOptionPane.showConfirmDialog(vista,
                    "Se registrará hoy una venta rápida por "+vista.formatearMoneda(v.getTotal())+".\n"
                    +"La venta ocurrió el "+vista.getFechaRealTexto()+" a las "+vista.getHoraRealTexto()+".\n"
                    +"El inventario se descontará ahora.\n\n¿Deseas continuar?",
                    "Confirmar venta rápida",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if(r!=JOptionPane.YES_OPTION) return;

            vista.establecerProcesando(true);
            int id=dao.registrar(v);
            JOptionPane.showMessageDialog(vista,
                    "Venta rápida registrada correctamente.\nRegistro VR-"+String.format("%05d",id),
                    "Venta rápida",JOptionPane.INFORMATION_MESSAGE);
            vista.limpiarFormulario();
            recargarAsync();

        } catch (IllegalArgumentException|IllegalStateException ex) {
            JOptionPane.showMessageDialog(vista,ex.getMessage(),"Venta rápida no registrada",JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            error("No fue posible registrar la venta rápida.",ex);
        } finally {
            vista.establecerProcesando(false);
        }
    }

    private void error(String mensaje,Throwable ex) {
        JOptionPane.showMessageDialog(vista,mensaje+"\n\nDetalle: "+ex.getMessage(),"Error de base de datos",JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
