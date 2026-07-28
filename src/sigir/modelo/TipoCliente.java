package sigir.modelo;

public class TipoCliente {

    private int idTipoCliente;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public TipoCliente() {
    }

    public TipoCliente(int idTipoCliente, String nombre) {
        this.idTipoCliente = idTipoCliente;
        this.nombre = nombre;
        this.activo = true;
    }

    public TipoCliente(
            int idTipoCliente,
            String nombre,
            String descripcion,
            boolean activo) {

        this.idTipoCliente = idTipoCliente;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public int getIdTipoCliente() {
        return idTipoCliente;
    }

    public void setIdTipoCliente(int idTipoCliente) {
        this.idTipoCliente = idTipoCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre == null ? "" : nombre;
    }
}
