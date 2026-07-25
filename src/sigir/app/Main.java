package sigir.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import sigir.vista.LoginFrame;

public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ex) {
                System.err.println(
                        "No se pudo cargar el estilo visual del sistema."
                );
            }

            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
