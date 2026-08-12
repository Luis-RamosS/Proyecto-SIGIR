package sigir.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CajaChicaResumen {

    private BigDecimal fondoMaximo = BigDecimal.ZERO;
    private BigDecimal saldoDisponible = BigDecimal.ZERO;
    private BigDecimal gastadoSemana = BigDecimal.ZERO;
    private BigDecimal reposicionSugerida = BigDecimal.ZERO;
    private int movimientosSemana;
    private LocalDate inicioSemana;
    private LocalDate finSemana;

    public BigDecimal getFondoMaximo() {
        return fondoMaximo == null ? BigDecimal.ZERO : fondoMaximo;
    }

    public void setFondoMaximo(BigDecimal fondoMaximo) {
        this.fondoMaximo = fondoMaximo;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible == null ? BigDecimal.ZERO : saldoDisponible;
    }

    public void setSaldoDisponible(BigDecimal saldoDisponible) {
        this.saldoDisponible = saldoDisponible;
    }

    public BigDecimal getGastadoSemana() {
        return gastadoSemana == null ? BigDecimal.ZERO : gastadoSemana;
    }

    public void setGastadoSemana(BigDecimal gastadoSemana) {
        this.gastadoSemana = gastadoSemana;
    }

    public BigDecimal getReposicionSugerida() {
        return reposicionSugerida == null ? BigDecimal.ZERO : reposicionSugerida;
    }

    public void setReposicionSugerida(BigDecimal reposicionSugerida) {
        this.reposicionSugerida = reposicionSugerida;
    }

    public int getMovimientosSemana() {
        return movimientosSemana;
    }

    public void setMovimientosSemana(int movimientosSemana) {
        this.movimientosSemana = movimientosSemana;
    }

    public LocalDate getInicioSemana() {
        return inicioSemana;
    }

    public void setInicioSemana(LocalDate inicioSemana) {
        this.inicioSemana = inicioSemana;
    }

    public LocalDate getFinSemana() {
        return finSemana;
    }

    public void setFinSemana(LocalDate finSemana) {
        this.finSemana = finSemana;
    }
}
