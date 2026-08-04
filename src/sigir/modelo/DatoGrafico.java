package sigir.modelo;

import java.math.BigDecimal;

public class DatoGrafico {
    private final String etiqueta;
    private final BigDecimal valor;

    public DatoGrafico(String etiqueta, BigDecimal valor) {
        this.etiqueta = etiqueta == null ? "" : etiqueta;
        this.valor = valor == null ? BigDecimal.ZERO : valor;
    }

    public String getEtiqueta() { return etiqueta; }
    public BigDecimal getValor() { return valor; }
}
