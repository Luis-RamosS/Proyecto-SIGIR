package sigir.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoCajaChica {

    private int idMovimiento;
    private LocalDateTime fechaMovimiento;
    private String tipo;
    private String categoria;
    private String concepto;
    private BigDecimal monto;
    private BigDecimal saldoPosterior;
    private String comprobante;
    private String observaciones;
    private int idUsuario;
    private String nombreUsuario;
    private String estado;

    public int getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(int idMovimiento) { this.idMovimiento = idMovimiento; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public BigDecimal getMonto() { return monto == null ? BigDecimal.ZERO : monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior == null ? BigDecimal.ZERO : saldoPosterior; }
    public void setSaldoPosterior(BigDecimal saldoPosterior) { this.saldoPosterior = saldoPosterior; }
    public String getComprobante() { return comprobante; }
    public void setComprobante(String comprobante) { this.comprobante = comprobante; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
