package sigir.modelo;

import java.time.LocalDateTime;

public class HistorialServicio {
    private int idHistorial;
    private int idOrden;
    private int idUsuario;
    private String numeroOrden;
    private String nombreCliente;
    private String nombreUsuario;
    private LocalDateTime fechaEvento;
    private String estadoAnterior;
    private String estadoNuevo;
    private String descripcion;

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int v) { idHistorial = v; }
    public int getIdOrden() { return idOrden; }
    public void setIdOrden(int v) { idOrden = v; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int v) { idUsuario = v; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String v) { numeroOrden = v; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String v) { nombreCliente = v; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String v) { nombreUsuario = v; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime v) { fechaEvento = v; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String v) { estadoAnterior = v; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String v) { estadoNuevo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { descripcion = v; }
}
