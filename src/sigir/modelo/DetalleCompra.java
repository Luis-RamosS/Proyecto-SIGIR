package sigir.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetalleCompra {

    private int idDetalleCompra;
    private int idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private boolean manejaNumeroSerie;
    private final List<String> numerosSerie = new ArrayList<>();

    public void recalcularSubtotal() {
        BigDecimal costo = costoUnitario == null
                ? BigDecimal.ZERO
                : costoUnitario;

        subtotal = costo
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public int getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario == null
                ? BigDecimal.ZERO
                : costoUnitario.setScale(2, RoundingMode.HALF_UP);
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal == null
                ? BigDecimal.ZERO
                : subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isManejaNumeroSerie() {
        return manejaNumeroSerie;
    }

    public void setManejaNumeroSerie(boolean manejaNumeroSerie) {
        this.manejaNumeroSerie = manejaNumeroSerie;
    }

    public List<String> getNumerosSerie() {
        return Collections.unmodifiableList(numerosSerie);
    }

    public void setNumerosSerie(List<String> series) {
        numerosSerie.clear();

        if (series != null) {
            for (String serie : series) {
                if (serie != null && !serie.isBlank()) {
                    numerosSerie.add(serie.trim());
                }
            }
        }
    }

    public String getResumenSeries() {
        if (!manejaNumeroSerie) {
            return "No aplica";
        }

        return numerosSerie.isEmpty()
                ? "Sin registrar"
                : numerosSerie.size() + " registradas";
    }
}
