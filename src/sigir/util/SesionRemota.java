package sigir.util;

import java.sql.SQLException;
import sigir.dao.SesionActivaDAO;

public final class SesionRemota {

    private static final SesionActivaDAO DAO = new SesionActivaDAO();
    private static String token;
    private static boolean hookRegistrado;

    private SesionRemota() {
    }

    public static synchronized void iniciar(String nuevoToken) {
        token = nuevoToken;

        if (!hookRegistrado) {
            hookRegistrado = true;
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> cerrarSilenciosamente("CIERRE_APLICACION"))
            );
        }
    }

    public static synchronized boolean haySesion() {
        return token != null && !token.isBlank();
    }

    public static synchronized boolean refrescar() throws SQLException {
        if (!haySesion()) {
            return false;
        }
        return DAO.actualizarHeartbeat(token);
    }

    public static synchronized void cerrar(String motivo) throws SQLException {
        if (!haySesion()) {
            return;
        }

        String actual = token;
        token = null;
        DAO.cerrarSesion(actual, motivo);
    }

    public static synchronized void cerrarSilenciosamente(String motivo) {
        try {
            cerrar(motivo);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
