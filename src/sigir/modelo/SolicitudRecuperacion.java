package sigir.modelo;

public final class SolicitudRecuperacion {

    private final int idUsuario;
    private final String nombreCompleto;
    private final String nombreUsuario;
    private final String correo;

    public SolicitudRecuperacion(
            int idUsuario,
            String nombreCompleto,
            String nombreUsuario,
            String correo) {

        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
    }

    public int getIdUsuario() {
        return idUsuario;
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
}
