package sigir.correo;

public final class CorreoConfig {

    /*
     * Para una prueba rápida puedes reemplazar los dos valores marcados.
     * Es más seguro utilizar las variables de entorno:
     *
     * SIGIR_SMTP_USER
     * SIGIR_SMTP_PASSWORD
     * SIGIR_SMTP_HOST
     * SIGIR_SMTP_PORT
     * SIGIR_SMTP_FROM
     */

    private static final String HOST =
            obtenerValor("SIGIR_SMTP_HOST", "smtp.gmail.com");

    private static final int PUERTO =
            obtenerEntero("SIGIR_SMTP_PORT", 587);

    private static final String USUARIO =
        obtenerValor(
                "SIGIR_SMTP_USER",
                ""
        );
    
    private static final String CLAVE_APLICACION =
        obtenerValor(
                "SIGIR_SMTP_PASSWORD",
                ""
        );
    
    private static final String REMITENTE =
        obtenerValor(
                "SIGIR_SMTP_FROM",
                USUARIO
        );
    private CorreoConfig() {
    }

    public static String getHost() {
        return HOST;
    }

    public static int getPuerto() {
        return PUERTO;
    }

    public static String getUsuario() {
        return USUARIO;
    }

    public static String getClaveAplicacion() {
        return CLAVE_APLICACION;
    }

    public static String getRemitente() {
        return REMITENTE;
    }

    public static void validarConfiguracion() {
        if (USUARIO.isBlank()
                || USUARIO.startsWith("TU_CORREO")
                || CLAVE_APLICACION.isBlank()
                || CLAVE_APLICACION.startsWith("TU_CLAVE")) {

            throw new IllegalStateException(
                    "Debes configurar el correo remitente y su clave de "
                    + "aplicación en CorreoConfig.java."
            );
        }
    }

    private static String obtenerValor(
            String nombre,
            String valorPredeterminado) {

        String propiedad = System.getProperty(nombre);

        if (propiedad != null && !propiedad.isBlank()) {
            return propiedad.trim();
        }

        String variable = System.getenv(nombre);

        if (variable != null && !variable.isBlank()) {
            return variable.trim();
        }

        return valorPredeterminado;
    }

    private static int obtenerEntero(
            String nombre,
            int valorPredeterminado) {

        String valor = obtenerValor(
                nombre,
                String.valueOf(valorPredeterminado)
        );

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException ex) {
            return valorPredeterminado;
        }
    }
}
