package sigir.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdenServicio {
    private int idOrden;
    private int idEquipo;
    private int idUsuarioRecibe;
    private String nombreUsuarioRecibe;
    private String numeroOrden;
    private LocalDateTime fechaRecepcion;
    private String problemaReportado;
    private String diagnostico;
    private String trabajoRealizado;
    private BigDecimal costoEstimado = BigDecimal.ZERO;
    private BigDecimal costoFinal = BigDecimal.ZERO;
    private String estado;
    private LocalDate fechaPrometida;
    private LocalDateTime fechaEntrega;
    private LocalDate garantiaHasta;
    private String observaciones;
    private int idCliente;
    private String nombreCliente;
    private String numeroIdentidad;
    private String telefonoCliente;
    private String tipoEquipo;
    private String marcaEquipo;
    private String modeloEquipo;
    private String numeroSerieEquipo;
    private String colorEquipo;
    private String accesoriosRecibidos;
    private final List<RepuestoOrden> repuestos = new ArrayList<>();
    private final List<HistorialServicio> historial = new ArrayList<>();

    public BigDecimal getTotalRepuestos() {
        return repuestos.stream().map(RepuestoOrden::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    public int getIdOrden() { return idOrden; }
    public void setIdOrden(int v) { idOrden = v; }
    public int getIdEquipo() { return idEquipo; }
    public void setIdEquipo(int v) { idEquipo = v; }
    public int getIdUsuarioRecibe() { return idUsuarioRecibe; }
    public void setIdUsuarioRecibe(int v) { idUsuarioRecibe = v; }
    public String getNombreUsuarioRecibe() { return nombreUsuarioRecibe; }
    public void setNombreUsuarioRecibe(String v) { nombreUsuarioRecibe = v; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String v) { numeroOrden = v; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime v) { fechaRecepcion = v; }
    public String getProblemaReportado() { return problemaReportado; }
    public void setProblemaReportado(String v) { problemaReportado = v; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String v) { diagnostico = v; }
    public String getTrabajoRealizado() { return trabajoRealizado; }
    public void setTrabajoRealizado(String v) { trabajoRealizado = v; }
    public BigDecimal getCostoEstimado() { return costoEstimado == null ? BigDecimal.ZERO : costoEstimado; }
    public void setCostoEstimado(BigDecimal v) { costoEstimado = v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP); }
    public BigDecimal getCostoFinal() { return costoFinal == null ? BigDecimal.ZERO : costoFinal; }
    public void setCostoFinal(BigDecimal v) { costoFinal = v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP); }
    public String getEstado() { return estado; }
    public void setEstado(String v) { estado = v; }
    public LocalDate getFechaPrometida() { return fechaPrometida; }
    public void setFechaPrometida(LocalDate v) { fechaPrometida = v; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime v) { fechaEntrega = v; }
    public LocalDate getGarantiaHasta() { return garantiaHasta; }
    public void setGarantiaHasta(LocalDate v) { garantiaHasta = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { observaciones = v; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int v) { idCliente = v; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String v) { nombreCliente = v; }
    public String getNumeroIdentidad() { return numeroIdentidad; }
    public void setNumeroIdentidad(String v) { numeroIdentidad = v; }
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String v) { telefonoCliente = v; }
    public String getTipoEquipo() { return tipoEquipo; }
    public void setTipoEquipo(String v) { tipoEquipo = v; }
    public String getMarcaEquipo() { return marcaEquipo; }
    public void setMarcaEquipo(String v) { marcaEquipo = v; }
    public String getModeloEquipo() { return modeloEquipo; }
    public void setModeloEquipo(String v) { modeloEquipo = v; }
    public String getNumeroSerieEquipo() { return numeroSerieEquipo; }
    public void setNumeroSerieEquipo(String v) { numeroSerieEquipo = v; }
    public String getColorEquipo() { return colorEquipo; }
    public void setColorEquipo(String v) { colorEquipo = v; }
    public String getAccesoriosRecibidos() { return accesoriosRecibidos; }
    public void setAccesoriosRecibidos(String v) { accesoriosRecibidos = v; }
    public List<RepuestoOrden> getRepuestos() { return Collections.unmodifiableList(repuestos); }
    public void setRepuestos(List<RepuestoOrden> v) { repuestos.clear(); if (v != null) repuestos.addAll(v); }
    public List<HistorialServicio> getHistorial() { return Collections.unmodifiableList(historial); }
    public void setHistorial(List<HistorialServicio> v) { historial.clear(); if (v != null) historial.addAll(v); }
}
