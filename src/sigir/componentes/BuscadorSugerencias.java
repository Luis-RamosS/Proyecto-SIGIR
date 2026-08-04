package sigir.componentes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Agrega sugerencias desplegables a un JTextField.
 *
 * Cuando el campo está vacío no muestra elementos. Al escribir, filtra
 * por coincidencia y permite seleccionar con mouse, flechas o Enter.
 *
 * @param <T> tipo de elemento que se buscará.
 */
public final class BuscadorSugerencias<T> {

    private static final int MAXIMO_RESULTADOS = 8;

    private final JTextField campo;
    private final Function<T, String> textoVisible;
    private final Function<T, String> textoBusqueda;
    private final Consumer<T> alCambiarSeleccion;

    private final DefaultListModel<T> modelo =
            new DefaultListModel<>();

    private final JList<T> lista =
            new JList<>(modelo);

    private final JScrollPane scroll =
            new JScrollPane(lista);

    private final JPopupMenu popup =
            new JPopupMenu();

    private final List<T> elementos =
            new ArrayList<>();

    private boolean ajustandoTexto;
    private T seleccionado;

    public BuscadorSugerencias(
            JTextField campo,
            Function<T, String> textoVisible,
            Function<T, String> textoBusqueda,
            Consumer<T> alCambiarSeleccion) {

        this.campo = Objects.requireNonNull(
                campo,
                "El campo de búsqueda es obligatorio."
        );

        this.textoVisible = Objects.requireNonNull(
                textoVisible,
                "El formato visible es obligatorio."
        );

        this.textoBusqueda = Objects.requireNonNull(
                textoBusqueda,
                "El texto de búsqueda es obligatorio."
        );

        this.alCambiarSeleccion =
                alCambiarSeleccion == null
                        ? valor -> {
                        }
                        : alCambiarSeleccion;

        configurarLista();
        configurarPopup();
        configurarEventos();
    }

    public void setElementos(List<T> nuevosElementos) {
        elementos.clear();

        if (nuevosElementos != null) {
            elementos.addAll(nuevosElementos);
        }

        if (campo.getText().isBlank()) {
            ocultar();
        } else {
            actualizarSugerencias();
        }
    }

    public T getSeleccionado() {
        return seleccionado;
    }

    public void seleccionar(T elemento) {
        if (elemento == null) {
            limpiar();
            return;
        }

        seleccionado = elemento;
        ajustandoTexto = true;

        try {
            campo.setText(
                    textoSeguro(
                            textoVisible.apply(elemento)
                    )
            );

            campo.setCaretPosition(
                    campo.getText().length()
            );

        } finally {
            ajustandoTexto = false;
        }

        ocultar();
        alCambiarSeleccion.accept(elemento);
    }

    public void limpiar() {
        boolean habiaSeleccion =
                seleccionado != null;

        seleccionado = null;
        ajustandoTexto = true;

        try {
            campo.setText("");

        } finally {
            ajustandoTexto = false;
        }

        ocultar();

        if (habiaSeleccion) {
            alCambiarSeleccion.accept(null);
        }
    }

    public void ocultar() {
        modelo.clear();
        popup.setVisible(false);
    }

    private void configurarLista() {
        lista.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        lista.setFixedCellHeight(42);
        lista.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        lista.setSelectionBackground(
                new Color(232, 241, 252)
        );

        lista.setSelectionForeground(
                new Color(24, 50, 87)
        );

        lista.setCellRenderer(
                new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus
                        );

                        @SuppressWarnings("unchecked")
                        T elemento = (T) value;

                        setText(
                                elemento == null
                                        ? ""
                                        : textoSeguro(
                                                textoVisible.apply(
                                                        elemento
                                                )
                                        )
                        );

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        0, 12, 0, 12
                                )
                        );

                        return this;
                    }
                }
        );
    }

    private void configurarPopup() {
        popup.setBorder(
                BorderFactory.createLineBorder(
                        new Color(205, 216, 229)
                )
        );

        popup.setFocusable(false);

        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        popup.add(scroll);
    }

    private void configurarEventos() {
        campo.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        programarActualizacion();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        programarActualizacion();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        programarActualizacion();
                    }
                }
        );

        campo.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        manejarTecla(e);
                    }
                }
        );

        campo.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (!campo.getText().isBlank()
                                && seleccionado == null) {

                            actualizarSugerencias();
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        Timer temporizador =
                                new Timer(
                                        150,
                                        evento -> ocultar()
                                );

                        temporizador.setRepeats(false);
                        temporizador.start();
                    }
                }
        );

        lista.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() >= 1
                                && lista.getSelectedValue()
                                != null) {

                            seleccionar(
                                    lista.getSelectedValue()
                            );
                        }
                    }
                }
        );
    }

    private void programarActualizacion() {
        if (ajustandoTexto) {
            return;
        }

        SwingUtilities.invokeLater(
                this::actualizarSugerencias
        );
    }

    private void actualizarSugerencias() {
        if (ajustandoTexto) {
            return;
        }

        if (seleccionado != null) {
            seleccionado = null;
            alCambiarSeleccion.accept(null);
        }

        String consulta =
                normalizar(campo.getText());

        if (consulta.isBlank()) {
            ocultar();
            return;
        }

        List<T> coincidencias =
                elementos.stream()
                        .filter(Objects::nonNull)
                        .filter(elemento ->
                                normalizar(
                                        textoBusqueda.apply(
                                                elemento
                                        )
                                ).contains(consulta)
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                (T elemento) ->
                                                        puntuacion(
                                                                elemento,
                                                                consulta
                                                        )
                                        )
                                        .thenComparing(
                                                (T elemento) ->
                                                        normalizar(
                                                                textoVisible.apply(
                                                                        elemento
                                                                )
                                                        )
                                        )
                        )
                        .limit(MAXIMO_RESULTADOS)
                        .toList();

        modelo.clear();

        for (T elemento : coincidencias) {
            modelo.addElement(elemento);
        }

        if (modelo.isEmpty()) {
            ocultar();
            return;
        }

        lista.setSelectedIndex(0);
        lista.ensureIndexIsVisible(0);

        mostrarPopup();
    }

    private int puntuacion(
            T elemento,
            String consulta) {

        String visible =
                normalizar(
                        textoVisible.apply(elemento)
                );

        String busqueda =
                normalizar(
                        textoBusqueda.apply(elemento)
                );

        if (visible.startsWith(consulta)) {
            return 0;
        }

        for (String palabra
                : visible.split("\\s+")) {

            if (palabra.startsWith(consulta)) {
                return 1;
            }
        }

        if (visible.contains(consulta)) {
            return 2;
        }

        if (busqueda.startsWith(consulta)) {
            return 3;
        }

        return 4;
    }

    private void mostrarPopup() {
        if (!campo.isShowing()
                || !campo.isEnabled()) {

            return;
        }

        int cantidad =
                Math.min(
                        modelo.getSize(),
                        MAXIMO_RESULTADOS
                );

        int alto =
                cantidad * lista.getFixedCellHeight() + 4;

        int ancho =
                Math.max(
                        campo.getWidth(),
                        380
                );

        scroll.setPreferredSize(
                new Dimension(ancho, alto)
        );

        popup.setPopupSize(ancho, alto);
        popup.show(
                campo,
                0,
                campo.getHeight()
        );
    }

    private void manejarTecla(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            ocultar();
            e.consume();
            return;
        }

        if (!popup.isVisible()) {
            return;
        }

        int indice = lista.getSelectedIndex();

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            indice = Math.min(
                    indice + 1,
                    modelo.getSize() - 1
            );

            lista.setSelectedIndex(indice);
            lista.ensureIndexIsVisible(indice);
            e.consume();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            indice = Math.max(indice - 1, 0);

            lista.setSelectedIndex(indice);
            lista.ensureIndexIsVisible(indice);
            e.consume();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            T elemento = lista.getSelectedValue();

            if (elemento != null) {
                seleccionar(elemento);
                e.consume();
            }
        }
    }

    private String normalizar(String texto) {
        String valor =
                textoSeguro(texto)
                        .trim()
                        .toLowerCase();

        return Normalizer.normalize(
                valor,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }
}
