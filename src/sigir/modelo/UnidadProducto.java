package sigir.modelo;

public class UnidadProducto {
    private int idUnidad;
    private int idProducto;
    private String numeroSerie;
    private String codigoInterno;
    private String estado;

    public int getIdUnidad() { return idUnidad; }
    public void setIdUnidad(int idUnidad) { this.idUnidad = idUnidad; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getCodigoInterno() { return codigoInterno; }
    public void setCodigoInterno(String codigoInterno) { this.codigoInterno = codigoInterno; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        String serie = numeroSerie == null || numeroSerie.isBlank() ? "Sin serie" : numeroSerie;
        String codigo = codigoInterno == null || codigoInterno.isBlank() ? "" : " | " + codigoInterno;
        return serie + codigo;
    }
}
