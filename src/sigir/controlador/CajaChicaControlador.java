package sigir.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.CajaChicaDAO;
import sigir.modelo.CajaChicaResumen;
import sigir.modelo.MovimientoCajaChica;
import sigir.util.Sesion;
import sigir.vista.paneles.CajaChicaPanel;

public class CajaChicaControlador {

    private final CajaChicaPanel vista;
    private final CajaChicaDAO dao;
    private SwingWorker<Datos, Void> trabajador;

    private record Datos(
            CajaChicaResumen resumen,
            List<MovimientoCajaChica> movimientos) {
    }

    public CajaChicaControlador(CajaChicaPanel vista) {
        this.vista = vista;
        this.dao = new CajaChicaDAO();
    }

    public void iniciarAsync() {
        vista.configurarPermisos(Sesion.esDueno());
        recargarAsync();
    }

    public void recargarAsync() {
        if (trabajador != null && !trabajador.isDone()) {
            trabajador.cancel(true);
        }

        LocalDate desde;
        LocalDate hasta;

        try {
            desde = vista.getFechaDesde();
            hasta = vista.getFechaHasta();

            if (desde.isAfter(hasta)) {
                throw new IllegalArgumentException(
                        "La fecha inicial no puede ser posterior a la fecha final."
                );
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Fechas no válidas",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        vista.establecerProcesando(true);

        trabajador = new SwingWorker<>() {
            @Override
            protected Datos doInBackground() throws Exception {
                return new Datos(
                        dao.obtenerResumen(),
                        dao.listarMovimientos(desde, hasta)
                );
            }

            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        Datos datos = get();
                        vista.mostrarResumen(datos.resumen());
                        vista.mostrarMovimientos(datos.movimientos());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null
                            ? ex
                            : ex.getCause();
                    mostrarError(
                            "No fue posible cargar la caja chica.",
                            causa
                    );
                } finally {
                    vista.establecerProcesando(false);
                }
            }
        };

        trabajador.execute();
    }

    public void registrarMovimiento() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException(
                        "No existe una sesión activa."
                );
            }

            String tipo = vista.getTipoMovimiento();
            String concepto = vista.getConcepto();
            String categoria = vista.getCategoria();
            BigDecimal monto = vista.getMonto();

            if (!"EGRESO".equals(tipo)
                    && !"AJUSTE_ENTRADA".equals(tipo)
                    && !"AJUSTE_SALIDA".equals(tipo)) {
                throw new IllegalArgumentException(
                        "Selecciona un tipo de movimiento válido."
                );
            }

            if (("AJUSTE_ENTRADA".equals(tipo)
                    || "AJUSTE_SALIDA".equals(tipo))
                    && !Sesion.esDueno()) {
                throw new IllegalStateException(
                        "Solo el dueño puede registrar ajustes de caja chica."
                );
            }

            if (concepto == null || concepto.trim().length() < 4) {
                throw new IllegalArgumentException(
                        "Escribe un concepto de al menos 4 caracteres."
                );
            }

            if (monto.signum() <= 0) {
                throw new IllegalArgumentException(
                        "El monto debe ser mayor que cero."
                );
            }

            if ("EGRESO".equals(tipo)
                    && (categoria == null || categoria.isBlank())) {
                throw new IllegalArgumentException(
                        "Selecciona una categoría para el gasto."
                );
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    vista,
                    "Se registrará un "
                    + tipo.replace('_', ' ').toLowerCase()
                    + " por " + vista.formatearMoneda(monto)
                    + ".\n\n¿Deseas continuar?",
                    "Confirmar movimiento",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            vista.establecerProcesando(true);

            dao.registrarMovimiento(
                    Sesion.getIdUsuario(),
                    tipo,
                    categoria,
                    concepto,
                    monto,
                    vista.getComprobante(),
                    vista.getObservaciones()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "Movimiento de caja chica registrado correctamente.",
                    "Caja chica",
                    JOptionPane.INFORMATION_MESSAGE
            );

            vista.limpiarMovimiento();
            recargarAsync();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Movimiento no registrado",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible registrar el movimiento.",
                    ex
            );
        } finally {
            vista.establecerProcesando(false);
        }
    }

    public void reponerFondo() {
        try {
            if (!Sesion.esDueno()) {
                throw new IllegalStateException(
                        "Solo el dueño puede reponer el fondo de caja chica."
                );
            }

            BigDecimal sugerida = vista.getReposicionSugerida();

            if (sugerida.signum() <= 0) {
                throw new IllegalStateException(
                        "La caja chica ya se encuentra en L 2,500.00."
                );
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    vista,
                    "Saldo actual: "
                    + vista.formatearMoneda(vista.getSaldoActual())
                    + "\nReposición necesaria: "
                    + vista.formatearMoneda(sugerida)
                    + "\nFondo final: L 2,500.00"
                    + "\n\n¿Deseas realizar la reposición?",
                    "Reposición semanal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            BigDecimal repuesto = dao.reponerHastaFondoMaximo(
                    Sesion.getIdUsuario(),
                    "Reposición semanal del fondo"
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "Se repusieron "
                    + vista.formatearMoneda(repuesto)
                    + ".\nLa caja chica queda nuevamente en su fondo máximo.",
                    "Reposición completada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            recargarAsync();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Reposición no realizada",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible reponer la caja chica.",
                    ex
            );
        }
    }

    public void registrarArqueo() {
        try {
            if (!Sesion.esDueno()) {
                throw new IllegalStateException(
                        "Solo el dueño puede registrar el arqueo de caja chica."
                );
            }

            BigDecimal fisico = vista.getSaldoFisico();

            if (fisico.signum() < 0
                    || fisico.compareTo(new BigDecimal("2500.00")) > 0) {
                throw new IllegalArgumentException(
                        "El dinero contado debe estar entre L 0.00 y L 2,500.00."
                );
            }

            BigDecimal diferencia = fisico.subtract(vista.getSaldoActual());

            int respuesta = JOptionPane.showConfirmDialog(
                    vista,
                    "Saldo según SIGIR: "
                    + vista.formatearMoneda(vista.getSaldoActual())
                    + "\nDinero contado: "
                    + vista.formatearMoneda(fisico)
                    + "\nDiferencia: "
                    + vista.formatearMoneda(diferencia)
                    + "\n\n¿Deseas guardar este arqueo?",
                    "Confirmar arqueo",
                    JOptionPane.YES_NO_OPTION,
                    diferencia.signum() == 0
                            ? JOptionPane.QUESTION_MESSAGE
                            : JOptionPane.WARNING_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            dao.registrarArqueo(
                    Sesion.getIdUsuario(),
                    fisico,
                    "Arqueo semanal"
            );

            JOptionPane.showMessageDialog(
                    vista,
                    diferencia.signum() == 0
                            ? "Arqueo registrado. No existen diferencias."
                            : "Arqueo registrado con una diferencia de "
                            + vista.formatearMoneda(diferencia) + ".",
                    "Arqueo de caja chica",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Arqueo no registrado",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible registrar el arqueo.",
                    ex
            );
        }
    }

    private void mostrarError(String mensaje, Throwable ex) {
        JOptionPane.showMessageDialog(
                vista,
                mensaje + "\n\nDetalle: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
    }
}
