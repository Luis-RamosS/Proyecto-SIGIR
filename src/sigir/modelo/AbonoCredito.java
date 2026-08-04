package sigir.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AbonoCredito {
    private int idAbono;
    private int idCredito;
    private int idUsuario;
    private String nombreUsuario;
    private String nombreCliente;
    private String numeroFactura;
    private LocalDateTime fechaAbono;
    private BigDecimal monto = BigDecimal.ZERO;
    private String metodoPago;
    private String referencia;
    private String observaciones;

    public int getIdAbono() { return idAbono; }
    public void setIdAbono(int idAbono) { this.idAbono = idAbono; }
    public int getIdCredito() { return idCredito; }
    public void setIdCredito(int idCredito) { this.idCredito = idCredito; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public LocalDateTime getFechaAbono() { return fechaAbono; }
    public void setFechaAbono(LocalDateTime fechaAbono) { this.fechaAbono = fechaAbono; }
    public BigDecimal getMonto() { return monto == null ? BigDecimal.ZERO : monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
