package sigir.controlador;

import java.awt.Cursor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.ClienteDAO;
import sigir.dao.TipoClienteDAO;
import sigir.modelo.Cliente;
import sigir.modelo.TipoCliente;
import sigir.vista.paneles.ClientesPanel;

public class ClienteControlador {

    private final ClientesPanel vista;
    private final ClienteDAO clienteDAO;
    private final TipoClienteDAO tipoClienteDAO;

    private List<Cliente> clientes =
            new ArrayList<>();

    private Integer idClienteSeleccionado;
    private String estadoClienteSeleccionado;
    private Integer idPendienteSeleccionar;

    private SwingWorker<DatosCarga, Void>
            trabajadorCarga;

    private SwingWorker<List<Cliente>, Void>
            trabajadorBusqueda;

    private long ultimaCarga;
    private long versionBusqueda;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record DatosCarga(
            List<TipoCliente> tipos,
            List<Cliente> clientes
    ) {
    }

    private record FiltroClientes(
            String texto,
            Integer idTipo,
            String estado
    ) {
    }

    public ClienteControlador(
            ClientesPanel vista) {

        this.vista = vista;
        this.clienteDAO = new ClienteDAO();
        this.tipoClienteDAO =
                new TipoClienteDAO();
    }

    public void iniciarAsync() {
        nuevo();
        cargarTodoAsync();
    }

    public void recargarAsync() {
        cargarTodoAsync();
    }

    public void iniciar() {
        iniciarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void recargarSiNecesario() {
        long transcurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (transcurrido
                >= VIGENCIA_DATOS_MS) {

            cargarTodoAsync();
        }
    }

    private void cargarTodoAsync() {
        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            recargaPendiente = true;
            return;
        }

        final FiltroClientes filtro =
                capturarFiltro();

        recargaPendiente = false;

        if (trabajadorBusqueda != null
                && !trabajadorBusqueda.isDone()) {

            trabajadorBusqueda.cancel(true);
        }

        final long versionCarga =
                ++versionBusqueda;

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

                List<TipoCliente> tipos =
                        tipoClienteDAO.listarActivos();

                List<Cliente> clientesCargados =
                        clienteDAO.listar(
                                filtro.texto(),
                                filtro.idTipo(),
                                filtro.estado()
                        );

                return new DatosCarga(
                        tipos,
                        clientesCargados
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    vista.cargarTiposCliente(
                            datos.tipos()
                    );

                    if (versionCarga
                            == versionBusqueda) {

                        aplicarClientes(
                                datos.clientes()
                        );
                    }

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La carga fue cancelada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarErrorBaseDatos(
                            "No fue posible cargar "
                            + "el módulo de clientes.",
                            causa
                    );

                } finally {
                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );

                    if (recargaPendiente) {
                        cargarTodoAsync();
                    }
                }
            }
        };

        trabajadorCarga.execute();
    }

    public void buscar() {
        final FiltroClientes filtro =
                capturarFiltro();

        final long versionActual =
                ++versionBusqueda;

        if (trabajadorBusqueda != null
                && !trabajadorBusqueda.isDone()) {

            trabajadorBusqueda.cancel(true);
        }

        trabajadorBusqueda =
                new SwingWorker<>() {

            @Override
            protected List<Cliente>
                    doInBackground()
                    throws Exception {

                return clienteDAO.listar(
                        filtro.texto(),
                        filtro.idTipo(),
                        filtro.estado()
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
                    aplicarClientes(
                            get()
                    );

                    ultimaCarga =
                            System.currentTimeMillis();

                } catch (InterruptedException ex) {
                    Thread.currentThread()
                            .interrupt();

                } catch (CancellationException ex) {
                    // La búsqueda fue reemplazada.

                } catch (ExecutionException ex) {
                    Throwable causa =
                            ex.getCause() == null
                                    ? ex
                                    : ex.getCause();

                    mostrarErrorBaseDatos(
                            "No fue posible cargar "
                            + "los clientes.",
                            causa
                    );
                }
            }
        };

        trabajadorBusqueda.execute();
    }

    private void aplicarClientes(
            List<Cliente> resultado) {

        clientes =
                new ArrayList<>(
                        resultado
                );

        vista.mostrarClientes(clientes);

        vista.mostrarCantidad(
                clientes.size()
        );

        if (idPendienteSeleccionar != null) {
            Integer id =
                    idPendienteSeleccionar;

            idPendienteSeleccionar = null;

            seleccionarClienteEnTabla(id);
        }
    }

    private FiltroClientes capturarFiltro() {
        TipoCliente tipo =
                vista.getTipoFiltro();

        Integer idTipo =
                tipo == null
                || tipo.getIdTipoCliente() <= 0
                        ? null
                        : tipo.getIdTipoCliente();

        return new FiltroClientes(
                vista.getTextoBusqueda(),
                idTipo,
                vista.getEstadoFiltro()
        );
    }

    public void nuevo() {
        idClienteSeleccionado = null;
        estadoClienteSeleccionado = null;
        idPendienteSeleccionar = null;

        vista.limpiarFormulario();

        vista.setModoEdicion(
                false,
                null
        );
    }

    public void seleccionarFila() {
        int filaModelo =
                vista.getFilaSeleccionadaModelo();

        if (filaModelo < 0
                || filaModelo >= clientes.size()) {

            return;
        }

        Cliente cliente =
                clientes.get(filaModelo);

        idClienteSeleccionado =
                cliente.getIdCliente();

        estadoClienteSeleccionado =
                cliente.getEstado();

        vista.mostrarCliente(cliente);

        vista.setModoEdicion(
                true,
                estadoClienteSeleccionado
        );
    }

    public void guardar() {
        try {
            Cliente cliente =
                    vista.obtenerClienteFormulario();

            validar(cliente);

            if (clienteDAO.existeIdentidad(
                    cliente.getNumeroIdentidad(),
                    idClienteSeleccionado
            )) {

                vista.enfocarIdentidad();

                JOptionPane.showMessageDialog(
                        vista,
                        "Ya existe un cliente con ese "
                        + "número de identidad o RTN.",
                        "Identidad duplicada",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (idClienteSeleccionado == null) {
                int idGenerado =
                        clienteDAO.insertar(cliente);

                idClienteSeleccionado =
                        idGenerado;

                JOptionPane.showMessageDialog(
                        vista,
                        "Cliente registrado correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                cliente.setIdCliente(
                        idClienteSeleccionado
                );

                clienteDAO.actualizar(cliente);

                JOptionPane.showMessageDialog(
                        vista,
                        "Cliente actualizado correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            idPendienteSeleccionar =
                    idClienteSeleccionado;

            buscar();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible guardar "
                    + "el cliente.",
                    ex
            );
        }
    }

    public void cambiarEstado() {
        if (idClienteSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un cliente de la tabla.",
                    "Cliente no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean estaActivo =
                "ACTIVO".equalsIgnoreCase(
                        estadoClienteSeleccionado
                );

        String nuevoEstado =
                estaActivo
                        ? "INACTIVO"
                        : "ACTIVO";

        String accion =
                estaActivo
                        ? "desactivar"
                        : "activar";

        int respuesta =
                JOptionPane.showConfirmDialog(
                        vista,
                        "¿Deseas " + accion
                        + " al cliente seleccionado?",
                        Character.toUpperCase(
                                accion.charAt(0)
                        )
                        + accion.substring(1)
                        + " cliente",
                        JOptionPane.YES_NO_OPTION,
                        estaActivo
                                ? JOptionPane.WARNING_MESSAGE
                                : JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta
                != JOptionPane.YES_OPTION) {

            return;
        }

        try {
            clienteDAO.cambiarEstado(
                    idClienteSeleccionado,
                    nuevoEstado
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "El cliente ahora está "
                    + nuevoEstado.toLowerCase()
                    + ".",
                    "SIGIR",
                    JOptionPane.INFORMATION_MESSAGE
            );

            idPendienteSeleccionar =
                    idClienteSeleccionado;

            buscar();

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cambiar "
                    + "el estado del cliente.",
                    ex
            );
        }
    }

    private void validar(
            Cliente cliente) {

        if (cliente.getNombreCompleto() == null
                || cliente.getNombreCompleto()
                        .isBlank()) {

            vista.enfocarNombre();

            throw new IllegalArgumentException(
                    "Ingresa el nombre completo "
                    + "del cliente."
            );
        }

        if (cliente.getNombreCompleto()
                .length() > 120) {

            vista.enfocarNombre();

            throw new IllegalArgumentException(
                    "El nombre no puede superar "
                    + "120 caracteres."
            );
        }

        if (cliente.getIdTipoCliente() <= 0) {
            vista.enfocarTipo();

            throw new IllegalArgumentException(
                    "Selecciona el tipo de cliente."
            );
        }

        String identidad =
                cliente.getNumeroIdentidad();

        if (identidad != null
                && identidad.length() > 20) {

            vista.enfocarIdentidad();

            throw new IllegalArgumentException(
                    "La identidad o RTN no puede "
                    + "superar 20 caracteres."
            );
        }

        String telefono =
                cliente.getTelefono();

        if (telefono != null
                && telefono.length() > 20) {

            vista.enfocarTelefono();

            throw new IllegalArgumentException(
                    "El teléfono no puede superar "
                    + "20 caracteres."
            );
        }

        if (telefono != null
                && !telefono.matches(
                        "[0-9+()\\-\\s]{7,20}"
                )) {

            vista.enfocarTelefono();

            throw new IllegalArgumentException(
                    "El teléfono contiene "
                    + "caracteres no válidos."
            );
        }

        String correo =
                cliente.getCorreo();

        if (correo != null
                && correo.length() > 100) {

            vista.enfocarCorreo();

            throw new IllegalArgumentException(
                    "El correo no puede superar "
                    + "100 caracteres."
            );
        }

        if (correo != null
                && !correo.matches(
                        "^[A-Za-z0-9._%+-]+"
                        + "@[A-Za-z0-9.-]+"
                        + "\\.[A-Za-z]{2,63}$"
                )) {

            vista.enfocarCorreo();

            throw new IllegalArgumentException(
                    "Ingresa un correo "
                    + "electrónico válido."
            );
        }

        String direccion =
                cliente.getDireccion();

        if (direccion != null
                && direccion.length() > 255) {

            vista.enfocarDireccion();

            throw new IllegalArgumentException(
                    "La dirección no puede superar "
                    + "255 caracteres."
            );
        }
    }

    private void seleccionarClienteEnTabla(
            int idCliente) {

        for (int i = 0;
                i < clientes.size();
                i++) {

            if (clientes.get(i)
                    .getIdCliente()
                    == idCliente) {

                vista.seleccionarFilaModelo(i);
                seleccionarFila();
                break;
            }
        }
    }

    private void mostrarErrorBaseDatos(
            String mensaje,
            Throwable ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje
                + "\n\nDetalle: "
                + (
                    ex == null
                            ? "Error desconocido"
                            : ex.getMessage()
                ),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        if (ex != null) {
            ex.printStackTrace();
        }
    }
}
