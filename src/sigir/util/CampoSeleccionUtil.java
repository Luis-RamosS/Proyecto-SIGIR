package sigir.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Utilidades de edición para campos de texto.
 */
public final class CampoSeleccionUtil {

    private CampoSeleccionUtil() {
    }

    /**
     * Hace que al entrar al campo o volver a hacer clic sobre él se seleccione
     * todo el contenido, facilitando reemplazar el valor actual.
     */
    public static void seleccionarTodoAlEnfocar(JTextField... campos) {
        if (campos == null) {
            return;
        }

        for (JTextField campo : campos) {
            if (campo == null) {
                continue;
            }

            campo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(campo::selectAll);
                }
            });

            campo.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (campo.isFocusOwner()) {
                        SwingUtilities.invokeLater(campo::selectAll);
                    }
                }
            });
        }
    }
}
