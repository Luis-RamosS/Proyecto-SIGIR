package sigir.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class RepuestoOrden {
    private int idRepuestoOrden;
    private int idOrden;
    private int idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private LocalDateTime fechaAsignacion;

    public void recalcularSubtotal() {
        subtotal = getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);
    }
    public int getIdRepuestoOrden() { return idRepuestoOrden; }
    public void setIdRepuestoOrden(int v) { idRepuestoOrden = v; }
    public int getIdOrden() { return idOrden; }
    public void setIdOrden(int v) { idOrden = v; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int v) { idProducto = v; }
    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String v) { codigoProducto = v; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String v) { nombreProducto = v; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int v) { cantidad = v; recalcularSubtotal(); }
    public BigDecimal getPrecioUnitario() { return precioUnitario == null ? BigDecimal.ZERO : precioUnitario; }
    public void setPrecioUnitario(BigDecimal v) { precioUnitario = v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP); recalcularSubtotal(); }
    public BigDecimal getSubtotal() { return subtotal == null ? BigDecimal.ZERO : subtotal; }
    public void setSubtotal(BigDecimal v) { subtotal = v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP); }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime v) { fechaAsignacion = v; }
}
