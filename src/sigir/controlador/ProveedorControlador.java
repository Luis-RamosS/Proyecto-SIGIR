package sigir.controlador;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import sigir.dao.ProveedorDAO;
import sigir.modelo.Proveedor;
import sigir.vista.paneles.ProveedoresPanel;

public class ProveedorControlador {

    private final ProveedoresPanel vista;
    private final ProveedorDAO proveedorDAO;

    private List<Proveedor> proveedores = new ArrayList<>();
    private Integer idProveedorSeleccionado;
    private String estadoProveedorSeleccionado;

    public ProveedorControlador(ProveedoresPanel vista) {
        this.vista = vista;
        this.proveedorDAO = new ProveedorDAO();
    }

    public void iniciar() {
        buscar();
        nuevo();
    }

    public void recargar() {
        buscar();
    }

    public void buscar() {
        try {
            proveedores = proveedorDAO.listar(
                    vista.getTextoBusqueda(),
                    vista.getEstadoFiltro()
            );

            vista.mostrarProveedores(proveedores);
            vista.mostrarCantidad(proveedores.size());

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cargar los proveedores.",
                    ex
            );
        }
    }

    public void nuevo() {
        idProveedorSeleccionado = null;
        estadoProveedorSeleccionado = null;

        vista.limpiarFormulario();
        vista.setModoEdicion(false, null);
    }

    public void seleccionarFila() {
        int filaModelo = vista.getFilaSeleccionadaModelo();

        if (filaModelo < 0
                || filaModelo >= proveedores.size()) {

            return;
        }

        Proveedor proveedor = proveedores.get(filaModelo);

        idProveedorSeleccionado =
                proveedor.getIdProveedor();

        estadoProveedorSeleccionado =
                proveedor.getEstado();

        vista.mostrarProveedor(proveedor);
        vista.setModoEdicion(
                true,
                estadoProveedorSeleccionado
        );
    }

    public void guardar() {
        try {
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
                        "Ya existe un proveedor con ese RTN.",
                        "RTN duplicado",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (idProveedorSeleccionado == null) {
                int idGenerado =
                        proveedorDAO.insertar(proveedor);

                idProveedorSeleccionado = idGenerado;

                JOptionPane.showMessageDialog(
                        vista,
                        "Proveedor registrado correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                proveedor.setIdProveedor(
                        idProveedorSeleccionado
                );

                proveedorDAO.actualizar(proveedor);

                JOptionPane.showMessageDialog(
                        vista,
                        "Proveedor actualizado correctamente.",
                        "SIGIR",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            buscar();
            seleccionarProveedorEnTabla(
                    idProveedorSeleccionado
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible guardar el proveedor.",
                    ex
            );
        }
    }

    public void cambiarEstado() {
        if (idProveedorSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un proveedor de la tabla.",
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
                estaActivo ? "INACTIVO" : "ACTIVO";

        String accion =
                estaActivo ? "desactivar" : "activar";

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "¿Deseas " + accion
                + " al proveedor seleccionado?",
                Character.toUpperCase(accion.charAt(0))
                + accion.substring(1) + " proveedor",
                JOptionPane.YES_NO_OPTION,
                estaActivo
                        ? JOptionPane.WARNING_MESSAGE
                        : JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
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
                    + nuevoEstado.toLowerCase() + ".",
                    "SIGIR",
                    JOptionPane.INFORMATION_MESSAGE
            );

            buscar();
            seleccionarProveedorEnTabla(
                    idProveedorSeleccionado
            );

        } catch (SQLException ex) {
            mostrarErrorBaseDatos(
                    "No fue posible cambiar el estado del proveedor.",
                    ex
            );
        }
    }

    private void validar(Proveedor proveedor) {

        if (proveedor.getNombreProveedor() == null
                || proveedor.getNombreProveedor().isBlank()) {

            vista.enfocarNombreProveedor();

            throw new IllegalArgumentException(
                    "Ingresa el nombre del proveedor."
            );
        }

        if (proveedor.getNombreProveedor().length() > 120) {
            vista.enfocarNombreProveedor();

            throw new IllegalArgumentException(
                    "El nombre del proveedor no puede superar "
                    + "120 caracteres."
            );
        }

        String rtn = proveedor.getRtn();

        if (rtn != null && rtn.length() > 20) {
            vista.enfocarRtn();

            throw new IllegalArgumentException(
                    "El RTN no puede superar 20 caracteres."
            );
        }

        String contacto = proveedor.getNombreContacto();

        if (contacto != null && contacto.length() > 100) {
            vista.enfocarContacto();

            throw new IllegalArgumentException(
                    "El nombre de contacto no puede superar "
                    + "100 caracteres."
            );
        }

        String telefono = proveedor.getTelefono();

        if (telefono != null && telefono.length() > 20) {
            vista.enfocarTelefono();

            throw new IllegalArgumentException(
                    "El teléfono no puede superar 20 caracteres."
            );
        }

        if (telefono != null
                && !telefono.matches("[0-9+()\\-\\s]{7,20}")) {

            vista.enfocarTelefono();

            throw new IllegalArgumentException(
                    "El teléfono contiene caracteres no válidos."
            );
        }

        String correo = proveedor.getCorreo();

        if (correo != null && correo.length() > 100) {
            vista.enfocarCorreo();

            throw new IllegalArgumentException(
                    "El correo no puede superar 100 caracteres."
            );
        }

        if (correo != null
                && !correo.matches(
                        "^[A-Za-z0-9._%+-]+"
                        + "@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
                )) {

            vista.enfocarCorreo();

            throw new IllegalArgumentException(
                    "Ingresa un correo electrónico válido."
            );
        }

        String direccion = proveedor.getDireccion();

        if (direccion != null && direccion.length() > 255) {
            vista.enfocarDireccion();

            throw new IllegalArgumentException(
                    "La dirección no puede superar 255 caracteres."
            );
        }
    }

    private void seleccionarProveedorEnTabla(
            int idProveedor) {

        for (int i = 0; i < proveedores.size(); i++) {
            if (proveedores.get(i).getIdProveedor()
                    == idProveedor) {

                vista.seleccionarFilaModelo(i);
                seleccionarFila();
                break;
            }
        }
    }

    private void mostrarErrorBaseDatos(
            String mensaje,
            SQLException ex) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje + "\n\nDetalle: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}
