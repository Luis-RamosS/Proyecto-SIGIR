package sigir.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatosInicio {

    private ResumenInicio resumen = new ResumenInicio();

    private final List<VentaRecienteInicio>
            ventasRecientes = new ArrayList<>();

    private final List<ProductoStockInicio>
            productosStockBajo = new ArrayList<>();

    private final List<ActividadDiariaInicio>
            actividadSemanal = new ArrayList<>();

    public ResumenInicio getResumen() {
        return resumen;
    }

    public void setResumen(ResumenInicio resumen) {
        this.resumen = resumen;
    }

    public List<VentaRecienteInicio>
            getVentasRecientes() {

        return Collections.unmodifiableList(
                ventasRecientes
        );
    }

    public void setVentasRecientes(
            List<VentaRecienteInicio> ventas) {

        ventasRecientes.clear();

        if (ventas != null) {
            ventasRecientes.addAll(ventas);
        }
    }

    public List<ProductoStockInicio>
            getProductosStockBajo() {

        return Collections.unmodifiableList(
                productosStockBajo
        );
    }

    public void setProductosStockBajo(
            List<ProductoStockInicio> productos) {

        productosStockBajo.clear();

        if (productos != null) {
            productosStockBajo.addAll(productos);
        }
    }

    public List<ActividadDiariaInicio>
            getActividadSemanal() {

        return Collections.unmodifiableList(
                actividadSemanal
        );
    }

    public void setActividadSemanal(
            List<ActividadDiariaInicio> actividad) {

        actividadSemanal.clear();

        if (actividad != null) {
            actividadSemanal.addAll(actividad);
        }
    }
}
