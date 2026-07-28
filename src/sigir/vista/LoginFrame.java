package sigir.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import sigir.componentes.BotonGradiente;
import sigir.componentes.IconoLinea;
import sigir.componentes.LogoSIGIR;
import sigir.componentes.PanelFondoLogin;
import sigir.componentes.PanelTarjeta;
import sigir.componentes.RoundedBorder;
import sigir.util.Colores;

import java.sql.SQLException;
import java.util.Arrays;
import sigir.dao.UsuarioDAO;
import sigir.modelo.Usuario;
import sigir.util.Sesion;

public class LoginFrame extends JFrame {

    private static final String PLACEHOLDER_USUARIO = "Ingresa tu usuario";
    private static final String PLACEHOLDER_PASSWORD = "Ingresa tu contraseña";

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    private boolean usuarioPlaceholderActivo = true;
    private boolean passwordPlaceholderActivo = true;
    private boolean mostrarPassword = false;

    public LoginFrame() {
        configurarVentana();
        construirInterfaz();
    }

    private void configurarVentana() {
        setTitle("SIGIR - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setSize(1366, 850);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void construirInterfaz() {
        PanelFondoLogin fondo = new PanelFondoLogin();
        fondo.setLayout(new GridBagLayout());

        PanelTarjeta tarjeta = new PanelTarjeta(34);
        tarjeta.setPreferredSize(new Dimension(620, 750));
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(36, 66, 48, 66));

        LogoSIGIR logo = new LogoSIGIR();
        logo.setPreferredSize(new Dimension(100, 86));
        logo.setMaximumSize(new Dimension(100, 86));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("SIGIR");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 49));
        lblTitulo.setForeground(Colores.TEXTO_PRINCIPAL);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión Integral");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 23));
        lblSubtitulo.setForeground(Colores.TEXTO_SECUNDARIO);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel separador = crearSeparador();

        JLabel lblUsuario = crearEtiqueta("Usuario");
        JPanel campoUsuario = crearContenedorCampo(
                txtUsuario,
                new IconoLinea(
                        IconoLinea.Tipo.USUARIO,
                        Colores.TEXTO_SECUNDARIO,
                        28
                ),
                false
        );

        JLabel lblPassword = crearEtiqueta("Contraseña");
        JPanel campoPassword = crearContenedorCampo(
                txtPassword,
                new IconoLinea(
                        IconoLinea.Tipo.CANDADO,
                        Colores.TEXTO_SECUNDARIO,
                        27
                ),
                true
        );

        configurarPlaceholderUsuario();
        configurarPlaceholderPassword();

        JPanel opciones = crearPanelOpciones();

        BotonGradiente btnIniciar = new BotonGradiente("Iniciar sesión");
        btnIniciar.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnIniciar.setPreferredSize(new Dimension(488, 60));
        btnIniciar.setMaximumSize(new Dimension(488, 60));
        btnIniciar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIniciar.addActionListener(e -> iniciarSesion());

        getRootPane().setDefaultButton(btnIniciar);

        tarjeta.add(logo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(2));
        tarjeta.add(lblSubtitulo);
        tarjeta.add(Box.createVerticalStrut(17));
        tarjeta.add(separador);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(lblUsuario);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(campoUsuario);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(lblPassword);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(campoPassword);
        tarjeta.add(Box.createVerticalStrut(17));
        tarjeta.add(opciones);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(btnIniciar);

        fondo.add(tarjeta);
        setContentPane(fondo);
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(Colores.TEXTO_PRINCIPAL);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(488, 26));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JPanel crearSeparador() {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.CENTER,
                12,
                0
        ));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(488, 16));
        panel.setMaximumSize(new Dimension(488, 16));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel linea1 = new JLabel();
        linea1.setOpaque(true);
        linea1.setBackground(new Color(219, 225, 233));
        linea1.setPreferredSize(new Dimension(205, 1));

        JLabel punto = new JLabel("•");
        punto.setFont(new Font("SansSerif", Font.BOLD, 18));
        punto.setForeground(new Color(205, 214, 225));

        JLabel linea2 = new JLabel();
        linea2.setOpaque(true);
        linea2.setBackground(new Color(219, 225, 233));
        linea2.setPreferredSize(new Dimension(205, 1));

        panel.add(linea1);
        panel.add(punto);
        panel.add(linea2);
        return panel;
    }

    private JPanel crearContenedorCampo(
            JTextComponent campo,
            IconoLinea icono,
            boolean incluirOjo) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(488, 60));
        panel.setMaximumSize(new Dimension(488, 60));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.setBorder(new CompoundBorder(
                new RoundedBorder(Colores.BORDE, 14, 1.4f),
                new EmptyBorder(0, 16, 0, 12)
        ));

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setPreferredSize(new Dimension(34, 34));

        campo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
        campo.setOpaque(false);
        campo.setFont(new Font("SansSerif", Font.PLAIN, 17));
        campo.setForeground(Colores.TEXTO_PRINCIPAL);
        campo.setCaretColor(Colores.TEXTO_PRINCIPAL);

        panel.add(lblIcono);
        panel.add(campo);

        if (incluirOjo) {
            JButton btnOjo = new JButton(new IconoLinea(
                    IconoLinea.Tipo.OJO,
                    Colores.TEXTO_SECUNDARIO,
                    27
            ));

            btnOjo.setBorderPainted(false);
            btnOjo.setContentAreaFilled(false);
            btnOjo.setFocusPainted(false);
            btnOjo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnOjo.setPreferredSize(new Dimension(38, 38));
            btnOjo.setToolTipText("Mostrar u ocultar contraseña");
            btnOjo.addActionListener(e -> alternarPassword());

            panel.add(btnOjo);
        }

        FocusAdapter focoBorde = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                panel.setBorder(new CompoundBorder(
                        new RoundedBorder(
                                Colores.BORDE_FOCO,
                                14,
                                1.7f
                        ),
                        new EmptyBorder(0, 16, 0, 12)
                ));
                panel.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                panel.setBorder(new CompoundBorder(
                        new RoundedBorder(
                                Colores.BORDE,
                                14,
                                1.4f
                        ),
                        new EmptyBorder(0, 16, 0, 12)
                ));
                panel.repaint();
            }
        };

        campo.addFocusListener(focoBorde);
        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(488, 34));
        panel.setMaximumSize(new Dimension(488, 34));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox chkRecordarme = new JCheckBox("Recordarme");
        chkRecordarme.setOpaque(false);
        chkRecordarme.setFocusPainted(false);
        chkRecordarme.setFont(new Font("SansSerif", Font.PLAIN, 16));
        chkRecordarme.setForeground(Colores.TEXTO_PRINCIPAL);
        chkRecordarme.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        JLabel lblOlvido = new JLabel("¿Olvidaste tu contraseña?");
        lblOlvido.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lblOlvido.setForeground(Colores.ENLACE);
        lblOlvido.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblOlvido.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                FrmRecuperarContrasena formularioRecuperacion
                        = new FrmRecuperarContrasena(LoginFrame.this);

                formularioRecuperacion.setLocationRelativeTo(
                        LoginFrame.this
                );

                formularioRecuperacion.setVisible(true);

                LoginFrame.this.setVisible(false);
            }
        });

        panel.add(chkRecordarme, java.awt.BorderLayout.WEST);
        panel.add(lblOlvido, java.awt.BorderLayout.EAST);
        return panel;
    }

    private void configurarPlaceholderUsuario() {
        txtUsuario.setText(PLACEHOLDER_USUARIO);
        txtUsuario.setForeground(Colores.PLACEHOLDER);

        txtUsuario.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usuarioPlaceholderActivo) {
                    txtUsuario.setText("");
                    txtUsuario.setForeground(Colores.TEXTO_PRINCIPAL);
                    usuarioPlaceholderActivo = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtUsuario.getText().isBlank()) {
                    txtUsuario.setText(PLACEHOLDER_USUARIO);
                    txtUsuario.setForeground(Colores.PLACEHOLDER);
                    usuarioPlaceholderActivo = true;
                }
            }
        });
    }

    private void configurarPlaceholderPassword() {
        txtPassword.setEchoChar((char) 0);
        txtPassword.setText(PLACEHOLDER_PASSWORD);
        txtPassword.setForeground(Colores.PLACEHOLDER);

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (passwordPlaceholderActivo) {
                    txtPassword.setText("");
                    txtPassword.setForeground(Colores.TEXTO_PRINCIPAL);
                    txtPassword.setEchoChar(mostrarPassword ? (char) 0 : '●');
                    passwordPlaceholderActivo = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPassword.getPassword().length == 0) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText(PLACEHOLDER_PASSWORD);
                    txtPassword.setForeground(Colores.PLACEHOLDER);
                    passwordPlaceholderActivo = true;
                }
            }
        });
    }

    private void alternarPassword() {
        if (passwordPlaceholderActivo) {
            return;
        }

        mostrarPassword = !mostrarPassword;
        txtPassword.setEchoChar(mostrarPassword ? (char) 0 : '●');
        txtPassword.requestFocusInWindow();
    }

    private void iniciarSesion() {

        String nombreUsuario = usuarioPlaceholderActivo
                ? ""
                : txtUsuario.getText().trim();

        char[] contrasena = passwordPlaceholderActivo
                ? new char[0]
                : txtPassword.getPassword();

        if (nombreUsuario.isBlank()
                || contrasena.length == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingresa el usuario y la contraseña.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            Arrays.fill(contrasena, '\0');
            return;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            Usuario usuarioAutenticado
                    = usuarioDAO.iniciarSesion(
                            nombreUsuario,
                            contrasena
                    );

            if (usuarioAutenticado == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Usuario o contraseña incorrectos.",
                        "Acceso denegado",
                        JOptionPane.ERROR_MESSAGE
                );

                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
                return;
            }

            Sesion.iniciar(usuarioAutenticado);

            FrmInicio inicio = new FrmInicio(
                    usuarioAutenticado.getNombreCompleto()
            );

            inicio.setVisible(true);
            dispose();

        } catch (IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Acceso no permitido",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible consultar la base de datos.\n"
                    + ex.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE
            );

            ex.printStackTrace();

        } finally {
            Arrays.fill(contrasena, '\0');
        }
    }
}
