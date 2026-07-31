package sigir.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetalleVenta {
    private int idDetalleVenta;
    private int idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioLista = BigDecimal.ZERO;
    private BigDecimal descuentoUnitario = BigDecimal.ZERO;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private int diasGarantia;
    private boolean manejaNumeroSerie;
    private final List<UnidadProducto> unidades = new ArrayList<>();

    public void recalcular() {
        BigDecimal lista = precioLista == null ? BigDecimal.ZERO : precioLista;
        BigDecimal descuento = descuentoUnitario == null ? BigDecimal.ZERO : descuentoUnitario;
        precioUnitario = lista.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
        subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSubtotalBruto() {
        return getPrecioLista().multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDescuentoTotalLinea() {
        return getDescuentoUnitario().multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public String getResumenSeries() {
        if (!manejaNumeroSerie) return "No aplica";
        return unidades.isEmpty() ? "Sin seleccionar" : unidades.size() + " seleccionadas";
    }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; recalcular(); }
    public BigDecimal getPrecioLista() { return precioLista == null ? BigDecimal.ZERO : precioLista; }
    public void setPrecioLista(BigDecimal precioLista) { this.precioLista = precioLista == null ? BigDecimal.ZERO : precioLista.setScale(2, RoundingMode.HALF_UP); recalcular(); }
    public BigDecimal getDescuentoUnitario() { return descuentoUnitario == null ? BigDecimal.ZERO : descuentoUnitario; }
    public void setDescuentoUnitario(BigDecimal descuentoUnitario) { this.descuentoUnitario = descuentoUnitario == null ? BigDecimal.ZERO : descuentoUnitario.setScale(2, RoundingMode.HALF_UP); recalcular(); }
    public BigDecimal getPrecioUnitario() { return precioUnitario == null ? BigDecimal.ZERO : precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario == null ? BigDecimal.ZERO : precioUnitario.setScale(2, RoundingMode.HALF_UP); }
    public BigDecimal getSubtotal() { return subtotal == null ? BigDecimal.ZERO : subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal == null ? BigDecimal.ZERO : subtotal.setScale(2, RoundingMode.HALF_UP); }
    public int getDiasGarantia() { return diasGarantia; }
    public void setDiasGarantia(int diasGarantia) { this.diasGarantia = diasGarantia; }
    public boolean isManejaNumeroSerie() { return manejaNumeroSerie; }
    public void setManejaNumeroSerie(boolean manejaNumeroSerie) { this.manejaNumeroSerie = manejaNumeroSerie; }
    public List<UnidadProducto> getUnidades() { return Collections.unmodifiableList(unidades); }
    public void setUnidades(List<UnidadProducto> nuevas) { unidades.clear(); if (nuevas != null) unidades.addAll(nuevas); }
}
