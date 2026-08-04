package sigir.modelo;

public class RolSistema {

    private int idRol;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
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

    public String getNombreVisible() {
        if ("DUENO".equalsIgnoreCase(nombre)) {
            return "DUEÑO";
        }

        return nombre == null
                ? ""
                : nombre;
    }

    @Override
    public String toString() {
        return getNombreVisible();
    }
}
