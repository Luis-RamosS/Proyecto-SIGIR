package sigir.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JOptionPane;
import sigir.dao.VentaDAO;
import sigir.modelo.*;
import sigir.util.Sesion;
import sigir.vista.paneles.VentasPanel;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

public class VentaControlador {
    private final VentasPanel vista;
    private final VentaDAO dao = new VentaDAO();
    private final List<DetalleVenta> detalles = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private List<Venta> ventas = new ArrayList<>();
    
    private SwingWorker<DatosCarga, Void> trabajadorCarga;
    private SwingWorker<List<Venta>, Void> trabajadorBusqueda;

    private long ultimaCarga;
    private long versionBusqueda;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record DatosCarga(
            List<Cliente> clientes,
            List<Producto> productos,
            List<Venta> ventas
    ) {
    }

    private record FiltrosHistorial(
            String texto,
            LocalDate desde,
            LocalDate hasta,
            String metodo,
            String estado
    ) {
    }

    public VentaControlador(VentasPanel vista) {
        this.vista = vista;
    }

    public void iniciarAsync() {
        nuevaVenta();
        cargarAsync();
    }

    public void recargarAsync() {
        cargarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void recargarSiNecesario() {
        long tiempoTranscurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (tiempoTranscurrido
                >= VIGENCIA_DATOS_MS) {

            cargarAsync();
        }
    }

    private void cargarAsync() {
        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            return;
        }

        final FiltrosHistorial filtros;

        try {
            filtros = capturarFiltrosHistorial();
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }

        vista.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR
                )
        );

        trabajadorCarga =
                new SwingWorker<>() {

            @Override
            protected DatosCarga doInBackground()
                    throws Exception {

                List<Cliente> clientesCargados =
                        dao.listarClientesActivos();

                List<Producto> productosCargados =
                        dao.listarProductosDisponibles();

                List<Venta> ventasCargadas =
                        dao.listarVentas(
                                filtros.texto(),
                                filtros.desde(),
                                filtros.hasta(),
                                filtros.metodo(),
                                filtros.estado()
                        );

                return new DatosCarga(
                        clientesCargados,
                        productosCargados,
                        ventasCargadas
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    productos =
                            new ArrayList<>(
                                    datos.productos()
                            );

                    ventas =
                            new ArrayList<>(
                                    datos.ventas()
                            );

                    vista.cargarClientes(
                            datos.clientes()
                    );

                    vista.cargarProductos(
                            productos
                    );

                    vista.mostrarVentas(
                            ventas
                    );

                    vista.mostrarCantidadVentas(
                            ventas.size()
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    error(
                            "No fue posible cargar "
                            + "el módulo de ventas.",
                            causa
                    );

                } finally {
                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );
                }
            }
        };

        trabajadorCarga.execute();
    }

    private FiltrosHistorial
            capturarFiltrosHistorial() {

        LocalDate desde =
                vista.getFechaDesdeFiltro();

        LocalDate hasta =
                vista.getFechaHastaFiltro();

        if (desde != null
                && hasta != null
                && desde.isAfter(hasta)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser "
                    + "posterior a la fecha final."
            );
        }

        return new FiltrosHistorial(
                vista.getTextoBusquedaHistorial(),
                desde,
                hasta,
                vista.getMetodoPagoFiltro(),
                vista.getEstadoFiltro()
        );
    }

    public void nuevaVenta(){
        detalles.clear();
        vista.limpiarVenta();
        vista.setNumeroFactura(generarFactura());
        actualizarResumen();
    }

    public void filtrarProductos(){
        vista.cargarProductos(productos);
    }

    public void seleccionarProducto(){
        Producto p=vista.getProductoSeleccionado();
        if(p==null||p.getIdProducto()<=0){ vista.mostrarDatosProducto(BigDecimal.ZERO,0,false); return; }
        vista.mostrarDatosProducto(p.getPrecioVenta(),p.getStockActual(),p.isManejaNumeroSerie());
    }

    public void agregarProducto(){
        try{
            Producto p=vista.getProductoSeleccionado();
            if(p==null||p.getIdProducto()<=0) throw new IllegalArgumentException("Selecciona un producto.");
            if(detalles.stream().anyMatch(d->d.getIdProducto()==p.getIdProducto())) throw new IllegalArgumentException("El producto ya fue agregado. Quítalo para cambiar la cantidad.");
            int cantidad=vista.getCantidadProducto();
            if(cantidad<=0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            if(cantidad>p.getStockActual()) throw new IllegalArgumentException("Stock insuficiente. Disponible: "+p.getStockActual());
            int garantia=vista.getDiasGarantia();
            if(garantia<0) throw new IllegalArgumentException("Los días de garantía no pueden ser negativos.");
            DetalleVenta d=new DetalleVenta(); d.setIdProducto(p.getIdProducto()); d.setCodigoProducto(p.getCodigo()); d.setNombreProducto(p.getNombre()); d.setCantidad(cantidad); d.setPrecioLista(p.getPrecioVenta()); d.setDescuentoUnitario(BigDecimal.ZERO); d.setDiasGarantia(garantia); d.setManejaNumeroSerie(p.isManejaNumeroSerie());
            if(p.isManejaNumeroSerie()){
                List<UnidadProducto> disponibles=dao.listarUnidadesDisponibles(p.getIdProducto());
                if(disponibles.size()<cantidad) throw new IllegalArgumentException("Solo hay "+disponibles.size()+" unidades serializadas disponibles.");
                List<UnidadProducto> sel=vista.solicitarUnidadesProducto(p,disponibles,cantidad);
                if(sel==null) return;
                d.setUnidades(sel);
            }
            detalles.add(d); vista.limpiarProductoSeleccionado(); actualizarResumen();
        }catch(IllegalArgumentException ex){ aviso(ex.getMessage()); }
        catch(SQLException ex){ error("No fue posible consultar las unidades serializadas.",ex); }
    }

    public void eliminarProducto(){
        int fila=vista.getFilaDetalleSeleccionadaModelo();
        if(fila<0||fila>=detalles.size()){ aviso("Selecciona un producto de la tabla."); return; }
        detalles.remove(fila); actualizarResumen();
    }

    public void actualizarDescuento(int fila,String valor){
        try{
            if(fila<0||fila>=detalles.size()) return;
            BigDecimal descuento=vista.convertirDecimal(valor,"descuento por unidad");
            DetalleVenta d=detalles.get(fila);
            if(descuento.compareTo(BigDecimal.ZERO)<0) throw new IllegalArgumentException("El descuento no puede ser negativo.");
            if(descuento.compareTo(d.getPrecioLista())>0) throw new IllegalArgumentException("El descuento no puede superar el precio de lista.");
            if(descuento.compareTo(BigDecimal.ZERO)>0&&!Sesion.esDueno()) throw new IllegalArgumentException("Solo el usuario con rol DUEÑO puede autorizar descuentos.");
            d.setDescuentoUnitario(descuento);
        }catch(IllegalArgumentException ex){ aviso(ex.getMessage()); }
        finally{ actualizarResumen(); }
    }

    public void actualizarPago(){ actualizarResumen(); }

    public void registrarVenta(){
        try{
            if(!Sesion.haySesionActiva()) throw new IllegalStateException("No existe una sesión activa.");
            Cliente c=vista.getClienteSeleccionado();
            if(c==null||c.getIdCliente()<=0) throw new IllegalArgumentException("Selecciona un cliente.");
            if(detalles.isEmpty()) throw new IllegalArgumentException("Agrega al menos un producto.");
            Venta v=construirVenta(c); validar(v);
            if(dao.existeNumeroFactura(v.getNumeroFactura())){ v.setNumeroFactura(generarFactura()); vista.setNumeroFactura(v.getNumeroFactura()); }
            String mensajeConfirmacion;

            if ("CREDITO".equals(v.getTipoVenta())) {
                BigDecimal saldoCredito =
                        v.getTotal()
                                .subtract(v.getMontoPagado())
                                .max(BigDecimal.ZERO);

                mensajeConfirmacion =
                        "Se registrará una venta a crédito por "
                        + vista.formatearMoneda(v.getTotal())
                        + ".\nAbono inicial: "
                        + vista.formatearMoneda(v.getMontoPagado())
                        + ".\nSaldo pendiente: "
                        + vista.formatearMoneda(saldoCredito)
                        + ".\n\nEl cierre diario contará solamente "
                        + "el dinero realmente recibido."
                        + "\n\n¿Deseas continuar?";
            } else {
                mensajeConfirmacion =
                        "Se registrará la venta por "
                        + vista.formatearMoneda(v.getTotal())
                        + ".\nEl inventario se actualizará "
                        + "automáticamente.\n\n¿Deseas continuar?";
            }

            int r=JOptionPane.showConfirmDialog(
                    vista,
                    mensajeConfirmacion,
                    "Confirmar venta",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if(r!=JOptionPane.YES_OPTION) return;
            vista.establecerProcesando(true);
            int id=dao.registrar(v); v.setIdVenta(id);
            JOptionPane.showMessageDialog(vista,"Venta registrada correctamente.\nFactura: "+v.getNumeroFactura(),"Venta completada",JOptionPane.INFORMATION_MESSAGE);
            if(JOptionPane.showConfirmDialog(vista,"¿Deseas ver o imprimir la factura?","Factura",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) vista.mostrarFactura(v,true);
            nuevaVenta(); recargarAsync(); vista.mostrarPestanaHistorial();
        }catch(IllegalArgumentException|IllegalStateException ex){ aviso(ex.getMessage()); }
        catch(SQLException ex){ error("No fue posible registrar la venta.",ex); }
        finally{ vista.establecerProcesando(false); }
    }

    public void buscarVentas() {
        final FiltrosHistorial filtros;

        try {
            filtros = capturarFiltrosHistorial();
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }

        long versionActual =
                ++versionBusqueda;

        if (trabajadorBusqueda != null
                && !trabajadorBusqueda.isDone()) {

            trabajadorBusqueda.cancel(true);
        }

        trabajadorBusqueda =
                new SwingWorker<>() {

            @Override
            protected List<Venta> doInBackground()
                    throws Exception {

                return dao.listarVentas(
                        filtros.texto(),
                        filtros.desde(),
                        filtros.hasta(),
                        filtros.metodo(),
                        filtros.estado()
                );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                           != versionBusqueda) {

                    return;
                }

                try {
                    List<Venta> resultado = get();

                    ventas =
                            new ArrayList<>(
                                    resultado
                            );

                    vista.mostrarVentas(ventas);

                    vista.mostrarCantidadVentas(
                            ventas.size()
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (java.util.concurrent.CancellationException ex) {
                    // La búsqueda fue sustituida por otra más reciente.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    error(
                            "No fue posible consultar "
                            + "las ventas.",
                            causa
                    );
                }
            }
        };

        trabajadorBusqueda.execute();
    }

    public void verDetalleVenta(){ mostrarSeleccionada(false); }
    public void imprimirVenta(){ mostrarSeleccionada(true); }

    private void mostrarSeleccionada(boolean imprimir){
        Venta v=seleccionada(); if(v==null) return;
        try{ Venta completa=dao.obtenerVentaCompleta(v.getIdVenta()); if(completa==null) throw new SQLException("La venta ya no existe."); vista.mostrarFactura(completa,imprimir); }
        catch(SQLException ex){ error("No fue posible cargar la venta.",ex); }
    }

    public void anularVenta(){
        Venta v=seleccionada(); if(v==null) return;
        if("ANULADA".equalsIgnoreCase(v.getEstado())){ aviso("La venta ya está anulada."); return; }
        if(!Sesion.esDueno()){ aviso("Solo el usuario con rol DUEÑO puede anular ventas."); return; }
        int r=JOptionPane.showConfirmDialog(vista,"La venta "+v.getNumeroFactura()+" será anulada y los productos volverán al inventario.\n\n¿Deseas continuar?","Anular venta",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(r!=JOptionPane.YES_OPTION) return;
        try{ dao.anular(v.getIdVenta(),Sesion.getIdUsuario()); JOptionPane.showMessageDialog(vista,"Venta anulada e inventario restaurado.","Venta anulada",JOptionPane.INFORMATION_MESSAGE); recargarAsync(); }
        catch(SQLException ex){ error("No fue posible anular la venta.",ex); }
    }

    private Venta construirVenta(Cliente c){
        Venta v=new Venta(); v.setIdCliente(c.getIdCliente()); v.setNombreCliente(c.getNombreCompleto()); v.setIdUsuario(Sesion.getIdUsuario()); v.setNombreUsuario(Sesion.getNombreCompleto()); v.setNumeroFactura(vista.getNumeroFactura());
        LocalDate fecha=vista.getFechaVenta(); v.setFechaVenta(LocalDateTime.of(fecha,fecha.equals(LocalDate.now())?LocalTime.now().withNano(0):LocalTime.NOON));
        v.setMetodoPago(vista.getMetodoPago()); v.setTipoVenta("CREDITO".equals(v.getMetodoPago())?"CREDITO":"CONTADO"); v.setDetalles(new ArrayList<>(detalles)); v.setObservaciones(vista.getObservaciones()); v.setComprobanteTransferencia(vista.getComprobanteTransferencia());
        if(v.getDescuento().compareTo(BigDecimal.ZERO)>0){ v.setIdUsuarioAutorizaDescuento(Sesion.getIdUsuario()); v.setTipoDescuento("AUTORIZADO"); v.setMotivoDescuento(vista.getMotivoDescuento()); }
        switch(v.getMetodoPago()){
            case "EFECTIVO" -> { BigDecimal recibido=vista.getMontoRecibido(); v.setMontoPagado(recibido); v.setCambio(recibido.subtract(v.getTotal()).max(BigDecimal.ZERO)); }
            case "TRANSFERENCIA","TARJETA" -> { v.setMontoPagado(v.getTotal()); v.setCambio(BigDecimal.ZERO); }
            case "CREDITO" -> {
                BigDecimal abonoInicial =
                        vista.getMontoRecibido();

                v.setMontoPagado(abonoInicial);
                v.setCambio(BigDecimal.ZERO);
                v.setFechaVencimientoCredito(
                        vista.getFechaVencimientoCredito()
                );
                v.setMontoCuotaCredito(
                        vista.getMontoCuotaCredito()
                );
            }
            default -> throw new IllegalArgumentException("Selecciona un método de pago válido.");
        }
        if ("CREDITO".equals(v.getTipoVenta())) {
            v.setEstado(
                    v.getMontoPagado()
                            .compareTo(v.getTotal()) >= 0
                            ? "COMPLETADA"
                            : "PENDIENTE"
            );
        } else {
            v.setEstado("COMPLETADA");
        }
        return v;
    }

    private void validar(Venta v){
        if(v.getFechaVenta().toLocalDate().isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de venta no puede estar en el futuro.");
        if(v.getDescuento().compareTo(BigDecimal.ZERO)>0){
            if(!Sesion.esDueno()) throw new IllegalArgumentException("Solo el DUEÑO puede autorizar descuentos.");
            if(v.getMotivoDescuento()==null||v.getMotivoDescuento().trim().length()<5) throw new IllegalArgumentException("Escribe el motivo del descuento con al menos 5 caracteres.");
        }
        if("EFECTIVO".equals(v.getMetodoPago())&&v.getMontoPagado().compareTo(v.getTotal())<0) throw new IllegalArgumentException("El monto recibido no cubre el total.");
        if("TRANSFERENCIA".equals(v.getMetodoPago()) && (v.getComprobanteTransferencia()==null || v.getComprobanteTransferencia().isBlank())) throw new IllegalArgumentException("Ingresa el comprobante o referencia de la transferencia.");
        if("CREDITO".equals(v.getTipoVenta())){
            if(v.getMontoPagado().compareTo(BigDecimal.ZERO)<0) {
                throw new IllegalArgumentException(
                        "El abono inicial no puede ser negativo."
                );
            }
            if(v.getMontoPagado().compareTo(v.getTotal())>0) {
                throw new IllegalArgumentException(
                        "El abono inicial no puede superar el total de la venta."
                );
            }
            if(v.getFechaVencimientoCredito()!=null&&v.getFechaVencimientoCredito().isBefore(v.getFechaVenta().toLocalDate())) throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la venta.");
            if(v.getMontoCuotaCredito()!=null&&v.getMontoCuotaCredito().compareTo(BigDecimal.ZERO)<=0) throw new IllegalArgumentException("La cuota debe ser mayor que cero.");
        }
    }

    private void actualizarResumen(){
        vista.mostrarDetalles(detalles);
        BigDecimal subtotal=detalles.stream().map(DetalleVenta::getSubtotalBruto).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);
        BigDecimal descuento=detalles.stream().map(DetalleVenta::getDescuentoTotalLinea).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);
        BigDecimal total=subtotal.subtract(descuento).setScale(2,RoundingMode.HALF_UP);
        BigDecimal porcentaje=subtotal.compareTo(BigDecimal.ZERO)==0?BigDecimal.ZERO:descuento.multiply(BigDecimal.valueOf(100)).divide(subtotal,2,RoundingMode.HALF_UP);
        BigDecimal recibido; try{ recibido=vista.getMontoRecibido(); }catch(Exception ex){ recibido=BigDecimal.ZERO; }
        BigDecimal cambio="EFECTIVO".equals(vista.getMetodoPago())?recibido.subtract(total).max(BigDecimal.ZERO):BigDecimal.ZERO;
        vista.actualizarResumen(detalles.size(),detalles.stream().mapToInt(DetalleVenta::getCantidad).sum(),subtotal,descuento,porcentaje,total,cambio);
        vista.configurarDescuento(descuento.compareTo(BigDecimal.ZERO)>0,Sesion.esDueno());
    }

    private Venta seleccionada(){ int f=vista.getFilaVentaSeleccionadaModelo(); if(f<0||f>=ventas.size()){ aviso("Selecciona una venta del historial."); return null; } return ventas.get(f); }
    private String generarFactura(){ return "VTA-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))+"-"+ThreadLocalRandom.current().nextInt(100,1000); }
    private String texto(String s){ return s==null?"":s; }
    private void aviso(String m){ JOptionPane.showMessageDialog(vista,m,"SIGIR",JOptionPane.WARNING_MESSAGE); }
    private void error(String m,Throwable ex){ JOptionPane.showMessageDialog(vista,m+"\n\nDetalle: "+(ex==null?"Error desconocido":ex.getMessage()),"Error de base de datos",JOptionPane.ERROR_MESSAGE); if(ex!=null)ex.printStackTrace(); }
}
