package sigir.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import sigir.modelo.ActividadDiariaInicio;

public class GraficoActividadInicioPanel
        extends JPanel {

    private final List<ActividadDiariaInicio> datos =
            new ArrayList<>();

    public GraficoActividadInicioPanel() {
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    public void setDatos(
            List<ActividadDiariaInicio> nuevosDatos) {

        datos.clear();

        if (nuevosDatos != null) {
            datos.addAll(nuevosDatos);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 =
                (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int ancho = getWidth();
            int alto = getHeight();

            if (datos.isEmpty()) {
                g2.setColor(
                        new Color(98, 124, 159)
                );

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                12
                        )
                );

                g2.drawString(
                        "Sin actividad disponible.",
                        20,
                        alto / 2
                );

                return;
            }

            int maximo = datos.stream()
                    .mapToInt(
                            ActividadDiariaInicio
                                    ::getOperaciones
                    )
                    .max()
                    .orElse(1);

            maximo = Math.max(maximo, 1);

            int margenSuperior = 20;
            int margenInferior = 34;
            int margenIzquierdo = 20;
            int margenDerecho = 20;

            int anchoUtil = Math.max(
                    100,
                    ancho
                    - margenIzquierdo
                    - margenDerecho
            );

            int altoUtil = Math.max(
                    60,
                    alto
                    - margenSuperior
                    - margenInferior
            );

            int espacio = anchoUtil / datos.size();
            int anchoBarra = Math.max(
                    22,
                    Math.min(54, espacio - 22)
            );

            for (int i = 0;
                    i < datos.size();
                    i++) {

                ActividadDiariaInicio dato =
                        datos.get(i);

                double proporcion =
                        (double) dato.getOperaciones()
                        / maximo;

                int alturaBarra =
                        dato.getOperaciones() == 0
                                ? 4
                                : Math.max(
                                        8,
                                        (int) Math.round(
                                                altoUtil
                                                * proporcion
                                        )
                                );

                int x = margenIzquierdo
                        + i * espacio
                        + (espacio - anchoBarra) / 2;

                int y = margenSuperior
                        + altoUtil
                        - alturaBarra;

                g2.setColor(
                        dato.getOperaciones() == 0
                                ? new Color(
                                        220,
                                        227,
                                        236
                                )
                                : new Color(
                                        75,
                                        121,
                                        176
                                )
                );

                g2.fillRoundRect(
                        x,
                        y,
                        anchoBarra,
                        alturaBarra,
                        8,
                        8
                );

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                10
                        )
                );

                g2.setColor(
                        new Color(24, 50, 87)
                );

                String valor =
                        String.valueOf(
                                dato.getOperaciones()
                        );

                FontMetrics fmValor =
                        g2.getFontMetrics();

                g2.drawString(
                        valor,
                        x
                        + (anchoBarra
                        - fmValor.stringWidth(valor))
                        / 2,
                        Math.max(12, y - 5)
                );

                String dia = dato.getFecha()
                        .getDayOfWeek()
                        .getDisplayName(
                                TextStyle.SHORT,
                                new Locale("es", "HN")
                        );

                dia = dia.replace(".", "");
                dia = Character.toUpperCase(
                        dia.charAt(0)
                ) + dia.substring(1);

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                11
                        )
                );

                FontMetrics fmDia =
                        g2.getFontMetrics();

                g2.drawString(
                        dia,
                        x
                        + (anchoBarra
                        - fmDia.stringWidth(dia))
                        / 2,
                        margenSuperior
                        + altoUtil
                        + 22
                );
            }

        } finally {
            g2.dispose();
        }
    }
}
