package sigir.util;

import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class FiltroTiempoReal {

    private FiltroTiempoReal() {
    }

    public static void activar(
            JTextField campo,
            Runnable accionBusqueda) {

        Timer temporizador = new Timer(
                200,
                e -> accionBusqueda.run()
        );

        temporizador.setRepeats(false);

        campo.getDocument().addDocumentListener(
                new DocumentListener() {

            private void actualizar() {
                temporizador.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizar();
            }
        });
    }
}