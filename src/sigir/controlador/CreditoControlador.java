package sigir.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
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

    public CreditoControlador(CreditosPanel vista) {
        this.vista = vista;
        this.dao = new CreditoDAO();
    }

    public void iniciar() {
        recargar();
    }

    public void recargar() {
        buscarCreditos();
        cargarCreditosParaAbono();
        buscarAbonos();
        actualizarIndicadores();
    }

    public void buscarCreditos() {
        try {
            creditos = dao.listar(
                    vista.getTextoBusquedaCredito(),
                    vista.getEstadoCreditoFiltro()
            );
            vista.mostrarCreditos(creditos);
        } catch (SQLException ex) {
            mostrarError("No fue posible consultar los créditos.", ex);
        }
    }

    public void buscarAbonos() {
        try {
            abonos = dao.listarAbonos(
                    vista.getTextoBusquedaAbono()
            );
            vista.mostrarAbonos(abonos);
        } catch (SQLException ex) {
            mostrarError("No fue posible consultar los abonos.", ex);
        }
    }

    public void cargarCreditosParaAbono() {
        try {
            vista.cargarCreditosParaAbono(
                    dao.listarDisponiblesParaAbono()
            );
        } catch (SQLException ex) {
            mostrarError("No fue posible cargar los créditos pendientes.", ex);
        }
    }

    public void seleccionarCredito() {
        Credito credito = vista.getCreditoSeleccionadoParaAbono();

        if (credito == null) {
            vista.mostrarDatosCredito(null);
            return;
        }

        vista.mostrarDatosCredito(credito);
    }

    public void registrarAbono() {
        try {
            if (!Sesion.haySesionActiva()) {
                throw new IllegalStateException("No existe una sesión activa.");
            }

            Credito credito = vista.getCreditoSeleccionadoParaAbono();

            if (credito == null) {
                throw new IllegalArgumentException(
                        "Selecciona un crédito pendiente."
                );
            }

            BigDecimal monto = vista.getMontoAbono();

            if (monto.signum() <= 0) {
                throw new IllegalArgumentException(
                        "El monto del abono debe ser mayor que cero."
                );
            }

            if (monto.compareTo(credito.getSaldoPendiente()) > 0) {
                throw new IllegalArgumentException(
                        "El abono no puede superar el saldo pendiente."
                );
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

            if (respuesta != JOptionPane.YES_OPTION) return;

            vista.establecerProcesando(true);

            dao.registrarAbono(
                    credito.getIdCredito(),
                    Sesion.getIdUsuario(),
                    monto,
                    vista.getMetodoPagoAbono(),
                    vista.getReferenciaAbono(),
                    vista.getObservacionesAbono()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "Abono registrado correctamente.",
                    "Abono completado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            vista.limpiarFormularioAbono();
            recargar();

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

    private void actualizarIndicadores() {
        try {
            vista.actualizarIndicadores(
                    dao.contarPendientes(),
                    dao.contarVencidos(),
                    dao.contarPagados()
            );
        } catch (SQLException ex) {
            mostrarError("No fue posible actualizar los indicadores.", ex);
        }
    }

    private void mostrarError(String mensaje, SQLException ex) {
        JOptionPane.showMessageDialog(
                vista,
                mensaje + "\n\nDetalle: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
    }
}
