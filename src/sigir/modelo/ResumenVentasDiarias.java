package sigir.modelo;

import java.math.BigDecimal;

public class ResumenVentasDiarias {

    private BigDecimal transferencias = BigDecimal.ZERO;
    private BigDecimal creditos = BigDecimal.ZERO;
    private BigDecimal tarjetas = BigDecimal.ZERO;
    private BigDecimal totalVentas = BigDecimal.ZERO;

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

    public BigDecimal getTotalVentas() {
        return totalVentas == null ? BigDecimal.ZERO : totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }
}
