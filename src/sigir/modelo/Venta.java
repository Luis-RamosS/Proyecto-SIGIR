package sigir.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venta {
    private int idVenta;
    private int idCliente;
    private String nombreCliente;
    private int idUsuario;
    private String nombreUsuario;
    private Integer idUsuarioAutorizaDescuento;
    private String numeroFactura;
    private LocalDateTime fechaVenta;
    private String tipoVenta;
    private String metodoPago;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private String tipoDescuento;
    private String motivoDescuento;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal montoPagado = BigDecimal.ZERO;
    private BigDecimal cambio = BigDecimal.ZERO;
    private String estado;
    private String observaciones;
    private String comprobanteTransferencia;
    private LocalDate fechaVencimientoCredito;
    private BigDecimal montoCuotaCredito;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    public void recalcularTotales() {
        subtotal = detalles.stream().map(DetalleVenta::getSubtotalBruto).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        descuento = detalles.stream().map(DetalleVenta::getDescuentoTotalLinea).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        total = subtotal.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPorcentajeDescuento() {
        return subtotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : descuento.multiply(BigDecimal.valueOf(100)).divide(subtotal, 2, RoundingMode.HALF_UP);
    }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public Integer getIdUsuarioAutorizaDescuento() { return idUsuarioAutorizaDescuento; }
    public void setIdUsuarioAutorizaDescuento(Integer v) { idUsuarioAutorizaDescuento = v; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }
    public String getTipoVenta() { return tipoVenta; }
    public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getSubtotal() { return subtotal == null ? BigDecimal.ZERO : subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal == null ? BigDecimal.ZERO : subtotal; }
    public BigDecimal getDescuento() { return descuento == null ? BigDecimal.ZERO : descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento == null ? BigDecimal.ZERO : descuento; }
    public String getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(String tipoDescuento) { this.tipoDescuento = tipoDescuento; }
    public String getMotivoDescuento() { return motivoDescuento; }
    public void setMotivoDescuento(String motivoDescuento) { this.motivoDescuento = motivoDescuento; }
    public BigDecimal getTotal() { return total == null ? BigDecimal.ZERO : total; }
    public void setTotal(BigDecimal total) { this.total = total == null ? BigDecimal.ZERO : total; }
    public BigDecimal getMontoPagado() { return montoPagado == null ? BigDecimal.ZERO : montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado == null ? BigDecimal.ZERO : montoPagado; }
    public BigDecimal getCambio() { return cambio == null ? BigDecimal.ZERO : cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio == null ? BigDecimal.ZERO : cambio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getComprobanteTransferencia() { return comprobanteTransferencia; }
    public void setComprobanteTransferencia(String comprobanteTransferencia) { this.comprobanteTransferencia = comprobanteTransferencia; }
    public LocalDate getFechaVencimientoCredito() { return fechaVencimientoCredito; }
    public void setFechaVencimientoCredito(LocalDate fechaVencimientoCredito) { this.fechaVencimientoCredito = fechaVencimientoCredito; }
    public BigDecimal getMontoCuotaCredito() { return montoCuotaCredito; }
    public void setMontoCuotaCredito(BigDecimal montoCuotaCredito) { this.montoCuotaCredito = montoCuotaCredito; }
    public List<DetalleVenta> getDetalles() { return Collections.unmodifiableList(detalles); }
    public void setDetalles(List<DetalleVenta> nuevos) { detalles.clear(); if (nuevos != null) detalles.addAll(nuevos); recalcularTotales(); }
}
