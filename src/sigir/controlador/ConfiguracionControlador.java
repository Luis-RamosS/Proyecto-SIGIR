package sigir.controlador;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import sigir.dao.ConfiguracionDAO;
import sigir.modelo.ConfiguracionSistema;
import sigir.util.ConfiguracionGlobal;
import sigir.util.Sesion;
import sigir.vista.paneles.ConfiguracionPanel;

public class ConfiguracionControlador {

    private final ConfiguracionPanel vista;
    private final ConfiguracionDAO dao;

    private ConfiguracionSistema configuracionActual;

    public ConfiguracionControlador(
            ConfiguracionPanel vista) {

        this.vista = vista;
        this.dao = new ConfiguracionDAO();
    }

    public void iniciar() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        cargar();
        probarConexion();
        actualizarEstadoSecretos();
    }

    public void recargar() {
        iniciar();
    }

    public void cargar() {
        try {
            configuracionActual = dao.obtener();
            vista.mostrarConfiguracion(
                    configuracionActual
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible cargar la configuración.",
                    ex
            );
        }
    }

    public void guardar() {
        try {
            validarAcceso();

            ConfiguracionSistema configuracion =
                    vista.construirConfiguracion();

            validarConfiguracion(configuracion);

            dao.guardar(
                    configuracion,
                    Sesion.getIdUsuario()
            );

            configuracionActual = dao.obtener();

            ConfiguracionGlobal.establecer(
                    configuracionActual
            );

            vista.mostrarConfiguracion(
                    configuracionActual
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "La configuración fue guardada correctamente.",
                    "Configuración actualizada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Configuración no guardada",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible guardar la configuración.",
                    ex
            );
        }
    }

    public void restaurarValoresCargados() {
        if (configuracionActual == null) {
            cargar();
            return;
        }

        vista.mostrarConfiguracion(
                configuracionActual
        );
    }

    public void probarConexion() {
        try {
            String diagnostico =
                    dao.obtenerDiagnosticoConexion();

            vista.mostrarDiagnosticoConexion(
                    diagnostico,
                    true
            );

        } catch (SQLException ex) {
            vista.mostrarDiagnosticoConexion(
                    "La conexión falló.\n\n"
                    + ex.getMessage(),
                    false
            );
        }
    }

    public void probarConfiguracionCorreo() {
        try {
            ConfiguracionSistema configuracion =
                    vista.construirConfiguracion();

            validarCorreo(configuracion);

            String host =
                    configuracion.getSmtpHost();

            int puerto =
                    configuracion.getSmtpPuerto();

            try (Socket socket = new Socket()) {
                socket.connect(
                        new InetSocketAddress(
                                host,
                                puerto
                        ),
                        5000
                );
            }

            String variable =
                    System.getenv(
                            "SIGIR_GMAIL_APP_PASSWORD"
                    );

            boolean tieneSecreto =
                    variable != null
                    && !variable.isBlank();

            String resultado =
                    "El servidor SMTP respondió correctamente.\n"
                    + "Host: "
                    + host
                    + "\nPuerto: "
                    + puerto
                    + "\nTLS: "
                    + (
                        configuracion.isSmtpTls()
                                ? "Activado"
                                : "Desactivado"
                    )
                    + "\nContraseña de aplicación: "
                    + (
                        tieneSecreto
                                ? "Disponible en la variable de entorno"
                                : "No configurada"
                    );

            vista.mostrarResultadoCorreo(
                    resultado,
                    tieneSecreto
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Configuración de correo",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (IOException ex) {
            vista.mostrarResultadoCorreo(
                    "No fue posible conectarse al servidor SMTP.\n\n"
                    + ex.getMessage(),
                    false
            );
        }
    }

    public void crearRespaldo() {
        try {
            validarAcceso();

            String carpeta =
                    vista.getRutaRespaldoServidor();

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "SQL Server creará un respaldo completo "
                            + "de la base SIGIR.\n\n"
                            + "Carpeta del servidor:\n"
                            + carpeta
                            + "\n\n¿Deseas continuar?",
                            "Crear respaldo",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

            if (respuesta
                    != JOptionPane.YES_OPTION) {

                return;
            }

            vista.establecerProcesoRespaldo(true);

            String archivo =
                    dao.crearRespaldo(
                            carpeta,
                            Sesion.getIdUsuario()
                    );

            vista.setRutaArchivoRestauracion(
                    archivo
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "El respaldo fue creado correctamente.\n\n"
                    + archivo,
                    "Respaldo completado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Respaldo no creado",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible crear el respaldo.\n"
                    + "La carpeta debe existir en la computadora "
                    + "donde está instalado SQL Server y su servicio "
                    + "debe tener permiso de escritura.",
                    ex
            );

        } finally {
            vista.establecerProcesoRespaldo(false);
        }
    }

    public void verificarRespaldo() {
        try {
            validarAcceso();

            String archivo =
                    vista.getRutaArchivoRestauracion();

            vista.establecerProcesoRespaldo(true);

            dao.verificarRespaldo(
                    archivo,
                    Sesion.getIdUsuario()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "SQL Server verificó el respaldo correctamente.\n\n"
                    + archivo,
                    "Respaldo válido",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Respaldo no verificado",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "El archivo no pudo verificarse.",
                    ex
            );

        } finally {
            vista.establecerProcesoRespaldo(false);
        }
    }

    public void restaurarRespaldo() {
        try {
            validarAcceso();

            String archivo =
                    vista.getRutaArchivoRestauracion();

            String confirmacion =
                    JOptionPane.showInputDialog(
                            vista,
                            "Esta operación reemplazará la base SIGIR "
                            + "con el respaldo indicado.\n"
                            + "Se perderán los cambios realizados "
                            + "después de ese respaldo.\n\n"
                            + "Escribe RESTAURAR SIGIR para continuar:",
                            "Confirmación de restauración",
                            JOptionPane.WARNING_MESSAGE
                    );

            if (confirmacion == null) {
                return;
            }

            if (!"RESTAURAR SIGIR".equals(
                    confirmacion.trim())) {

                throw new IllegalArgumentException(
                        "La frase de confirmación no coincide."
                );
            }

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "Última confirmación.\n\n"
                            + "Archivo del servidor:\n"
                            + archivo
                            + "\n\n¿Restaurar ahora?",
                            "Restaurar base de datos",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE
                    );

            if (respuesta
                    != JOptionPane.YES_OPTION) {

                return;
            }

            vista.establecerProcesoRespaldo(true);

            dao.verificarRespaldo(
                    archivo,
                    Sesion.getIdUsuario()
            );

            dao.restaurarRespaldo(
                    archivo,
                    Sesion.getIdUsuario()
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "La base SIGIR fue restaurada.\n"
                    + "Cierra y vuelve a abrir el sistema "
                    + "antes de continuar trabajando.",
                    "Restauración completada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Restauración cancelada",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible restaurar la base de datos.\n"
                    + "Verifica que la ruta pertenezca al servidor SQL, "
                    + "que el archivo sea válido y que el usuario SQL "
                    + "tenga permisos de restauración.",
                    ex
            );

        } finally {
            vista.establecerProcesoRespaldo(false);
        }
    }

    private void actualizarEstadoSecretos() {
        String correo =
                System.getenv(
                        "SIGIR_GMAIL_APP_PASSWORD"
                );

        String base =
                System.getenv(
                        "SIGIR_DB_PASSWORD"
                );

        vista.mostrarEstadoSecretos(
                correo != null
                && !correo.isBlank(),
                base != null
                && !base.isBlank()
        );
    }

    private void validarAcceso() {
        if (!Sesion.haySesionActiva()) {
            throw new IllegalStateException(
                    "No existe una sesión activa."
            );
        }

        if (!Sesion.esDueno()) {
            throw new IllegalStateException(
                    "Solo el dueño puede modificar "
                    + "la configuración."
            );
        }
    }

    private void validarConfiguracion(
            ConfiguracionSistema configuracion) {

        if (configuracion.getNombreEmpresa() == null
                || configuracion.getNombreEmpresa()
                        .trim()
                        .length() < 3) {

            throw new IllegalArgumentException(
                    "Escribe el nombre de la empresa."
            );
        }

        if (configuracion.getRtn() != null
                && !configuracion.getRtn()
                        .matches("[0-9-]{10,20}")) {

            throw new IllegalArgumentException(
                    "El RTN solamente puede contener "
                    + "números y guiones."
            );
        }

        if (configuracion.getCorreo() != null
                && !configuracion.getCorreo()
                        .matches(
                                "^[A-Za-z0-9._%+-]+"
                                + "@[A-Za-z0-9.-]+"
                                + "\\.[A-Za-z]{2,}$"
                        )) {

            throw new IllegalArgumentException(
                    "El correo de la empresa no es válido."
            );
        }

        if (configuracion.getPorcentajeImpuesto()
                .signum() < 0
                || configuracion.getPorcentajeImpuesto()
                        .compareTo(
                                new java.math.BigDecimal("100")
                        ) > 0) {

            throw new IllegalArgumentException(
                    "El impuesto debe estar entre 0 y 100."
            );
        }

        if (configuracion.getPrefijoFactura() == null
                || !configuracion.getPrefijoFactura()
                        .matches("[A-Za-z0-9-]{1,20}")) {

            throw new IllegalArgumentException(
                    "El prefijo de factura solo puede contener "
                    + "letras, números y guiones."
            );
        }

        if (configuracion.getMonedaCodigo() == null
                || !configuracion.getMonedaCodigo()
                        .matches("[A-Za-z]{3,10}")) {

            throw new IllegalArgumentException(
                    "Escribe un código de moneda válido, "
                    + "por ejemplo HNL."
            );
        }

        validarCorreo(configuracion);
    }

    private void validarCorreo(
            ConfiguracionSistema configuracion) {

        if (configuracion.getSmtpHost() == null
                || configuracion.getSmtpHost()
                        .isBlank()) {

            throw new IllegalArgumentException(
                    "Escribe el servidor SMTP."
            );
        }

        if (configuracion.getSmtpPuerto() < 1
                || configuracion.getSmtpPuerto()
                        > 65535) {

            throw new IllegalArgumentException(
                    "El puerto SMTP debe estar "
                    + "entre 1 y 65535."
            );
        }

        if (configuracion.getSmtpUsuario() != null
                && !configuracion.getSmtpUsuario()
                        .isBlank()
                && !configuracion.getSmtpUsuario()
                        .contains("@")) {

            throw new IllegalArgumentException(
                    "El usuario SMTP debe ser "
                    + "un correo electrónico válido."
            );
        }
    }

    private void mostrarError(
            String mensaje,
            SQLException ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje
                + "\n\nDetalle: "
                + ex.getMessage(),
                "Error de SQL Server",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}
