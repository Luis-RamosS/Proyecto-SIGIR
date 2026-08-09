package sigir.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaRapida {

    private int idVentaRapida;
    private int idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private int idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaHoraReal;
    private LocalDateTime fechaRegistro;
    private int cantidad;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private String metodoPago;
    private String numeroSerie;
    private String observaciones;

    public int getIdVentaRapida() { return idVentaRapida; }
    public void setIdVentaRapida(int idVentaRapida) { this.idVentaRapida = idVentaRapida; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public LocalDateTime getFechaHoraReal() { return fechaHoraReal; }
    public void setFechaHoraReal(LocalDateTime fechaHoraReal) { this.fechaHoraReal = fechaHoraReal; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario == null ? BigDecimal.ZERO : precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getTotal() { return total == null ? BigDecimal.ZERO : total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
