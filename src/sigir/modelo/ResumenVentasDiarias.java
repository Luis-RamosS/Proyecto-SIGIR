package sigir.modelo;

import java.math.BigDecimal;

public class ResumenVentasDiarias {

    private BigDecimal efectivo = BigDecimal.ZERO;
    private BigDecimal transferencias = BigDecimal.ZERO;
    private BigDecimal creditos = BigDecimal.ZERO;
    private BigDecimal tarjetas = BigDecimal.ZERO;
    private BigDecimal ventasRapidas = BigDecimal.ZERO;
    private BigDecimal totalVentas = BigDecimal.ZERO;

    public BigDecimal getEfectivo() {
        return efectivo == null ? BigDecimal.ZERO : efectivo;
    }

    public void setEfectivo(BigDecimal efectivo) {
        this.efectivo = efectivo;
    }

    public BigDecimal getTransferencias() {
        return transferencias == null ? BigDecimal.ZERO : transferencias;
    }

    public void setTransferencias(BigDecimal transferencias) {
        this.transferencias = transferencias;
    }

    public BigDecimal getCreditos() {
        return creditos == null ? BigDecimal.ZERO : creditos;
    }

    public void setCreditos(BigDecimal creditos) {
        this.creditos = creditos;
    }

    public BigDecimal getTarjetas() {
        return tarjetas == null ? BigDecimal.ZERO : tarjetas;
    }

    public void setTarjetas(BigDecimal tarjetas) {
        this.tarjetas = tarjetas;
    }

    public BigDecimal getVentasRapidas() {
        return ventasRapidas == null
                ? BigDecimal.ZERO
                : ventasRapidas;
    }

    public void setVentasRapidas(
            BigDecimal ventasRapidas) {

        this.ventasRapidas = ventasRapidas;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas == null ? BigDecimal.ZERO : totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }
}
