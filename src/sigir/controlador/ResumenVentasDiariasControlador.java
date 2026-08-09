package sigir.controlador;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.VentaDAO;
import sigir.modelo.ResumenVentasDiarias;
import sigir.vista.paneles.ResumenVentasDiariasPanel;

public class ResumenVentasDiariasControlador {

    private final ResumenVentasDiariasPanel vista;
    private final VentaDAO dao;
    private SwingWorker<ResumenVentasDiarias, Void> trabajador;
    private long ultimaCarga;
    private static final long VIGENCIA_MS = 30_000;

    public ResumenVentasDiariasControlador(ResumenVentasDiariasPanel vista) {
        this.vista = vista;
        this.dao = new VentaDAO();
    }

    public void recargar() {
        cargar(vista.getFechaSeleccionada());
    }

    public void recargarSiNecesario() {
        if (System.currentTimeMillis() - ultimaCarga >= VIGENCIA_MS) {
            recargar();
        }
    }

    private void cargar(LocalDate fecha) {
        if (trabajador != null && !trabajador.isDone()) {
            trabajador.cancel(true);
        }

        vista.establecerCargando(true);
        trabajador = new SwingWorker<>() {
            @Override
            protected ResumenVentasDiarias doInBackground() throws Exception {
                return dao.obtenerResumenDiario(fecha);
            }

            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        vista.mostrarResumen(get());
                        ultimaCarga = System.currentTimeMillis();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(
                            vista,
                            "No fue posible cargar el resumen diario.\n\nDetalle: " + causa.getMessage(),
                            "Resumen de ventas",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    vista.establecerCargando(false);
                }
            }
        };
        trabajador.execute();
    }
}
