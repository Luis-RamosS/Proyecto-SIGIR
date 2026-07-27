package sigir.modelo;

import java.time.LocalDateTime;

public class Usuario {

    private final int idUsuario;
    private final int idRol;
    private final String nombreCompleto;
    private final String nombreUsuario;
    private final String correo;
    private final String telefono;
    private final String estado;
    private final String rol;
    private final LocalDateTime ultimoAcceso;

    public Usuario(
            int idUsuario,
            int idRol,
            String nombreCompleto,
            String nombreUsuario,
            String correo,
            String telefono,
            String estado,
            String rol,
            LocalDateTime ultimoAcceso) {

        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
        this.rol = rol;
        this.ultimoAcceso = ultimoAcceso;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getIdRol() {
        return idRol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEstado() {
        return estado;
    }

    public String getRol() {
        return rol;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public boolean esDueno() {
        return "DUENO".equalsIgnoreCase(rol);
    }

    public boolean esTrabajador() {
        return "TRABAJADOR".equalsIgnoreCase(rol);
    }

    @Override
    public String toString() {
        return nombreCompleto;
    }
}