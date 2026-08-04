package sigir.modelo;

public class UsuarioFiltro {
    private int idUsuario;
    private String nombreCompleto;
    private String nombreUsuario;
    private String rol;

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        if (idUsuario <= 0) return "TODOS";
        return nombreCompleto + " — " + nombreUsuario;
    }
}
