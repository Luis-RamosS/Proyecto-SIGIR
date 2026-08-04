package sigir.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import sigir.componentes.BuscadorSugerencias;
import sigir.controlador.VentaControlador;
import sigir.modelo.*;
import sigir.util.FacturaVentaUtil;
import sigir.util.FiltroTiempoReal;
import sigir.util.Sesion;

public class VentasPanel extends javax.swing.JPanel {
    private static final DateTimeFormatter FORMATO_FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moneda=NumberFormat.getCurrencyInstance(new Locale("es","HN"));
    private final VentaControlador controlador;
    private boolean actualizandoDetalle;
    private BuscadorSugerencias<Cliente> buscadorClientes;
    private BuscadorSugerencias<Producto> buscadorProductos;
    private Cliente clienteSeleccionado;
    private Producto productoSeleccionado;
    private boolean iniciado;

    public VentasPanel(){
        initComponents();
        controlador=new VentaControlador(this);
        configurarComponentes();
        aplicarEstilos();
        configurarBuscadores();
        configurarEventos();
        FiltroTiempoReal.activar(
                txtBuscarHistorial,
                controlador::buscarVentas
        );
        
    }

    public void activar() {

        if (!iniciado) {
            iniciado = true;
            controlador.iniciarAsync();
            return;
        }

        controlador.recargarSiNecesario();
    }

    public void recargar() {
        controlador.recargarAsync();
    }
    
    private void configurarComponentes(){
        txtNumeroFactura.setEditable(false); txtUsuario.setEditable(false); txtPrecioVenta.setEditable(false); txtStockDisponible.setEditable(false); txtCambio.setEditable(false); txtDescuentoPorcentaje.setEditable(false);
        cmbMetodoPago.setModel(new DefaultComboBoxModel<>(new String[]{"EFECTIVO","TRANSFERENCIA","TARJETA","CREDITO"}));
        cmbMetodoFiltro.setModel(new DefaultComboBoxModel<>(new String[]{"TODOS","EFECTIVO","TRANSFERENCIA","TARJETA","CREDITO"}));
        cmbEstadoFiltro.setModel(new DefaultComboBoxModel<>(new String[]{"TODOS","COMPLETADA","PENDIENTE","ANULADA"}));
        txtFechaVenta.setText(LocalDate.now().format(FORMATO_FECHA)); txtFechaDesde.setText(LocalDate.now().minusMonths(1).format(FORMATO_FECHA)); txtFechaHasta.setText(LocalDate.now().format(FORMATO_FECHA));
        txtUsuario.setText(Sesion.haySesionActiva()?Sesion.getNombreCompleto():"");
        tblDetalleVenta.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tblHistorialVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tblHistorialVentas.setAutoCreateRowSorter(true);
        configurarMetodoPago();
    }


    private void configurarBuscadores() {
        buscadorClientes =
                new BuscadorSugerencias<>(
                        txtBuscarCliente,
                        this::textoVisibleCliente,
                        this::textoBusquedaCliente,
                        cliente ->
                                clienteSeleccionado = cliente
                );

        buscadorProductos =
                new BuscadorSugerencias<>(
                        txtBuscarProducto,
                        this::textoVisibleProducto,
                        this::textoBusquedaProducto,
                        producto -> {
                            productoSeleccionado = producto;
                            controlador.seleccionarProducto();
                        }
                );

        txtBuscarCliente.setToolTipText(
                "Escribe el nombre, identidad, teléfono o correo."
        );

        txtBuscarProducto.setToolTipText(
                "Escribe el código, nombre, marca o modelo."
        );
    }

    private String textoVisibleCliente(Cliente cliente) {
        if (cliente == null) {
            return "";
        }

        String nombre =
                textoSeguro(
                        cliente.getNombreCompleto()
                );

        String identidad =
                textoSeguro(
                        cliente.getNumeroIdentidad()
                );

        return identidad.isBlank()
                ? nombre
                : nombre + " — " + identidad;
    }

    private String textoBusquedaCliente(Cliente cliente) {
        if (cliente == null) {
            return "";
        }

        return textoSeguro(cliente.getNombreCompleto())
                + " "
                + textoSeguro(cliente.getNumeroIdentidad())
                + " "
                + textoSeguro(cliente.getTelefono())
                + " "
                + textoSeguro(cliente.getCorreo());
    }

    private String textoVisibleProducto(Producto producto) {
        if (producto == null) {
            return "";
        }

        return textoSeguro(producto.getCodigo())
                + " — "
                + textoSeguro(producto.getNombre())
                + " | Stock: "
                + producto.getStockActual();
    }

    private String textoBusquedaProducto(Producto producto) {
        if (producto == null) {
            return "";
        }

        return textoSeguro(producto.getCodigo())
                + " "
                + textoSeguro(producto.getNombre())
                + " "
                + textoSeguro(producto.getDescripcion())
                + " "
                + textoSeguro(producto.getMarca())
                + " "
                + textoSeguro(producto.getModelo())
                + " "
                + textoSeguro(producto.getNombreCategoria());
    }

    private void aplicarEstilos(){
        Color borde=new Color(220,227,236), azul=new Color(49,105,181), texto=new Color(24,50,87);
        for(JPanel p:new JPanel[]{pnlInformacionVenta,pnlResumenVenta,pnlAgregarProducto,pnlDetalleVenta,pnlFiltrosHistorial,pnlTablaHistorial}) p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borde),BorderFactory.createEmptyBorder(7,7,7,7)));
        for(JTextField c:new JTextField[]{txtNumeroFactura,txtFechaVenta,txtUsuario,txtMontoRecibido,txtCambio,txtFechaVencimiento,txtMontoCuota,txtObservaciones,txtBuscarCliente,txtBuscarProducto,txtCantidad,txtPrecioVenta,txtStockDisponible,txtDiasGarantia,txtDescuentoPorcentaje,txtMotivoDescuento,txtBuscarHistorial,txtFechaDesde,txtFechaHasta}) c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,216,229)),BorderFactory.createEmptyBorder(0,8,0,8)));
        for(JButton b:new JButton[]{btnAgregarProducto,btnGuardarVenta}){ b.setBackground(azul); b.setForeground(Color.WHITE); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        for(JButton b:new JButton[]{btnQuitarProducto,btnNuevaVenta,btnActualizarHistorial,btnVerDetalle,btnImprimirFactura,btnAnularVenta}){ b.setBackground(Color.WHITE); b.setForeground(texto); b.setBorder(BorderFactory.createLineBorder(borde)); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        btnAnularVenta.setForeground(new Color(192,52,52)); estilizarTabla(tblDetalleVenta); estilizarTabla(tblHistorialVentas);
    }

    private void estilizarTabla(JTable t){ t.setRowHeight(38); t.setShowVerticalLines(false); t.setGridColor(new Color(232,237,243)); t.setSelectionBackground(new Color(229,239,252)); JTableHeader h=t.getTableHeader(); h.setBackground(new Color(248,250,253)); h.setForeground(new Color(34,59,94)); h.setFont(new Font("Segoe UI",Font.BOLD,12)); h.setReorderingAllowed(false); }

    private void configurarEventos(){
        btnAgregarProducto.addActionListener(
                e -> controlador.agregarProducto()
        );
        btnQuitarProducto.addActionListener(
                e -> controlador.eliminarProducto()
        );
        btnNuevaVenta.addActionListener(
                e -> controlador.nuevaVenta()
        );
        btnGuardarVenta.addActionListener(
                e -> controlador.registrarVenta()
        );
        cmbMetodoPago.addActionListener(e->{ configurarMetodoPago(); controlador.actualizarPago(); });
        txtMontoRecibido.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){ public void insertUpdate(javax.swing.event.DocumentEvent e){controlador.actualizarPago();} public void removeUpdate(javax.swing.event.DocumentEvent e){controlador.actualizarPago();} public void changedUpdate(javax.swing.event.DocumentEvent e){controlador.actualizarPago();} });
        cmbMetodoFiltro.addActionListener(e->{if(cmbMetodoFiltro.getItemCount()>0)controlador.buscarVentas();}); cmbEstadoFiltro.addActionListener(e->{if(cmbEstadoFiltro.getItemCount()>0)controlador.buscarVentas();});
        btnActualizarHistorial.addActionListener(e -> controlador.recargarAsync()); btnVerDetalle.addActionListener(e->controlador.verDetalleVenta()); btnImprimirFactura.addActionListener(e->controlador.imprimirVenta()); btnAnularVenta.addActionListener(e->controlador.anularVenta());
    }

    private void configurarMetodoPago(){
        String m=getMetodoPago(); boolean efectivo="EFECTIVO".equals(m), credito="CREDITO".equals(m);
        txtMontoRecibido.setEnabled(efectivo); if(!efectivo)txtMontoRecibido.setText("0.00");
        txtFechaVencimiento.setEnabled(credito); txtMontoCuota.setEnabled(credito); lblFechaVencimiento.setEnabled(credito); lblMontoCuota.setEnabled(credito);
        if(!credito){txtFechaVencimiento.setText("");txtMontoCuota.setText("");}
    }

    public void cargarClientes(List<Cliente> clientes) {
        int idSeleccionado =
                clienteSeleccionado == null
                        ? 0
                        : clienteSeleccionado.getIdCliente();

        buscadorClientes.setElementos(clientes);

        if (idSeleccionado > 0) {
            clientes.stream()
                    .filter(cliente ->
                            cliente.getIdCliente()
                            == idSeleccionado
                    )
                    .findFirst()
                    .ifPresent(
                            buscadorClientes::seleccionar
                    );
        }
    }

    public void cargarProductos(List<Producto> productos) {
        int idSeleccionado =
                productoSeleccionado == null
                        ? 0
                        : productoSeleccionado.getIdProducto();

        buscadorProductos.setElementos(productos);

        if (idSeleccionado > 0) {
            productos.stream()
                    .filter(producto ->
                            producto.getIdProducto()
                            == idSeleccionado
                    )
                    .findFirst()
                    .ifPresent(
                            buscadorProductos::seleccionar
                    );
        }
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public String getTextoBusquedaProducto() {
        return txtBuscarProducto.getText().trim();
    }
    public int getCantidadProducto(){ return entero(txtCantidad.getText(),"cantidad"); }
    public int getDiasGarantia(){ return entero(txtDiasGarantia.getText(),"días de garantía"); }

    public void mostrarDatosProducto(BigDecimal precio,int stock,boolean serie){ txtPrecioVenta.setText(precio==null?"0.00":precio.setScale(2,RoundingMode.HALF_UP).toPlainString()); txtStockDisponible.setText(String.valueOf(stock)); lblAvisoSerie.setText(serie?"Este producto requiere seleccionar las unidades con número de serie.":"El producto no requiere número de serie."); }

    public List<UnidadProducto> solicitarUnidadesProducto(Producto p,List<UnidadProducto> disponibles,int cantidad){
        JList<UnidadProducto> lista=new JList<>(disponibles.toArray(UnidadProducto[]::new)); lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); JScrollPane sp=new JScrollPane(lista); JPanel panel=new JPanel(new BorderLayout(0,8)); panel.add(new JLabel("<html>Selecciona exactamente <b>"+cantidad+"</b> unidades de <b>"+p.getNombre()+"</b>.</html>"),BorderLayout.NORTH); panel.add(sp,BorderLayout.CENTER);
        while(true){ int r=JOptionPane.showConfirmDialog(this,panel,"Seleccionar números de serie",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE); if(r!=JOptionPane.OK_OPTION)return null; List<UnidadProducto> s=lista.getSelectedValuesList(); if(s.size()==cantidad)return s; JOptionPane.showMessageDialog(this,"Debes seleccionar exactamente "+cantidad+" unidades.","Cantidad incorrecta",JOptionPane.WARNING_MESSAGE); }
    }

    public void mostrarDetalles(List<DetalleVenta> detalles){
        actualizandoDetalle=true;
        DefaultTableModel m=new DefaultTableModel(new String[]{"Código","Producto","Cantidad","Precio lista","Descuento unit. (L)","Precio final","Subtotal","Series"},0){ @Override public boolean isCellEditable(int r,int c){return c==4;} @Override public Class<?> getColumnClass(int c){return c==2?Integer.class:String.class;} };
        for(DetalleVenta d:detalles)m.addRow(new Object[]{d.getCodigoProducto(),d.getNombreProducto(),d.getCantidad(),formatearMoneda(d.getPrecioLista()),d.getDescuentoUnitario().setScale(2,RoundingMode.HALF_UP).toPlainString(),formatearMoneda(d.getPrecioUnitario()),formatearMoneda(d.getSubtotal()),d.getResumenSeries()});
        m.addTableModelListener(e->{ if(actualizandoDetalle||e.getType()!=TableModelEvent.UPDATE||e.getColumn()!=4)return; int fila=e.getFirstRow(); Object v=m.getValueAt(fila,4); SwingUtilities.invokeLater(()->controlador.actualizarDescuento(fila,v==null?"":v.toString())); });
        tblDetalleVenta.setModel(m); actualizandoDetalle=false; estilizarTabla(tblDetalleVenta);
    }

    public int getFilaDetalleSeleccionadaModelo(){ int f=tblDetalleVenta.getSelectedRow(); return f<0?-1:tblDetalleVenta.convertRowIndexToModel(f); }
    public void actualizarResumen(int productos,int unidades,BigDecimal subtotal,BigDecimal descuento,BigDecimal porcentaje,BigDecimal total,BigDecimal cambio){ lblProductosValor.setText(String.valueOf(productos)); lblUnidadesValor.setText(String.valueOf(unidades)); lblSubtotalValor.setText(formatearMoneda(subtotal)); txtDescuentoPorcentaje.setText(porcentaje.setScale(2,RoundingMode.HALF_UP).toPlainString()+" %"); lblDescuentoMontoValor.setText(formatearMoneda(descuento)); lblTotalValor.setText(formatearMoneda(total)); txtCambio.setText(cambio.setScale(2,RoundingMode.HALF_UP).toPlainString()); }
    public void configurarDescuento(boolean hay,boolean puede){ txtMotivoDescuento.setEnabled(hay&&puede); lblEstadoDescuento.setText(!hay?"Sin descuento aplicado":puede?"Descuento autorizado por el DUEÑO actual":"Requiere autorización del DUEÑO"); if(!hay)txtMotivoDescuento.setText(""); }

    public String getNumeroFactura(){return txtNumeroFactura.getText().trim();} public void setNumeroFactura(String n){txtNumeroFactura.setText(n);} public LocalDate getFechaVenta(){return fechaObligatoria(txtFechaVenta.getText(),"fecha de venta");} public String getMetodoPago(){Object v=cmbMetodoPago.getSelectedItem();return v==null?"":v.toString();} public BigDecimal getMontoRecibido(){return convertirDecimal(txtMontoRecibido.getText(),"monto recibido");} public String getObservaciones(){return opcional(txtObservaciones.getText());} public String getMotivoDescuento(){return opcional(txtMotivoDescuento.getText());} public LocalDate getFechaVencimientoCredito(){return fechaOpcional(txtFechaVencimiento.getText(),"fecha de vencimiento");} public BigDecimal getMontoCuotaCredito(){String v=txtMontoCuota.getText().trim();return v.isBlank()?null:convertirDecimal(v,"monto de cuota");}

    public void limpiarVenta() {
        clienteSeleccionado = null;
        buscadorClientes.limpiar();

        txtFechaVenta.setText(
                LocalDate.now().format(FORMATO_FECHA)
        );

        txtUsuario.setText(
                Sesion.haySesionActiva()
                        ? Sesion.getNombreCompleto()
                        : ""
        );

        cmbMetodoPago.setSelectedItem("EFECTIVO");
        txtMontoRecibido.setText("0.00");
        txtCambio.setText("0.00");
        txtObservaciones.setText("");
        txtMotivoDescuento.setText("");
        txtFechaVencimiento.setText("");
        txtMontoCuota.setText("");

        limpiarProductoSeleccionado();
        tabsVentas.setSelectedIndex(0);
        configurarMetodoPago();
    }

    public void limpiarProductoSeleccionado() {
        productoSeleccionado = null;
        buscadorProductos.limpiar();

        txtCantidad.setText("1");
        txtPrecioVenta.setText("0.00");
        txtStockDisponible.setText("0");
        txtDiasGarantia.setText("0");

        lblAvisoSerie.setText(
                "Empieza a escribir para buscar un producto."
        );
    }
    public void establecerProcesando(boolean p){
        btnGuardarVenta.setEnabled(!p);
        btnAgregarProducto.setEnabled(!p);
        btnQuitarProducto.setEnabled(!p);
        btnNuevaVenta.setEnabled(!p);
        txtBuscarCliente.setEnabled(!p);
        txtBuscarProducto.setEnabled(!p);
        btnGuardarVenta.setText(
                p ? "Guardando..." : "Guardar / Cobrar"
        );
    }

    public String getTextoBusquedaHistorial(){return txtBuscarHistorial.getText().trim();} public LocalDate getFechaDesdeFiltro(){return fechaOpcional(txtFechaDesde.getText(),"fecha inicial");} public LocalDate getFechaHastaFiltro(){return fechaOpcional(txtFechaHasta.getText(),"fecha final");} public String getMetodoPagoFiltro(){Object v=cmbMetodoFiltro.getSelectedItem();return v==null?"TODOS":v.toString();} public String getEstadoFiltro(){Object v=cmbEstadoFiltro.getSelectedItem();return v==null?"TODOS":v.toString();}

    public void mostrarVentas(List<Venta> ventas){ DefaultTableModel m=new DefaultTableModel(new String[]{"ID","Factura","Fecha","Cliente","Vendedor","Método","Descuento","Total","Estado"},0){@Override public boolean isCellEditable(int r,int c){return false;} @Override public Class<?> getColumnClass(int c){return c==0?Integer.class:String.class;}}; DateTimeFormatter f=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); for(Venta v:ventas)m.addRow(new Object[]{v.getIdVenta(),v.getNumeroFactura(),v.getFechaVenta()==null?"":v.getFechaVenta().format(f),v.getNombreCliente(),v.getNombreUsuario(),v.getMetodoPago(),formatearMoneda(v.getDescuento()),formatearMoneda(v.getTotal()),v.getEstado()}); tblHistorialVentas.setModel(m); estilizarTabla(tblHistorialVentas); }
    public int getFilaVentaSeleccionadaModelo(){int f=tblHistorialVentas.getSelectedRow();return f<0?-1:tblHistorialVentas.convertRowIndexToModel(f);} public void mostrarCantidadVentas(int c){lblCantidadVentas.setText(c==1?"Mostrando 1 venta":"Mostrando "+c+" ventas");} public void mostrarPestanaHistorial(){tabsVentas.setSelectedIndex(1);}

    public void mostrarFactura(Venta venta,boolean imprimir){ JTextArea a=new JTextArea(FacturaVentaUtil.generarTexto(venta),28,58); a.setEditable(false); a.setFont(new Font("Consolas",Font.PLAIN,13)); JScrollPane s=new JScrollPane(a); if(!imprimir){JOptionPane.showMessageDialog(this,s,"Detalle de venta",JOptionPane.INFORMATION_MESSAGE);return;} Object[] op={"Imprimir","Cerrar"}; if(JOptionPane.showOptionDialog(this,s,"Factura",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,op,op[0])==0)try{a.print();}catch(PrinterException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error de impresión",JOptionPane.ERROR_MESSAGE);} }

    public String formatearMoneda(BigDecimal v){return moneda.format(v==null?BigDecimal.ZERO:v);} public BigDecimal convertirDecimal(String t,String campo){String v=t==null?"":t.trim().replace("L","").replace("%","").replace(",","");if(v.isBlank())return BigDecimal.ZERO;try{return new BigDecimal(v).setScale(2,RoundingMode.HALF_UP);}catch(NumberFormatException ex){throw new IllegalArgumentException("El "+campo+" debe ser un número válido.");}} private int entero(String t,String campo){try{return Integer.parseInt(t==null?"":t.trim());}catch(NumberFormatException ex){throw new IllegalArgumentException("La "+campo+" debe ser un número entero.");}} private LocalDate fechaObligatoria(String t,String c){LocalDate f=fechaOpcional(t,c);if(f==null)throw new IllegalArgumentException("Ingresa la "+c+" en formato dd/MM/yyyy.");return f;} private LocalDate fechaOpcional(String t,String c){if(t==null||t.trim().isEmpty())return null;try{return LocalDate.parse(t.trim(),FORMATO_FECHA);}catch(DateTimeParseException ex){throw new IllegalArgumentException("La "+c+" debe tener formato dd/MM/yyyy.");}} private String opcional(String v){return v==null||v.trim().isEmpty()?null:v.trim();}

    private String textoSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents(){
        pnlEncabezado=new JPanel(null); lblTitulo=new JLabel("Facturación / Ventas"); lblSubtitulo=new JLabel("Registra ventas, descuentos por producto y genera la factura."); tabsVentas=new JTabbedPane(); pnlNuevaVenta=new JPanel(null); pnlHistorial=new JPanel(null);
        pnlInformacionVenta=new JPanel(null); pnlResumenVenta=new JPanel(null); pnlAgregarProducto=new JPanel(null); pnlDetalleVenta=new JPanel(null); pnlFiltrosHistorial=new JPanel(null); pnlTablaHistorial=new JPanel(null);
        lblTituloInformacion=new JLabel("Información de la venta"); lblFactura=new JLabel("Número de factura"); txtNumeroFactura=new JTextField(); lblFechaVenta=new JLabel("Fecha"); txtFechaVenta=new JTextField(); lblCliente=new JLabel("Buscar cliente"); txtBuscarCliente=new JTextField(); lblUsuario=new JLabel("Vendedor"); txtUsuario=new JTextField(); lblMetodoPago=new JLabel("Método de pago"); cmbMetodoPago=new JComboBox<>(); lblMontoRecibido=new JLabel("Monto recibido"); txtMontoRecibido=new JTextField("0.00"); lblCambio=new JLabel("Cambio"); txtCambio=new JTextField("0.00"); lblFechaVencimiento=new JLabel("Vencimiento"); txtFechaVencimiento=new JTextField(); lblMontoCuota=new JLabel("Monto cuota"); txtMontoCuota=new JTextField(); lblObservaciones=new JLabel("Observaciones"); txtObservaciones=new JTextField();
        lblTituloResumen=new JLabel("Resumen de la venta"); lblProductos=new JLabel("Productos"); lblProductosValor=new JLabel("0",SwingConstants.RIGHT); lblUnidades=new JLabel("Unidades"); lblUnidadesValor=new JLabel("0",SwingConstants.RIGHT); lblSubtotal=new JLabel("Subtotal"); lblSubtotalValor=new JLabel("L 0.00",SwingConstants.RIGHT); lblDescuentoPorcentaje=new JLabel("Descuento total (%)"); txtDescuentoPorcentaje=new JTextField("0.00 %"); lblDescuentoMonto=new JLabel("Descuento total"); lblDescuentoMontoValor=new JLabel("L 0.00",SwingConstants.RIGHT); lblTotal=new JLabel("TOTAL"); lblTotalValor=new JLabel("L 0.00",SwingConstants.RIGHT); lblMotivoDescuento=new JLabel("Motivo del descuento"); txtMotivoDescuento=new JTextField(); lblEstadoDescuento=new JLabel("Sin descuento aplicado");
        lblTituloAgregarProducto=new JLabel("Agregar producto"); lblBuscarProducto=new JLabel("Buscar producto"); txtBuscarProducto=new JTextField(); lblCantidad=new JLabel("Cantidad"); txtCantidad=new JTextField("1"); lblPrecioVenta=new JLabel("Precio"); txtPrecioVenta=new JTextField("0.00"); lblStockDisponible=new JLabel("Stock"); txtStockDisponible=new JTextField("0"); lblDiasGarantia=new JLabel("Garantía (días)"); txtDiasGarantia=new JTextField("0"); btnAgregarProducto=new JButton("+ Agregar"); lblAvisoSerie=new JLabel("Selecciona un producto para ver sus datos.");
        lblTituloDetalle=new JLabel("Detalle de productos"); tblDetalleVenta=new JTable(); scrollDetalleVenta=new JScrollPane(tblDetalleVenta); btnQuitarProducto=new JButton("Quitar producto"); btnNuevaVenta=new JButton("Nueva venta"); btnGuardarVenta=new JButton("Guardar / Cobrar");
        lblTituloFiltros=new JLabel("Filtros de ventas"); txtBuscarHistorial=new JTextField(); lblDesde=new JLabel("Desde"); txtFechaDesde=new JTextField(); lblHasta=new JLabel("Hasta"); txtFechaHasta=new JTextField(); cmbMetodoFiltro=new JComboBox<>(); cmbEstadoFiltro=new JComboBox<>(); btnActualizarHistorial=new JButton("Actualizar"); lblTituloHistorial=new JLabel("Historial de ventas"); tblHistorialVentas=new JTable(); scrollHistorialVentas=new JScrollPane(tblHistorialVentas); lblCantidadVentas=new JLabel("Mostrando 0 ventas"); btnVerDetalle=new JButton("Ver detalle"); btnImprimirFactura=new JButton("Imprimir"); btnAnularVenta=new JButton("Anular venta");
        setBackground(new Color(247,249,252)); setPreferredSize(new java.awt.Dimension(1180,760)); setLayout(null);
        pnlEncabezado.setBackground(new Color(247,249,252)); lblTitulo.setFont(new Font("Segoe UI",Font.BOLD,28)); lblTitulo.setForeground(new Color(24,50,87)); lblSubtitulo.setFont(new Font("Segoe UI",Font.PLAIN,14)); lblSubtitulo.setForeground(new Color(98,124,159)); pnlEncabezado.add(lblTitulo); lblTitulo.setBounds(0,4,420,40); pnlEncabezado.add(lblSubtitulo); lblSubtitulo.setBounds(0,45,700,24); add(pnlEncabezado); pnlEncabezado.setBounds(28,10,1110,76);
        for(JPanel p:new JPanel[]{pnlNuevaVenta,pnlHistorial})p.setBackground(new Color(247,249,252));
        for(JPanel p:new JPanel[]{pnlInformacionVenta,pnlResumenVenta,pnlAgregarProducto,pnlDetalleVenta,pnlFiltrosHistorial,pnlTablaHistorial})p.setBackground(Color.WHITE);
        addLabel(pnlInformacionVenta,lblTituloInformacion,16,8,250,25,16,true); addLabel(pnlInformacionVenta,lblFactura,16,38,130,18,11,true); addField(pnlInformacionVenta,txtNumeroFactura,16,57,220,32); addLabel(pnlInformacionVenta,lblFechaVenta,248,38,70,18,11,true); addField(pnlInformacionVenta,txtFechaVenta,248,57,120,32); addLabel(pnlInformacionVenta,lblCliente,380,38,130,18,11,true); addField(pnlInformacionVenta,txtBuscarCliente,380,57,280,32);
        addLabel(pnlInformacionVenta,lblUsuario,16,96,100,18,11,true);addField(pnlInformacionVenta,txtUsuario,16,115,220,32);addLabel(pnlInformacionVenta,lblMetodoPago,248,96,110,18,11,true);pnlInformacionVenta.add(cmbMetodoPago);cmbMetodoPago.setBounds(248,115,160,32);addLabel(pnlInformacionVenta,lblMontoRecibido,420,96,110,18,11,true);addField(pnlInformacionVenta,txtMontoRecibido,420,115,105,32);addLabel(pnlInformacionVenta,lblCambio,537,96,70,18,11,true);addField(pnlInformacionVenta,txtCambio,537,115,123,32);
        addLabel(pnlInformacionVenta,lblFechaVencimiento,16,155,100,18,11,true);addField(pnlInformacionVenta,txtFechaVencimiento,16,174,140,31);addLabel(pnlInformacionVenta,lblMontoCuota,168,155,100,18,11,true);addField(pnlInformacionVenta,txtMontoCuota,168,174,120,31);addLabel(pnlInformacionVenta,lblObservaciones,300,155,100,18,11,true);addField(pnlInformacionVenta,txtObservaciones,300,174,360,31);pnlNuevaVenta.add(pnlInformacionVenta);pnlInformacionVenta.setBounds(0,8,685,220);
        addLabel(pnlResumenVenta,lblTituloResumen,16,8,220,25,16,true);addLabel(pnlResumenVenta,lblProductos,16,40,100,20,12,false);addLabel(pnlResumenVenta,lblProductosValor,230,38,90,24,18,true);addLabel(pnlResumenVenta,lblUnidades,16,66,100,20,12,false);addLabel(pnlResumenVenta,lblUnidadesValor,230,64,90,24,18,true);addLabel(pnlResumenVenta,lblSubtotal,16,93,100,20,12,false);addLabel(pnlResumenVenta,lblSubtotalValor,160,91,160,24,14,true);addLabel(pnlResumenVenta,lblDescuentoPorcentaje,16,120,145,20,12,false);addField(pnlResumenVenta,txtDescuentoPorcentaje,200,116,120,29);addLabel(pnlResumenVenta,lblDescuentoMonto,16,150,130,20,12,false);addLabel(pnlResumenVenta,lblDescuentoMontoValor,160,148,160,24,13,true);addLabel(pnlResumenVenta,lblTotal,16,178,80,22,14,true);addLabel(pnlResumenVenta,lblTotalValor,125,173,195,32,22,true);addLabel(pnlResumenVenta,lblMotivoDescuento,16,207,140,18,10,true);addField(pnlResumenVenta,txtMotivoDescuento,160,204,160,30);addLabel(pnlResumenVenta,lblEstadoDescuento,16,232,300,16,9,false);pnlNuevaVenta.add(pnlResumenVenta);pnlResumenVenta.setBounds(699,8,350,250);
        addLabel(pnlAgregarProducto,lblTituloAgregarProducto,16,7,200,25,16,true);addLabel(pnlAgregarProducto,lblBuscarProducto,16,38,120,16,10,true);addField(pnlAgregarProducto,txtBuscarProducto,16,55,457,32);addLabel(pnlAgregarProducto,lblCantidad,485,38,65,16,10,true);addField(pnlAgregarProducto,txtCantidad,485,55,65,32);addLabel(pnlAgregarProducto,lblPrecioVenta,562,38,60,16,10,true);addField(pnlAgregarProducto,txtPrecioVenta,562,55,95,32);addLabel(pnlAgregarProducto,lblStockDisponible,669,38,60,16,10,true);addField(pnlAgregarProducto,txtStockDisponible,669,55,65,32);addLabel(pnlAgregarProducto,lblDiasGarantia,746,38,95,16,10,true);addField(pnlAgregarProducto,txtDiasGarantia,746,55,85,32);pnlAgregarProducto.add(btnAgregarProducto);btnAgregarProducto.setBounds(846,49,180,38);addLabel(pnlAgregarProducto,lblAvisoSerie,16,91,800,17,10,false);pnlNuevaVenta.add(pnlAgregarProducto);pnlAgregarProducto.setBounds(0,270,1049,115);
        addLabel(pnlDetalleVenta,lblTituloDetalle,16,7,220,25,16,true);pnlDetalleVenta.add(scrollDetalleVenta);scrollDetalleVenta.setBounds(0,37,1049,155);pnlDetalleVenta.add(btnQuitarProducto);btnQuitarProducto.setBounds(16,202,145,36);pnlDetalleVenta.add(btnNuevaVenta);btnNuevaVenta.setBounds(700,202,140,36);pnlDetalleVenta.add(btnGuardarVenta);btnGuardarVenta.setBounds(852,202,180,36);pnlNuevaVenta.add(pnlDetalleVenta);pnlDetalleVenta.setBounds(0,397,1049,248);
        addLabel(pnlFiltrosHistorial,lblTituloFiltros,16,8,200,25,16,true);addField(pnlFiltrosHistorial,txtBuscarHistorial,16,43,250,34);addLabel(pnlFiltrosHistorial,lblDesde,278,36,60,15,10,true);addField(pnlFiltrosHistorial,txtFechaDesde,278,52,120,30);addLabel(pnlFiltrosHistorial,lblHasta,410,36,60,15,10,true);addField(pnlFiltrosHistorial,txtFechaHasta,410,52,120,30);pnlFiltrosHistorial.add(cmbMetodoFiltro);cmbMetodoFiltro.setBounds(542,43,155,34);pnlFiltrosHistorial.add(cmbEstadoFiltro);cmbEstadoFiltro.setBounds(709,43,145,34);pnlFiltrosHistorial.add(btnActualizarHistorial);btnActualizarHistorial.setBounds(866,43,110,34);pnlHistorial.add(pnlFiltrosHistorial);pnlFiltrosHistorial.setBounds(0,8,1049,95);
        addLabel(pnlTablaHistorial,lblTituloHistorial,16,8,210,25,16,true);pnlTablaHistorial.add(scrollHistorialVentas);scrollHistorialVentas.setBounds(0,37,1049,475);addLabel(pnlTablaHistorial,lblCantidadVentas,16,518,260,22,11,false);pnlTablaHistorial.add(btnVerDetalle);btnVerDetalle.setBounds(610,514,120,36);pnlTablaHistorial.add(btnImprimirFactura);btnImprimirFactura.setBounds(742,514,115,36);pnlTablaHistorial.add(btnAnularVenta);btnAnularVenta.setBounds(869,514,145,36);pnlHistorial.add(pnlTablaHistorial);pnlTablaHistorial.setBounds(0,115,1049,560);
        tabsVentas.addTab("Nueva venta",pnlNuevaVenta);tabsVentas.addTab("Historial",pnlHistorial);add(tabsVentas);tabsVentas.setBounds(28,86,1070,700);
    }// </editor-fold>                        

    private void addLabel(JPanel p,JLabel l,int x,int y,int w,int h,int size,boolean bold){l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(new Color(38,64,99));p.add(l);l.setBounds(x,y,w,h);} private void addField(JPanel p,JTextField f,int x,int y,int w,int h){f.setFont(new Font("Segoe UI",Font.PLAIN,12));p.add(f);f.setBounds(x,y,w,h);}

    // Variables declaration - do not modify                     
    private JPanel pnlEncabezado,pnlNuevaVenta,pnlHistorial,pnlInformacionVenta,pnlResumenVenta,pnlAgregarProducto,pnlDetalleVenta,pnlFiltrosHistorial,pnlTablaHistorial;
    private JTabbedPane tabsVentas; private JLabel lblTitulo,lblSubtitulo,lblTituloInformacion,lblFactura,lblFechaVenta,lblCliente,lblUsuario,lblMetodoPago,lblMontoRecibido,lblCambio,lblFechaVencimiento,lblMontoCuota,lblObservaciones,lblTituloResumen,lblProductos,lblProductosValor,lblUnidades,lblUnidadesValor,lblSubtotal,lblSubtotalValor,lblDescuentoPorcentaje,lblDescuentoMonto,lblDescuentoMontoValor,lblTotal,lblTotalValor,lblMotivoDescuento,lblEstadoDescuento,lblTituloAgregarProducto,lblBuscarProducto,lblCantidad,lblPrecioVenta,lblStockDisponible,lblDiasGarantia,lblAvisoSerie,lblTituloDetalle,lblTituloFiltros,lblDesde,lblHasta,lblTituloHistorial,lblCantidadVentas;
    private JTextField txtNumeroFactura,txtFechaVenta,txtUsuario,txtMontoRecibido,txtCambio,txtFechaVencimiento,txtMontoCuota,txtObservaciones,txtDescuentoPorcentaje,txtMotivoDescuento,txtBuscarCliente,txtBuscarProducto,txtCantidad,txtPrecioVenta,txtStockDisponible,txtDiasGarantia,txtBuscarHistorial,txtFechaDesde,txtFechaHasta; private JComboBox<String> cmbMetodoPago,cmbMetodoFiltro,cmbEstadoFiltro; private JButton btnAgregarProducto,btnQuitarProducto,btnNuevaVenta,btnGuardarVenta,btnActualizarHistorial,btnVerDetalle,btnImprimirFactura,btnAnularVenta; private JTable tblDetalleVenta,tblHistorialVentas; private JScrollPane scrollDetalleVenta,scrollHistorialVentas;
    // End of variables declaration                   
}
