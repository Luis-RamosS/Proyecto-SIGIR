package sigir.util;

import sigir.modelo.Usuario;

public final class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario de la sesión no puede ser nulo."
            );
        }

        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static boolean esDueno() {
        return haySesionActiva()
                && usuarioActual.esDueno();
    }

    public static boolean esTrabajador() {
        return haySesionActiva()
                && usuarioActual.esTrabajador();
    }

    public static int getIdUsuario() {

        if (!haySesionActiva()) {
            throw new IllegalStateException(
                    "No existe una sesión activa."
            );
        }

        return usuarioActual.getIdUsuario();
    }

    public static String getNombreCompleto() {

        if (!haySesionActiva()) {
            return "";
        }

        return usuarioActual.getNombreCompleto();
    }

    public static String getRol() {

        if (!haySesionActiva()) {
            return "";
        }

        return usuarioActual.getRol();
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}