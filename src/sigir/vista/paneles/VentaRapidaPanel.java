package sigir.vista.paneles;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.Timer;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import sigir.componentes.BuscadorSugerencias;
import sigir.controlador.VentaRapidaControlador;
import sigir.modelo.Producto;
import sigir.modelo.VentaRapida;
import sigir.util.CampoSeleccionUtil;
import sigir.util.HorarioVentaRapidaUtil;
import sigir.util.SelectorFechaUtil;

public class VentaRapidaPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
    private final VentaRapidaControlador controlador;
    private BuscadorSugerencias<Producto> buscadorProductos;
    private Producto productoSeleccionado;
    private boolean iniciado;
    private boolean procesando;
    private JLabel lblHorario;
    private Timer timerHorario;

    public VentaRapidaPanel() {
        initComponents();
        controlador = new VentaRapidaControlador(this);
        configurarComponentes();
        configurarBuscador();
        configurarEventos();
        aplicarEstilos();
        configurarHorario();
    }

    public void activar() {
        actualizarEstadoHorario();
        if(!iniciado) {
            iniciado=true;
            controlador.iniciarAsync();
        } else {
            controlador.recargarAsync();
        }
    }

    public void recargar() {
        controlador.recargarAsync();
    }

    private void configurarComponentes() {
        moneda.setMinimumFractionDigits(2);
        moneda.setMaximumFractionDigits(2);
        txtFechaReal.setText(LocalDate.now().minusDays(1).format(FECHA));
        spnHoraReal.setModel(new SpinnerDateModel());
        spnHoraReal.setEditor(new JSpinner.DateEditor(spnHoraReal,"HH:mm"));
        cmbMetodoPago.setModel(new DefaultComboBoxModel<>(new String[]{"EFECTIVO","TRANSFERENCIA","TARJETA"}));
        txtStock.setEditable(false);
        txtTotal.setEditable(false);
        txtStock.setFocusable(false);
        txtTotal.setFocusable(false);
        txtStock.setBackground(new Color(244,247,251));
        txtTotal.setBackground(new Color(244,247,251));
        SelectorFechaUtil.instalar(txtFechaReal,false);
        CampoSeleccionUtil.seleccionarTodoAlEnfocar(txtBuscarProducto,txtCantidad,txtPrecioUnitario,txtNumeroSerie);
        tblHistorial.setAutoCreateRowSorter(true);
        tblHistorial.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    private void configurarBuscador() {
        buscadorProductos=new BuscadorSugerencias<>(
                txtBuscarProducto,
                p -> p==null?"":p.getCodigo()+" — "+p.getNombre()+" | Stock: "+p.getStockActual(),
                p -> p==null?"":seguro(p.getCodigo())+" "+seguro(p.getNombre())+" "+seguro(p.getMarca())+" "+seguro(p.getModelo())+" "+seguro(p.getDescripcion()),
                p -> { productoSeleccionado=p; controlador.seleccionarProducto(); }
        );
    }

    private void configurarHorario() {
        lblHorario = new JLabel();
        lblHorario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        add(lblHorario);
        lblHorario.setBounds(690, 18, 438, 28);

        timerHorario = new Timer(5000, e -> actualizarEstadoHorario());
        timerHorario.start();
        actualizarEstadoHorario();
    }

    public void actualizarEstadoHorario() {
        boolean habilitada = HorarioVentaRapidaUtil.estaHabilitadaAhora();

        lblHorario.setText(
                habilitada
                        ? "Disponible ahora · Horario: "
                        + HorarioVentaRapidaUtil.descripcionHorario()
                        : "No disponible · Horario: "
                        + HorarioVentaRapidaUtil.descripcionHorario()
        );

        lblHorario.setForeground(
                habilitada
                        ? new Color(34, 155, 85)
                        : new Color(196, 74, 74)
        );

        btnRegistrar.setEnabled(habilitada && !procesando);
        btnRegistrar.setToolTipText(
                habilitada
                        ? "Registrar una venta rápida"
                        : "Las ventas rápidas solo se registran de "
                        + HorarioVentaRapidaUtil.descripcionHorario()
        );
    }

    private void configurarEventos() {
        btnRegistrar.addActionListener(e -> controlador.registrar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnActualizar.addActionListener(e -> controlador.recargarAsync());
        DocumentListener dl=new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){controlador.actualizarTotal();}
            @Override public void removeUpdate(DocumentEvent e){controlador.actualizarTotal();}
            @Override public void changedUpdate(DocumentEvent e){controlador.actualizarTotal();}
        };
        txtCantidad.getDocument().addDocumentListener(dl);
        txtPrecioUnitario.getDocument().addDocumentListener(dl);
    }

    private void aplicarEstilos() {
        Color borde=new Color(220,227,236);
        pnlRegistro.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borde),BorderFactory.createEmptyBorder(8,8,8,8)));
        pnlHistorial.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borde),BorderFactory.createEmptyBorder(8,8,8,8)));
        for(javax.swing.JTextField f:new javax.swing.JTextField[]{txtFechaReal,txtBuscarProducto,txtCantidad,txtPrecioUnitario,txtStock,txtTotal,txtNumeroSerie,txtObservaciones}) {
            f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,216,229)),BorderFactory.createEmptyBorder(0,8,0,8)));
        }
        btnRegistrar.setBackground(new Color(49,105,181));btnRegistrar.setForeground(Color.WHITE);btnRegistrar.setBorderPainted(false);btnRegistrar.setFocusPainted(false);
        for(javax.swing.JButton b:new javax.swing.JButton[]{btnRegistrar,btnLimpiar,btnActualizar}) b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        estilizarTabla();
    }

    private void estilizarTabla() {
        tblHistorial.setRowHeight(36);tblHistorial.setShowVerticalLines(false);tblHistorial.setGridColor(new Color(232,237,243));tblHistorial.setSelectionBackground(new Color(205,225,249));tblHistorial.setSelectionForeground(new Color(24,50,87));
        JTableHeader h=tblHistorial.getTableHeader();h.setBackground(new Color(248,250,253));h.setForeground(new Color(34,59,94));h.setFont(new Font("Segoe UI",Font.BOLD,12));h.setReorderingAllowed(false);
    }

    public void cargarProductos(List<Producto> productos) {
        int id=productoSeleccionado==null?0:productoSeleccionado.getIdProducto();
        buscadorProductos.setElementos(productos);
        if(id>0) productos.stream().filter(p->p.getIdProducto()==id).findFirst().ifPresent(buscadorProductos::seleccionar);
    }

    public Producto getProductoSeleccionado(){return productoSeleccionado;}

    public void mostrarDatosProducto(BigDecimal precio,int stock,boolean serie) {
        txtPrecioUnitario.setText(precio==null?"0.00":precio.setScale(2,RoundingMode.HALF_UP).toPlainString());
        txtStock.setText(String.valueOf(stock));
        txtNumeroSerie.setEnabled(serie);
        lblNumeroSerie.setEnabled(serie);
        lblAvisoSerie.setText(serie?"Este producto requiere indicar el número de serie vendido.":"Este producto no maneja número de serie.");
        if(!serie) txtNumeroSerie.setText("");
        controlador.actualizarTotal();
    }

    public LocalDate getFechaReal(){return LocalDate.parse(txtFechaReal.getText().trim(),FECHA);}
    public String getFechaRealTexto(){return txtFechaReal.getText().trim();}
    public LocalTime getHoraReal(){return LocalTime.parse(getHoraRealTexto(),HORA);}
    public String getHoraRealTexto(){return new java.text.SimpleDateFormat("HH:mm").format((Date)spnHoraReal.getValue());}
    public int getCantidad(){try{return Integer.parseInt(txtCantidad.getText().trim());}catch(NumberFormatException ex){throw new IllegalArgumentException("La cantidad debe ser un número entero.");}}
    public BigDecimal getPrecioUnitario(){try{return new BigDecimal(txtPrecioUnitario.getText().trim().replace("L","").replace(",","")).setScale(2,RoundingMode.HALF_UP);}catch(NumberFormatException ex){throw new IllegalArgumentException("El precio unitario debe ser válido.");}}
    public String getMetodoPago(){Object o=cmbMetodoPago.getSelectedItem();return o==null?"":o.toString();}
    public String getNumeroSerie(){String v=txtNumeroSerie.getText().trim();return v.isBlank()?null:v;}
    public String getObservaciones(){String v=txtObservaciones.getText().trim();return v.isBlank()?null:v;}
    public void setTotal(BigDecimal total){txtTotal.setText((total==null?BigDecimal.ZERO:total).setScale(2,RoundingMode.HALF_UP).toPlainString());}
    public String formatearMoneda(BigDecimal v){return moneda.format(v==null?BigDecimal.ZERO:v);}

    public void limpiarFormulario() {
        productoSeleccionado=null;
        buscadorProductos.limpiar();
        txtFechaReal.setText(LocalDate.now().minusDays(1).format(FECHA));
        spnHoraReal.setValue(new Date());
        txtCantidad.setText("1");txtPrecioUnitario.setText("0.00");txtStock.setText("0");txtTotal.setText("0.00");txtNumeroSerie.setText("");txtObservaciones.setText("");cmbMetodoPago.setSelectedItem("EFECTIVO");lblAvisoSerie.setText("Selecciona el producto vendido.");
    }

    public void establecerProcesando(boolean procesando) {
        this.procesando = procesando;
        btnActualizar.setEnabled(!procesando);
        btnRegistrar.setText(procesando ? "Procesando..." : "Registrar venta rápida");
        actualizarEstadoHorario();
    }

    public void mostrarHistorial(List<VentaRapida> ventas) {
        DefaultTableModel m=new DefaultTableModel(new String[]{"Registro","Ocurrió","Registrada","Producto","Cant.","Precio","Total","Método","Serie","Usuario"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        DateTimeFormatter fh=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for(VentaRapida v:ventas)m.addRow(new Object[]{"VR-"+String.format("%05d",v.getIdVentaRapida()),v.getFechaHoraReal()==null?"":v.getFechaHoraReal().format(fh),v.getFechaRegistro()==null?"":v.getFechaRegistro().format(fh),seguro(v.getCodigoProducto())+" - "+seguro(v.getNombreProducto()),v.getCantidad(),formatearMoneda(v.getPrecioUnitario()),formatearMoneda(v.getTotal()),v.getMetodoPago(),seguro(v.getNumeroSerie()),v.getNombreUsuario()});
        tblHistorial.setModel(m);estilizarTabla();
    }

    private String seguro(String s){return s==null?"":s;}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo=new javax.swing.JLabel();lblSubtitulo=new javax.swing.JLabel();pnlRegistro=new javax.swing.JPanel();pnlHistorial=new javax.swing.JPanel();
        lblFechaReal=new javax.swing.JLabel();txtFechaReal=new javax.swing.JTextField();lblHoraReal=new javax.swing.JLabel();spnHoraReal=new javax.swing.JSpinner();lblBuscarProducto=new javax.swing.JLabel();txtBuscarProducto=new javax.swing.JTextField();lblCantidad=new javax.swing.JLabel();txtCantidad=new javax.swing.JTextField("1");lblPrecioUnitario=new javax.swing.JLabel();txtPrecioUnitario=new javax.swing.JTextField("0.00");lblStock=new javax.swing.JLabel();txtStock=new javax.swing.JTextField("0");lblTotal=new javax.swing.JLabel();txtTotal=new javax.swing.JTextField("0.00");lblMetodoPago=new javax.swing.JLabel();cmbMetodoPago=new javax.swing.JComboBox<>();lblNumeroSerie=new javax.swing.JLabel();txtNumeroSerie=new javax.swing.JTextField();lblObservaciones=new javax.swing.JLabel();txtObservaciones=new javax.swing.JTextField();lblAvisoSerie=new javax.swing.JLabel();btnRegistrar=new javax.swing.JButton();btnLimpiar=new javax.swing.JButton();
        lblTituloHistorial=new javax.swing.JLabel();btnActualizar=new javax.swing.JButton();tblHistorial=new javax.swing.JTable();scrollHistorial=new javax.swing.JScrollPane(tblHistorial);
        setBackground(new Color(247,249,252));setPreferredSize(new java.awt.Dimension(1180,760));setLayout(null);
        lblTitulo.setFont(new Font("Segoe UI",Font.BOLD,28));lblTitulo.setForeground(new Color(24,50,87));lblTitulo.setText("Venta rápida");add(lblTitulo);lblTitulo.setBounds(28,14,360,40);
        lblSubtitulo.setForeground(new Color(98,124,159));lblSubtitulo.setText("Registra hoy una venta que ocurrió fuera del horario de atención.");add(lblSubtitulo);lblSubtitulo.setBounds(28,54,720,24);
        pnlRegistro.setBackground(Color.WHITE);pnlRegistro.setLayout(null);add(pnlRegistro);pnlRegistro.setBounds(28,92,1100,245);
        lblFechaReal.setText("Fecha en que ocurrió");pnlRegistro.add(lblFechaReal);lblFechaReal.setBounds(16,18,150,18);pnlRegistro.add(txtFechaReal);txtFechaReal.setBounds(16,40,145,34);
        lblHoraReal.setText("Hora");pnlRegistro.add(lblHoraReal);lblHoraReal.setBounds(176,18,70,18);pnlRegistro.add(spnHoraReal);spnHoraReal.setBounds(176,40,100,34);
        lblBuscarProducto.setText("Buscar producto");pnlRegistro.add(lblBuscarProducto);lblBuscarProducto.setBounds(292,18,120,18);pnlRegistro.add(txtBuscarProducto);txtBuscarProducto.setBounds(292,40,330,34);
        lblCantidad.setText("Cantidad");pnlRegistro.add(lblCantidad);lblCantidad.setBounds(638,18,70,18);pnlRegistro.add(txtCantidad);txtCantidad.setBounds(638,40,80,34);
        lblPrecioUnitario.setText("Precio unitario");pnlRegistro.add(lblPrecioUnitario);lblPrecioUnitario.setBounds(734,18,100,18);pnlRegistro.add(txtPrecioUnitario);txtPrecioUnitario.setBounds(734,40,110,34);
        lblStock.setText("Stock actual");pnlRegistro.add(lblStock);lblStock.setBounds(860,18,90,18);pnlRegistro.add(txtStock);txtStock.setBounds(860,40,90,34);
        lblTotal.setText("Total");pnlRegistro.add(lblTotal);lblTotal.setBounds(966,18,70,18);pnlRegistro.add(txtTotal);txtTotal.setBounds(966,40,110,34);
        lblMetodoPago.setText("Método de pago");pnlRegistro.add(lblMetodoPago);lblMetodoPago.setBounds(16,92,120,18);pnlRegistro.add(cmbMetodoPago);cmbMetodoPago.setBounds(16,114,145,34);
        lblNumeroSerie.setText("Número de serie (si aplica)");pnlRegistro.add(lblNumeroSerie);lblNumeroSerie.setBounds(176,92,180,18);pnlRegistro.add(txtNumeroSerie);txtNumeroSerie.setBounds(176,114,230,34);
        lblObservaciones.setText("Observaciones");pnlRegistro.add(lblObservaciones);lblObservaciones.setBounds(422,92,110,18);pnlRegistro.add(txtObservaciones);txtObservaciones.setBounds(422,114,654,34);
        lblAvisoSerie.setForeground(new Color(98,124,159));lblAvisoSerie.setText("Selecciona el producto vendido.");pnlRegistro.add(lblAvisoSerie);lblAvisoSerie.setBounds(16,164,620,22);
        btnLimpiar.setText("Limpiar");pnlRegistro.add(btnLimpiar);btnLimpiar.setBounds(784,178,130,38);btnRegistrar.setText("Registrar venta rápida");pnlRegistro.add(btnRegistrar);btnRegistrar.setBounds(926,178,150,38);
        pnlHistorial.setBackground(Color.WHITE);pnlHistorial.setLayout(null);add(pnlHistorial);pnlHistorial.setBounds(28,352,1100,355);
        lblTituloHistorial.setFont(new Font("Segoe UI",Font.BOLD,16));lblTituloHistorial.setText("Historial de ventas rápidas");pnlHistorial.add(lblTituloHistorial);lblTituloHistorial.setBounds(16,10,260,26);btnActualizar.setText("Actualizar");pnlHistorial.add(btnActualizar);btnActualizar.setBounds(950,8,125,32);pnlHistorial.add(scrollHistorial);scrollHistorial.setBounds(0,48,1100,307);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JComboBox<String> cmbMetodoPago;
    private javax.swing.JLabel lblAvisoSerie;
    private javax.swing.JLabel lblBuscarProducto;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblFechaReal;
    private javax.swing.JLabel lblHoraReal;
    private javax.swing.JLabel lblMetodoPago;
    private javax.swing.JLabel lblNumeroSerie;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblPrecioUnitario;
    private javax.swing.JLabel lblStock;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloHistorial;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JPanel pnlRegistro;
    private javax.swing.JScrollPane scrollHistorial;
    private javax.swing.JSpinner spnHoraReal;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtFechaReal;
    private javax.swing.JTextField txtNumeroSerie;
    private javax.swing.JTextField txtObservaciones;
    private javax.swing.JTextField txtPrecioUnitario;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
