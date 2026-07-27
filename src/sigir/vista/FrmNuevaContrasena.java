package sigir.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import sigir.componentes.RoundedBorder;
import sigir.dao.RecuperacionContrasenaDAO;
import sigir.util.PasswordUtil;

public class FrmNuevaContrasena extends javax.swing.JFrame {

    private JFrame ventanaLogin;
    private long idRecuperacion;
    private String correo;
    private boolean navegando;
    private char ecoOriginal;

    public FrmNuevaContrasena() {
        this(null, 0L, "");
    }

    public FrmNuevaContrasena(
            JFrame ventanaLogin,
            long idRecuperacion,
            String correo) {

        this.ventanaLogin = ventanaLogin;
        this.idRecuperacion = idRecuperacion;
        this.correo = correo == null ? "" : correo;

        initComponents();
        configurarVentana();
        aplicarEstilos();
        configurarEventos();
    }

    private void configurarVentana() {
        setLocationRelativeTo(null);
        ecoOriginal = txtNuevaContrasena.getEchoChar();

        lblCorreo.setText(
                correo.isBlank()
                        ? "Crea una contraseña segura."
                        : "Cuenta: " + correo
        );
    }

    private void aplicarEstilos() {
        Color borde = new Color(203, 213, 225);

        pnlTarjeta.setBorder(new CompoundBorder(
                new RoundedBorder(
                        new Color(219, 226, 235),
                        26,
                        1.2f
                ),
                new EmptyBorder(18, 36, 18, 36)
        ));

        txtNuevaContrasena.setBorder(new CompoundBorder(
                new RoundedBorder(borde, 13, 1.3f),
                new EmptyBorder(0, 14, 0, 14)
        ));

        txtConfirmarContrasena.setBorder(new CompoundBorder(
                new RoundedBorder(borde, 13, 1.3f),
                new EmptyBorder(0, 14, 0, 14)
        ));

        btnGuardar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);

        btnCancelar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardarContrasena());
        btnCancelar.addActionListener(e -> volverAlLogin());

        chkMostrar.addActionListener(e -> {
            char eco = chkMostrar.isSelected()
                    ? (char) 0
                    : ecoOriginal;

            txtNuevaContrasena.setEchoChar(eco);
            txtConfirmarContrasena.setEchoChar(eco);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!navegando && ventanaLogin != null) {
                    ventanaLogin.setVisible(true);
                }
            }
        });
    }

    private void guardarContrasena() {
        char[] nueva = txtNuevaContrasena.getPassword();
        char[] confirmar = txtConfirmarContrasena.getPassword();

        try {
            String error = validarContrasena(nueva, confirmar);

            if (error != null) {
                JOptionPane.showMessageDialog(
                        this,
                        error,
                        "Contraseña no válida",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (idRecuperacion <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No existe una solicitud de recuperación válida.",
                        "Recuperación inválida",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            char[] copiaContrasena =
                    Arrays.copyOf(nueva, nueva.length);

            establecerProcesando(true);

            new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        String hash =
                                PasswordUtil.generarHash(copiaContrasena);

                        RecuperacionContrasenaDAO dao =
                                new RecuperacionContrasenaDAO();

                        dao.cambiarContrasena(
                                idRecuperacion,
                                hash
                        );

                        return null;

                    } finally {
                        Arrays.fill(copiaContrasena, '\0');
                    }
                }

                @Override
                protected void done() {
                    establecerProcesando(false);

                    try {
                        get();

                        JOptionPane.showMessageDialog(
                                FrmNuevaContrasena.this,
                                "La contraseña se cambió correctamente. "
                                + "Ya puedes iniciar sesión.",
                                "Contraseña actualizada",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        volverAlLogin();

                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        mostrarError(
                                "La operación fue interrumpida.",
                                ex
                        );

                    } catch (ExecutionException ex) {
                        mostrarError(
                                "No fue posible cambiar la contraseña.",
                                obtenerCausa(ex)
                        );
                    }
                }
            }.execute();

        } finally {
            Arrays.fill(nueva, '\0');
            Arrays.fill(confirmar, '\0');
        }
    }

    private String validarContrasena(
            char[] nueva,
            char[] confirmar) {

        if (nueva.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }

        if (!Arrays.equals(nueva, confirmar)) {
            return "Las contraseñas no coinciden.";
        }

        boolean mayuscula = false;
        boolean minuscula = false;
        boolean numero = false;
        boolean simbolo = false;

        for (char caracter : nueva) {
            if (Character.isUpperCase(caracter)) {
                mayuscula = true;
            } else if (Character.isLowerCase(caracter)) {
                minuscula = true;
            } else if (Character.isDigit(caracter)) {
                numero = true;
            } else {
                simbolo = true;
            }
        }

        if (!mayuscula || !minuscula || !numero || !simbolo) {
            return "La contraseña debe incluir mayúscula, minúscula, "
                    + "número y símbolo.";
        }

        return null;
    }

    private void establecerProcesando(boolean procesando) {
        txtNuevaContrasena.setEnabled(!procesando);
        txtConfirmarContrasena.setEnabled(!procesando);
        chkMostrar.setEnabled(!procesando);
        btnGuardar.setEnabled(!procesando);
        btnCancelar.setEnabled(!procesando);

        btnGuardar.setText(
                procesando
                        ? "Guardando..."
                        : "Cambiar contraseña"
        );
    }

    private void volverAlLogin() {
        navegando = true;

        if (ventanaLogin != null) {
            ventanaLogin.setVisible(true);
        } else {
            new LoginFrame().setVisible(true);
        }

        dispose();
    }

    private Throwable obtenerCausa(Throwable error) {
        Throwable actual = error;

        while (actual.getCause() != null) {
            actual = actual.getCause();
        }

        return actual;
    }

    private void mostrarError(
            String titulo,
            Throwable error) {

        JOptionPane.showMessageDialog(
                this,
                titulo + "\n\n"
                + (error == null
                        ? "Error desconocido."
                        : error.getMessage()),
                "Recuperación de contraseña",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblCorreo = new javax.swing.JLabel();
        lblNueva = new javax.swing.JLabel();
        txtNuevaContrasena = new javax.swing.JPasswordField();
        lblConfirmar = new javax.swing.JLabel();
        txtConfirmarContrasena = new javax.swing.JPasswordField();
        chkMostrar = new javax.swing.JCheckBox();
        lblReglas = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SIGIR - Nueva contraseña");
        setMinimumSize(new java.awt.Dimension(760, 600));
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(244, 247, 251));
        pnlFondo.setPreferredSize(new java.awt.Dimension(760, 600));
        pnlFondo.setLayout(null);

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setLayout(null);

        lblLogo.setFont(new java.awt.Font("Segoe UI Symbol", 1, 42));
        lblLogo.setForeground(new java.awt.Color(84, 116, 158));
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setText("🔒");
        pnlTarjeta.add(lblLogo);
        lblLogo.setBounds(210, 18, 100, 54);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblTitulo.setForeground(new java.awt.Color(28, 39, 55));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Crear nueva contraseña");
        pnlTarjeta.add(lblTitulo);
        lblTitulo.setBounds(55, 76, 410, 38);

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblCorreo.setForeground(new java.awt.Color(100, 116, 139));
        lblCorreo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCorreo.setText("Crea una contraseña segura.");
        pnlTarjeta.add(lblCorreo);
        lblCorreo.setBounds(55, 116, 410, 24);

        lblNueva.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblNueva.setForeground(new java.awt.Color(28, 39, 55));
        lblNueva.setText("Nueva contraseña");
        pnlTarjeta.add(lblNueva);
        lblNueva.setBounds(56, 158, 180, 22);

        txtNuevaContrasena.setFont(new java.awt.Font("Segoe UI", 0, 15));
        pnlTarjeta.add(txtNuevaContrasena);
        txtNuevaContrasena.setBounds(56, 186, 408, 50);

        lblConfirmar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblConfirmar.setForeground(new java.awt.Color(28, 39, 55));
        lblConfirmar.setText("Confirmar contraseña");
        pnlTarjeta.add(lblConfirmar);
        lblConfirmar.setBounds(56, 250, 200, 22);

        txtConfirmarContrasena.setFont(new java.awt.Font("Segoe UI", 0, 15));
        pnlTarjeta.add(txtConfirmarContrasena);
        txtConfirmarContrasena.setBounds(56, 278, 408, 50);

        chkMostrar.setBackground(new java.awt.Color(255, 255, 255));
        chkMostrar.setFont(new java.awt.Font("Segoe UI", 0, 13));
        chkMostrar.setForeground(new java.awt.Color(55, 70, 91));
        chkMostrar.setText("Mostrar contraseñas");
        pnlTarjeta.add(chkMostrar);
        chkMostrar.setBounds(56, 340, 190, 28);

        lblReglas.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblReglas.setForeground(new java.awt.Color(100, 116, 139));
        lblReglas.setText("<html>Mínimo 8 caracteres e incluir mayúscula, minúscula,<br>número y símbolo.</html>");
        pnlTarjeta.add(lblReglas);
        lblReglas.setBounds(56, 374, 408, 42);

        btnGuardar.setBackground(new java.awt.Color(91, 126, 170));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 16));
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Cambiar contraseña");
        pnlTarjeta.add(btnGuardar);
        btnGuardar.setBounds(56, 430, 408, 52);

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnCancelar.setForeground(new java.awt.Color(100, 116, 139));
        btnCancelar.setText("Cancelar y volver al inicio de sesión");
        pnlTarjeta.add(btnCancelar);
        btnCancelar.setBounds(110, 494, 300, 30);

        pnlFondo.add(pnlTarjeta);
        pnlTarjeta.setBounds(120, 24, 520, 548);

        getContentPane().add(pnlFondo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmNuevaContrasena().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JCheckBox chkMostrar;
    private javax.swing.JLabel lblConfirmar;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblNueva;
    private javax.swing.JLabel lblReglas;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JPasswordField txtConfirmarContrasena;
    private javax.swing.JPasswordField txtNuevaContrasena;
    // End of variables declaration//GEN-END:variables
}
