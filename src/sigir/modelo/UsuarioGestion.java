package sigir.modelo;

import java.time.LocalDateTime;

public class UsuarioGestion {

    private int idUsuario;
    private int idRol;
    private String nombreRol;
    private String descripcionRol;
    private String nombreCompleto;
    private String nombreUsuario;
    private String correo;
    private boolean correoVerificado;
    private String telefono;
    private String estado;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCambioContrasena;
    private LocalDateTime fechaCreacion;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getDescripcionRol() {
        return descripcionRol;
    }

    public void setDescripcionRol(
            String descripcionRol) {

        this.descripcionRol = descripcionRol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(
            String nombreCompleto) {

        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(
            String nombreUsuario) {

        this.nombreUsuario = nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(
            boolean correoVerificado) {

        this.correoVerificado =
                correoVerificado;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(
            int intentosFallidos) {

        this.intentosFallidos =
                intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(
            LocalDateTime bloqueadoHasta) {

        this.bloqueadoHasta =
                bloqueadoHasta;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(
            LocalDateTime ultimoAcceso) {

        this.ultimoAcceso = ultimoAcceso;
    }

    public LocalDateTime
            getFechaCambioContrasena() {

        return fechaCambioContrasena;
    }

    public void setFechaCambioContrasena(
            LocalDateTime fechaCambioContrasena) {

        this.fechaCambioContrasena =
                fechaCambioContrasena;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion) {

        this.fechaCreacion = fechaCreacion;
    }

    public boolean esDueno() {
        return "DUENO".equalsIgnoreCase(
                nombreRol
        );
    }

    public String getNombreRolVisible() {
        return esDueno()
                ? "DUEÑO"
                : nombreRol == null
                        ? ""
                        : nombreRol;
    }
}
