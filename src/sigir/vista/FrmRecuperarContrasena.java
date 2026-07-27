package sigir.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import sigir.componentes.RoundedBorder;
import sigir.correo.CorreoServicio;
import sigir.dao.RecuperacionContrasenaDAO;
import sigir.modelo.SolicitudRecuperacion;
import sigir.util.CodigoRecuperacionUtil;

public class FrmRecuperarContrasena extends javax.swing.JFrame {

    private JFrame ventanaLogin;
    private boolean navegando;

    public FrmRecuperarContrasena() {
        this(null, "");
    }

    public FrmRecuperarContrasena(JFrame ventanaLogin) {
        this(ventanaLogin, "");
    }

    public FrmRecuperarContrasena(
            JFrame ventanaLogin,
            String correoInicial) {

        this.ventanaLogin = ventanaLogin;

        initComponents();
        configurarVentana();
        aplicarEstilos();
        configurarEventos();

        txtCorreo.setText(
                correoInicial == null ? "" : correoInicial
        );
    }

    private void configurarVentana() {
        setLocationRelativeTo(null);
    }

    private void aplicarEstilos() {
        Color borde = new Color(203, 213, 225);

        pnlTarjeta.setBorder(new CompoundBorder(
                new RoundedBorder(
                        new Color(219, 226, 235),
                        26,
                        1.2f
                ),
                new EmptyBorder(22, 36, 22, 36)
        ));

        txtCorreo.setBorder(new CompoundBorder(
                new RoundedBorder(borde, 13, 1.3f),
                new EmptyBorder(0, 14, 0, 14)
        ));

        btnEnviar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);

        btnVolver.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setContentAreaFilled(false);
    }

    private void configurarEventos() {
        btnEnviar.addActionListener(e -> enviarCodigo());
        btnVolver.addActionListener(e -> volverAlLogin());
        txtCorreo.addActionListener(e -> enviarCodigo());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!navegando && ventanaLogin != null) {
                    ventanaLogin.setVisible(true);
                }
            }
        });
    }

    private void enviarCodigo() {
        String correo = CodigoRecuperacionUtil.normalizarCorreo(
                txtCorreo.getText()
        );

        if (!CodigoRecuperacionUtil.esCorreoValido(correo)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingresa una dirección de correo válida.",
                    "Correo inválido",
                    JOptionPane.WARNING_MESSAGE
            );
            txtCorreo.requestFocusInWindow();
            return;
        }

        establecerProcesando(true);

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                String codigo =
                        CodigoRecuperacionUtil.generarCodigo();

                String codigoHash =
                        CodigoRecuperacionUtil.generarHashSha256(codigo);

                RecuperacionContrasenaDAO dao =
                        new RecuperacionContrasenaDAO();

                Optional<SolicitudRecuperacion> solicitud =
                        dao.crearSolicitud(
                                correo,
                                codigoHash,
                                10
                        );

                if (solicitud.isPresent()) {
                    CorreoServicio servicio = new CorreoServicio();
                    servicio.enviarCodigo(
                            solicitud.get(),
                            codigo
                    );
                }

                return solicitud.isPresent();
            }

            @Override
            protected void done() {
                establecerProcesando(false);

                try {
                    get();

                    JOptionPane.showMessageDialog(
                            FrmRecuperarContrasena.this,
                            "Si el correo está registrado y verificado, "
                            + "recibirás un código de recuperación.",
                            "Solicitud procesada",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    navegando = true;

                    FrmValidarCodigo ventana =
                            new FrmValidarCodigo(
                                    ventanaLogin,
                                    correo
                            );

                    ventana.setVisible(true);
                    dispose();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(
                            "La operación fue interrumpida.",
                            ex
                    );

                } catch (ExecutionException ex) {
                    mostrarError(
                            "No fue posible enviar el código.",
                            obtenerCausa(ex)
                    );
                }
            }
        }.execute();
    }

    private void establecerProcesando(boolean procesando) {
        txtCorreo.setEnabled(!procesando);
        btnEnviar.setEnabled(!procesando);
        btnVolver.setEnabled(!procesando);

        btnEnviar.setText(
                procesando
                        ? "Enviando..."
                        : "Enviar código"
        );

        lblEstado.setText(
                procesando
                        ? "Conectando con el servidor de correo..."
                        : "El código tendrá una vigencia de 10 minutos."
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

        String detalle = error == null
                ? "Error desconocido."
                : error.getMessage();

        JOptionPane.showMessageDialog(
                this,
                titulo + "\n\n" + detalle,
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
        lblDescripcion = new javax.swing.JLabel();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        btnEnviar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        lblEstado = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SIGIR - Recuperar contraseña");
        setMinimumSize(new java.awt.Dimension(760, 540));
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(244, 247, 251));
        pnlFondo.setPreferredSize(new java.awt.Dimension(760, 540));
        pnlFondo.setLayout(null);

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setLayout(null);

        lblLogo.setFont(new java.awt.Font("Segoe UI Symbol", 1, 42));
        lblLogo.setForeground(new java.awt.Color(84, 116, 158));
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setText("⬡");
        pnlTarjeta.add(lblLogo);
        lblLogo.setBounds(210, 28, 100, 54);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblTitulo.setForeground(new java.awt.Color(28, 39, 55));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Recuperar contraseña");
        pnlTarjeta.add(lblTitulo);
        lblTitulo.setBounds(70, 88, 380, 38);

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblDescripcion.setForeground(new java.awt.Color(100, 116, 139));
        lblDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDescripcion.setText("<html><div style='text-align:center'>Ingresa el correo asociado a tu cuenta de SIGIR.<br>Te enviaremos un código de verificación.</div></html>");
        pnlTarjeta.add(lblDescripcion);
        lblDescripcion.setBounds(55, 130, 410, 52);

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblCorreo.setForeground(new java.awt.Color(28, 39, 55));
        lblCorreo.setText("Correo electrónico");
        pnlTarjeta.add(lblCorreo);
        lblCorreo.setBounds(56, 202, 180, 22);

        txtCorreo.setFont(new java.awt.Font("Segoe UI", 0, 15));
        txtCorreo.setForeground(new java.awt.Color(28, 39, 55));
        pnlTarjeta.add(txtCorreo);
        txtCorreo.setBounds(56, 230, 408, 52);

        btnEnviar.setBackground(new java.awt.Color(91, 126, 170));
        btnEnviar.setFont(new java.awt.Font("Segoe UI", 1, 16));
        btnEnviar.setForeground(new java.awt.Color(255, 255, 255));
        btnEnviar.setText("Enviar código");
        pnlTarjeta.add(btnEnviar);
        btnEnviar.setBounds(56, 304, 408, 52);

        btnVolver.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnVolver.setForeground(new java.awt.Color(64, 112, 178));
        btnVolver.setText("← Volver al inicio de sesión");
        pnlTarjeta.add(btnVolver);
        btnVolver.setBounds(130, 370, 260, 34);

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblEstado.setForeground(new java.awt.Color(100, 116, 139));
        lblEstado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEstado.setText("El código tendrá una vigencia de 10 minutos.");
        pnlTarjeta.add(lblEstado);
        lblEstado.setBounds(55, 418, 410, 24);

        pnlFondo.add(pnlTarjeta);
        pnlTarjeta.setBounds(120, 36, 520, 468);

        getContentPane().add(pnlFondo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmRecuperarContrasena().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTextField txtCorreo;
    // End of variables declaration//GEN-END:variables
}
