package sigir.util;

import java.awt.Component;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class ExportadorReporteUtil {

    private ExportadorReporteUtil() {
    }

    public static void exportarCsv(
            Component padre,
            JTable tabla,
            String nombreSugerido) {

        if (tabla == null || tabla.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(
                    padre,
                    "Primero consulta un reporte.",
                    "Sin datos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar reporte para Excel");
        selector.setFileFilter(
                new FileNameExtensionFilter(
                        "Archivo CSV (*.csv)",
                        "csv"
                )
        );

        selector.setSelectedFile(
                new File(
                        limpiarNombre(nombreSugerido)
                        + ".csv"
                )
        );

        if (selector.showSaveDialog(padre)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = asegurarExtension(
                selector.getSelectedFile(),
                ".csv"
        );

        if (archivo.exists()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    padre,
                    "El archivo ya existe.\n"
                    + "¿Deseas reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (BufferedWriter escritor =
                     Files.newBufferedWriter(
                             archivo.toPath(),
                             StandardCharsets.UTF_8
                     )) {

            escritor.write('\ufeff');

            for (int columna = 0;
                    columna < tabla.getColumnCount();
                    columna++) {

                if (columna > 0) escritor.write(';');

                escritor.write(
                        escapar(
                                tabla.getColumnName(columna)
                        )
                );
            }

            escritor.newLine();

            for (int filaVista = 0;
                    filaVista < tabla.getRowCount();
                    filaVista++) {

                int filaModelo =
                        tabla.convertRowIndexToModel(filaVista);

                for (int columna = 0;
                        columna < tabla.getColumnCount();
                        columna++) {

                    if (columna > 0) escritor.write(';');

                    Object valor = tabla.getModel()
                            .getValueAt(
                                    filaModelo,
                                    columna
                            );

                    escritor.write(
                            escapar(
                                    valor == null
                                            ? ""
                                            : valor.toString()
                            )
                    );
                }

                escritor.newLine();
            }

            JOptionPane.showMessageDialog(
                    padre,
                    "Reporte exportado correctamente:\n"
                    + archivo.getAbsolutePath(),
                    "Exportación completada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    padre,
                    "No fue posible exportar el reporte.\n\n"
                    + ex.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void imprimir(
            Component padre,
            JTable tabla,
            String titulo) {

        if (tabla == null
                || tabla.getColumnCount() == 0
                || tabla.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    padre,
                    "Primero consulta un reporte con datos.",
                    "Sin datos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            boolean completado = tabla.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(
                            titulo == null
                                    ? "Reporte SIGIR"
                                    : titulo
                    ),
                    new MessageFormat("Página {0}")
            );

            if (!completado) return;

            JOptionPane.showMessageDialog(
                    padre,
                    "El reporte fue enviado a impresión.\n"
                    + "Para obtener un PDF selecciona "
                    + "\"Microsoft Print to PDF\".",
                    "Impresión completada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                    padre,
                    "No fue posible imprimir el reporte.\n\n"
                    + ex.getMessage(),
                    "Error de impresión",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String escapar(String valor) {
        String limpio = valor == null ? "" : valor;

        boolean requiereComillas =
                limpio.contains(";")
                || limpio.contains("\"")
                || limpio.contains("\n")
                || limpio.contains("\r");

        limpio = limpio.replace("\"", "\"\"");

        return requiereComillas
                ? "\"" + limpio + "\""
                : limpio;
    }

    private static File asegurarExtension(
            File archivo,
            String extension) {

        if (archivo.getName()
                .toLowerCase()
                .endsWith(extension)) {
            return archivo;
        }

        return new File(
                archivo.getParentFile(),
                archivo.getName() + extension
        );
    }

    private static String limpiarNombre(String nombre) {
        String base = nombre == null
                ? "reporte_SIGIR"
                : nombre.trim();

        if (base.isBlank()) {
            base = "reporte_SIGIR";
        }

        return base.replaceAll(
                "[\\\\/:*?\"<>|]+",
                "_"
        );
    }
}
