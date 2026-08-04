package sigir.controlador;

import java.awt.Cursor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import sigir.dao.GestionUsuarioDAO;
import sigir.modelo.RolSistema;
import sigir.modelo.UsuarioGestion;
import sigir.util.PasswordUtil;
import sigir.util.Sesion;
import sigir.vista.paneles.UsuariosPanel;

public class UsuarioControlador {

    private final UsuariosPanel vista;
    private final GestionUsuarioDAO dao;

    private List<UsuarioGestion> usuarios =
            new ArrayList<>();

    private List<RolSistema> roles =
            new ArrayList<>();

    private SwingWorker<DatosCarga, Void>
            trabajadorCarga;

    private SwingWorker<List<UsuarioGestion>, Void>
            trabajadorBusqueda;

    private long ultimaCarga;
    private long versionBusqueda;
    private boolean recargaPendiente;

    private static final long VIGENCIA_DATOS_MS =
            30_000;

    private record Indicadores(
            int total,
            int activos,
            int bloqueados
    ) {
    }

    private record DatosCarga(
            List<RolSistema> roles,
            List<UsuarioGestion> usuarios,
            Indicadores indicadores
    ) {
    }

    private record FiltroUsuarios(
            String texto,
            Integer idRol,
            String estado
    ) {
    }

    public UsuarioControlador(
            UsuariosPanel vista) {

        this.vista = vista;
        this.dao = new GestionUsuarioDAO();
    }

    public void iniciarAsync() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        nuevoUsuario();
        cargarTodoAsync();
    }

    public void recargarAsync() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        cargarTodoAsync();
    }

    public void iniciar() {
        iniciarAsync();
    }

    public void recargar() {
        recargarAsync();
    }

    public void recargarSiNecesario() {
        if (!Sesion.esDueno()) {
            vista.mostrarSinAcceso();
            return;
        }

        long tiempoTranscurrido =
                System.currentTimeMillis()
                - ultimaCarga;

        if (tiempoTranscurrido
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

        final FiltroUsuarios filtro =
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

                List<RolSistema> rolesCargados =
                        dao.listarRolesActivos();

                List<UsuarioGestion> usuariosCargados =
                        dao.listarUsuarios(
                                filtro.texto(),
                                filtro.idRol(),
                                filtro.estado()
                        );

                int[] valores =
                        dao.contarIndicadores();

                return new DatosCarga(
                        rolesCargados,
                        usuariosCargados,
                        new Indicadores(
                                valores[0],
                                valores[1],
                                valores[2]
                        )
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCarga datos = get();

                    roles =
                            new ArrayList<>(
                                    datos.roles()
                            );

                    vista.cargarRoles(roles);

                    if (versionCarga
                            == versionBusqueda) {

                        usuarios =
                                new ArrayList<>(
                                        datos.usuarios()
                                );

                        vista.mostrarUsuarios(
                                usuarios
                        );
                    }

                    vista.actualizarIndicadores(
                            datos.indicadores().total(),
                            datos.indicadores().activos(),
                            datos.indicadores().bloqueados()
                    );

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

                    mostrarError(
                            "No fue posible cargar "
                            + "el módulo de usuarios.",
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

    public void buscarUsuarios() {
        if (!Sesion.esDueno()) {
            return;
        }

        final FiltroUsuarios filtro =
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
            protected List<UsuarioGestion>
                    doInBackground()
                    throws Exception {

                return dao.listarUsuarios(
                        filtro.texto(),
                        filtro.idRol(),
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
                    usuarios =
                            new ArrayList<>(
                                    get()
                            );

                    vista.mostrarUsuarios(
                            usuarios
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

                    mostrarError(
                            "No fue posible consultar "
                            + "los usuarios.",
                            causa
                    );
                }
            }
        };

        trabajadorBusqueda.execute();
    }

    private FiltroUsuarios capturarFiltro() {
        return new FiltroUsuarios(
                vista.getTextoBusqueda(),
                vista.getIdRolFiltro(),
                vista.getEstadoFiltro()
        );
    }

    public void seleccionarUsuario() {
        int fila =
                vista.getFilaUsuarioSeleccionadaModelo();

        if (fila < 0
                || fila >= usuarios.size()) {

            return;
        }

        vista.mostrarUsuario(
                usuarios.get(fila)
        );
    }

    public void nuevoUsuario() {
        vista.limpiarFormulario();
    }

    public void guardarUsuario() {
        char[] contrasena =
                vista.getContrasena();

        char[] confirmacion =
                vista.getConfirmacionContrasena();

        try {
            if (!Sesion.esDueno()) {
                throw new IllegalStateException(
                        "Solo el dueño puede "
                        + "administrar usuarios."
                );
            }

            UsuarioGestion usuario =
                    vista.construirUsuarioFormulario();

            validarUsuario(usuario);

            UsuarioGestion existente =
                    buscarUsuarioPorId(
                            usuario.getIdUsuario()
                    );

            validarProtecciones(
                    usuario,
                    existente
            );

            if (dao.existeNombreUsuario(
                    usuario.getNombreUsuario(),
                    usuario.getIdUsuario())) {

                throw new IllegalArgumentException(
                        "El nombre de usuario "
                        + "ya está registrado."
                );
            }

            if (dao.existeCorreo(
                    usuario.getCorreo(),
                    usuario.getIdUsuario())) {

                throw new IllegalArgumentException(
                        "El correo electrónico "
                        + "ya está registrado."
                );
            }

            boolean nuevo =
                    usuario.getIdUsuario() <= 0;

            if (nuevo) {
                validarContrasena(
                        contrasena,
                        confirmacion
                );

                String hash =
                        PasswordUtil.generarHash(
                                contrasena
                        );

                int idUsuario =
                        dao.insertar(
                                usuario,
                                hash
                        );

                JOptionPane.showMessageDialog(
                        vista,
                        "Usuario creado correctamente.\n"
                        + "ID: "
                        + idUsuario,
                        "Usuario registrado",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                dao.actualizar(usuario);

                JOptionPane.showMessageDialog(
                        vista,
                        "Los datos del usuario "
                        + "fueron actualizados.",
                        "Usuario actualizado",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            nuevoUsuario();
            recargarAsync();

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "No se guardó el usuario",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible guardar "
                    + "el usuario.",
                    ex
            );

        } finally {
            Arrays.fill(
                    contrasena,
                    '\0'
            );

            Arrays.fill(
                    confirmacion,
                    '\0'
            );
        }
    }

    public void restablecerContrasena() {
        int idUsuario =
                vista.getIdUsuarioFormulario();

        if (idUsuario <= 0) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Selecciona un usuario registrado.",
                    "Usuario no seleccionado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        char[][] valores =
                vista.solicitarNuevaContrasena();

        if (valores == null) {
            return;
        }

        char[] nueva = valores[0];
        char[] confirmacion = valores[1];

        try {
            validarContrasena(
                    nueva,
                    confirmacion
            );

            UsuarioGestion usuario =
                    buscarUsuarioPorId(
                            idUsuario
                    );

            if (usuario == null) {
                throw new IllegalArgumentException(
                        "El usuario seleccionado "
                        + "ya no está disponible."
                );
            }

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            vista,
                            "Se cambiará la contraseña de "
                            + usuario.getNombreCompleto()
                            + ".\n"
                            + "Si la cuenta está bloqueada, "
                            + "será desbloqueada.\n\n"
                            + "¿Deseas continuar?",
                            "Confirmar contraseña",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

            if (respuesta
                    != JOptionPane.YES_OPTION) {

                return;
            }

            String hash =
                    PasswordUtil.generarHash(
                            nueva
                    );

            dao.restablecerContrasena(
                    idUsuario,
                    hash
            );

            JOptionPane.showMessageDialog(
                    vista,
                    "La contraseña fue restablecida "
                    + "correctamente.",
                    "Contraseña actualizada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            recargarAsync();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    vista,
                    ex.getMessage(),
                    "Contraseña no válida",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            mostrarError(
                    "No fue posible restablecer "
                    + "la contraseña.",
                    ex
            );

        } finally {
            Arrays.fill(
                    nueva,
                    '\0'
            );

            Arrays.fill(
                    confirmacion,
                    '\0'
            );
        }
    }

    private void validarUsuario(
            UsuarioGestion usuario) {

        if (usuario.getNombreCompleto() == null
                || usuario.getNombreCompleto()
                        .trim()
                        .length() < 5) {

            throw new IllegalArgumentException(
                    "Escribe el nombre completo "
                    + "del usuario."
            );
        }

        if (usuario.getNombreUsuario() == null
                || !usuario.getNombreUsuario()
                        .matches(
                                "[A-Za-z0-9._-]{4,50}"
                        )) {

            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener "
                    + "entre 4 y 50 caracteres y "
                    + "solo puede incluir letras, "
                    + "números, punto, guion "
                    + "o guion bajo."
            );
        }

        if (usuario.getCorreo() == null
                || !usuario.getCorreo()
                        .matches(
                                "^[A-Za-z0-9._%+-]+"
                                + "@[A-Za-z0-9.-]+"
                                + "\\.[A-Za-z]{2,}$"
                        )) {

            throw new IllegalArgumentException(
                    "Escribe un correo "
                    + "electrónico válido."
            );
        }

        if (usuario.getIdRol() <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona un rol."
            );
        }

        if (usuario.getEstado() == null
                || !List.of(
                        "ACTIVO",
                        "INACTIVO",
                        "BLOQUEADO"
                ).contains(
                        usuario.getEstado()
                )) {

            throw new IllegalArgumentException(
                    "Selecciona un estado válido."
            );
        }
    }

    private void validarProtecciones(
            UsuarioGestion nuevoValor,
            UsuarioGestion anterior)
            throws SQLException {

        if (anterior == null) {
            return;
        }

        boolean esMismaCuenta =
                anterior.getIdUsuario()
                == Sesion.getIdUsuario();

        if (esMismaCuenta) {
            if (!"ACTIVO".equals(
                    nuevoValor.getEstado())) {

                throw new IllegalArgumentException(
                        "No puedes desactivar o "
                        + "bloquear tu propia cuenta."
                );
            }

            if (anterior.getIdRol()
                    != nuevoValor.getIdRol()) {

                throw new IllegalArgumentException(
                        "No puedes cambiar el rol "
                        + "de tu propia cuenta."
                );
            }
        }

        boolean dejaDeSerDuenoActivo =
                anterior.esDueno()
                && "ACTIVO".equalsIgnoreCase(
                        anterior.getEstado()
                )
                && (
                    !esRolDueno(
                            nuevoValor.getIdRol()
                    )
                    || !"ACTIVO".equals(
                            nuevoValor.getEstado()
                    )
                );

        if (dejaDeSerDuenoActivo
                && dao.contarDuenosActivos() <= 1) {

            throw new IllegalArgumentException(
                    "Debe permanecer al menos "
                    + "un dueño activo."
            );
        }
    }

    private boolean esRolDueno(
            int idRol) {

        return roles.stream()
                .anyMatch(rol ->
                        rol.getIdRol() == idRol
                        && "DUENO".equalsIgnoreCase(
                                rol.getNombre()
                        )
                );
    }

    private void validarContrasena(
            char[] nueva,
            char[] confirmacion) {

        if (nueva == null
                || nueva.length < 8) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener "
                    + "al menos 8 caracteres."
            );
        }

        if (!Arrays.equals(
                nueva,
                confirmacion)) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden."
            );
        }

        boolean mayuscula = false;
        boolean minuscula = false;
        boolean numero = false;
        boolean simbolo = false;

        for (char caracter : nueva) {
            if (Character.isUpperCase(
                    caracter)) {

                mayuscula = true;

            } else if (Character.isLowerCase(
                    caracter)) {

                minuscula = true;

            } else if (Character.isDigit(
                    caracter)) {

                numero = true;

            } else {
                simbolo = true;
            }
        }

        if (!mayuscula
                || !minuscula
                || !numero
                || !simbolo) {

            throw new IllegalArgumentException(
                    "La contraseña debe incluir "
                    + "mayúscula, minúscula, "
                    + "número y símbolo."
            );
        }
    }

    private UsuarioGestion buscarUsuarioPorId(
            int idUsuario) {

        if (idUsuario <= 0) {
            return null;
        }

        return usuarios.stream()
                .filter(usuario ->
                        usuario.getIdUsuario()
                        == idUsuario
                )
                .findFirst()
                .orElse(null);
    }

    private void mostrarError(
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
