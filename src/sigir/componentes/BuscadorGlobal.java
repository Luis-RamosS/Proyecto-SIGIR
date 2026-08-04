package sigir.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import sigir.dao.BusquedaGlobalDAO;
import sigir.modelo.ResultadoBusquedaGlobal;

public final class BuscadorGlobal {

    private BuscadorGlobal() {
    }

    public static void instalar(
            JTextField campo,
            Consumer<ResultadoBusquedaGlobal> accion) {

        if (campo == null || accion == null) {
            throw new IllegalArgumentException(
                    "El campo y la acción son obligatorios."
            );
        }

        new ControladorBusqueda(
                campo,
                accion
        ).instalar();
    }

    private static final class ControladorBusqueda {

        private final JTextField campo;
        private final Consumer<ResultadoBusquedaGlobal>
                accion;

        private final BusquedaGlobalDAO dao =
                new BusquedaGlobalDAO();

        private final DefaultListModel<ResultadoBusquedaGlobal>
                modelo = new DefaultListModel<>();

        private final JList<ResultadoBusquedaGlobal> lista =
                new JList<>(modelo);

        private final JPopupMenu popup =
                new JPopupMenu();

        private final Timer temporizador;

        private SwingWorker
                <List<ResultadoBusquedaGlobal>, Void>
                trabajador;

        ControladorBusqueda(
                JTextField campo,
                Consumer<ResultadoBusquedaGlobal> accion) {

            this.campo = campo;
            this.accion = accion;

            temporizador = new Timer(
                    250,
                    e -> buscar()
            );

            temporizador.setRepeats(false);
        }

        void instalar() {
            configurarPopup();

            campo.getDocument().addDocumentListener(
                    new DocumentListener() {
                        private void actualizar() {
                            temporizador.restart();
                        }

                        @Override
                        public void insertUpdate(
                                DocumentEvent e) {
                            actualizar();
                        }

                        @Override
                        public void removeUpdate(
                                DocumentEvent e) {
                            actualizar();
                        }

                        @Override
                        public void changedUpdate(
                                DocumentEvent e) {
                            actualizar();
                        }
                    }
            );

            campo.addKeyListener(
                    new KeyAdapter() {
                        @Override
                        public void keyPressed(
                                KeyEvent e) {

                            if (e.getKeyCode()
                                    == KeyEvent.VK_DOWN) {

                                if (modelo.getSize() > 0) {
                                    int indice =
                                            Math.min(
                                                    lista
                                                    .getSelectedIndex()
                                                    + 1,
                                                    modelo.getSize()
                                                    - 1
                                            );

                                    lista.setSelectedIndex(
                                            Math.max(
                                                    0,
                                                    indice
                                            )
                                    );

                                    lista.requestFocusInWindow();
                                }

                            } else if (e.getKeyCode()
                                    == KeyEvent.VK_ESCAPE) {

                                popup.setVisible(false);
                            }
                        }
                    }
            );
        }

        private void configurarPopup() {
            popup.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(210, 220, 232)
                    )
            );

            popup.setFocusable(false);

            lista.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );

            lista.setFixedCellHeight(58);
            lista.setBackground(Color.WHITE);
            lista.setCellRenderer(
                    new ResultadoRenderer()
            );

            lista.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(
                                MouseEvent e) {

                            if (e.getClickCount() >= 1) {
                                seleccionar();
                            }
                        }
                    }
            );

            lista.addKeyListener(
                    new KeyAdapter() {
                        @Override
                        public void keyPressed(
                                KeyEvent e) {

                            if (e.getKeyCode()
                                    == KeyEvent.VK_ENTER) {
                                seleccionar();

                            } else if (e.getKeyCode()
                                    == KeyEvent.VK_ESCAPE) {

                                popup.setVisible(false);
                                campo.requestFocusInWindow();
                            }
                        }
                    }
            );

            JScrollPane scroll =
                    new JScrollPane(lista);

            scroll.setBorder(null);
            scroll.setPreferredSize(
                    new Dimension(520, 300)
            );

            popup.setLayout(new BorderLayout());
            popup.add(scroll, BorderLayout.CENTER);
        }

        private void buscar() {
            String texto = campo.getText().trim();

            if (texto.isBlank()) {
                cancelar();
                modelo.clear();
                popup.setVisible(false);
                return;
            }

            cancelar();

            trabajador = new SwingWorker<>() {
                @Override
                protected List<ResultadoBusquedaGlobal>
                        doInBackground() throws Exception {

                    return dao.buscar(texto);
                }

                @Override
                protected void done() {
                    if (isCancelled()) {
                        return;
                    }

                    try {
                        List<ResultadoBusquedaGlobal>
                                resultados = get();

                        if (!texto.equals(
                                campo.getText().trim())) {
                            return;
                        }

                        mostrar(resultados);

                    } catch (Exception ex) {
                        modelo.clear();
                        popup.setVisible(false);
                    }
                }
            };

            trabajador.execute();
        }

        private void mostrar(
                List<ResultadoBusquedaGlobal> resultados) {

            modelo.clear();

            for (ResultadoBusquedaGlobal resultado
                    : resultados) {

                modelo.addElement(resultado);
            }

            if (modelo.isEmpty()
                    || !campo.isShowing()) {

                popup.setVisible(false);
                return;
            }

            popup.setPopupSize(
                    Math.max(
                            campo.getWidth(),
                            520
                    ),
                    Math.min(
                            320,
                            modelo.size() * 58 + 4
                    )
            );

            popup.show(
                    campo,
                    0,
                    campo.getHeight() + 2
            );
        }

        private void seleccionar() {
            ResultadoBusquedaGlobal resultado =
                    lista.getSelectedValue();

            if (resultado == null
                    && modelo.getSize() > 0) {

                resultado = modelo.getElementAt(0);
            }

            if (resultado == null) {
                return;
            }

            popup.setVisible(false);
            campo.setText("");
            accion.accept(resultado);

            SwingUtilities.invokeLater(
                    campo::requestFocusInWindow
            );
        }

        private void cancelar() {
            if (trabajador != null
                    && !trabajador.isDone()) {

                trabajador.cancel(true);
            }
        }
    }

    private static final class ResultadoRenderer
            extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JPanel panel = new JPanel(null);
            panel.setOpaque(true);

            panel.setBackground(
                    isSelected
                            ? new Color(232, 241, 252)
                            : Color.WHITE
            );

            ResultadoBusquedaGlobal resultado =
                    (ResultadoBusquedaGlobal) value;

            JLabel tipo = new JLabel(
                    resultado.getTipo()
            );

            tipo.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            10
                    )
            );

            tipo.setForeground(
                    new Color(49, 105, 181)
            );

            tipo.setBounds(12, 5, 110, 16);

            JLabel titulo = new JLabel(
                    resultado.getTitulo()
            );

            titulo.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            12
                    )
            );

            titulo.setForeground(
                    new Color(24, 50, 87)
            );

            titulo.setBounds(12, 21, 480, 18);

            JLabel detalle = new JLabel(
                    resultado.getDetalle()
            );

            detalle.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            detalle.setForeground(
                    new Color(98, 124, 159)
            );

            detalle.setBounds(12, 39, 480, 16);

            panel.add(tipo);
            panel.add(titulo);
            panel.add(detalle);

            return panel;
        }
    }
}
