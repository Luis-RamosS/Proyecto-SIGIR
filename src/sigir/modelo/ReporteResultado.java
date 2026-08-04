package sigir.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReporteResultado {
    private String titulo;
    private String descripcion;
    private String etiquetaResumen;
    private BigDecimal valorResumen = BigDecimal.ZERO;
    private boolean resumenMonetario;
    private final List<String> columnas = new ArrayList<>();
    private final List<Object[]> filas = new ArrayList<>();
    private final List<DatoGrafico> datosGrafico = new ArrayList<>();

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getEtiquetaResumen() { return etiquetaResumen; }
    public void setEtiquetaResumen(String etiquetaResumen) {
        this.etiquetaResumen = etiquetaResumen;
    }
    public BigDecimal getValorResumen() {
        return valorResumen == null ? BigDecimal.ZERO : valorResumen;
    }
    public void setValorResumen(BigDecimal valorResumen) {
        this.valorResumen = valorResumen;
    }
    public boolean isResumenMonetario() { return resumenMonetario; }
    public void setResumenMonetario(boolean resumenMonetario) {
        this.resumenMonetario = resumenMonetario;
    }
    public List<String> getColumnas() {
        return Collections.unmodifiableList(columnas);
    }
    public void setColumnas(List<String> nuevasColumnas) {
        columnas.clear();
        if (nuevasColumnas != null) columnas.addAll(nuevasColumnas);
    }
    public List<Object[]> getFilas() {
        return Collections.unmodifiableList(filas);
    }
    public void agregarFila(Object... valores) { filas.add(valores); }
    public List<DatoGrafico> getDatosGrafico() {
        return Collections.unmodifiableList(datosGrafico);
    }
    public void setDatosGrafico(List<DatoGrafico> datos) {
        datosGrafico.clear();
        if (datos != null) datosGrafico.addAll(datos);
    }
    public int getCantidadRegistros() { return filas.size(); }
}
