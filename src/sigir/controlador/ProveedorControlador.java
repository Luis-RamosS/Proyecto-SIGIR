package sigir.controlador;

import java.awt.Cursor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.ProveedorDAO;
import sigir.modelo.Proveedor;
import sigir.vista.paneles.ProveedoresPanel;

public class ProveedorControlador {

    private final ProveedoresPanel vista;
    private final ProveedorDAO proveedorDAO;

    private List<Proveedor> proveedores =
            new ArrayList<>();

    private Integer idProveedorSeleccionado;
    private String estadoProveedorSeleccionado;
    private Integer idPendienteSeleccionar;

    private String firmaFormularioBase;
    private boolean ignorandoSeleccion;

    private SwingWorker<List<Proveedor>, Void>
            trabajadorCarga;

    private long ultimaCarga;
    private long versionBusqueda;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record FiltroProveedores(
            String texto,
            String estado
    ) {
    }

    public ProveedorControlador(
            ProveedoresPanel vista) {

        this.vista = vista;
        this.proveedorDAO =
                new ProveedorDAO();
    }

    public void iniciarAsync() {
        nuevo();
        cargarAsync();
    }

    public void recargarAsync() {
        cargarAsync();
    }

    public void iniciar() {
        iniciarAsync();
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

            recargaPendiente = true;
            return;
        }

        final FiltroProveedores filtro =
                capturarFiltro();

        final long versionActual =
                ++versionBusqueda;

        recargaPendiente = false;

        vista.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR
                )
        );

        trabajadorCarga =
                new SwingWorker<>() {

            @Override
            protected List<Proveedor>
                    doInBackground()
                    throws Exception {

                return proveedorDAO.listar(
                        filtro.texto(),
                        filtro.estado()
                );
            }

            @Override
            protected void done() {
                try {
                    if (versionActual
                            == versionBusqueda) {

                        aplicarProveedores(
                                get()
                        );

                        ultimaCarga =
                                System.currentTimeMillis();
                    }

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
                            + "los proveedores.",
                            causa
                    );

                } finally {
                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );

                    if (recargaPendiente) {
                        cargarAsync();
                    }
                }
            }
        };

        trabajadorCarga.execute();
    }

    public void buscar() {
        final FiltroProveedores filtro =
                capturarFiltro();

        final long versionActual =
                ++versionBusqueda;

        if (trabajadorCarga != null
                && !trabajadorCarga.isDone()) {

            trabajadorCarga.cancel(true);
        }

        vista.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR
                )
        );

        trabajadorCarga =
                new SwingWorker<>() {

            @Override
            protected List<Proveedor>
                    doInBackground()
                    throws Exception {

                return proveedorDAO.listar(
                        filtro.texto(),
                        filtro.estado()
                );
            }

            @Override
            protected void done() {
                if (isCancelled()
                        || versionActual
                        != versionBusqueda) {

                    vista.setCursor(
                            Cursor.getDefaultCursor()
                    );
                    return;
                }

                try {
                    aplicarProveedores(
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
                            + "los proveedores.",
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

    private void aplicarProveedores(
            List<Proveedor> resultado) {

        proveedores =
                new ArrayList<>(
                        resultado
                );

        vista.mostrarProveedores(
                proveedores
        );

        vista.mostrarCantidad(
                proveedores.size()
        );

        if (idPendienteSeleccionar != null) {
            Integer id =
                    idPendienteSeleccionar;

            idPendienteSeleccionar = null;

            seleccionarProveedorEnTabla(id);
        }
    }

    private FiltroProveedores
            capturarFiltro() {

        return new FiltroProveedores(
                vista.getTextoBusqueda(),
                vista.getEstadoFiltro()
        );
    }

    public void nuevo() {
        if (!confirmarDescartarCambios()) {
            restaurarSeleccionActual();
            return;
        }

        idProveedorSeleccionado = null;
        estadoProveedorSeleccionado = null;
        idPendienteSeleccionar = null;

        vista.limpiarFormulario();

        vista.setModoEdicion(
                false,
                null
        );

        actualizarFirmaFormularioBase();
    }

    public void seleccionarFila() {
        if (ignorandoSeleccion) {
            return;
        }

        int filaModelo =
                vista.getFilaSeleccionadaModelo();

        if (filaModelo < 0
                || filaModelo
                >= proveedores.size()) {

            return;
        }

        Proveedor proveedor =
                proveedores.get(filaModelo);

        if (!confirmarDescartarCambios()) {
            restaurarSeleccionActual();
            return;
        }

        idProveedorSeleccionado =
                proveedor.getIdProveedor();

        estadoProveedorSeleccionado =
                proveedor.getEstado();

        vista.mostrarProveedor(
                proveedor
        );

        vista.setModoEdicion(
                true,
                estadoProveedorSeleccionado
        );

        actualizarFirmaFormularioBase();
    }

    public void guardar() {
        try {
            boolean esNuevo =
                    idProveedorSeleccionado == null;

            Proveedor proveedor =
                    vista.obtenerProveedorFormulario();

            validar(proveedor);

            if (proveedorDAO.existeRtn(
                    proveedor.getRtn(),
                    idProveedorSeleccionado
            )) {

                vista.enfocarRtn();

                JOptionPane.showMessageDialog(
                        vista,
                        "Ya existe un proveedor "
                        + "con ese RTN.",
                        "RTN duplicado",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (idProveedorSeleccionado == null) {
                int idGenerado =
                        proveedorDAO.insertar(
                                proveedor
                        );

                idProveedorSeleccionado =
                        idGenerado;

                proveedor.setIdProveedor(
                        idGenerado
                );

            } else {
                proveedor.setIdProveedor(
                        idProveedorSeleccionado
                );

                proveedorDAO.actualizar(
                        proveedor
                );

                JOptionPane.showMessageDialog(
                        vista,
                        "Proveedor actualizado "
                        + "correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            if (esNuevo) {
                // Después de registrar un proveedor nuevo,
                // el formulario vuelve a quedar limpio.
                idProveedorSeleccionado = null;
                estadoProveedorSeleccionado = null;
                idPendienteSeleccionar = null;

                vista.limpiarFormulario();
                vista.setModoEdicion(
                        false,
                        null
                );

                actualizarFirmaFormularioBase();
                buscar();

                int respuesta =
                        JOptionPane.showConfirmDialog(
                                vista,
                                "Proveedor registrado correctamente.\n\n"
                                + "¿Deseas realizar una compra "
                                + "con este proveedor?",
                                "Proveedor registrado",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );

                if (respuesta
                        == JOptionPane.YES_OPTION) {

                    vista.abrirCompraConProveedor(
                            proveedor
                    );
                }

            } else {
                actualizarFirmaFormularioBase();

                idPendienteSeleccionar =
                        idProveedorSeleccionado;

                buscar();
            }

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
                    + "el proveedor.",
                    ex
            );
        }
    }

    public void cambiarEstado() {
        if (idProveedorSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un proveedor "
                    + "de la tabla.",
                    "Proveedor no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean estaActivo =
                "ACTIVO".equalsIgnoreCase(
                        estadoProveedorSeleccionado
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
                        + " al proveedor seleccionado?",
                        Character.toUpperCase(
                                accion.charAt(0)
                        )
                        + accion.substring(1)
                        + " proveedor",
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
            proveedorDAO.cambiarEstado(
                    idProveedorSeleccionado,
                    nuevoEstado
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "El proveedor ahora está "
                    + nuevoEstado.toLowerCase()
                    + ".",
                    "SIGIR",
                    JOptionPane.INFORMATION_MESSAGE
            );

            idPendienteSeleccionar =
                    idProveedorSeleccionado;

            buscar();

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cambiar "
                    + "el estado del proveedor.",
                    ex
            );
        }
    }

    private void validar(
            Proveedor proveedor) {

        if (proveedor.getNombreProveedor() == null
                || proveedor.getNombreProveedor()
                        .isBlank()) {

            vista.enfocarNombreProveedor();

            throw new IllegalArgumentException(
                    "Ingresa el nombre "
                    + "del proveedor."
            );
        }

        if (proveedor.getNombreProveedor()
                .length() > 120) {

            vista.enfocarNombreProveedor();

            throw new IllegalArgumentException(
                    "El nombre del proveedor "
                    + "no puede superar "
                    + "120 caracteres."
            );
        }

        String rtn =
                proveedor.getRtn();

        if (rtn != null
                && rtn.length() > 20) {

            vista.enfocarRtn();

            throw new IllegalArgumentException(
                    "El RTN no puede superar "
                    + "20 caracteres."
            );
        }

        String contacto =
                proveedor.getNombreContacto();

        if (contacto != null
                && contacto.length() > 100) {

            vista.enfocarContacto();

            throw new IllegalArgumentException(
                    "El nombre de contacto "
                    + "no puede superar "
                    + "100 caracteres."
            );
        }

        String telefono =
                proveedor.getTelefono();

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
                proveedor.getCorreo();

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
                proveedor.getDireccion();

        if (direccion != null
                && direccion.length() > 255) {

            vista.enfocarDireccion();

            throw new IllegalArgumentException(
                    "La dirección no puede superar "
                    + "255 caracteres."
            );
        }
    }

    private void actualizarFirmaFormularioBase() {
        firmaFormularioBase =
                vista.firmaFormulario();
    }

    private boolean hayCambiosSinGuardar() {
        if (firmaFormularioBase == null) {
            return false;
        }

        return !firmaFormularioBase.equals(
                vista.firmaFormulario()
        );
    }

    private boolean confirmarDescartarCambios() {
        if (!hayCambiosSinGuardar()) {
            return true;
        }

        int respuesta =
                JOptionPane.showConfirmDialog(
                        vista,
                        "Hay cambios sin guardar.\\n\\n"
                        + "¿Deseas descartar los cambios?",
                        "Descartar cambios",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        return respuesta
                == JOptionPane.YES_OPTION;
    }

    private void restaurarSeleccionActual() {
        ignorandoSeleccion = true;

        try {
            if (idProveedorSeleccionado == null) {
                vista.limpiarSeleccionTabla();
                return;
            }

            for (int i = 0;
                    i < proveedores.size();
                    i++) {

                if (proveedores.get(i)
                        .getIdProveedor()
                        == idProveedorSeleccionado) {

                    vista.seleccionarFilaModelo(i);
                    return;
                }
            }

            vista.limpiarSeleccionTabla();

        } finally {
            ignorandoSeleccion = false;
        }
    }

    private void seleccionarProveedorEnTabla(
            int idProveedor) {

        for (int i = 0;
                i < proveedores.size();
                i++) {

            if (proveedores.get(i)
                    .getIdProveedor()
                    == idProveedor) {

                ignorandoSeleccion = true;

                try {
                    vista.seleccionarFilaModelo(i);
                } finally {
                    ignorandoSeleccion = false;
                }

                Proveedor proveedor =
                        proveedores.get(i);

                idProveedorSeleccionado =
                        proveedor.getIdProveedor();

                estadoProveedorSeleccionado =
                        proveedor.getEstado();

                vista.mostrarProveedor(proveedor);
                vista.setModoEdicion(
                        true,
                        estadoProveedorSeleccionado
                );

                actualizarFirmaFormularioBase();
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
