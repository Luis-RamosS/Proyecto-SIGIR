package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import sigir.controlador.ConfiguracionControlador;
import sigir.modelo.ConfiguracionSistema;

public class ConfiguracionPanel
        extends javax.swing.JPanel {

    private static final DateTimeFormatter
            FORMATO_FECHA_HORA =
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm"
                    );

    private static final int MAXIMO_LOGO_BYTES =
            2 * 1024 * 1024;

    private final ConfiguracionControlador controlador;

    private boolean iniciado;
    private byte[] logoActual;
    private String logoNombreActual;

    public ConfiguracionPanel() {
        initComponents();
        configurarComponentes();
        aplicarEstilos();

        controlador =
                new ConfiguracionControlador(this);

        configurarEventos();
    }

    public void activar() {
        if (!iniciado) {
            iniciado = true;
            controlador.iniciarAsync();
            return;
        }

        controlador.recargarSiNecesario();
    }

    public void recargar() {
        controlador.recargarAsync();
    }

    private void configurarComponentes() {
        txtResultadoCorreo.setEditable(false);
        txtResultadoCorreo.setFocusable(false);

        txtDiagnosticoConexion.setEditable(false);
        txtDiagnosticoConexion.setFocusable(false);

        txtUltimaActualizacion.setEditable(false);
        txtUltimaActualizacion.setFocusable(false);

        txtUltimaActualizacion.setBackground(
                new Color(244, 247, 251)
        );

        txtResultadoCorreo.setBackground(
                new Color(247, 249, 252)
        );

        txtDiagnosticoConexion.setBackground(
                new Color(247, 249, 252)
        );

        lblVistaLogo.setHorizontalAlignment(
                javax.swing.SwingConstants.CENTER
        );

        lblVistaLogo.setVerticalAlignment(
                javax.swing.SwingConstants.CENTER
        );

        lblVistaLogo.setText("Sin logo");
    }

    private void aplicarEstilos() {
        Color borde = new Color(220, 227, 236);
        Color azul = new Color(49, 105, 181);
        Color texto = new Color(24, 50, 87);
        Color rojo = new Color(192, 52, 52);

        javax.swing.JPanel[] paneles = {
            pnlTarjetaEmpresa,
            pnlTarjetaActualizacion,
            pnlTarjetaSeguridad,
            pnlDatosEmpresa,
            pnlLogoFactura,
            pnlCorreo,
            pnlSeguridadCorreo,
            pnlConexion,
            pnlRespaldos
        };

        for (javax.swing.JPanel panel : paneles) {
            panel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borde),
                            BorderFactory.createEmptyBorder(
                                    8, 8, 8, 8
                            )
                    )
            );
        }

        lblVistaLogo.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borde),
                        BorderFactory.createEmptyBorder(
                                8, 8, 8, 8
                        )
                )
        );

        javax.swing.JButton[] principales = {
            btnGuardar,
            btnProbarCorreo,
            btnProbarConexion,
            btnCrearRespaldo
        };

        for (javax.swing.JButton boton
                : principales) {

            boton.setBackground(azul);
            boton.setForeground(Color.WHITE);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
        }

        javax.swing.JButton[] secundarios = {
            btnRestaurarDatos,
            btnSeleccionarLogo,
            btnQuitarLogo,
            btnVerificarRespaldo
        };

        for (javax.swing.JButton boton
                : secundarios) {

            boton.setBackground(Color.WHITE);
            boton.setForeground(texto);
            boton.setBorder(
                    BorderFactory.createLineBorder(borde)
            );
            boton.setFocusPainted(false);
        }

        btnRestaurarRespaldo.setBackground(Color.WHITE);
        btnRestaurarRespaldo.setForeground(rojo);
        btnRestaurarRespaldo.setBorder(
                BorderFactory.createLineBorder(
                        new Color(235, 185, 185)
                )
        );

        btnRestaurarRespaldo.setFocusPainted(false);

        for (javax.swing.JButton boton
                : new javax.swing.JButton[]{
                    btnGuardar,
                    btnRestaurarDatos,
                    btnSeleccionarLogo,
                    btnQuitarLogo,
                    btnProbarCorreo,
                    btnProbarConexion,
                    btnCrearRespaldo,
                    btnVerificarRespaldo,
                    btnRestaurarRespaldo
                }) {

            boton.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(
                e -> controlador.guardar()
        );

        btnRestaurarDatos.addActionListener(
                e -> controlador
                        .restaurarValoresCargados()
        );

        btnSeleccionarLogo.addActionListener(
                e -> seleccionarLogo()
        );

        btnQuitarLogo.addActionListener(
                e -> quitarLogo()
        );

        btnProbarCorreo.addActionListener(
                e -> controlador
                        .probarConfiguracionCorreo()
        );

        btnProbarConexion.addActionListener(
                e -> controlador.probarConexion()
        );

        btnCrearRespaldo.addActionListener(
                e -> controlador.crearRespaldo()
        );

        btnVerificarRespaldo.addActionListener(
                e -> controlador.verificarRespaldo()
        );

        btnRestaurarRespaldo.addActionListener(
                e -> controlador.restaurarRespaldo()
        );
    }

    public void mostrarConfiguracion(
            ConfiguracionSistema configuracion) {

        txtNombreEmpresa.setText(
                texto(configuracion.getNombreEmpresa())
        );

        txtRtn.setText(
                texto(configuracion.getRtn())
        );

        txtTelefono.setText(
                texto(configuracion.getTelefono())
        );

        txtCorreoEmpresa.setText(
                texto(configuracion.getCorreo())
        );

        txtDireccion.setText(
                texto(configuracion.getDireccion())
        );

        txtMonedaCodigo.setText(
                texto(configuracion.getMonedaCodigo())
        );

        txtSimboloMoneda.setText(
                texto(configuracion.getSimboloMoneda())
        );

        txtImpuesto.setText(
                configuracion.getPorcentajeImpuesto()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        .toPlainString()
        );

        txtPrefijoFactura.setText(
                texto(configuracion.getPrefijoFactura())
        );

        txtPieFactura.setText(
                texto(configuracion.getPieFactura())
        );

        txtSmtpHost.setText(
                texto(configuracion.getSmtpHost())
        );

        txtSmtpPuerto.setText(
                configuracion.getSmtpPuerto() <= 0
                        ? ""
                        : String.valueOf(
                                configuracion.getSmtpPuerto()
                        )
        );

        txtSmtpUsuario.setText(
                texto(configuracion.getSmtpUsuario())
        );

        txtNombreRemitente.setText(
                texto(
                        configuracion.getNombreRemitente()
                )
        );

        chkSmtpTls.setSelected(
                configuracion.isSmtpTls()
        );

        txtRutaRespaldoServidor.setText(
                texto(
                        configuracion
                                .getRutaRespaldoServidor()
                )
        );

        logoActual = configuracion.getLogo();
        logoNombreActual =
                configuracion.getLogoNombre();

        mostrarLogo(
                logoActual,
                logoNombreActual
        );

        lblEmpresaActualValor.setText(
                texto(configuracion.getNombreEmpresa())
        );

        if (configuracion.getFechaActualizacion()
                == null) {

            txtUltimaActualizacion.setText(
                    "Sin información"
            );

            lblActualizacionValor.setText(
                    "No disponible"
            );

        } else {
            String fecha =
                    configuracion
                            .getFechaActualizacion()
                            .format(
                                    FORMATO_FECHA_HORA
                            );

            String usuario =
                    configuracion
                            .getNombreUsuarioActualiza();

            txtUltimaActualizacion.setText(
                    fecha
                    + (
                        usuario == null
                        || usuario.isBlank()
                                ? ""
                                : " — " + usuario
                    )
            );

            lblActualizacionValor.setText(fecha);
        }

        txtResultadoCorreo.setText(
                "Presiona Probar configuración "
                + "para verificar el servidor SMTP."
        );
    }

    public ConfiguracionSistema
            construirConfiguracion() {

        ConfiguracionSistema configuracion =
                new ConfiguracionSistema();

        configuracion.setNombreEmpresa(
                txtNombreEmpresa
                        .getText()
                        .trim()
        );

        configuracion.setRtn(
                opcional(txtRtn.getText())
        );

        configuracion.setTelefono(
                opcional(txtTelefono.getText())
        );

        configuracion.setCorreo(
                opcional(
                        txtCorreoEmpresa.getText()
                )
        );

        configuracion.setDireccion(
                opcional(txtDireccion.getText())
        );

        configuracion.setMonedaCodigo(
                txtMonedaCodigo
                        .getText()
                        .trim()
                        .toUpperCase()
        );

        configuracion.setSimboloMoneda(
                txtSimboloMoneda
                        .getText()
                        .trim()
        );

        configuracion.setPorcentajeImpuesto(
                convertirDecimal(
                        txtImpuesto.getText(),
                        "porcentaje de impuesto"
                )
        );

        configuracion.setPrefijoFactura(
                txtPrefijoFactura
                        .getText()
                        .trim()
                        .toUpperCase()
        );

        configuracion.setPieFactura(
                opcional(txtPieFactura.getText())
        );

        configuracion.setLogo(logoActual);
        configuracion.setLogoNombre(
                logoNombreActual
        );

        configuracion.setSmtpHost(
                txtSmtpHost
                        .getText()
                        .trim()
        );

        configuracion.setSmtpPuerto(
                convertirEntero(
                        txtSmtpPuerto.getText(),
                        "puerto SMTP"
                )
        );

        configuracion.setSmtpUsuario(
                opcional(
                        txtSmtpUsuario.getText()
                )
        );

        configuracion.setSmtpTls(
                chkSmtpTls.isSelected()
        );

        configuracion.setNombreRemitente(
                opcional(
                        txtNombreRemitente.getText()
                )
        );

        configuracion.setRutaRespaldoServidor(
                opcional(
                        txtRutaRespaldoServidor
                                .getText()
                )
        );

        return configuracion;
    }

    public void mostrarDiagnosticoConexion(
            String diagnostico,
            boolean correcta) {

        txtDiagnosticoConexion.setText(
                diagnostico
        );

        txtDiagnosticoConexion.setCaretPosition(0);

        lblConexionEstado.setText(
                correcta
                        ? "Conexión disponible"
                        : "Conexión no disponible"
        );

        lblConexionEstado.setForeground(
                correcta
                        ? new Color(34, 155, 85)
                        : new Color(192, 52, 52)
        );

        lblSeguridadValor.setText(
                correcta
                        ? "Base conectada"
                        : "Revisar conexión"
        );
    }

    public void mostrarResultadoCorreo(
            String resultado,
            boolean correcto) {

        txtResultadoCorreo.setText(resultado);
        txtResultadoCorreo.setCaretPosition(0);

        lblCorreoEstado.setText(
                correcto
                        ? "Configuración disponible"
                        : "Configuración incompleta"
        );

        lblCorreoEstado.setForeground(
                correcto
                        ? new Color(34, 155, 85)
                        : new Color(192, 52, 52)
        );
    }

    public void mostrarEstadoSecretos(
            boolean correoConfigurado,
            boolean baseConfigurada) {

        lblEstadoClaveCorreo.setText(
                correoConfigurado
                        ? "Configurada"
                        : "No configurada"
        );

        lblEstadoClaveCorreo.setForeground(
                correoConfigurado
                        ? new Color(34, 155, 85)
                        : new Color(192, 52, 52)
        );

        lblEstadoClaveBase.setText(
                baseConfigurada
                        ? "Configurada"
                        : "No configurada o no utilizada"
        );

        lblEstadoClaveBase.setForeground(
                baseConfigurada
                        ? new Color(34, 155, 85)
                        : new Color(98, 124, 159)
        );
    }

    public String getRutaRespaldoServidor() {
        return txtRutaRespaldoServidor
                .getText()
                .trim();
    }

    public String getRutaArchivoRestauracion() {
        return txtRutaArchivoBak
                .getText()
                .trim();
    }

    public void setRutaArchivoRestauracion(
            String ruta) {

        txtRutaArchivoBak.setText(ruta);
    }

    public void establecerOperacion(
            boolean procesando,
            String accion) {

        boolean habilitado = !procesando;

        btnGuardar.setEnabled(habilitado);
        btnRestaurarDatos.setEnabled(habilitado);
        btnProbarCorreo.setEnabled(habilitado);
        btnProbarConexion.setEnabled(habilitado);
        btnCrearRespaldo.setEnabled(habilitado);
        btnVerificarRespaldo.setEnabled(habilitado);
        btnRestaurarRespaldo.setEnabled(habilitado);

        setCursor(
                Cursor.getPredefinedCursor(
                        procesando
                                ? Cursor.WAIT_CURSOR
                                : Cursor.DEFAULT_CURSOR
                )
        );

        btnGuardar.setText(
                procesando
                && "GUARDAR".equals(accion)
                        ? "Guardando..."
                        : "Guardar configuración"
        );

        btnProbarCorreo.setText(
                procesando
                && "CORREO".equals(accion)
                        ? "Probando..."
                        : "Probar configuración"
        );

        btnProbarConexion.setText(
                procesando
                && "CONEXION".equals(accion)
                        ? "Probando..."
                        : "Probar conexión"
        );

        btnCrearRespaldo.setText(
                procesando
                && "CREAR_RESPALDO".equals(accion)
                        ? "Creando..."
                        : "Crear respaldo"
        );

        btnVerificarRespaldo.setText(
                procesando
                && "VERIFICAR_RESPALDO".equals(accion)
                        ? "Verificando..."
                        : "Verificar respaldo"
        );

        btnRestaurarRespaldo.setText(
                procesando
                && "RESTAURAR_RESPALDO".equals(accion)
                        ? "Restaurando..."
                        : "Restaurar base SIGIR"
        );
    }

    public void establecerProcesoRespaldo(
            boolean procesando) {

        establecerOperacion(
                procesando,
                procesando
                        ? "CREAR_RESPALDO"
                        : null
        );
    }

    public void mostrarSinAcceso() {
        tabsConfiguracion.setVisible(false);
        btnGuardar.setVisible(false);
        btnRestaurarDatos.setVisible(false);

        lblTitulo.setText(
                "Acceso restringido"
        );

        lblSubtitulo.setText(
                "Solo el dueño puede modificar "
                + "la configuración del sistema."
        );

        lblEmpresaActualValor.setText("-");
        lblActualizacionValor.setText("-");
        lblSeguridadValor.setText("-");
    }

    private void seleccionarLogo() {
        JFileChooser selector =
                new JFileChooser();

        selector.setDialogTitle(
                "Seleccionar logo de la empresa"
        );

        selector.setFileFilter(
                new FileNameExtensionFilter(
                        "Imágenes PNG, JPG o JPEG",
                        "png",
                        "jpg",
                        "jpeg"
                )
        );

        if (selector.showOpenDialog(this)
                != JFileChooser.APPROVE_OPTION) {

            return;
        }

        File archivo =
                selector.getSelectedFile();

        try {
            byte[] datos =
                    Files.readAllBytes(
                            archivo.toPath()
                    );

            if (datos.length > MAXIMO_LOGO_BYTES) {
                throw new IllegalArgumentException(
                        "El logo no puede superar 2 MB."
                );
            }

            ImageIcon imagen =
                    new ImageIcon(datos);

            if (imagen.getIconWidth() <= 0
                    || imagen.getIconHeight() <= 0) {

                throw new IllegalArgumentException(
                        "El archivo seleccionado "
                        + "no contiene una imagen válida."
                );
            }

            logoActual = datos;
            logoNombreActual =
                    archivo.getName();

            mostrarLogo(
                    logoActual,
                    logoNombreActual
            );

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible leer el logo.\n\n"
                    + ex.getMessage(),
                    "Logo no cargado",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Logo no válido",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void quitarLogo() {
        logoActual = null;
        logoNombreActual = null;
        mostrarLogo(null, null);
    }

    private void mostrarLogo(
            byte[] datos,
            String nombre) {

        if (datos == null
                || datos.length == 0) {

            lblVistaLogo.setIcon(null);
            lblVistaLogo.setText("Sin logo");
            lblNombreLogo.setText(
                    "No se ha seleccionado una imagen"
            );
            return;
        }

        ImageIcon original =
                new ImageIcon(datos);

        int anchoDisponible =
                Math.max(
                        100,
                        lblVistaLogo.getWidth() - 24
                );

        int altoDisponible =
                Math.max(
                        80,
                        lblVistaLogo.getHeight() - 24
                );

        double escala =
                Math.min(
                        (double) anchoDisponible
                        / original.getIconWidth(),
                        (double) altoDisponible
                        / original.getIconHeight()
                );

        int ancho =
                Math.max(
                        1,
                        (int) Math.round(
                                original.getIconWidth()
                                * escala
                        )
                );

        int alto =
                Math.max(
                        1,
                        (int) Math.round(
                                original.getIconHeight()
                                * escala
                        )
                );

        Image imagen =
                original.getImage()
                        .getScaledInstance(
                                ancho,
                                alto,
                                Image.SCALE_SMOOTH
                        );

        lblVistaLogo.setText("");
        lblVistaLogo.setIcon(
                new ImageIcon(imagen)
        );

        lblNombreLogo.setText(
                nombre == null
                        ? "Logo guardado"
                        : nombre
        );
    }

    private BigDecimal convertirDecimal(
            String valor,
            String campo) {

        try {
            return new BigDecimal(
                    valor.trim()
                            .replace(",", ".")
            ).setScale(
                    2,
                    RoundingMode.HALF_UP
            );

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El " + campo
                    + " debe ser un número válido."
            );
        }
    }

    private int convertirEntero(
            String valor,
            String campo) {

        try {
            return Integer.parseInt(
                    valor.trim()
            );

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El " + campo
                    + " debe ser un número entero."
            );
        }
    }

    private String opcional(String valor) {
        return valor == null
                || valor.trim().isBlank()
                        ? null
                        : valor.trim();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        btnRestaurarDatos = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        pnlTarjetaEmpresa = new javax.swing.JPanel();
        lblEmpresaActualTitulo = new javax.swing.JLabel();
        lblEmpresaActualValor = new javax.swing.JLabel();
        pnlTarjetaActualizacion = new javax.swing.JPanel();
        lblActualizacionTitulo = new javax.swing.JLabel();
        lblActualizacionValor = new javax.swing.JLabel();
        pnlTarjetaSeguridad = new javax.swing.JPanel();
        lblSeguridadTitulo = new javax.swing.JLabel();
        lblSeguridadValor = new javax.swing.JLabel();
        tabsConfiguracion = new javax.swing.JTabbedPane();
        pnlEmpresaFactura = new javax.swing.JPanel();
        pnlDatosEmpresa = new javax.swing.JPanel();
        lblTituloDatosEmpresa = new javax.swing.JLabel();
        lblNombreEmpresa = new javax.swing.JLabel();
        txtNombreEmpresa = new javax.swing.JTextField();
        lblRtn = new javax.swing.JLabel();
        txtRtn = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblCorreoEmpresa = new javax.swing.JLabel();
        txtCorreoEmpresa = new javax.swing.JTextField();
        lblDireccion = new javax.swing.JLabel();
        scrollDireccion = new javax.swing.JScrollPane();
        txtDireccion = new javax.swing.JTextArea();
        lblUltimaActualizacion = new javax.swing.JLabel();
        txtUltimaActualizacion = new javax.swing.JTextField();
        pnlLogoFactura = new javax.swing.JPanel();
        lblTituloLogoFactura = new javax.swing.JLabel();
        lblVistaLogo = new javax.swing.JLabel();
        lblNombreLogo = new javax.swing.JLabel();
        btnSeleccionarLogo = new javax.swing.JButton();
        btnQuitarLogo = new javax.swing.JButton();
        lblMonedaCodigo = new javax.swing.JLabel();
        txtMonedaCodigo = new javax.swing.JTextField();
        lblSimboloMoneda = new javax.swing.JLabel();
        txtSimboloMoneda = new javax.swing.JTextField();
        lblImpuesto = new javax.swing.JLabel();
        txtImpuesto = new javax.swing.JTextField();
        lblPrefijoFactura = new javax.swing.JLabel();
        txtPrefijoFactura = new javax.swing.JTextField();
        lblPieFactura = new javax.swing.JLabel();
        scrollPieFactura = new javax.swing.JScrollPane();
        txtPieFactura = new javax.swing.JTextArea();
        pnlCorreoSistema = new javax.swing.JPanel();
        pnlCorreo = new javax.swing.JPanel();
        lblTituloCorreo = new javax.swing.JLabel();
        lblSmtpHost = new javax.swing.JLabel();
        txtSmtpHost = new javax.swing.JTextField();
        lblSmtpPuerto = new javax.swing.JLabel();
        txtSmtpPuerto = new javax.swing.JTextField();
        lblSmtpUsuario = new javax.swing.JLabel();
        txtSmtpUsuario = new javax.swing.JTextField();
        lblNombreRemitente = new javax.swing.JLabel();
        txtNombreRemitente = new javax.swing.JTextField();
        chkSmtpTls = new javax.swing.JCheckBox();
        btnProbarCorreo = new javax.swing.JButton();
        lblCorreoEstado = new javax.swing.JLabel();
        scrollResultadoCorreo = new javax.swing.JScrollPane();
        txtResultadoCorreo = new javax.swing.JTextArea();
        pnlSeguridadCorreo = new javax.swing.JPanel();
        lblTituloSeguridadCorreo = new javax.swing.JLabel();
        lblExplicacionSecreto = new javax.swing.JLabel();
        lblVariableCorreo = new javax.swing.JLabel();
        lblEstadoClaveCorreo = new javax.swing.JLabel();
        lblVariableBase = new javax.swing.JLabel();
        lblEstadoClaveBase = new javax.swing.JLabel();
        lblNotaSeguridad = new javax.swing.JLabel();
        pnlConexionRespaldos = new javax.swing.JPanel();
        pnlConexion = new javax.swing.JPanel();
        lblTituloConexion = new javax.swing.JLabel();
        lblConexionEstado = new javax.swing.JLabel();
        btnProbarConexion = new javax.swing.JButton();
        scrollDiagnosticoConexion = new javax.swing.JScrollPane();
        txtDiagnosticoConexion = new javax.swing.JTextArea();
        lblNotaConexion = new javax.swing.JLabel();
        pnlRespaldos = new javax.swing.JPanel();
        lblTituloRespaldos = new javax.swing.JLabel();
        lblRutaRespaldoServidor = new javax.swing.JLabel();
        txtRutaRespaldoServidor = new javax.swing.JTextField();
        btnCrearRespaldo = new javax.swing.JButton();
        lblRutaArchivoBak = new javax.swing.JLabel();
        txtRutaArchivoBak = new javax.swing.JTextField();
        btnVerificarRespaldo = new javax.swing.JButton();
        btnRestaurarRespaldo = new javax.swing.JButton();
        lblNotaRespaldos = new javax.swing.JLabel();

        setBackground(new java.awt.Color(247, 249, 252));
        setMinimumSize(new java.awt.Dimension(1080, 700));
        setPreferredSize(new java.awt.Dimension(1180, 760));
        setLayout(null);

        pnlEncabezado.setBackground(new java.awt.Color(247, 249, 252));
        pnlEncabezado.setLayout(null);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(24, 50, 87));
        lblTitulo.setText("Configuración del Sistema");
        pnlEncabezado.add(lblTitulo);
        lblTitulo.setBounds(0, 4, 520, 40);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSubtitulo.setForeground(new java.awt.Color(98, 124, 159));
        lblSubtitulo.setText("Administra la empresa, facturación, correo, conexión y respaldos.");
        pnlEncabezado.add(lblSubtitulo);
        lblSubtitulo.setBounds(0, 46, 760, 24);

        btnRestaurarDatos.setText("Deshacer cambios");
        pnlEncabezado.add(btnRestaurarDatos);
        btnRestaurarDatos.setBounds(810, 20, 135, 38);

        btnGuardar.setText("Guardar configuración");
        pnlEncabezado.add(btnGuardar);
        btnGuardar.setBounds(957, 20, 165, 38);

        add(pnlEncabezado);
        pnlEncabezado.setBounds(28, 10, 1122, 76);

        pnlTarjetaEmpresa.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaEmpresa.setLayout(null);
        lblEmpresaActualTitulo.setText("Empresa configurada");
        pnlTarjetaEmpresa.add(lblEmpresaActualTitulo);
        lblEmpresaActualTitulo.setBounds(18, 15, 180, 20);
        lblEmpresaActualValor.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblEmpresaActualValor.setText("Inversiones Rodríguez");
        pnlTarjetaEmpresa.add(lblEmpresaActualValor);
        lblEmpresaActualValor.setBounds(18, 43, 300, 34);
        add(pnlTarjetaEmpresa);
        pnlTarjetaEmpresa.setBounds(28, 88, 330, 95);

        pnlTarjetaActualizacion.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaActualizacion.setLayout(null);
        lblActualizacionTitulo.setText("Última actualización");
        pnlTarjetaActualizacion.add(lblActualizacionTitulo);
        lblActualizacionTitulo.setBounds(18, 15, 180, 20);
        lblActualizacionValor.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblActualizacionValor.setText("No disponible");
        pnlTarjetaActualizacion.add(lblActualizacionValor);
        lblActualizacionValor.setBounds(18, 43, 300, 34);
        add(pnlTarjetaActualizacion);
        pnlTarjetaActualizacion.setBounds(372, 88, 330, 95);

        pnlTarjetaSeguridad.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaSeguridad.setLayout(null);
        lblSeguridadTitulo.setText("Estado de SQL Server");
        pnlTarjetaSeguridad.add(lblSeguridadTitulo);
        lblSeguridadTitulo.setBounds(18, 15, 180, 20);
        lblSeguridadValor.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblSeguridadValor.setText("Verificando...");
        pnlTarjetaSeguridad.add(lblSeguridadValor);
        lblSeguridadValor.setBounds(18, 43, 300, 34);
        add(pnlTarjetaSeguridad);
        pnlTarjetaSeguridad.setBounds(716, 88, 330, 95);

        pnlEmpresaFactura.setBackground(new java.awt.Color(247, 249, 252));
        pnlEmpresaFactura.setLayout(null);

        pnlDatosEmpresa.setBackground(new java.awt.Color(255, 255, 255));
        pnlDatosEmpresa.setLayout(null);
        lblTituloDatosEmpresa.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloDatosEmpresa.setText("Información de la empresa");
        pnlDatosEmpresa.add(lblTituloDatosEmpresa);
        lblTituloDatosEmpresa.setBounds(16, 8, 260, 26);
        lblNombreEmpresa.setText("Nombre de la empresa");
        pnlDatosEmpresa.add(lblNombreEmpresa);
        lblNombreEmpresa.setBounds(16, 44, 160, 18);
        pnlDatosEmpresa.add(txtNombreEmpresa);
        txtNombreEmpresa.setBounds(16, 64, 600, 34);
        lblRtn.setText("RTN");
        pnlDatosEmpresa.add(lblRtn);
        lblRtn.setBounds(16, 108, 80, 18);
        pnlDatosEmpresa.add(txtRtn);
        txtRtn.setBounds(16, 128, 190, 34);
        lblTelefono.setText("Teléfono");
        pnlDatosEmpresa.add(lblTelefono);
        lblTelefono.setBounds(218, 108, 90, 18);
        pnlDatosEmpresa.add(txtTelefono);
        txtTelefono.setBounds(218, 128, 170, 34);
        lblCorreoEmpresa.setText("Correo");
        pnlDatosEmpresa.add(lblCorreoEmpresa);
        lblCorreoEmpresa.setBounds(400, 108, 90, 18);
        pnlDatosEmpresa.add(txtCorreoEmpresa);
        txtCorreoEmpresa.setBounds(400, 128, 216, 34);
        lblDireccion.setText("Dirección");
        pnlDatosEmpresa.add(lblDireccion);
        lblDireccion.setBounds(16, 172, 100, 18);
        txtDireccion.setColumns(20);
        txtDireccion.setRows(5);
        scrollDireccion.setViewportView(txtDireccion);
        pnlDatosEmpresa.add(scrollDireccion);
        scrollDireccion.setBounds(16, 192, 600, 120);
        lblUltimaActualizacion.setText("Última modificación guardada");
        pnlDatosEmpresa.add(lblUltimaActualizacion);
        lblUltimaActualizacion.setBounds(16, 326, 220, 18);
        pnlDatosEmpresa.add(txtUltimaActualizacion);
        txtUltimaActualizacion.setBounds(16, 346, 600, 34);
        pnlEmpresaFactura.add(pnlDatosEmpresa);
        pnlDatosEmpresa.setBounds(0, 8, 640, 510);

        pnlLogoFactura.setBackground(new java.awt.Color(255, 255, 255));
        pnlLogoFactura.setLayout(null);
        lblTituloLogoFactura.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloLogoFactura.setText("Logo y facturación");
        pnlLogoFactura.add(lblTituloLogoFactura);
        lblTituloLogoFactura.setBounds(16, 8, 230, 26);
        lblVistaLogo.setText("Sin logo");
        pnlLogoFactura.add(lblVistaLogo);
        lblVistaLogo.setBounds(16, 42, 360, 145);
        lblNombreLogo.setForeground(new java.awt.Color(98, 124, 159));
        lblNombreLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNombreLogo.setText("No se ha seleccionado una imagen");
        pnlLogoFactura.add(lblNombreLogo);
        lblNombreLogo.setBounds(16, 190, 360, 22);
        btnSeleccionarLogo.setText("Seleccionar logo");
        pnlLogoFactura.add(btnSeleccionarLogo);
        btnSeleccionarLogo.setBounds(16, 218, 170, 34);
        btnQuitarLogo.setText("Quitar");
        pnlLogoFactura.add(btnQuitarLogo);
        btnQuitarLogo.setBounds(198, 218, 178, 34);
        lblMonedaCodigo.setText("Código moneda");
        pnlLogoFactura.add(lblMonedaCodigo);
        lblMonedaCodigo.setBounds(16, 264, 110, 18);
        pnlLogoFactura.add(txtMonedaCodigo);
        txtMonedaCodigo.setBounds(16, 284, 95, 34);
        lblSimboloMoneda.setText("Símbolo");
        pnlLogoFactura.add(lblSimboloMoneda);
        lblSimboloMoneda.setBounds(123, 264, 80, 18);
        pnlLogoFactura.add(txtSimboloMoneda);
        txtSimboloMoneda.setBounds(123, 284, 70, 34);
        lblImpuesto.setText("Impuesto (%)");
        pnlLogoFactura.add(lblImpuesto);
        lblImpuesto.setBounds(205, 264, 100, 18);
        txtImpuesto.setText("0.00");
        pnlLogoFactura.add(txtImpuesto);
        txtImpuesto.setBounds(205, 284, 80, 34);
        lblPrefijoFactura.setText("Prefijo factura");
        pnlLogoFactura.add(lblPrefijoFactura);
        lblPrefijoFactura.setBounds(297, 264, 100, 18);
        pnlLogoFactura.add(txtPrefijoFactura);
        txtPrefijoFactura.setBounds(297, 284, 79, 34);
        lblPieFactura.setText("Pie de factura o comprobante");
        pnlLogoFactura.add(lblPieFactura);
        lblPieFactura.setBounds(16, 330, 220, 18);
        txtPieFactura.setColumns(20);
        txtPieFactura.setRows(5);
        scrollPieFactura.setViewportView(txtPieFactura);
        pnlLogoFactura.add(scrollPieFactura);
        scrollPieFactura.setBounds(16, 350, 360, 130);
        pnlEmpresaFactura.add(pnlLogoFactura);
        pnlLogoFactura.setBounds(654, 8, 395, 510);

        tabsConfiguracion.addTab("Empresa y facturación", pnlEmpresaFactura);

        pnlCorreoSistema.setBackground(new java.awt.Color(247, 249, 252));
        pnlCorreoSistema.setLayout(null);

        pnlCorreo.setBackground(new java.awt.Color(255, 255, 255));
        pnlCorreo.setLayout(null);
        lblTituloCorreo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloCorreo.setText("Correo del sistema");
        pnlCorreo.add(lblTituloCorreo);
        lblTituloCorreo.setBounds(16, 8, 240, 26);
        lblSmtpHost.setText("Servidor SMTP");
        pnlCorreo.add(lblSmtpHost);
        lblSmtpHost.setBounds(16, 44, 120, 18);
        pnlCorreo.add(txtSmtpHost);
        txtSmtpHost.setBounds(16, 64, 300, 34);
        lblSmtpPuerto.setText("Puerto");
        pnlCorreo.add(lblSmtpPuerto);
        lblSmtpPuerto.setBounds(328, 44, 80, 18);
        pnlCorreo.add(txtSmtpPuerto);
        txtSmtpPuerto.setBounds(328, 64, 145, 34);
        lblSmtpUsuario.setText("Correo utilizado para enviar");
        pnlCorreo.add(lblSmtpUsuario);
        lblSmtpUsuario.setBounds(16, 108, 200, 18);
        pnlCorreo.add(txtSmtpUsuario);
        txtSmtpUsuario.setBounds(16, 128, 457, 34);
        lblNombreRemitente.setText("Nombre del remitente");
        pnlCorreo.add(lblNombreRemitente);
        lblNombreRemitente.setBounds(16, 172, 170, 18);
        pnlCorreo.add(txtNombreRemitente);
        txtNombreRemitente.setBounds(16, 192, 300, 34);
        chkSmtpTls.setText("Usar conexión TLS");
        pnlCorreo.add(chkSmtpTls);
        chkSmtpTls.setBounds(328, 190, 150, 36);
        btnProbarCorreo.setText("Probar configuración");
        pnlCorreo.add(btnProbarCorreo);
        btnProbarCorreo.setBounds(16, 242, 190, 38);
        lblCorreoEstado.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblCorreoEstado.setText("Sin verificar");
        pnlCorreo.add(lblCorreoEstado);
        lblCorreoEstado.setBounds(220, 250, 250, 22);
        txtResultadoCorreo.setColumns(20);
        txtResultadoCorreo.setRows(5);
        scrollResultadoCorreo.setViewportView(txtResultadoCorreo);
        pnlCorreo.add(scrollResultadoCorreo);
        scrollResultadoCorreo.setBounds(16, 294, 457, 185);
        pnlCorreoSistema.add(pnlCorreo);
        pnlCorreo.setBounds(0, 8, 495, 510);

        pnlSeguridadCorreo.setBackground(new java.awt.Color(255, 255, 255));
        pnlSeguridadCorreo.setLayout(null);
        lblTituloSeguridadCorreo.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloSeguridadCorreo.setText("Protección de credenciales");
        pnlSeguridadCorreo.add(lblTituloSeguridadCorreo);
        lblTituloSeguridadCorreo.setBounds(16, 8, 270, 26);
        lblExplicacionSecreto.setText("<html>Las contraseñas no se guardan en la base de datos ni se muestran en esta ventana. Deben mantenerse en variables de entorno del equipo donde se ejecuta SIGIR.</html>");
        pnlSeguridadCorreo.add(lblExplicacionSecreto);
        lblExplicacionSecreto.setBounds(16, 46, 500, 70);
        lblVariableCorreo.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblVariableCorreo.setText("SIGIR_GMAIL_APP_PASSWORD");
        pnlSeguridadCorreo.add(lblVariableCorreo);
        lblVariableCorreo.setBounds(16, 140, 260, 24);
        lblEstadoClaveCorreo.setText("No configurada");
        pnlSeguridadCorreo.add(lblEstadoClaveCorreo);
        lblEstadoClaveCorreo.setBounds(290, 140, 210, 24);
        lblVariableBase.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblVariableBase.setText("SIGIR_DB_PASSWORD");
        pnlSeguridadCorreo.add(lblVariableBase);
        lblVariableBase.setBounds(16, 184, 260, 24);
        lblEstadoClaveBase.setText("No configurada");
        pnlSeguridadCorreo.add(lblEstadoClaveBase);
        lblEstadoClaveBase.setBounds(290, 184, 210, 24);
        lblNotaSeguridad.setText("<html><b>Importante:</b> nunca subas contraseñas, app passwords ni archivos con credenciales a GitHub. El usuario SMTP y los demás datos no secretos sí pueden guardarse en la configuración.</html>");
        pnlSeguridadCorreo.add(lblNotaSeguridad);
        lblNotaSeguridad.setBounds(16, 240, 500, 100);
        pnlCorreoSistema.add(pnlSeguridadCorreo);
        pnlSeguridadCorreo.setBounds(509, 8, 540, 510);

        tabsConfiguracion.addTab("Correo y seguridad", pnlCorreoSistema);

        pnlConexionRespaldos.setBackground(new java.awt.Color(247, 249, 252));
        pnlConexionRespaldos.setLayout(null);

        pnlConexion.setBackground(new java.awt.Color(255, 255, 255));
        pnlConexion.setLayout(null);
        lblTituloConexion.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloConexion.setText("Diagnóstico de SQL Server");
        pnlConexion.add(lblTituloConexion);
        lblTituloConexion.setBounds(16, 8, 270, 26);
        lblConexionEstado.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblConexionEstado.setText("Sin verificar");
        pnlConexion.add(lblConexionEstado);
        lblConexionEstado.setBounds(16, 44, 270, 24);
        btnProbarConexion.setText("Probar conexión");
        pnlConexion.add(btnProbarConexion);
        btnProbarConexion.setBounds(327, 38, 145, 36);
        txtDiagnosticoConexion.setColumns(20);
        txtDiagnosticoConexion.setFont(new java.awt.Font("Consolas", 0, 12));
        txtDiagnosticoConexion.setRows(5);
        scrollDiagnosticoConexion.setViewportView(txtDiagnosticoConexion);
        pnlConexion.add(scrollDiagnosticoConexion);
        scrollDiagnosticoConexion.setBounds(16, 88, 456, 300);
        lblNotaConexion.setText("<html>Esta sección muestra la conexión activa. Para cambiar servidor, puerto, usuario o contraseña debes modificar la configuración utilizada por <b>ConexionBD</b> y reiniciar SIGIR.</html>");
        pnlConexion.add(lblNotaConexion);
        lblNotaConexion.setBounds(16, 405, 456, 75);
        pnlConexionRespaldos.add(pnlConexion);
        pnlConexion.setBounds(0, 8, 495, 510);

        pnlRespaldos.setBackground(new java.awt.Color(255, 255, 255));
        pnlRespaldos.setLayout(null);
        lblTituloRespaldos.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblTituloRespaldos.setText("Respaldos de la base de datos");
        pnlRespaldos.add(lblTituloRespaldos);
        lblTituloRespaldos.setBounds(16, 8, 290, 26);
        lblRutaRespaldoServidor.setText("Carpeta existente en el servidor SQL");
        pnlRespaldos.add(lblRutaRespaldoServidor);
        lblRutaRespaldoServidor.setBounds(16, 48, 280, 18);
        pnlRespaldos.add(txtRutaRespaldoServidor);
        txtRutaRespaldoServidor.setBounds(16, 68, 505, 34);
        btnCrearRespaldo.setText("Crear respaldo");
        pnlRespaldos.add(btnCrearRespaldo);
        btnCrearRespaldo.setBounds(16, 116, 505, 40);
        lblRutaArchivoBak.setText("Ruta completa del archivo .bak en el servidor SQL");
        pnlRespaldos.add(lblRutaArchivoBak);
        lblRutaArchivoBak.setBounds(16, 176, 360, 18);
        pnlRespaldos.add(txtRutaArchivoBak);
        txtRutaArchivoBak.setBounds(16, 196, 505, 34);
        btnVerificarRespaldo.setText("Verificar respaldo");
        pnlRespaldos.add(btnVerificarRespaldo);
        btnVerificarRespaldo.setBounds(16, 244, 245, 38);
        btnRestaurarRespaldo.setText("Restaurar base SIGIR");
        pnlRespaldos.add(btnRestaurarRespaldo);
        btnRestaurarRespaldo.setBounds(276, 244, 245, 38);
        lblNotaRespaldos.setText("<html><b>La ruta pertenece al servidor SQL, no necesariamente a esta computadora.</b><br><br>La carpeta debe existir y la cuenta del servicio de SQL Server debe tener permisos. Restaurar reemplaza la base actual y debe hacerse sin otros usuarios conectados.</html>");
        pnlRespaldos.add(lblNotaRespaldos);
        lblNotaRespaldos.setBounds(16, 310, 505, 135);
        pnlConexionRespaldos.add(pnlRespaldos);
        pnlRespaldos.setBounds(509, 8, 540, 510);

        tabsConfiguracion.addTab("Conexión y respaldos", pnlConexionRespaldos);

        add(tabsConfiguracion);
        tabsConfiguracion.setBounds(28, 195, 1070, 565);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCrearRespaldo;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnProbarConexion;
    private javax.swing.JButton btnProbarCorreo;
    private javax.swing.JButton btnQuitarLogo;
    private javax.swing.JButton btnRestaurarDatos;
    private javax.swing.JButton btnRestaurarRespaldo;
    private javax.swing.JButton btnSeleccionarLogo;
    private javax.swing.JButton btnVerificarRespaldo;
    private javax.swing.JCheckBox chkSmtpTls;
    private javax.swing.JLabel lblActualizacionTitulo;
    private javax.swing.JLabel lblActualizacionValor;
    private javax.swing.JLabel lblConexionEstado;
    private javax.swing.JLabel lblCorreoEmpresa;
    private javax.swing.JLabel lblCorreoEstado;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEmpresaActualTitulo;
    private javax.swing.JLabel lblEmpresaActualValor;
    private javax.swing.JLabel lblEstadoClaveBase;
    private javax.swing.JLabel lblEstadoClaveCorreo;
    private javax.swing.JLabel lblExplicacionSecreto;
    private javax.swing.JLabel lblImpuesto;
    private javax.swing.JLabel lblMonedaCodigo;
    private javax.swing.JLabel lblNombreEmpresa;
    private javax.swing.JLabel lblNombreLogo;
    private javax.swing.JLabel lblNombreRemitente;
    private javax.swing.JLabel lblNotaConexion;
    private javax.swing.JLabel lblNotaRespaldos;
    private javax.swing.JLabel lblNotaSeguridad;
    private javax.swing.JLabel lblPieFactura;
    private javax.swing.JLabel lblPrefijoFactura;
    private javax.swing.JLabel lblRtn;
    private javax.swing.JLabel lblRutaArchivoBak;
    private javax.swing.JLabel lblRutaRespaldoServidor;
    private javax.swing.JLabel lblSeguridadTitulo;
    private javax.swing.JLabel lblSeguridadValor;
    private javax.swing.JLabel lblSimboloMoneda;
    private javax.swing.JLabel lblSmtpHost;
    private javax.swing.JLabel lblSmtpPuerto;
    private javax.swing.JLabel lblSmtpUsuario;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloConexion;
    private javax.swing.JLabel lblTituloCorreo;
    private javax.swing.JLabel lblTituloDatosEmpresa;
    private javax.swing.JLabel lblTituloLogoFactura;
    private javax.swing.JLabel lblTituloRespaldos;
    private javax.swing.JLabel lblTituloSeguridadCorreo;
    private javax.swing.JLabel lblUltimaActualizacion;
    private javax.swing.JLabel lblVariableBase;
    private javax.swing.JLabel lblVariableCorreo;
    private javax.swing.JLabel lblVistaLogo;
    private javax.swing.JPanel pnlConexion;
    private javax.swing.JPanel pnlConexionRespaldos;
    private javax.swing.JPanel pnlCorreo;
    private javax.swing.JPanel pnlCorreoSistema;
    private javax.swing.JPanel pnlDatosEmpresa;
    private javax.swing.JPanel pnlEmpresaFactura;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlLogoFactura;
    private javax.swing.JPanel pnlRespaldos;
    private javax.swing.JPanel pnlSeguridadCorreo;
    private javax.swing.JPanel pnlTarjetaActualizacion;
    private javax.swing.JPanel pnlTarjetaEmpresa;
    private javax.swing.JPanel pnlTarjetaSeguridad;
    private javax.swing.JScrollPane scrollDiagnosticoConexion;
    private javax.swing.JScrollPane scrollDireccion;
    private javax.swing.JScrollPane scrollPieFactura;
    private javax.swing.JScrollPane scrollResultadoCorreo;
    private javax.swing.JTabbedPane tabsConfiguracion;
    private javax.swing.JTextArea txtDiagnosticoConexion;
    private javax.swing.JTextArea txtDireccion;
    private javax.swing.JTextField txtCorreoEmpresa;
    private javax.swing.JTextField txtImpuesto;
    private javax.swing.JTextField txtMonedaCodigo;
    private javax.swing.JTextField txtNombreEmpresa;
    private javax.swing.JTextField txtNombreRemitente;
    private javax.swing.JTextArea txtPieFactura;
    private javax.swing.JTextField txtPrefijoFactura;
    private javax.swing.JTextArea txtResultadoCorreo;
    private javax.swing.JTextField txtRtn;
    private javax.swing.JTextField txtRutaArchivoBak;
    private javax.swing.JTextField txtRutaRespaldoServidor;
    private javax.swing.JTextField txtSimboloMoneda;
    private javax.swing.JTextField txtSmtpHost;
    private javax.swing.JTextField txtSmtpPuerto;
    private javax.swing.JTextField txtSmtpUsuario;
    private javax.swing.JTextField txtTelefono;
    private javax.swing.JTextField txtUltimaActualizacion;
    // End of variables declaration//GEN-END:variables
}
