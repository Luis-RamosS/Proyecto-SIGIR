package sigir.controlador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import javax.swing.JOptionPane;
import sigir.dao.ReporteDAO;
import sigir.modelo.ReporteResultado;
import sigir.modelo.ResumenReportes;
import sigir.modelo.TipoReporte;
import sigir.util.ExportadorReporteUtil;
import sigir.vista.paneles.ReportesPanel;

public class ReporteControlador {

    private final ReportesPanel vista;
    private final ReporteDAO dao;
    private ReporteResultado resultadoActual;

    public ReporteControlador(ReportesPanel vista) {
        this.vista = vista;
        this.dao = new ReporteDAO();
    }

    public void iniciar() {
        vista.cargarTiposReporte(
                Arrays.asList(TipoReporte.values())
        );

        try {
            vista.cargarUsuarios(
                    dao.listarUsuarios()
            );
        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible cargar los usuarios.",
                    ex
            );
        }

        cambiarTipoReporte();
        consultar();
    }

    public void recargar() {
        try {
            vista.cargarUsuarios(
                    dao.listarUsuarios()
            );
        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible actualizar los usuarios.",
                    ex
            );
        }

        consultar();
    }

    public void cambiarTipoReporte() {
        TipoReporte tipo =
                vista.getTipoReporteSeleccionado();

        if (tipo != null) {
            vista.configurarFiltros(tipo);
        }
    }

    public void consultar() {
        try {
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

            vista.establecerConsultando(true);

            resultadoActual = dao.consultar(
                    tipo,
                    desde,
                    hasta,
                    vista.getEstadoSeleccionado(),
                    vista.getIdUsuarioSeleccionado()
            );

            ResumenReportes resumen =
                    dao.obtenerResumen(desde, hasta);

            vista.mostrarResumen(resumen);
            vista.mostrarResultado(resultadoActual);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Filtros del reporte",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible generar el reporte.",
                    ex
            );

        } finally {
            vista.establecerConsultando(false);
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

    private void mostrarError(
            String mensaje,
            SQLException ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje
                + "\n\nDetalle: "
                + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}
