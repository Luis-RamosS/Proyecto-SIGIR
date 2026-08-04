package sigir.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import sigir.modelo.DatoGrafico;

public class GraficoBarrasPanel extends JPanel {

    private String titulo = "Gráfico del reporte";
    private final List<DatoGrafico> datos = new ArrayList<>();

    public GraficoBarrasPanel() {
        setBackground(Color.WHITE);
    }

    public void setDatos(
            String titulo,
            List<DatoGrafico> nuevosDatos) {

        this.titulo = titulo == null
                ? "Gráfico del reporte"
                : titulo;

        datos.clear();

        if (nuevosDatos != null) {
            datos.addAll(nuevosDatos);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int ancho = getWidth();
            int alto = getHeight();

            g2.setColor(new Color(24, 50, 87));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(titulo, 24, 32);

            if (datos.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(98, 124, 159));

                String mensaje =
                        "No hay datos disponibles para graficar.";

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        mensaje,
                        Math.max(
                                24,
                                (ancho - fm.stringWidth(mensaje)) / 2
                        ),
                        alto / 2
                );
                return;
            }

            int izquierda = 68;
            int derecha = 28;
            int superior = 58;
            int inferior = 84;

            int anchoGrafico = Math.max(
                    100,
                    ancho - izquierda - derecha
            );

            int altoGrafico = Math.max(
                    100,
                    alto - superior - inferior
            );

            BigDecimal maximo = datos.stream()
                    .map(DatoGrafico::getValor)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);

            if (maximo.signum() <= 0) {
                maximo = BigDecimal.ONE;
            }

            g2.setStroke(new BasicStroke(1f));

            for (int i = 0; i <= 4; i++) {
                int y = superior
                        + altoGrafico
                        - altoGrafico * i / 4;

                g2.setColor(new Color(220, 227, 236));
                g2.drawLine(
                        izquierda,
                        y,
                        izquierda + anchoGrafico,
                        y
                );

                BigDecimal valor = maximo
                        .multiply(BigDecimal.valueOf(i))
                        .divide(
                                BigDecimal.valueOf(4),
                                2,
                                RoundingMode.HALF_UP
                        );

                g2.setColor(new Color(98, 124, 159));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(abreviar(valor), 10, y + 4);
            }

            int cantidad = Math.min(datos.size(), 12);
            int espacio = anchoGrafico / cantidad;
            int anchoBarra = Math.max(
                    18,
                    Math.min(54, espacio - 16)
            );

            for (int i = 0; i < cantidad; i++) {
                DatoGrafico dato = datos.get(i);

                double proporcion = dato.getValor()
                        .divide(
                                maximo,
                                6,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue();

                int alturaBarra = (int) Math.round(
                        altoGrafico * proporcion
                );

                int x = izquierda
                        + i * espacio
                        + (espacio - anchoBarra) / 2;

                int y = superior
                        + altoGrafico
                        - alturaBarra;

                g2.setColor(new Color(49, 105, 181));
                g2.fillRoundRect(
                        x,
                        y,
                        anchoBarra,
                        alturaBarra,
                        8,
                        8
                );

                g2.setColor(new Color(24, 50, 87));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));

                String valor = abreviar(dato.getValor());
                FontMetrics fmValor = g2.getFontMetrics();

                g2.drawString(
                        valor,
                        x + (anchoBarra
                        - fmValor.stringWidth(valor)) / 2,
                        Math.max(superior + 12, y - 6)
                );

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                String etiqueta = recortar(
                        dato.getEtiqueta(),
                        15
                );

                FontMetrics fmEtiqueta = g2.getFontMetrics();

                g2.drawString(
                        etiqueta,
                        x + (anchoBarra
                        - fmEtiqueta.stringWidth(etiqueta)) / 2,
                        superior + altoGrafico + 24
                );
            }

        } finally {
            g2.dispose();
        }
    }

    private String abreviar(BigDecimal valor) {
        BigDecimal absoluto = valor.abs();

        if (absoluto.compareTo(
                new BigDecimal("1000000")) >= 0) {

            return valor.divide(
                    new BigDecimal("1000000"),
                    1,
                    RoundingMode.HALF_UP
            ) + "M";
        }

        if (absoluto.compareTo(
                new BigDecimal("1000")) >= 0) {

            return valor.divide(
                    new BigDecimal("1000"),
                    1,
                    RoundingMode.HALF_UP
            ) + "K";
        }

        return valor.setScale(
                valor.scale() > 0 ? 1 : 0,
                RoundingMode.HALF_UP
        ).stripTrailingZeros().toPlainString();
    }

    private String recortar(String texto, int maximo) {
        if (texto == null) return "";

        String limpio = texto.trim();

        return limpio.length() <= maximo
                ? limpio
                : limpio.substring(0, maximo - 1) + "…";
    }
}
