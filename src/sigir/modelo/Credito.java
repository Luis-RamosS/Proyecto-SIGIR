package sigir.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Credito {
    private int idCredito;
    private int idVenta;
    private int idCliente;
    private String numeroFactura;
    private String nombreCliente;
    private String numeroIdentidad;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private BigDecimal totalCredito = BigDecimal.ZERO;
    private BigDecimal saldoPendiente = BigDecimal.ZERO;
    private BigDecimal montoCuota;
    private String estado;
    private String observaciones;

    public int getIdCredito() { return idCredito; }
    public void setIdCredito(int idCredito) { this.idCredito = idCredito; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getNumeroIdentidad() { return numeroIdentidad; }
    public void setNumeroIdentidad(String numeroIdentidad) { this.numeroIdentidad = numeroIdentidad; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public BigDecimal getTotalCredito() { return totalCredito == null ? BigDecimal.ZERO : totalCredito; }
    public void setTotalCredito(BigDecimal totalCredito) { this.totalCredito = totalCredito; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente == null ? BigDecimal.ZERO : saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }
    public BigDecimal getMontoCuota() { return montoCuota; }
    public void setMontoCuota(BigDecimal montoCuota) { this.montoCuota = montoCuota; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean estaVencido() {
        return fechaVencimiento != null
                && fechaVencimiento.isBefore(LocalDate.now())
                && getSaldoPendiente().signum() > 0
                && !"ANULADO".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return "CR-" + String.format("%05d", idCredito)
                + " — " + nombreCliente
                + " | Saldo: L " + getSaldoPendiente();
    }
}
