package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.componentes.BuscadorSugerencias;
import sigir.controlador.ReparacionControlador;
import sigir.modelo.*;
import sigir.util.FiltroTiempoReal;
import sigir.util.Sesion;
import sigir.util.CampoSeleccionUtil;
import sigir.util.SelectorFechaUtil;

public class ReparacionesPanel extends JPanel {
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moneda=NumberFormat.getCurrencyInstance(new Locale("es","HN"));
    private final ReparacionControlador controlador;
    private BuscadorSugerencias<Cliente> buscadorClientes;
    private BuscadorSugerencias<Producto> buscadorProductos;
    private Cliente clienteNuevaOrden;
    private Producto productoRepuesto;
    private boolean iniciado;
    private boolean actualizandoControles;

    public ReparacionesPanel(){
        initComponents();configurarComponentes();aplicarEstilos();
        controlador=new ReparacionControlador(this);
        configurarBuscadores();configurarEventos();
        FiltroTiempoReal.activar(txtBuscarOrden,controlador::buscarOrdenes);
        FiltroTiempoReal.activar(txtBuscarHistorial,controlador::buscarHistorial);
    }

    public void activar(){
        if(!iniciado){
            iniciado=true;
            controlador.iniciarAsync();
            return;
        }
        controlador.recargarSiNecesario();
    }

    public void recargar(){
        controlador.recargarAsync();
    }

    private void configurarComponentes(){
        moneda.setMinimumFractionDigits(2);moneda.setMaximumFractionDigits(2);
        for(JTextField c:new JTextField[]{txtNumeroOrden,txtUsuarioRecibe,txtOrdenGestion,txtClienteGestion,txtEquipoGestion,txtOrdenRepuesto,txtStockRepuesto}){c.setEditable(false);c.setFocusable(false);c.setBackground(new Color(244,247,251));}
        txtUsuarioRecibe.setText(Sesion.haySesionActiva()?Sesion.getNombreCompleto():"");
        cmbEstadoFiltro.setModel(new DefaultComboBoxModel<>(new String[]{"TODOS","RECIBIDO","DIAGNOSTICO","EN_REPARACION","LISTO","ENTREGADO","CANCELADO"}));
        cmbEstadoGestion.setModel(new DefaultComboBoxModel<>(new String[]{"RECIBIDO","DIAGNOSTICO","EN_REPARACION","LISTO","ENTREGADO"}));
        cmbEquipoExistente.setRenderer(new DefaultListCellRenderer(){@Override public java.awt.Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){super.getListCellRendererComponent(l,v,i,s,f);setText(v instanceof EquipoCliente e?e.toString():"Registrar equipo nuevo");return this;}});
        tblOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);tblOrdenes.setAutoCreateRowSorter(true);
        tblRepuestos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistorialGeneral.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);tblHistorialGeneral.setAutoCreateRowSorter(true);
        txtFechaPrometida.setText(LocalDate.now().plusDays(3).format(FECHA));
        CampoSeleccionUtil.seleccionarTodoAlEnfocar(
                txtBuscarCliente,
                txtBuscarRepuesto,
                txtBuscarOrden,
                txtBuscarHistorial,
                txtCostoEstimado,
                txtCostoEstimadoGestion,
                txtCostoFinal,
                txtPrecioRepuesto
        );

        SelectorFechaUtil.instalar(txtFechaPrometida, true);
        SelectorFechaUtil.instalar(txtFechaPrometidaGestion, true);
        SelectorFechaUtil.instalar(txtGarantiaHasta, true);

        txtGarantiaHasta.setToolTipText(
                "Selecciona la fecha de vencimiento de la garantía."
        );
    }

    private void configurarBuscadores(){
        buscadorClientes=new BuscadorSugerencias<>(txtBuscarCliente,this::visibleCliente,this::buscarCliente,c->{clienteNuevaOrden=c;controlador.seleccionarClienteNuevaOrden(c);});
        buscadorProductos=new BuscadorSugerencias<>(txtBuscarRepuesto,this::visibleProducto,this::buscarProducto,p->{productoRepuesto=p;controlador.seleccionarProductoRepuesto(p);});
    }

    private void aplicarEstilos(){
        Color borde=new Color(220,227,236),azul=new Color(49,105,181),texto=new Color(24,50,87);
        for(JPanel p:new JPanel[]{pnlTarjetaRecibidos,pnlTarjetaReparacion,pnlTarjetaListos,pnlClienteEquipo,pnlDatosOrden,pnlListadoOrdenes,pnlGestionOrden,pnlRepuestos,pnlHistorialGeneral})p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borde),BorderFactory.createEmptyBorder(8,8,8,8)));
        for(JButton b:new JButton[]{btnRegistrarOrden,btnGuardarSeguimiento,btnAgregarRepuesto}){b.setBackground(azul);b.setForeground(Color.WHITE);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        for(JButton b:new JButton[]{btnNuevaOrden,btnCargarOrden,btnCancelarOrden}){b.setBackground(Color.WHITE);b.setForeground(texto);b.setBorder(BorderFactory.createLineBorder(borde));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        btnCancelarOrden.setForeground(new Color(192,52,52));
        for(JTable t:new JTable[]{tblOrdenes,tblRepuestos,tblHistorialGeneral})estilizarTabla(t);
    }

    private void estilizarTabla(JTable t){t.setRowHeight(38);t.setShowVerticalLines(false);t.setGridColor(new Color(232,237,243));t.setSelectionBackground(new Color(229,239,252));t.setSelectionForeground(new Color(24,50,87));JTableHeader h=t.getTableHeader();h.setBackground(new Color(248,250,253));h.setForeground(new Color(34,59,94));h.setFont(new Font("Segoe UI",Font.BOLD,12));h.setReorderingAllowed(false);}

    private void configurarEventos(){
        cmbEquipoExistente.addActionListener(e->{
            if(!actualizandoControles){
                controlador.seleccionarEquipoExistente();
            }
        });
        btnRegistrarOrden.addActionListener(e->controlador.registrarOrden());btnNuevaOrden.addActionListener(e->controlador.nuevaOrden());
        cmbEstadoFiltro.addActionListener(e->{
            if(!actualizandoControles&&cmbEstadoFiltro.getItemCount()>0){
                controlador.buscarOrdenes();
            }
        });
        btnCargarOrden.addActionListener(e->controlador.cargarOrdenSeleccionada());btnGuardarSeguimiento.addActionListener(e->controlador.guardarSeguimiento());
        btnAgregarRepuesto.addActionListener(e->controlador.agregarRepuesto());btnCancelarOrden.addActionListener(e->controlador.cancelarOrden());
    }

    public void cargarClientes(List<Cliente> v){buscadorClientes.setElementos(v);} public void cargarProductos(List<Producto> v){buscadorProductos.setElementos(v);} public Cliente getClienteNuevaOrden(){return clienteNuevaOrden;}
    public void cargarEquiposCliente(List<EquipoCliente> equipos){
        actualizandoControles=true;
        try{
            DefaultComboBoxModel<EquipoCliente> m=new DefaultComboBoxModel<>();
            m.addElement(null);
            equipos.forEach(m::addElement);
            cmbEquipoExistente.setModel(m);
            mostrarEquipoExistente(null);
        }finally{
            actualizandoControles=false;
        }
    }
    public EquipoCliente getEquipoExistenteSeleccionado(){Object v=cmbEquipoExistente.getSelectedItem();return v instanceof EquipoCliente e?e:null;}
    public void mostrarEquipoExistente(EquipoCliente e){boolean nuevo=e==null;for(JTextComponentLike c:new JTextComponentLike[]{new JTextComponentLike(txtTipoEquipo),new JTextComponentLike(txtMarcaEquipo),new JTextComponentLike(txtModeloEquipo),new JTextComponentLike(txtSerieEquipo),new JTextComponentLike(txtColorEquipo),new JTextComponentLike(txtAccesorios),new JTextComponentLike(txtObservacionesEquipo)})c.setEditable(nuevo);if(nuevo){txtTipoEquipo.setText("");txtMarcaEquipo.setText("");txtModeloEquipo.setText("");txtSerieEquipo.setText("");txtColorEquipo.setText("");txtAccesorios.setText("");txtObservacionesEquipo.setText("");}else{txtTipoEquipo.setText(texto(e.getTipoEquipo()));txtMarcaEquipo.setText(texto(e.getMarca()));txtModeloEquipo.setText(texto(e.getModelo()));txtSerieEquipo.setText(texto(e.getNumeroSerie()));txtColorEquipo.setText(texto(e.getColor()));txtAccesorios.setText(texto(e.getAccesoriosRecibidos()));txtObservacionesEquipo.setText(texto(e.getObservaciones()));}}
    private static final class JTextComponentLike{private final javax.swing.text.JTextComponent c;JTextComponentLike(javax.swing.text.JTextComponent c){this.c=c;}void setEditable(boolean v){c.setEditable(v);}}

    public EquipoCliente construirEquipoNuevo(){EquipoCliente e=new EquipoCliente();e.setTipoEquipo(txtTipoEquipo.getText().trim());e.setMarca(opcional(txtMarcaEquipo.getText()));e.setModelo(opcional(txtModeloEquipo.getText()));e.setNumeroSerie(opcional(txtSerieEquipo.getText()));e.setColor(opcional(txtColorEquipo.getText()));e.setAccesoriosRecibidos(opcional(txtAccesorios.getText()));e.setObservaciones(opcional(txtObservacionesEquipo.getText()));return e;}
    public OrdenServicio construirNuevaOrden(){OrdenServicio o=new OrdenServicio();o.setNumeroOrden(txtNumeroOrden.getText().trim());o.setProblemaReportado(txtProblemaReportado.getText().trim());o.setCostoEstimado(decimal(txtCostoEstimado.getText(),"costo estimado"));o.setFechaPrometida(fechaOpcional(txtFechaPrometida.getText(),"fecha prometida"));o.setObservaciones(opcional(txtObservacionesOrden.getText()));return o;}
    public void setNumeroOrden(String n){txtNumeroOrden.setText(n);}
    public void limpiarNuevaOrden(){clienteNuevaOrden=null;buscadorClientes.limpiar();cargarEquiposCliente(List.of());txtNumeroOrden.setText("");txtUsuarioRecibe.setText(Sesion.haySesionActiva()?Sesion.getNombreCompleto():"");txtFechaPrometida.setText(LocalDate.now().plusDays(3).format(FECHA));txtCostoEstimado.setText("0.00");txtProblemaReportado.setText("");txtObservacionesOrden.setText("");tabsReparaciones.setSelectedIndex(0);}
    public void establecerProcesando(boolean v){btnRegistrarOrden.setEnabled(!v);btnNuevaOrden.setEnabled(!v);txtBuscarCliente.setEnabled(!v);btnRegistrarOrden.setText(v?"Registrando...":"Registrar orden");}

    public String getTextoBusquedaOrden(){return txtBuscarOrden.getText().trim();} public String getEstadoOrdenFiltro(){Object v=cmbEstadoFiltro.getSelectedItem();return v==null?"TODOS":v.toString();}
    public void mostrarOrdenes(List<OrdenServicio> lista){DefaultTableModel m=noEditable(new String[]{"Orden","Fecha","Cliente","Equipo","Marca / Modelo","Costo final","Estado"});DateTimeFormatter f=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");for(OrdenServicio o:lista)m.addRow(new Object[]{o.getNumeroOrden(),o.getFechaRecepcion()==null?"":o.getFechaRecepcion().format(f),o.getNombreCliente(),o.getTipoEquipo(),marcaModelo(o),formatearMoneda(o.getCostoFinal()),o.getEstado()});tblOrdenes.setModel(m);estilizarTabla(tblOrdenes);}
    public int getFilaOrdenSeleccionadaModelo(){int f=tblOrdenes.getSelectedRow();return f<0?-1:tblOrdenes.convertRowIndexToModel(f);}

    public void mostrarOrdenActual(OrdenServicio o){txtOrdenGestion.setText(o.getNumeroOrden());txtClienteGestion.setText(o.getNombreCliente());txtEquipoGestion.setText(o.getTipoEquipo()+" — "+marcaModelo(o));txtProblemaGestion.setText(texto(o.getProblemaReportado()));txtDiagnostico.setText(texto(o.getDiagnostico()));txtTrabajoRealizado.setText(texto(o.getTrabajoRealizado()));txtCostoEstimadoGestion.setText(o.getCostoEstimado().toPlainString());txtCostoFinal.setText(o.getCostoFinal().toPlainString());cmbEstadoGestion.setSelectedItem(o.getEstado());txtFechaPrometidaGestion.setText(o.getFechaPrometida()==null?"":o.getFechaPrometida().format(FECHA));txtGarantiaHasta.setText(o.getGarantiaHasta()==null?"":o.getGarantiaHasta().format(FECHA));txtObservacionesGestion.setText(texto(o.getObservaciones()));txtDescripcionCambio.setText("");txtOrdenRepuesto.setText(o.getNumeroOrden()+" — "+o.getNombreCliente());mostrarRepuestos(o.getRepuestos());lblTotalRepuestosValor.setText(formatearMoneda(o.getTotalRepuestos()));boolean finalizada="ENTREGADO".equalsIgnoreCase(o.getEstado())||"CANCELADO".equalsIgnoreCase(o.getEstado());btnGuardarSeguimiento.setEnabled(!finalizada);btnAgregarRepuesto.setEnabled(!finalizada);btnCancelarOrden.setEnabled(!finalizada);}
    public OrdenServicio construirCambiosOrden(OrdenServicio b){OrdenServicio o=new OrdenServicio();o.setIdOrden(b.getIdOrden());o.setIdEquipo(b.getIdEquipo());o.setNumeroOrden(b.getNumeroOrden());o.setDiagnostico(opcional(txtDiagnostico.getText()));o.setTrabajoRealizado(opcional(txtTrabajoRealizado.getText()));o.setCostoEstimado(decimal(txtCostoEstimadoGestion.getText(),"costo estimado"));o.setCostoFinal(decimal(txtCostoFinal.getText(),"costo final"));Object e=cmbEstadoGestion.getSelectedItem();o.setEstado(e==null?"RECIBIDO":e.toString());o.setFechaPrometida(fechaOpcional(txtFechaPrometidaGestion.getText(),"fecha prometida"));o.setGarantiaHasta(fechaOpcional(txtGarantiaHasta.getText(),"garantía"));o.setObservaciones(opcional(txtObservacionesGestion.getText()));return o;}
    public String getDescripcionCambio(){return opcional(txtDescripcionCambio.getText());} public void limpiarDescripcionCambio(){txtDescripcionCambio.setText("");}

    public Producto getProductoRepuestoSeleccionado(){return productoRepuesto;} public void mostrarProductoRepuesto(Producto p){if(p==null){txtStockRepuesto.setText("0");txtPrecioRepuesto.setText("0.00");}else{txtStockRepuesto.setText(String.valueOf(p.getStockActual()));txtPrecioRepuesto.setText(p.getPrecioVenta().setScale(2,RoundingMode.HALF_UP).toPlainString());}}
    public int getCantidadRepuesto(){try{return Integer.parseInt(txtCantidadRepuesto.getText().trim());}catch(NumberFormatException ex){throw new IllegalArgumentException("La cantidad del repuesto debe ser un número entero.");}}
    public BigDecimal getPrecioRepuesto(){return decimal(txtPrecioRepuesto.getText(),"precio del repuesto");}
    public void limpiarRepuesto(){productoRepuesto=null;buscadorProductos.limpiar();txtStockRepuesto.setText("0");txtCantidadRepuesto.setText("1");txtPrecioRepuesto.setText("0.00");}
    public void mostrarRepuestos(List<RepuestoOrden> lista){DefaultTableModel m=noEditable(new String[]{"Código","Producto","Cantidad","Precio","Subtotal"});for(RepuestoOrden r:lista)m.addRow(new Object[]{r.getCodigoProducto(),r.getNombreProducto(),r.getCantidad(),formatearMoneda(r.getPrecioUnitario()),formatearMoneda(r.getSubtotal())});tblRepuestos.setModel(m);estilizarTabla(tblRepuestos);}

    public String getTextoBusquedaHistorial(){return txtBuscarHistorial.getText().trim();}
    public void mostrarHistorialGeneral(List<HistorialServicio> lista){DefaultTableModel m=noEditable(new String[]{"Fecha","Orden","Cliente","Estado anterior","Estado nuevo","Usuario","Descripción"});DateTimeFormatter f=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");for(HistorialServicio h:lista)m.addRow(new Object[]{h.getFechaEvento()==null?"":h.getFechaEvento().format(f),h.getNumeroOrden(),h.getNombreCliente(),texto(h.getEstadoAnterior()),h.getEstadoNuevo(),h.getNombreUsuario(),h.getDescripcion()});tblHistorialGeneral.setModel(m);estilizarTabla(tblHistorialGeneral);}
    public void actualizarIndicadores(int r,int e,int l){lblRecibidosValor.setText(String.valueOf(r));lblReparacionValor.setText(String.valueOf(e));lblListosValor.setText(String.valueOf(l));}
    public void mostrarPestanaSeguimiento(){tabsReparaciones.setSelectedIndex(1);}
    public void limpiarOrdenActual(){for(JTextComponentLike c:new JTextComponentLike[]{new JTextComponentLike(txtOrdenGestion),new JTextComponentLike(txtClienteGestion),new JTextComponentLike(txtEquipoGestion),new JTextComponentLike(txtProblemaGestion),new JTextComponentLike(txtDiagnostico),new JTextComponentLike(txtTrabajoRealizado),new JTextComponentLike(txtFechaPrometidaGestion),new JTextComponentLike(txtGarantiaHasta),new JTextComponentLike(txtObservacionesGestion),new JTextComponentLike(txtDescripcionCambio),new JTextComponentLike(txtOrdenRepuesto)})c.c.setText("");txtCostoEstimadoGestion.setText("0.00");txtCostoFinal.setText("0.00");cmbEstadoGestion.setSelectedItem("RECIBIDO");lblTotalRepuestosValor.setText("L 0.00");mostrarRepuestos(List.of());btnGuardarSeguimiento.setEnabled(false);btnAgregarRepuesto.setEnabled(false);btnCancelarOrden.setEnabled(false);}

    public String formatearMoneda(BigDecimal v){return moneda.format(v==null?BigDecimal.ZERO:v);} private DefaultTableModel noEditable(String[] c){return new DefaultTableModel(c,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private String visibleCliente(Cliente c){if(c==null)return "";String i=texto(c.getNumeroIdentidad());return i.isBlank()?texto(c.getNombreCompleto()):texto(c.getNombreCompleto())+" — "+i;}
    private String buscarCliente(Cliente c){return c==null?"":texto(c.getNombreCompleto())+" "+texto(c.getNumeroIdentidad())+" "+texto(c.getTelefono())+" "+texto(c.getCorreo());}
    private String visibleProducto(Producto p){return p==null?"":texto(p.getCodigo())+" — "+texto(p.getNombre())+" | Stock: "+p.getStockActual();}
    private String buscarProducto(Producto p){return p==null?"":texto(p.getCodigo())+" "+texto(p.getNombre())+" "+texto(p.getDescripcion())+" "+texto(p.getMarca())+" "+texto(p.getModelo())+" "+texto(p.getNombreCategoria());}
    private String marcaModelo(OrdenServicio o){String m=texto(o.getMarcaEquipo()).trim(),d=texto(o.getModeloEquipo()).trim();return m.isBlank()?d:d.isBlank()?m:m+" / "+d;}
    private BigDecimal decimal(String v,String campo){String x=v==null?"":v.trim().replace("L","").replace(",","");if(x.isBlank())return BigDecimal.ZERO;try{return new BigDecimal(x).setScale(2,RoundingMode.HALF_UP);}catch(NumberFormatException ex){throw new IllegalArgumentException("El "+campo+" debe ser un número válido.");}}
    private LocalDate fechaOpcional(String v,String campo){if(v==null||v.trim().isBlank())return null;try{return LocalDate.parse(v.trim(),FECHA);}catch(DateTimeParseException ex){throw new IllegalArgumentException("La "+campo+" debe tener formato dd/MM/yyyy.");}}
    private String opcional(String v){return v==null||v.trim().isBlank()?null:v.trim();} private String texto(String v){return v==null?"":v;}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents(){
        pnlEncabezado=panel();lblTitulo=label("Reparaciones y Servicio Técnico",28,true);lblSubtitulo=label("Registra equipos, da seguimiento y controla los repuestos utilizados.",14,false);pnlEncabezado.add(lblTitulo);lblTitulo.setBounds(0,4,520,40);pnlEncabezado.add(lblSubtitulo);lblSubtitulo.setBounds(0,46,760,24);
        pnlTarjetaRecibidos=tarjeta("Equipos recibidos");lblRecibidosTitulo=(JLabel)pnlTarjetaRecibidos.getComponent(0);lblRecibidosValor=(JLabel)pnlTarjetaRecibidos.getComponent(1);
        pnlTarjetaReparacion=tarjeta("En reparación");lblReparacionTitulo=(JLabel)pnlTarjetaReparacion.getComponent(0);lblReparacionValor=(JLabel)pnlTarjetaReparacion.getComponent(1);lblReparacionValor.setForeground(new Color(216,126,25));
        pnlTarjetaListos=tarjeta("Listos para entregar");lblListosTitulo=(JLabel)pnlTarjetaListos.getComponent(0);lblListosValor=(JLabel)pnlTarjetaListos.getComponent(1);lblListosValor.setForeground(new Color(34,155,85));
        tabsReparaciones=new JTabbedPane();crearNuevaOrden();crearSeguimiento();crearRepuestos();crearHistorial();
        setBackground(new Color(247,249,252));setMinimumSize(new java.awt.Dimension(1080,700));setPreferredSize(new java.awt.Dimension(1180,760));setLayout(null);
        add(pnlEncabezado);pnlEncabezado.setBounds(28,10,1100,76);add(pnlTarjetaRecibidos);pnlTarjetaRecibidos.setBounds(28,88,330,100);add(pnlTarjetaReparacion);pnlTarjetaReparacion.setBounds(372,88,330,100);add(pnlTarjetaListos);pnlTarjetaListos.setBounds(716,88,330,100);add(tabsReparaciones);tabsReparaciones.setBounds(28,198,1135,570);
    }// </editor-fold>//GEN-END:initComponents

    private void crearNuevaOrden(){
        pnlNuevaOrden=panel();pnlClienteEquipo=panel();pnlDatosOrden=panel();
        lblTituloClienteEquipo=label("Cliente y equipo",16,true);lblBuscarCliente=label("Buscar cliente",11,true);txtBuscarCliente=new JTextField();lblEquipoExistente=label("Equipo existente",11,true);cmbEquipoExistente=new JComboBox<>();lblTipoEquipo=label("Tipo de equipo",11,true);txtTipoEquipo=new JTextField();lblMarcaEquipo=label("Marca",11,true);txtMarcaEquipo=new JTextField();lblModeloEquipo=label("Modelo",11,true);txtModeloEquipo=new JTextField();lblSerieEquipo=label("Número de serie",11,true);txtSerieEquipo=new JTextField();lblColorEquipo=label("Color",11,true);txtColorEquipo=new JTextField();lblAccesorios=label("Accesorios recibidos",11,true);txtAccesorios=new JTextArea();scrollAccesorios=new JScrollPane(txtAccesorios);lblObservacionesEquipo=label("Observaciones del equipo",11,true);txtObservacionesEquipo=new JTextArea();scrollObservacionesEquipo=new JScrollPane(txtObservacionesEquipo);
        addComp(pnlClienteEquipo,lblTituloClienteEquipo,16,8,220,26);addComp(pnlClienteEquipo,lblBuscarCliente,16,40,120,18);addComp(pnlClienteEquipo,txtBuscarCliente,16,60,320,34);addComp(pnlClienteEquipo,lblEquipoExistente,350,40,140,18);addComp(pnlClienteEquipo,cmbEquipoExistente,350,60,340,34);addComp(pnlClienteEquipo,lblTipoEquipo,16,104,120,18);addComp(pnlClienteEquipo,txtTipoEquipo,16,124,155,34);addComp(pnlClienteEquipo,lblMarcaEquipo,183,104,90,18);addComp(pnlClienteEquipo,txtMarcaEquipo,183,124,145,34);addComp(pnlClienteEquipo,lblModeloEquipo,340,104,90,18);addComp(pnlClienteEquipo,txtModeloEquipo,340,124,145,34);addComp(pnlClienteEquipo,lblSerieEquipo,497,104,120,18);addComp(pnlClienteEquipo,txtSerieEquipo,497,124,193,34);addComp(pnlClienteEquipo,lblColorEquipo,16,168,90,18);addComp(pnlClienteEquipo,txtColorEquipo,16,188,155,34);addComp(pnlClienteEquipo,lblAccesorios,183,168,150,18);addComp(pnlClienteEquipo,scrollAccesorios,183,188,245,56);addComp(pnlClienteEquipo,lblObservacionesEquipo,440,168,180,18);addComp(pnlClienteEquipo,scrollObservacionesEquipo,440,188,250,56);
        lblTituloDatosOrden=label("Datos de la orden",16,true);lblNumeroOrden=label("Número de orden",11,true);txtNumeroOrden=new JTextField();lblUsuarioRecibe=label("Usuario que recibe",11,true);txtUsuarioRecibe=new JTextField();lblFechaPrometida=label("Fecha prometida",11,true);txtFechaPrometida=new JTextField();lblCostoEstimado=label("Costo estimado",11,true);txtCostoEstimado=new JTextField("0.00");addComp(pnlDatosOrden,lblTituloDatosOrden,16,8,220,26);addComp(pnlDatosOrden,lblNumeroOrden,16,40,120,18);addComp(pnlDatosOrden,txtNumeroOrden,16,60,300,34);addComp(pnlDatosOrden,lblUsuarioRecibe,16,104,140,18);addComp(pnlDatosOrden,txtUsuarioRecibe,16,124,300,34);addComp(pnlDatosOrden,lblFechaPrometida,16,168,130,18);addComp(pnlDatosOrden,txtFechaPrometida,16,188,145,34);addComp(pnlDatosOrden,lblCostoEstimado,173,168,120,18);addComp(pnlDatosOrden,txtCostoEstimado,173,188,143,34);
        lblProblemaReportado=label("Problema reportado",11,true);txtProblemaReportado=new JTextArea();scrollProblemaReportado=new JScrollPane(txtProblemaReportado);lblObservacionesOrden=label("Observaciones de la orden",11,true);txtObservacionesOrden=new JTextArea();scrollObservacionesOrden=new JScrollPane(txtObservacionesOrden);btnNuevaOrden=new JButton("Limpiar");btnRegistrarOrden=new JButton("Registrar orden");addComp(pnlNuevaOrden,pnlClienteEquipo,0,8,710,260);addComp(pnlNuevaOrden,pnlDatosOrden,724,8,325,260);addComp(pnlNuevaOrden,lblProblemaReportado,16,282,160,18);addComp(pnlNuevaOrden,scrollProblemaReportado,16,304,500,125);addComp(pnlNuevaOrden,lblObservacionesOrden,530,282,180,18);addComp(pnlNuevaOrden,scrollObservacionesOrden,530,304,503,125);addComp(pnlNuevaOrden,btnNuevaOrden,720,450,140,40);addComp(pnlNuevaOrden,btnRegistrarOrden,873,450,160,40);tabsReparaciones.addTab("Nueva orden",pnlNuevaOrden);
    }

    private void crearSeguimiento(){
        pnlSeguimiento=panel();pnlListadoOrdenes=panel();pnlGestionOrden=panel();lblTituloListado=label("Órdenes registradas",16,true);txtBuscarOrden=new JTextField();cmbEstadoFiltro=new JComboBox<>();btnCargarOrden=new JButton("Cargar orden");tblOrdenes=new JTable();scrollOrdenes=new JScrollPane(tblOrdenes);addComp(pnlListadoOrdenes,lblTituloListado,16,8,220,26);addComp(pnlListadoOrdenes,txtBuscarOrden,16,42,245,34);addComp(pnlListadoOrdenes,cmbEstadoFiltro,273,42,145,34);addComp(pnlListadoOrdenes,btnCargarOrden,430,42,130,34);addComp(pnlListadoOrdenes,scrollOrdenes,0,90,580,430);
        lblOrdenGestion=label("Orden",11,true);txtOrdenGestion=new JTextField();lblClienteGestion=label("Cliente",11,true);txtClienteGestion=new JTextField();lblEquipoGestion=label("Equipo",11,true);txtEquipoGestion=new JTextField();lblProblemaGestion=label("Problema reportado",11,true);txtProblemaGestion=new JTextArea();scrollProblemaGestion=new JScrollPane(txtProblemaGestion);lblDiagnostico=label("Diagnóstico",11,true);txtDiagnostico=new JTextArea();scrollDiagnostico=new JScrollPane(txtDiagnostico);lblTrabajoRealizado=label("Trabajo realizado",11,true);txtTrabajoRealizado=new JTextArea();scrollTrabajoRealizado=new JScrollPane(txtTrabajoRealizado);lblCostoEstimadoGestion=label("Costo estimado",11,true);txtCostoEstimadoGestion=new JTextField("0.00");lblCostoFinal=label("Costo final",11,true);txtCostoFinal=new JTextField("0.00");lblEstadoGestion=label("Estado",11,true);cmbEstadoGestion=new JComboBox<>();lblFechaPrometidaGestion=label("Fecha prometida",11,true);txtFechaPrometidaGestion=new JTextField();lblGarantiaHasta=label("Garantía hasta",11,true);txtGarantiaHasta=new JTextField();lblObservacionesGestion=label("Observaciones",11,true);txtObservacionesGestion=new JTextField();lblDescripcionCambio=label("Descripción del cambio",11,true);txtDescripcionCambio=new JTextField();btnGuardarSeguimiento=new JButton("Guardar cambios");btnCancelarOrden=new JButton("Cancelar orden");
        addComp(pnlGestionOrden,lblOrdenGestion,14,10,80,18);addComp(pnlGestionOrden,txtOrdenGestion,14,30,180,32);addComp(pnlGestionOrden,lblClienteGestion,206,10,80,18);addComp(pnlGestionOrden,txtClienteGestion,206,30,310,32);addComp(pnlGestionOrden,lblEquipoGestion,14,70,80,18);addComp(pnlGestionOrden,txtEquipoGestion,14,90,502,32);addComp(pnlGestionOrden,lblProblemaGestion,14,130,120,18);addComp(pnlGestionOrden,scrollProblemaGestion,14,150,155,100);addComp(pnlGestionOrden,lblDiagnostico,181,130,100,18);addComp(pnlGestionOrden,scrollDiagnostico,181,150,155,100);addComp(pnlGestionOrden,lblTrabajoRealizado,348,130,130,18);addComp(pnlGestionOrden,scrollTrabajoRealizado,348,150,168,100);addComp(pnlGestionOrden,lblCostoEstimadoGestion,14,260,110,18);addComp(pnlGestionOrden,txtCostoEstimadoGestion,14,280,105,32);addComp(pnlGestionOrden,lblCostoFinal,131,260,90,18);addComp(pnlGestionOrden,txtCostoFinal,131,280,105,32);addComp(pnlGestionOrden,lblEstadoGestion,248,260,80,18);addComp(pnlGestionOrden,cmbEstadoGestion,248,280,268,32);addComp(pnlGestionOrden,lblFechaPrometidaGestion,14,320,120,18);addComp(pnlGestionOrden,txtFechaPrometidaGestion,14,340,135,32);addComp(pnlGestionOrden,lblGarantiaHasta,161,320,110,18);addComp(pnlGestionOrden,txtGarantiaHasta,161,340,135,32);addComp(pnlGestionOrden,lblObservacionesGestion,308,320,110,18);addComp(pnlGestionOrden,txtObservacionesGestion,308,340,208,32);addComp(pnlGestionOrden,lblDescripcionCambio,14,380,170,18);addComp(pnlGestionOrden,txtDescripcionCambio,14,400,502,32);addComp(pnlGestionOrden,btnCancelarOrden,14,452,150,38);addComp(pnlGestionOrden,btnGuardarSeguimiento,348,452,168,38);
        addComp(pnlSeguimiento,pnlListadoOrdenes,0,8,580,530);addComp(pnlSeguimiento,pnlGestionOrden,594,8,535,530);tabsReparaciones.addTab("Seguimiento",pnlSeguimiento);
    }

    private void crearRepuestos(){pnlRepuestos=panel();lblOrdenRepuesto=label("Orden cargada",11,true);txtOrdenRepuesto=new JTextField();lblBuscarRepuesto=label("Buscar repuesto",11,true);txtBuscarRepuesto=new JTextField();lblStockRepuesto=label("Stock",11,true);txtStockRepuesto=new JTextField("0");lblCantidadRepuesto=label("Cantidad",11,true);txtCantidadRepuesto=new JTextField("1");lblPrecioRepuesto=label("Precio",11,true);txtPrecioRepuesto=new JTextField("0.00");btnAgregarRepuesto=new JButton("+ Agregar");tblRepuestos=new JTable();scrollRepuestos=new JScrollPane(tblRepuestos);lblTotalRepuestos=label("Total de repuestos:",13,true);lblTotalRepuestosValor=label("L 0.00",16,true);addComp(pnlRepuestos,lblOrdenRepuesto,16,16,120,18);addComp(pnlRepuestos,txtOrdenRepuesto,16,36,500,34);addComp(pnlRepuestos,lblBuscarRepuesto,16,86,130,18);addComp(pnlRepuestos,txtBuscarRepuesto,16,106,500,34);addComp(pnlRepuestos,lblStockRepuesto,536,86,70,18);addComp(pnlRepuestos,txtStockRepuesto,536,106,80,34);addComp(pnlRepuestos,lblCantidadRepuesto,628,86,80,18);addComp(pnlRepuestos,txtCantidadRepuesto,628,106,80,34);addComp(pnlRepuestos,lblPrecioRepuesto,720,86,80,18);addComp(pnlRepuestos,txtPrecioRepuesto,720,106,110,34);addComp(pnlRepuestos,btnAgregarRepuesto,844,98,170,42);addComp(pnlRepuestos,scrollRepuestos,0,165,1049,315);addComp(pnlRepuestos,lblTotalRepuestos,760,492,150,28);addComp(pnlRepuestos,lblTotalRepuestosValor,910,488,120,32);tabsReparaciones.addTab("Repuestos",pnlRepuestos);}
    private void crearHistorial(){pnlHistorial=panel();pnlHistorialGeneral=panel();lblTituloHistorialGeneral=label("Historial general del servicio técnico",16,true);txtBuscarHistorial=new JTextField();tblHistorialGeneral=new JTable();scrollHistorialGeneral=new JScrollPane(tblHistorialGeneral);addComp(pnlHistorialGeneral,lblTituloHistorialGeneral,16,10,350,26);addComp(pnlHistorialGeneral,txtBuscarHistorial,16,44,350,34);addComp(pnlHistorialGeneral,scrollHistorialGeneral,0,92,1049,430);addComp(pnlHistorial,pnlHistorialGeneral,0,8,1049,530);tabsReparaciones.addTab("Historial general",pnlHistorial);}
    private JPanel panel(){JPanel p=new JPanel(null);p.setBackground(Color.WHITE);return p;} private JLabel label(String t,int s,boolean b){JLabel l=new JLabel(t);l.setFont(new Font("Segoe UI",b?Font.BOLD:Font.PLAIN,s));l.setForeground(new Color(24,50,87));return l;} private JPanel tarjeta(String t){JPanel p=panel();JLabel a=label(t,12,false),v=label("0",28,true);addComp(p,a,18,16,180,20);addComp(p,v,18,45,110,40);return p;} private void addComp(JPanel p,java.awt.Component c,int x,int y,int w,int h){p.add(c);c.setBounds(x,y,w,h);}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnAgregarRepuesto,btnCancelarOrden,btnCargarOrden,btnGuardarSeguimiento,btnNuevaOrden,btnRegistrarOrden;
    private JComboBox<EquipoCliente> cmbEquipoExistente; private JComboBox<String> cmbEstadoFiltro,cmbEstadoGestion;
    private JLabel lblAccesorios,lblBuscarCliente,lblBuscarRepuesto,lblCantidadRepuesto,lblClienteGestion,lblColorEquipo,lblCostoEstimado,lblCostoEstimadoGestion,lblCostoFinal,lblDescripcionCambio,lblDiagnostico,lblEquipoExistente,lblEquipoGestion,lblEstadoGestion,lblFechaPrometida,lblFechaPrometidaGestion,lblGarantiaHasta,lblListosTitulo,lblListosValor,lblMarcaEquipo,lblModeloEquipo,lblNumeroOrden,lblObservacionesEquipo,lblObservacionesGestion,lblObservacionesOrden,lblOrdenGestion,lblOrdenRepuesto,lblPrecioRepuesto,lblProblemaGestion,lblProblemaReportado,lblReparacionTitulo,lblReparacionValor,lblRecibidosTitulo,lblRecibidosValor,lblSerieEquipo,lblStockRepuesto,lblSubtitulo,lblTipoEquipo,lblTitulo,lblTituloClienteEquipo,lblTituloDatosOrden,lblTituloHistorialGeneral,lblTituloListado,lblTotalRepuestos,lblTotalRepuestosValor,lblTrabajoRealizado,lblUsuarioRecibe;
    private JPanel pnlClienteEquipo,pnlDatosOrden,pnlEncabezado,pnlGestionOrden,pnlHistorial,pnlHistorialGeneral,pnlListadoOrdenes,pnlNuevaOrden,pnlRepuestos,pnlSeguimiento,pnlTarjetaListos,pnlTarjetaRecibidos,pnlTarjetaReparacion;
    private JScrollPane scrollAccesorios,scrollDiagnostico,scrollHistorialGeneral,scrollObservacionesEquipo,scrollObservacionesOrden,scrollOrdenes,scrollProblemaGestion,scrollProblemaReportado,scrollRepuestos,scrollTrabajoRealizado;
    private JTabbedPane tabsReparaciones; private JTable tblHistorialGeneral,tblOrdenes,tblRepuestos;
    private JTextArea txtAccesorios,txtDiagnostico,txtObservacionesEquipo,txtObservacionesOrden,txtProblemaGestion,txtProblemaReportado,txtTrabajoRealizado;
    private JTextField txtBuscarCliente,txtBuscarHistorial,txtBuscarOrden,txtBuscarRepuesto,txtCantidadRepuesto,txtClienteGestion,txtColorEquipo,txtCostoEstimado,txtCostoEstimadoGestion,txtCostoFinal,txtDescripcionCambio,txtEquipoGestion,txtFechaPrometida,txtFechaPrometidaGestion,txtGarantiaHasta,txtMarcaEquipo,txtModeloEquipo,txtNumeroOrden,txtObservacionesGestion,txtOrdenGestion,txtOrdenRepuesto,txtPrecioRepuesto,txtSerieEquipo,txtStockRepuesto,txtTipoEquipo,txtUsuarioRecibe;
    // End of variables declaration//GEN-END:variables
}
