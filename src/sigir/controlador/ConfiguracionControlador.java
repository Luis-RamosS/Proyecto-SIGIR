package sigir.controlador;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.ConfiguracionDAO;
import sigir.modelo.ConfiguracionSistema;
import sigir.util.ConfiguracionGlobal;
import sigir.util.Sesion;
import sigir.vista.paneles.ConfiguracionPanel;

public class ConfiguracionControlador {

    private final ConfiguracionPanel vista;
    private final ConfiguracionDAO dao;

    private ConfiguracionSistema configuracionActual;

    private SwingWorker<?, ?> trabajadorActual;
    private long ultimaCarga;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record DatosCarga(
            ConfiguracionSistema configuracion,
            String diagnostico,
            boolean conexionCorrecta
    ) {
    }

    private record ResultadoCorreo(
            String mensaje,
            boolean secretoDisponible
    ) {
    }

    public ConfiguracionControlador(
            ConfiguracionPanel vista) {

        this.vista = vista;
        this.dao = new ConfiguracionDAO();
    }

    public void iniciarAsync() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        actualizarEstadoSecretos();
        cargarAsync();
    }

    public void recargarAsync() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        actualizarEstadoSecretos();
        cargarAsync();
    }

    public void iniciar() {
        iniciarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void cargar() {
        cargarAsync();
    }

    public void recargarSiNecesario() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        long tiempoTranscurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (tiempoTranscurrido
                >= VIGENCIA_DATOS_MS) {

            recargarAsync();
        }
    }

    private void cargarAsync() {
        if (hayOperacionActiva()) {
            recargaPendiente = true;
            return;
        }

        if (!iniciarOperacion("CARGAR")) {
            return;
        }

        SwingWorker<DatosCarga, Void> trabajador =
                new SwingWorker<>() {

            @Override
            protected DatosCarga doInBackground()
                    throws Exception {

                ConfiguracionSistema configuracion =
                        dao.obtener();

                String diagnostico;
                boolean conexionCorrecta;

                try {
                    diagnostico =
                            dao.obtenerDiagnosticoConexion();

                    conexionCorrecta = true;

                } catch (SQLException ex) {
                    diagnostico =
                            "La conexión falló.\n\n"
                            + ex.getMessage();

                    conexionCorrecta = false;
                }

                return new DatosCarga(
                        configuracion,
                        diagnostico,
                        conexionCorrecta
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    configuracionActual =
                            datos.configuracion();

                    vista.mostrarConfiguracion(
                            configuracionActual
                    );

                    vista.mostrarDiagnosticoConexion(
                            datos.diagnostico(),
                            datos.conexionCorrecta()
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    mostrarError(
                            "No fue posible cargar "
                            + "la configuración.",
                            causaReal(ex)
                    );

                } finally {
                    finalizarOperacion(true);
                }
            }
        };

        ejecutar(trabajador);
    }

    public void guardar() {
        final ConfiguracionSistema configuracion;

        try {
            validarAcceso();

            configuracion =
                    vista.construirConfiguracion();

            validarConfiguracion(
                    configuracion
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            mostrarAdvertencia(
                    ex.getMessage(),
                    "Configuración no guardada"
            );
            return;
        }

        if (!iniciarOperacion("GUARDAR")) {
            return;
        }

        SwingWorker<ConfiguracionSistema, Void>
                trabajador =
                new SwingWorker<>() {

            @Override
            protected ConfiguracionSistema
                    doInBackground()
                    throws Exception {

                dao.guardar(
                        configuracion,
                        Sesion.getIdUsuario()
                );

                return dao.obtener();
            }

            @Override
            protected void done() {
                try {
                    configuracionActual = get();

                    ConfiguracionGlobal.establecer(
                            configuracionActual
                    );

                    vista.mostrarConfiguracion(
                            configuracionActual
                    );

                    actualizarEstadoSecretos();

                    ultimaCarga =
                            System.currentTimeMillis();

                    JOptionPane.showMessageDialog(
                            vista,
                            "La configuración fue "
                            + "guardada correctamente.",
                            "Configuración actualizada",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    mostrarError(
                            "No fue posible guardar "
                            + "la configuración.",
                            causaReal(ex)
                    );

                } finally {
                    finalizarOperacion(true);
                }
            }
        };

        ejecutar(trabajador);
    }

    public void restaurarValoresCargados() {
        if (configuracionActual == null) {
            cargarAsync();
            return;
        }

        vista.mostrarConfiguracion(
                configuracionActual
        );
    }

    public void probarConexion() {
        if (!iniciarOperacion("CONEXION")) {
            return;
        }

        SwingWorker<String, Void> trabajador =
                new SwingWorker<>() {

            @Override
            protected String doInBackground()
                    throws Exception {

                return dao.obtenerDiagnosticoConexion();
            }

            @Override
            protected void done() {
                try {
                    vista.mostrarDiagnosticoConexion(
                            get(),
                            true
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            causaReal(ex);

                    vista.mostrarDiagnosticoConexion(
                            "La conexión falló.\n\n"
                            + mensaje(causa),
                            false
                    );

                } finally {
                    finalizarOperacion(false);
                }
            }
        };

        ejecutar(trabajador);
    }

    public void probarConfiguracionCorreo() {
        final ConfiguracionSistema configuracion;

        try {
            configuracion =
                    vista.construirConfiguracion();

            validarCorreo(configuracion);

        } catch (IllegalArgumentException ex) {
            mostrarAdvertencia(
                    ex.getMessage(),
                    "Configuración de correo"
            );
            return;
        }

        if (!iniciarOperacion("CORREO")) {
            return;
        }

        SwingWorker<ResultadoCorreo, Void>
                trabajador =
                new SwingWorker<>() {

            @Override
            protected ResultadoCorreo
                    doInBackground()
                    throws Exception {

                String host =
                        configuracion.getSmtpHost();

                int puerto =
                        configuracion.getSmtpPuerto();

                try (Socket socket =
                             new Socket()) {

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
                        "El servidor SMTP respondió "
                        + "correctamente.\n"
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
                                    ? "Disponible en la "
                                      + "variable de entorno"
                                    : "No configurada"
                        );

                return new ResultadoCorreo(
                        resultado,
                        tieneSecreto
                );
            }

            @Override
            protected void done() {
                try {
                    ResultadoCorreo resultado =
                            get();

                    vista.mostrarResultadoCorreo(
                            resultado.mensaje(),
                            resultado.secretoDisponible()
                    );

                    actualizarEstadoSecretos();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            causaReal(ex);

                    if (causa instanceof IOException) {
                        vista.mostrarResultadoCorreo(
                                "No fue posible conectarse "
                                + "al servidor SMTP.\n\n"
                                + mensaje(causa),
                                false
                        );

                    } else {
                        mostrarError(
                                "No fue posible probar "
                                + "la configuración de correo.",
                                causa
                        );
                    }

                } finally {
                    finalizarOperacion(false);
                }
            }
        };

        ejecutar(trabajador);
    }

    public void crearRespaldo() {
        final String carpeta;

        try {
            validarAcceso();

            carpeta =
                    vista.getRutaRespaldoServidor();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            mostrarAdvertencia(
                    ex.getMessage(),
                    "Respaldo no creado"
            );
            return;
        }

        int respuesta =
                JOptionPane.showConfirmDialog(
                        vista,
                        "SQL Server creará un respaldo "
                        + "completo de la base SIGIR.\n\n"
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

        if (!iniciarOperacion(
                "CREAR_RESPALDO")) {

            return;
        }

        SwingWorker<String, Void> trabajador =
                new SwingWorker<>() {

            @Override
            protected String doInBackground()
                    throws Exception {

                return dao.crearRespaldo(
                        carpeta,
                        Sesion.getIdUsuario()
                );
            }

            @Override
            protected void done() {
                try {
                    String archivo = get();

                    vista.setRutaArchivoRestauracion(
                            archivo
                    );

                    JOptionPane.showMessageDialog(
                            vista,
                            "El respaldo fue creado "
                            + "correctamente.\n\n"
                            + archivo,
                            "Respaldo completado",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    mostrarFalloOperacion(
                            "No fue posible crear "
                            + "el respaldo.\n"
                            + "La carpeta debe existir "
                            + "en la computadora donde "
                            + "está instalado SQL Server "
                            + "y su servicio debe tener "
                            + "permiso de escritura.",
                            "Respaldo no creado",
                            causaReal(ex)
                    );

                } finally {
                    finalizarOperacion(false);
                }
            }
        };

        ejecutar(trabajador);
    }

    public void verificarRespaldo() {
        final String archivo;

        try {
            validarAcceso();

            archivo =
                    vista.getRutaArchivoRestauracion();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            mostrarAdvertencia(
                    ex.getMessage(),
                    "Respaldo no verificado"
            );
            return;
        }

        if (!iniciarOperacion(
                "VERIFICAR_RESPALDO")) {

            return;
        }

        SwingWorker<Void, Void> trabajador =
                new SwingWorker<>() {

            @Override
            protected Void doInBackground()
                    throws Exception {

                dao.verificarRespaldo(
                        archivo,
                        Sesion.getIdUsuario()
                );

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    JOptionPane.showMessageDialog(
                            vista,
                            "SQL Server verificó "
                            + "el respaldo correctamente.\n\n"
                            + archivo,
                            "Respaldo válido",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    mostrarFalloOperacion(
                            "El archivo no pudo verificarse.",
                            "Respaldo no verificado",
                            causaReal(ex)
                    );

                } finally {
                    finalizarOperacion(false);
                }
            }
        };

        ejecutar(trabajador);
    }

    public void restaurarRespaldo() {
        final String archivo;

        try {
            validarAcceso();

            archivo =
                    vista.getRutaArchivoRestauracion();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            mostrarAdvertencia(
                    ex.getMessage(),
                    "Restauración cancelada"
            );
            return;
        }

        String confirmacion =
                JOptionPane.showInputDialog(
                        vista,
                        "Esta operación reemplazará "
                        + "la base SIGIR con el respaldo "
                        + "indicado.\n"
                        + "Se perderán los cambios "
                        + "realizados después de ese "
                        + "respaldo.\n\n"
                        + "Escribe RESTAURAR SIGIR "
                        + "para continuar:",
                        "Confirmación de restauración",
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacion == null) {
            return;
        }

        if (!"RESTAURAR SIGIR".equals(
                confirmacion.trim())) {

            mostrarAdvertencia(
                    "La frase de confirmación "
                    + "no coincide.",
                    "Restauración cancelada"
            );
            return;
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

        if (!iniciarOperacion(
                "RESTAURAR_RESPALDO")) {

            return;
        }

        SwingWorker<Void, Void> trabajador =
                new SwingWorker<>() {

            @Override
            protected Void doInBackground()
                    throws Exception {

                dao.verificarRespaldo(
                        archivo,
                        Sesion.getIdUsuario()
                );

                dao.restaurarRespaldo(
                        archivo,
                        Sesion.getIdUsuario()
                );

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    JOptionPane.showMessageDialog(
                            vista,
                            "La base SIGIR fue restaurada.\n"
                            + "Cierra y vuelve a abrir "
                            + "el sistema antes de "
                            + "continuar trabajando.",
                            "Restauración completada",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La operación fue cancelada.

                } catch (ExecutionException ex) {
                    mostrarFalloOperacion(
                            "No fue posible restaurar "
                            + "la base de datos.\n"
                            + "Verifica que la ruta "
                            + "pertenezca al servidor SQL, "
                            + "que el archivo sea válido "
                            + "y que el usuario SQL tenga "
                            + "permisos de restauración.",
                            "Restauración cancelada",
                            causaReal(ex)
                    );

                } finally {
                    recargaPendiente = false;
                    finalizarOperacion(false);
                }
            }
        };

        ejecutar(trabajador);
    }

    private boolean iniciarOperacion(
            String accion) {

        if (hayOperacionActiva()) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Ya hay una operación en curso. "
                    + "Espera a que termine.",
                    "Configuración ocupada",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        vista.establecerOperacion(
                true,
                accion
        );

        return true;
    }

    private void ejecutar(
            SwingWorker<?, ?> trabajador) {

        trabajadorActual = trabajador;
        trabajador.execute();
    }

    private boolean hayOperacionActiva() {
        return trabajadorActual != null
                && !trabajadorActual.isDone();
    }

    private void finalizarOperacion(
            boolean ejecutarRecargaPendiente) {

        trabajadorActual = null;

        vista.establecerOperacion(
                false,
                null
        );

        if (ejecutarRecargaPendiente
                && recargaPendiente) {

            recargaPendiente = false;
            cargarAsync();
        }
    }

    private Throwable causaReal(
            ExecutionException ex) {

        return ex.getCause() == null
                ? ex
                : ex.getCause();
    }

    private String mensaje(
            Throwable ex) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "Error desconocido";
        }

        return ex.getMessage();
    }

    private void mostrarFalloOperacion(
            String mensaje,
            String titulo,
            Throwable ex) {

        if (ex instanceof IllegalArgumentException
                || ex instanceof IllegalStateException) {

            mostrarAdvertencia(
                    mensaje(ex),
                    titulo
            );
            return;
        }

        mostrarError(
                mensaje,
                ex
        );
    }

    private void mostrarAdvertencia(
            String mensaje,
            String titulo) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje,
                titulo,
                JOptionPane.WARNING_MESSAGE
        );
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
                    "El prefijo de factura solo puede "
                    + "contener letras, números y guiones."
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
            Throwable ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje
                + "\n\nDetalle: "
                + mensaje(ex),
                "Error de SQL Server",
                JOptionPane.ERROR_MESSAGE
        );

        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
