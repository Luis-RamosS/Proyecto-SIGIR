package sigir.modelo;

import java.math.BigDecimal;

public class ResumenInicio {

    private int ventasHoy;
    private BigDecimal totalVendidoHoy = BigDecimal.ZERO;
    private int productosRegistrados;
    private int productosStockBajo;
    private int creditosPendientes;
    private BigDecimal saldoCreditosPendientes = BigDecimal.ZERO;
    private int reparacionesPendientes;

    public int getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(int ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public BigDecimal getTotalVendidoHoy() {
        return totalVendidoHoy == null
                ? BigDecimal.ZERO
                : totalVendidoHoy;
    }

    public void setTotalVendidoHoy(
            BigDecimal totalVendidoHoy) {

        this.totalVendidoHoy = totalVendidoHoy;
    }

    public int getProductosRegistrados() {
        return productosRegistrados;
    }

    public void setProductosRegistrados(
            int productosRegistrados) {

        this.productosRegistrados = productosRegistrados;
    }

    public int getProductosStockBajo() {
        return productosStockBajo;
    }

    public void setProductosStockBajo(
            int productosStockBajo) {

        this.productosStockBajo = productosStockBajo;
    }

    public int getCreditosPendientes() {
        return creditosPendientes;
    }

    public void setCreditosPendientes(
            int creditosPendientes) {

        this.creditosPendientes = creditosPendientes;
    }

    public BigDecimal getSaldoCreditosPendientes() {
        return saldoCreditosPendientes == null
                ? BigDecimal.ZERO
                : saldoCreditosPendientes;
    }

    public void setSaldoCreditosPendientes(
            BigDecimal saldoCreditosPendientes) {

        this.saldoCreditosPendientes =
                saldoCreditosPendientes;
    }

    public int getReparacionesPendientes() {
        return reparacionesPendientes;
    }

    public void setReparacionesPendientes(
            int reparacionesPendientes) {

        this.reparacionesPendientes =
                reparacionesPendientes;
    }
}
