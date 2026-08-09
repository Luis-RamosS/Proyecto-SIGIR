package sigir.controlador;

import java.awt.Cursor;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.CreditoDAO;
import sigir.modelo.AbonoCredito;
import sigir.modelo.Credito;
import sigir.util.Sesion;
import sigir.vista.paneles.CreditosPanel;

public class CreditoControlador {

    private final CreditosPanel vista;
    private final CreditoDAO dao;

    private List<Credito> creditos = new ArrayList<>();
    private List<AbonoCredito> abonos = new ArrayList<>();

    private SwingWorker<DatosCarga, Void> trabajadorCarga;
    private SwingWorker<List<Credito>, Void> trabajadorCreditos;
    private SwingWorker<List<AbonoCredito>, Void> trabajadorAbonos;

    private long ultimaCarga;
    private long versionCreditos;
    private long versionAbonos;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS = 30_000;

    private record Indicadores(int pendientes, int vencidos, int pagados) {
    }

    private record DatosCarga(
            List<Credito> creditos,
            List<Credito> creditosParaAbono,
            List<AbonoCredito> abonos,
            Indicadores indicadores) {
    }

    private record FiltroCredito(String texto, String estado) {
    }

    public CreditoControlador(CreditosPanel vista) {
        this.vista = vista;
        this.dao = new CreditoDAO();
    }

    public void iniciarAsync() {
        cargarTodoAsync();
    }

    public void recargarAsync() {
        cargarTodoAsync();
    }

    public void iniciar() {
        iniciarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void recargarSiNecesario() {
        long tiempoTranscurrido = System.currentTimeMillis() - ultimaCarga;
        if (tiempoTranscurrido >= VIGENCIA_DATOS_MS) {
            cargarTodoAsync();
        }
    }

    private void cargarTodoAsync() {
        if (trabajadorCarga != null && !trabajadorCarga.isDone()) {
            recargaPendiente = true;
            return;
        }

        final FiltroCredito filtro = capturarFiltroCredito();
        final String textoAbono = vista.getTextoBusquedaAbono();

        recargaPendiente = false;

        if (trabajadorCreditos != null && !trabajadorCreditos.isDone()) {
            trabajadorCreditos.cancel(true);
        }

        if (trabajadorAbonos != null && !trabajadorAbonos.isDone()) {
            trabajadorAbonos.cancel(true);
        }

        final long versionCreditosCarga = ++versionCreditos;
        final long versionAbonosCarga = ++versionAbonos;

        vista.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        trabajadorCarga = new SwingWorker<>() {
            @Override
            protected DatosCarga doInBackground() throws Exception {
                List<Credito> creditosCargados = dao.listar(
                        filtro.texto(),
                        filtro.estado()
                );

                List<Credito> disponibles = dao.listarDisponiblesParaAbono();
                List<AbonoCredito> abonosCargados = dao.listarAbonos(textoAbono);
                int[] valores = dao.contarIndicadores();

                Indicadores indicadores = new Indicadores(
                        valores[0],
                        valores[1],
                        valores[2]
                );

                return new DatosCarga(
                        creditosCargados,
                        disponibles,
                        abonosCargados,
                        indicadores
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    if (versionCreditosCarga == versionCreditos) {
                        creditos = new ArrayList<>(datos.creditos());
                        vista.mostrarCreditos(creditos);
                    }

                    vista.cargarCreditosParaAbono(datos.creditosParaAbono());

                    if (versionAbonosCarga == versionAbonos) {
                        abonos = new ArrayList<>(datos.abonos());
                        vista.mostrarAbonos(abonos);
                    }

                    vista.actualizarIndicadores(
                            datos.indicadores().pendientes(),
                            datos.indicadores().vencidos(),
                            datos.indicadores().pagados()
                    );

                    ultimaCarga = System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (CancellationException ex) {
                    // La carga fue cancelada.
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    mostrarError("No fue posible cargar el módulo de créditos.", causa);
                } finally {
                    vista.setCursor(Cursor.getDefaultCursor());
                    if (recargaPendiente) {
                        cargarTodoAsync();
                    }
                }
            }
        };

        trabajadorCarga.execute();
    }

    public void buscarCreditos() {
        final FiltroCredito filtro = capturarFiltroCredito();
        final long versionActual = ++versionCreditos;

        if (trabajadorCreditos != null && !trabajadorCreditos.isDone()) {
            trabajadorCreditos.cancel(true);
        }

        trabajadorCreditos = new SwingWorker<>() {
            @Override
            protected List<Credito> doInBackground() throws Exception {
                return dao.listar(filtro.texto(), filtro.estado());
            }

            @Override
            protected void done() {
                if (isCancelled() || versionActual != versionCreditos) {
                    return;
                }

                try {
                    creditos = new ArrayList<>(get());
                    vista.mostrarCreditos(creditos);
                    ultimaCarga = System.currentTimeMillis();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (CancellationException ex) {
                    // La búsqueda fue reemplazada.
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    mostrarError("No fue posible consultar los créditos.", causa);
                }
            }
        };

        trabajadorCreditos.execute();
    }

    public void buscarAbonos() {
        final String texto = vista.getTextoBusquedaAbono();
        final long versionActual = ++versionAbonos;

        if (trabajadorAbonos != null && !trabajadorAbonos.isDone()) {
            trabajadorAbonos.cancel(true);
        }

        trabajadorAbonos = new SwingWorker<>() {
            @Override
            protected List<AbonoCredito> doInBackground() throws Exception {
                return dao.listarAbonos(texto);
            }

            @Override
            protected void done() {
                if (isCancelled() || versionActual != versionAbonos) {
                    return;
                }

                try {
                    abonos = new ArrayList<>(get());
                    vista.mostrarAbonos(abonos);
                    ultimaCarga = System.currentTimeMillis();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (CancellationException ex) {
                    // La búsqueda fue reemplazada.
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    mostrarError("No fue posible consultar los abonos.", causa);
                }
            }
        };

        trabajadorAbonos.execute();
    }

    public void cargarCreditosParaAbono() {
        recargarAsync();
    }

    public void seleccionarCredito() {
        Credito credito = vista.getCreditoSeleccionadoParaAbono();
        vista.mostrarDatosCredito(credito);
    }

    public void registrarAbono() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException("No existe una sesión activa.");
            }

            Credito credito = vista.getCreditoSeleccionadoParaAbono();
            if (credito == null) {
                throw new IllegalArgumentException("Selecciona un crédito pendiente.");
            }

            BigDecimal monto = vista.getMontoAbono();
            LocalDate fechaAbono = vista.getFechaAbono();
            if (fechaAbono.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha del abono no puede estar en el futuro.");
            }
            if (monto.signum() <= 0) {
                throw new IllegalArgumentException("El monto del abono debe ser mayor que cero.");
            }

            if (monto.compareTo(credito.getSaldoPendiente()) > 0) {
                throw new IllegalArgumentException("El abono no puede superar el saldo pendiente.");
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    vista,
                    "Se registrará un abono de "
                    + vista.formatearMoneda(monto)
                    + " al crédito CR-"
                    + String.format("%05d", credito.getIdCredito())
                    + ".\n¿Deseas continuar?",
                    "Confirmar abono",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            vista.establecerProcesando(true);

            dao.registrarAbono(
                    credito.getIdCredito(),
                    Sesion.getIdUsuario(),
                    monto,
                    vista.getMetodoPagoAbono(),
                    vista.getReferenciaAbono(),
                    vista.getObservacionesAbono(),
                    fechaAbono
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "Abono registrado correctamente.",
                    "Abono completado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            vista.limpiarFormularioAbono();
            recargarAsync();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Abono no registrado",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            mostrarError("No fue posible registrar el abono.", ex);
        } finally {
            vista.establecerProcesando(false);
        }
    }

    public void verEstadoCuenta() {
        int fila = vista.getFilaCreditoSeleccionadaModelo();
        if (fila < 0 || fila >= creditos.size()) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un crédito de la tabla.",
                    "Crédito no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        vista.mostrarEstadoCuenta(creditos.get(fila));
    }

    private FiltroCredito capturarFiltroCredito() {
        return new FiltroCredito(
                vista.getTextoBusquedaCredito(),
                vista.getEstadoCreditoFiltro()
        );
    }

    private void mostrarError(String mensaje, Throwable ex) {
        JOptionPane.showMessageDialog(
                vista,
                mensaje + "\n\nDetalle: "
                + (ex == null ? "Error desconocido" : ex.getMessage()),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
