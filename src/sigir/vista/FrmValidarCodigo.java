package sigir.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import sigir.componentes.RoundedBorder;
import sigir.dao.RecuperacionContrasenaDAO;
import sigir.util.CodigoRecuperacionUtil;

public class FrmValidarCodigo extends javax.swing.JFrame {

    private JFrame ventanaLogin;
    private String correo;
    private boolean navegando;

    public FrmValidarCodigo() {
        this(null, "");
    }

    public FrmValidarCodigo(
            JFrame ventanaLogin,
            String correo) {

        this.ventanaLogin = ventanaLogin;
        this.correo = CodigoRecuperacionUtil.normalizarCorreo(correo);

        initComponents();
        configurarVentana();
        aplicarEstilos();
        configurarEventos();

        lblCorreoDestino.setText(
                "Código enviado a "
                + CodigoRecuperacionUtil.ocultarCorreo(this.correo)
        );
    }

    private void configurarVentana() {
        setLocationRelativeTo(null);

        ((AbstractDocument) txtCodigo.getDocument())
                .setDocumentFilter(new FiltroCodigo());
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

        txtCodigo.setBorder(new CompoundBorder(
                new RoundedBorder(borde, 13, 1.3f),
                new EmptyBorder(0, 14, 0, 14)
        ));

        btnValidar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnValidar.setFocusPainted(false);
        btnValidar.setBorderPainted(false);

        btnReenviar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnReenviar.setFocusPainted(false);
        btnReenviar.setBorderPainted(false);
        btnReenviar.setContentAreaFilled(false);

        btnCancelar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
    }

    private void configurarEventos() {
        btnValidar.addActionListener(e -> validarCodigo());
        btnReenviar.addActionListener(e -> reenviarCodigo());
        btnCancelar.addActionListener(e -> volverAlLogin());
        txtCodigo.addActionListener(e -> validarCodigo());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!navegando && ventanaLogin != null) {
                    ventanaLogin.setVisible(true);
                }
            }
        });
    }

    private void validarCodigo() {
        String codigo = txtCodigo.getText().trim();

        if (codigo.length() != 6) {
            JOptionPane.showMessageDialog(
                    this,
                    "Escribe el código de seis dígitos.",
                    "Código incompleto",
                    JOptionPane.WARNING_MESSAGE
            );
            txtCodigo.requestFocusInWindow();
            return;
        }

        establecerProcesando(true);

        new SwingWorker<Long, Void>() {

            @Override
            protected Long doInBackground() throws Exception {
                String codigoHash =
                        CodigoRecuperacionUtil.generarHashSha256(codigo);

                RecuperacionContrasenaDAO dao =
                        new RecuperacionContrasenaDAO();

                return dao.validarCodigo(
                        correo,
                        codigoHash
                );
            }

            @Override
            protected void done() {
                establecerProcesando(false);

                try {
                    long idRecuperacion = get();

                    navegando = true;

                    FrmNuevaContrasena ventana =
                            new FrmNuevaContrasena(
                                    ventanaLogin,
                                    idRecuperacion,
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
                            "El código no pudo validarse.",
                            obtenerCausa(ex)
                    );

                    txtCodigo.setText("");
                    txtCodigo.requestFocusInWindow();
                }
            }
        }.execute();
    }

    private void reenviarCodigo() {
        navegando = true;

        FrmRecuperarContrasena ventana =
                new FrmRecuperarContrasena(
                        ventanaLogin,
                        correo
                );

        ventana.setVisible(true);
        dispose();
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

    private void establecerProcesando(boolean procesando) {
        txtCodigo.setEnabled(!procesando);
        btnValidar.setEnabled(!procesando);
        btnReenviar.setEnabled(!procesando);
        btnCancelar.setEnabled(!procesando);

        btnValidar.setText(
                procesando
                        ? "Validando..."
                        : "Validar código"
        );
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

    private static final class FiltroCodigo extends DocumentFilter {

        @Override
        public void insertString(
                FilterBypass fb,
                int offset,
                String texto,
                AttributeSet atributos)
                throws BadLocationException {

            reemplazar(fb, offset, 0, texto, atributos);
        }

        @Override
        public void replace(
                FilterBypass fb,
                int offset,
                int longitud,
                String texto,
                AttributeSet atributos)
                throws BadLocationException {

            reemplazar(
                    fb,
                    offset,
                    longitud,
                    texto,
                    atributos
            );
        }

        private void reemplazar(
                FilterBypass fb,
                int offset,
                int longitud,
                String texto,
                AttributeSet atributos)
                throws BadLocationException {

            if (texto == null) {
                texto = "";
            }

            String soloNumeros = texto.replaceAll("\\D", "");

            int longitudActual =
                    fb.getDocument().getLength();

            int longitudFinal =
                    longitudActual - longitud + soloNumeros.length();

            if (longitudFinal <= 6) {
                fb.replace(
                        offset,
                        longitud,
                        soloNumeros,
                        atributos
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        lblCorreoDestino = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        btnValidar = new javax.swing.JButton();
        btnReenviar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SIGIR - Validar código");
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
        lblLogo.setText("✉");
        pnlTarjeta.add(lblLogo);
        lblLogo.setBounds(210, 24, 100, 54);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 27));
        lblTitulo.setForeground(new java.awt.Color(28, 39, 55));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Verificar código");
        pnlTarjeta.add(lblTitulo);
        lblTitulo.setBounds(70, 84, 380, 38);

        lblDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblDescripcion.setForeground(new java.awt.Color(100, 116, 139));
        lblDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDescripcion.setText("Escribe el código de seis dígitos recibido por correo.");
        pnlTarjeta.add(lblDescripcion);
        lblDescripcion.setBounds(55, 128, 410, 26);

        lblCorreoDestino.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblCorreoDestino.setForeground(new java.awt.Color(74, 104, 145));
        lblCorreoDestino.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCorreoDestino.setText("Código enviado al correo");
        pnlTarjeta.add(lblCorreoDestino);
        lblCorreoDestino.setBounds(55, 158, 410, 24);

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblCodigo.setForeground(new java.awt.Color(28, 39, 55));
        lblCodigo.setText("Código de verificación");
        pnlTarjeta.add(lblCodigo);
        lblCodigo.setBounds(56, 202, 190, 22);

        txtCodigo.setFont(new java.awt.Font("Consolas", 1, 28));
        txtCodigo.setForeground(new java.awt.Color(28, 39, 55));
        txtCodigo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pnlTarjeta.add(txtCodigo);
        txtCodigo.setBounds(56, 230, 408, 58);

        btnValidar.setBackground(new java.awt.Color(91, 126, 170));
        btnValidar.setFont(new java.awt.Font("Segoe UI", 1, 16));
        btnValidar.setForeground(new java.awt.Color(255, 255, 255));
        btnValidar.setText("Validar código");
        pnlTarjeta.add(btnValidar);
        btnValidar.setBounds(56, 310, 408, 52);

        btnReenviar.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnReenviar.setForeground(new java.awt.Color(64, 112, 178));
        btnReenviar.setText("No recibí el código, reenviar");
        pnlTarjeta.add(btnReenviar);
        btnReenviar.setBounds(110, 374, 300, 32);

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnCancelar.setForeground(new java.awt.Color(100, 116, 139));
        btnCancelar.setText("Cancelar y volver al inicio de sesión");
        pnlTarjeta.add(btnCancelar);
        btnCancelar.setBounds(110, 414, 300, 30);

        pnlFondo.add(pnlTarjeta);
        pnlTarjeta.setBounds(120, 36, 520, 468);

        getContentPane().add(pnlFondo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmValidarCodigo().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnReenviar;
    private javax.swing.JButton btnValidar;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblCorreoDestino;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTextField txtCodigo;
    // End of variables declaration//GEN-END:variables
}
