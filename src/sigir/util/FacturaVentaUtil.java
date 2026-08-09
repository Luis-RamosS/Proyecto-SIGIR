package sigir.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import sigir.modelo.DetalleVenta;
import sigir.modelo.Venta;

public final class FacturaVentaUtil {
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FacturaVentaUtil() {}

    public static String generarTexto(Venta venta) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIGIR\nSISTEMA DE GESTION INTEGRAL\n");
        sb.append("=============================================\n");
        sb.append("FACTURA: ").append(venta.getNumeroFactura()).append('\n');
        sb.append("FECHA: ").append(venta.getFechaVenta() == null ? "" : venta.getFechaVenta().format(FECHA)).append('\n');
        sb.append("CLIENTE: ").append(venta.getNombreCliente()).append('\n');
        sb.append("VENDEDOR: ").append(venta.getNombreUsuario()).append('\n');
        sb.append("PAGO: ").append(venta.getMetodoPago()).append('\n');
        if ("TRANSFERENCIA".equalsIgnoreCase(venta.getMetodoPago())
                && venta.getComprobanteTransferencia() != null
                && !venta.getComprobanteTransferencia().isBlank()) {
            sb.append("COMPROBANTE: ")
                    .append(venta.getComprobanteTransferencia())
                    .append('\n');
        }
        sb.append("---------------------------------------------\n");
        for (DetalleVenta d : venta.getDetalles()) {
            sb.append(d.getCodigoProducto()).append(" - ").append(d.getNombreProducto()).append('\n');
            sb.append("  ").append(d.getCantidad()).append(" x ").append(moneda(d.getPrecioLista()));
            if (d.getDescuentoUnitario().compareTo(BigDecimal.ZERO) > 0) sb.append("  Desc. ").append(moneda(d.getDescuentoUnitario()));
            sb.append(" = ").append(moneda(d.getSubtotal())).append('\n');
            if (d.isManejaNumeroSerie() && !d.getUnidades().isEmpty()) {
                sb.append("  Series: ").append(d.getUnidades().stream().map(u -> u.getNumeroSerie() == null ? "Sin serie" : u.getNumeroSerie()).collect(Collectors.joining(", "))).append('\n');
            }
        }
        sb.append("---------------------------------------------\n");
        sb.append("SUBTOTAL: ").append(moneda(venta.getSubtotal())).append('\n');
        sb.append("DESCUENTO: ").append(moneda(venta.getDescuento())).append(" (").append(venta.getPorcentajeDescuento()).append("%)\n");
        sb.append("TOTAL: ").append(moneda(venta.getTotal())).append('\n');
        sb.append("PAGADO: ").append(moneda(venta.getMontoPagado())).append('\n');
        sb.append("CAMBIO: ").append(moneda(venta.getCambio())).append('\n');
        sb.append("=============================================\nGracias por su compra.\n");
        return sb.toString();
    }

    private static String moneda(BigDecimal valor) { return MONEDA.format(valor == null ? BigDecimal.ZERO : valor); }
}
