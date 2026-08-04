package sigir.modelo;

import java.math.BigDecimal;

public class ResumenReportes {
    private BigDecimal ventasPeriodo = BigDecimal.ZERO;
    private BigDecimal comprasPeriodo = BigDecimal.ZERO;
    private int productosStockBajo;
    private int reparacionesPendientes;

    public BigDecimal getVentasPeriodo() {
        return ventasPeriodo == null ? BigDecimal.ZERO : ventasPeriodo;
    }
    public void setVentasPeriodo(BigDecimal ventasPeriodo) {
        this.ventasPeriodo = ventasPeriodo;
    }
    public BigDecimal getComprasPeriodo() {
        return comprasPeriodo == null ? BigDecimal.ZERO : comprasPeriodo;
    }
    public void setComprasPeriodo(BigDecimal comprasPeriodo) {
        this.comprasPeriodo = comprasPeriodo;
    }
    public int getProductosStockBajo() { return productosStockBajo; }
    public void setProductosStockBajo(int productosStockBajo) {
        this.productosStockBajo = productosStockBajo;
    }
    public int getReparacionesPendientes() { return reparacionesPendientes; }
    public void setReparacionesPendientes(int reparacionesPendientes) {
        this.reparacionesPendientes = reparacionesPendientes;
    }
}
