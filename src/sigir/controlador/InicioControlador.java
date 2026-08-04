package sigir.controlador;

import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.InicioDAO;
import sigir.modelo.DatosInicio;
import sigir.vista.paneles.InicioPanel;

public class InicioControlador {

    private final InicioPanel vista;
    private final InicioDAO dao;

    private SwingWorker<DatosInicio, Void> trabajador;

    public InicioControlador(InicioPanel vista) {
        this.vista = vista;
        this.dao = new InicioDAO();
    }

    public void iniciar() {
        recargar();
    }

    public void recargar() {
        if (trabajador != null
                && !trabajador.isDone()) {
            return;
        }

        vista.mostrarCargando(true);

        trabajador = new SwingWorker<>() {
            @Override
            protected DatosInicio doInBackground()
                    throws Exception {

                return dao.cargarDatos();
            }

            @Override
            protected void done() {
                try {
                    vista.mostrarDatos(get());
                    vista.mostrarEstadoCarga(
                            "Actualizado correctamente",
                            true
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();

                    vista.mostrarEstadoCarga(
                            "Actualización interrumpida",
                            false
                    );

                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();

                    vista.mostrarEstadoCarga(
                            "No se pudieron cargar los datos",
                            false
                    );

                    JOptionPane.showMessageDialog(
                            vista,
                            "No fue posible actualizar el inicio.\n\n"
                            + "Detalle: "
                            + (
                                causa == null
                                        ? ex.getMessage()
                                        : causa.getMessage()
                            ),
                            "Error de base de datos",
                            JOptionPane.ERROR_MESSAGE
                    );

                    ex.printStackTrace();

                } finally {
                    vista.mostrarCargando(false);
                }
            }
        };

        trabajador.execute();
    }
}
