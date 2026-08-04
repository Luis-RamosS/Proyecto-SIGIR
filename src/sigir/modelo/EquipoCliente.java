package sigir.modelo;

import java.time.LocalDateTime;

public class EquipoCliente {
    private int idEquipo;
    private int idCliente;
    private String nombreCliente;
    private String tipoEquipo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String color;
    private String accesoriosRecibidos;
    private String observaciones;
    private LocalDateTime fechaRegistro;

    public int getIdEquipo() { return idEquipo; }
    public void setIdEquipo(int idEquipo) { this.idEquipo = idEquipo; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getTipoEquipo() { return tipoEquipo; }
    public void setTipoEquipo(String tipoEquipo) { this.tipoEquipo = tipoEquipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getAccesoriosRecibidos() { return accesoriosRecibidos; }
    public void setAccesoriosRecibidos(String accesoriosRecibidos) { this.accesoriosRecibidos = accesoriosRecibidos; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        String texto = tipoEquipo == null ? "Equipo" : tipoEquipo;
        if (marca != null && !marca.isBlank()) texto += " — " + marca;
        if (modelo != null && !modelo.isBlank()) texto += " " + modelo;
        if (numeroSerie != null && !numeroSerie.isBlank()) texto += " | S/N: " + numeroSerie;
        return texto;
    }
}
