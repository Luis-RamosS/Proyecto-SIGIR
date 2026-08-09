package sigir.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Selector de fecha hecho únicamente con Swing, sin librerías externas.
 */
public final class SelectorFechaUtil {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private SelectorFechaUtil() {
    }

    public static void instalar(JTextField campo) {
        instalar(campo, true);
    }

    public static void instalar(JTextField campo, boolean permitirVacio) {
        if (campo == null) {
            return;
        }

        campo.setEditable(false);
        campo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        campo.setToolTipText("Haz clic para seleccionar una fecha.");

        campo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                abrir(campo, permitirVacio);
            }
        });

        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    abrir(campo, permitirVacio);
                }
            }
        });
    }

    private static void abrir(JTextField campo, boolean permitirVacio) {
        LocalDate inicial = LocalDate.now();
        String texto = campo.getText() == null ? "" : campo.getText().trim();

        if (!texto.isBlank()) {
            try {
                inicial = LocalDate.parse(texto, FORMATO);
            } catch (DateTimeParseException ex) {
                inicial = LocalDate.now();
            }
        }

        Window propietario = SwingUtilities.getWindowAncestor(campo);
        CalendarioDialog dialogo = new CalendarioDialog(
                propietario,
                inicial,
                permitirVacio,
                campo
        );
        dialogo.setVisible(true);
    }

    private static final class CalendarioDialog extends JDialog {

        private static final Locale LOCALE_ES = new Locale("es", "HN");

        private YearMonth mes;
        private LocalDate seleccion;
        private final JTextField destino;
        private final boolean permitirVacio;
        private final JLabel lblMes = new JLabel("", SwingConstants.CENTER);
        private final JPanel pnlDias = new JPanel(new GridLayout(0, 7, 4, 4));

        CalendarioDialog(
                Window propietario,
                LocalDate inicial,
                boolean permitirVacio,
                JTextField destino) {

            super(propietario, "Seleccionar fecha", ModalityType.APPLICATION_MODAL);
            this.seleccion = inicial;
            this.mes = YearMonth.from(inicial);
            this.destino = destino;
            this.permitirVacio = permitirVacio;

            construir();
        }

        private void construir() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setResizable(false);
            setLayout(new BorderLayout(8, 8));

            JPanel superior = new JPanel(new BorderLayout(6, 0));
            JButton anterior = new JButton("‹");
            JButton siguiente = new JButton("›");
            lblMes.setFont(new Font("Segoe UI", Font.BOLD, 15));

            anterior.addActionListener(e -> {
                mes = mes.minusMonths(1);
                reconstruirDias();
            });

            siguiente.addActionListener(e -> {
                mes = mes.plusMonths(1);
                reconstruirDias();
            });

            superior.add(anterior, BorderLayout.WEST);
            superior.add(lblMes, BorderLayout.CENTER);
            superior.add(siguiente, BorderLayout.EAST);
            superior.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

            add(superior, BorderLayout.NORTH);

            JPanel centro = new JPanel(new BorderLayout(0, 6));
            centro.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            JPanel encabezados = new JPanel(new GridLayout(1, 7, 4, 4));
            String[] dias = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do"};
            for (String dia : dias) {
                JLabel etiqueta = new JLabel(dia, SwingConstants.CENTER);
                etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 11));
                encabezados.add(etiqueta);
            }

            centro.add(encabezados, BorderLayout.NORTH);
            centro.add(pnlDias, BorderLayout.CENTER);
            add(centro, BorderLayout.CENTER);

            JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            if (permitirVacio) {
                JButton limpiar = new JButton("Limpiar");
                limpiar.addActionListener(e -> {
                    destino.setText("");
                    dispose();
                });
                inferior.add(limpiar);
            }

            JButton hoy = new JButton("Hoy");
            hoy.addActionListener(e -> seleccionar(LocalDate.now()));
            inferior.add(hoy);

            add(inferior, BorderLayout.SOUTH);

            reconstruirDias();
            pack();
            setSize(Math.max(getWidth(), 330), Math.max(getHeight(), 330));
            setLocationRelativeTo((Component) destino);
        }

        private void reconstruirDias() {
            pnlDias.removeAll();

            String nombreMes = mes.getMonth()
                    .getDisplayName(TextStyle.FULL, LOCALE_ES);

            if (!nombreMes.isBlank()) {
                nombreMes = Character.toUpperCase(nombreMes.charAt(0))
                        + nombreMes.substring(1);
            }

            lblMes.setText(nombreMes + " " + mes.getYear());

            LocalDate primero = mes.atDay(1);
            int desplazamiento = primero.getDayOfWeek().getValue()
                    - DayOfWeek.MONDAY.getValue();

            for (int i = 0; i < desplazamiento; i++) {
                pnlDias.add(new JLabel(""));
            }

            for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
                LocalDate fecha = mes.atDay(dia);
                JButton boton = new JButton(String.valueOf(dia));
                boton.setFocusPainted(false);

                if (fecha.equals(seleccion)) {
                    boton.setFont(boton.getFont().deriveFont(Font.BOLD));
                }

                boton.addActionListener(e -> seleccionar(fecha));
                pnlDias.add(boton);
            }

            pnlDias.revalidate();
            pnlDias.repaint();
        }

        private void seleccionar(LocalDate fecha) {
            seleccion = fecha;
            destino.setText(fecha.format(FORMATO));
            dispose();
        }
    }
}
