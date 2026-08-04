package sigir.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

public class ConfiguracionSistema {

    private int idConfiguracion = 1;

    private String nombreEmpresa;
    private String rtn;
    private String direccion;
    private String telefono;
    private String correo;

    private String monedaCodigo = "HNL";
    private String simboloMoneda = "L";
    private BigDecimal porcentajeImpuesto =
            BigDecimal.ZERO;

    private String prefijoFactura = "FAC";
    private String pieFactura;

    private byte[] logo;
    private String logoNombre;

    private String smtpHost = "smtp.gmail.com";
    private int smtpPuerto = 587;
    private String smtpUsuario;
    private boolean smtpTls = true;
    private String nombreRemitente;

    private String rutaRespaldoServidor =
            "C:\\SIGIR\\Respaldos";

    private LocalDateTime fechaActualizacion;
    private Integer idUsuarioActualiza;
    private String nombreUsuarioActualiza;

    public int getIdConfiguracion() {
        return idConfiguracion;
    }

    public void setIdConfiguracion(
            int idConfiguracion) {

        this.idConfiguracion = idConfiguracion;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(
            String nombreEmpresa) {

        this.nombreEmpresa = nombreEmpresa;
    }

    public String getRtn() {
        return rtn;
    }

    public void setRtn(String rtn) {
        this.rtn = rtn;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getMonedaCodigo() {
        return monedaCodigo;
    }

    public void setMonedaCodigo(
            String monedaCodigo) {

        this.monedaCodigo = monedaCodigo;
    }

    public String getSimboloMoneda() {
        return simboloMoneda;
    }

    public void setSimboloMoneda(
            String simboloMoneda) {

        this.simboloMoneda = simboloMoneda;
    }

    public BigDecimal getPorcentajeImpuesto() {
        return porcentajeImpuesto == null
                ? BigDecimal.ZERO
                : porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(
            BigDecimal porcentajeImpuesto) {

        this.porcentajeImpuesto =
                porcentajeImpuesto;
    }

    public String getPrefijoFactura() {
        return prefijoFactura;
    }

    public void setPrefijoFactura(
            String prefijoFactura) {

        this.prefijoFactura = prefijoFactura;
    }

    public String getPieFactura() {
        return pieFactura;
    }

    public void setPieFactura(String pieFactura) {
        this.pieFactura = pieFactura;
    }

    public byte[] getLogo() {
        return logo == null
                ? null
                : Arrays.copyOf(
                        logo,
                        logo.length
                );
    }

    public void setLogo(byte[] logo) {
        this.logo = logo == null
                ? null
                : Arrays.copyOf(
                        logo,
                        logo.length
                );
    }

    public String getLogoNombre() {
        return logoNombre;
    }

    public void setLogoNombre(
            String logoNombre) {

        this.logoNombre = logoNombre;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPuerto() {
        return smtpPuerto;
    }

    public void setSmtpPuerto(int smtpPuerto) {
        this.smtpPuerto = smtpPuerto;
    }

    public String getSmtpUsuario() {
        return smtpUsuario;
    }

    public void setSmtpUsuario(
            String smtpUsuario) {

        this.smtpUsuario = smtpUsuario;
    }

    public boolean isSmtpTls() {
        return smtpTls;
    }

    public void setSmtpTls(boolean smtpTls) {
        this.smtpTls = smtpTls;
    }

    public String getNombreRemitente() {
        return nombreRemitente;
    }

    public void setNombreRemitente(
            String nombreRemitente) {

        this.nombreRemitente = nombreRemitente;
    }

    public String getRutaRespaldoServidor() {
        return rutaRespaldoServidor;
    }

    public void setRutaRespaldoServidor(
            String rutaRespaldoServidor) {

        this.rutaRespaldoServidor =
                rutaRespaldoServidor;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion) {

        this.fechaActualizacion =
                fechaActualizacion;
    }

    public Integer getIdUsuarioActualiza() {
        return idUsuarioActualiza;
    }

    public void setIdUsuarioActualiza(
            Integer idUsuarioActualiza) {

        this.idUsuarioActualiza =
                idUsuarioActualiza;
    }

    public String getNombreUsuarioActualiza() {
        return nombreUsuarioActualiza;
    }

    public void setNombreUsuarioActualiza(
            String nombreUsuarioActualiza) {

        this.nombreUsuarioActualiza =
                nombreUsuarioActualiza;
    }
}
