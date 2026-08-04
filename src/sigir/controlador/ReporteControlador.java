package sigir.controlador;

import java.awt.Cursor;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.ReporteDAO;
import sigir.modelo.ReporteResultado;
import sigir.modelo.ResumenReportes;
import sigir.modelo.TipoReporte;
import sigir.modelo.UsuarioFiltro;
import sigir.util.ExportadorReporteUtil;
import sigir.vista.paneles.ReportesPanel;

public class ReporteControlador {

    private final ReportesPanel vista;
    private final ReporteDAO dao;

    private ReporteResultado resultadoActual;

    private SwingWorker<DatosCarga, Void>
            trabajadorCarga;

    private SwingWorker<DatosConsulta, Void>
            trabajadorConsulta;

    private long ultimaCarga;
    private long versionConsulta;
    private boolean recargaPendiente;
    private int operacionesActivas;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record FiltrosReporte(
            TipoReporte tipo,
            LocalDate desde,
            LocalDate hasta,
            String estado,
            Integer idUsuario
    ) {
    }

    private record DatosConsulta(
            ReporteResultado resultado,
            ResumenReportes resumen
    ) {
    }

    private record DatosCarga(
            List<UsuarioFiltro> usuarios,
            ReporteResultado resultado,
            ResumenReportes resumen
    ) {
    }

    public ReporteControlador(
            ReportesPanel vista) {

        this.vista = vista;
        this.dao = new ReporteDAO();
    }

    public void iniciarAsync() {
        vista.cargarTiposReporte(
                Arrays.asList(
                        TipoReporte.values()
                )
        );

        cambiarTipoReporte();
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
        long tiempoTranscurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (tiempoTranscurrido
                >= VIGENCIA_DATOS_MS) {

            cargarTodoAsync();
        }
    }

    public void cambiarTipoReporte() {
        TipoReporte tipo =
                vista.getTipoReporteSeleccionado();

        if (tipo != null) {
            vista.configurarFiltros(tipo);
        }
    }

    private void cargarTodoAsync() {
        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            recargaPendiente = true;
            return;
        }

        final FiltrosReporte filtros;

        try {
            filtros = capturarFiltros();

        } catch (IllegalArgumentException ex) {
            mostrarAvisoFiltros(
                    ex.getMessage()
            );
            return;
        }

        recargaPendiente = false;

        final long versionActual =
                ++versionConsulta;

        iniciarOperacion();

        trabajadorCarga =
                new SwingWorker<>() {

            @Override
            protected DatosCarga doInBackground()
                    throws Exception {

                List<UsuarioFiltro> usuarios =
                        dao.listarUsuarios();

                ReporteResultado resultado =
                        dao.consultar(
                                filtros.tipo(),
                                filtros.desde(),
                                filtros.hasta(),
                                filtros.estado(),
                                filtros.idUsuario()
                        );

                ResumenReportes resumen =
                        dao.obtenerResumen(
                                filtros.desde(),
                                filtros.hasta()
                        );

                return new DatosCarga(
                        usuarios,
                        resultado,
                        resumen
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    vista.cargarUsuarios(
                            datos.usuarios()
                    );

                    if (versionActual
                            == versionConsulta) {

                        resultadoActual =
                                datos.resultado();

                        vista.mostrarResumen(
                                datos.resumen()
                        );

                        vista.mostrarResultado(
                                resultadoActual
                        );
                    }

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La carga fue cancelada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible cargar "
                            + "el módulo de reportes.",
                            causa
                    );

                } finally {
                    terminarOperacion();

                    if (recargaPendiente) {
                        cargarTodoAsync();
                    }
                }
            }
        };

        trabajadorCarga.execute();
    }

    public void consultarAsync() {
        final FiltrosReporte filtros;

        try {
            filtros = capturarFiltros();

        } catch (IllegalArgumentException ex) {
            mostrarAvisoFiltros(
                    ex.getMessage()
            );
            return;
        }

        final long versionActual =
                ++versionConsulta;

        if (trabajadorConsulta != null
                && !trabajadorConsulta.isDone()) {

            trabajadorConsulta.cancel(true);
        }

        iniciarOperacion();

        trabajadorConsulta =
                new SwingWorker<>() {

            @Override
            protected DatosConsulta doInBackground()
                    throws Exception {

                ReporteResultado resultado =
                        dao.consultar(
                                filtros.tipo(),
                                filtros.desde(),
                                filtros.hasta(),
                                filtros.estado(),
                                filtros.idUsuario()
                        );

                ResumenReportes resumen =
                        dao.obtenerResumen(
                                filtros.desde(),
                                filtros.hasta()
                        );

                return new DatosConsulta(
                        resultado,
                        resumen
                );
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()
                            || versionActual
                            != versionConsulta) {

                        return;
                    }

                    DatosConsulta datos = get();

                    resultadoActual =
                            datos.resultado();

                    vista.mostrarResumen(
                            datos.resumen()
                    );

                    vista.mostrarResultado(
                            resultadoActual
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La consulta fue reemplazada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarError(
                            "No fue posible generar "
                            + "el reporte.",
                            causa
                    );

                } finally {
                    terminarOperacion();
                }
            }
        };

        trabajadorConsulta.execute();
    }

    public void consultar() {
        consultarAsync();
    }

    private FiltrosReporte capturarFiltros() {
        TipoReporte tipo =
                vista.getTipoReporteSeleccionado();

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "Selecciona un tipo de reporte."
            );
        }

        LocalDate desde =
                vista.getFechaDesde();

        LocalDate hasta =
                vista.getFechaHasta();

        if (tipo.isUsaFechas()
                && desde.isAfter(hasta)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser "
                    + "posterior a la fecha final."
            );
        }

        return new FiltrosReporte(
                tipo,
                desde,
                hasta,
                vista.getEstadoSeleccionado(),
                vista.getIdUsuarioSeleccionado()
        );
    }

    private void iniciarOperacion() {
        operacionesActivas++;

        vista.establecerConsultando(true);

        vista.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR
                )
        );
    }

    private void terminarOperacion() {
        operacionesActivas =
                Math.max(
                        0,
                        operacionesActivas - 1
                );

        boolean consultando =
                operacionesActivas > 0;

        vista.establecerConsultando(
                consultando
        );

        if (!consultando) {
            vista.setCursor(
                    Cursor.getDefaultCursor()
            );
        }
    }

    public void exportar() {
        if (resultadoActual == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Primero consulta un reporte.",
                    "Sin reporte",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ExportadorReporteUtil.exportarCsv(
                vista,
                vista.getTablaResultados(),
                "SIGIR_"
                + vista.getTipoReporteSeleccionado()
                        .getCodigo()
                + "_"
                + LocalDate.now()
        );
    }

    public void imprimir() {
        if (resultadoActual == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Primero consulta un reporte.",
                    "Sin reporte",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ExportadorReporteUtil.imprimir(
                vista,
                vista.getTablaResultados(),
                "SIGIR - "
                + resultadoActual.getTitulo()
        );
    }

    private void mostrarAvisoFiltros(
            String mensaje) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje,
                "Filtros del reporte",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(
            String mensaje,
            Throwable ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje
                + "\n\nDetalle: "
                + (
                    ex == null
                            ? "Error desconocido"
                            : ex.getMessage()
                ),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
