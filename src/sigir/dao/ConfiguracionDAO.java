package sigir.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import sigir.conexion.ConexionBD;
import sigir.modelo.ConfiguracionSistema;

public class ConfiguracionDAO {

    private static final int ID_CONFIGURACION = 1;

    private volatile boolean estructuraAsegurada;

    public void asegurarEstructura()
            throws SQLException {

        if (estructuraAsegurada) {
            return;
        }

        synchronized (this) {
            if (estructuraAsegurada) {
                return;
            }

            asegurarEstructuraInterna();
            estructuraAsegurada = true;
        }
    }

    private void asegurarEstructuraInterna()
            throws SQLException {

        String sql = """
                IF OBJECT_ID(
                    N'dbo.configuracion_sistema',
                    N'U'
                ) IS NULL
                BEGIN
                    CREATE TABLE dbo.configuracion_sistema
                    (
                        id_configuracion TINYINT NOT NULL,
                        nombre_empresa NVARCHAR(150) NOT NULL,
                        rtn VARCHAR(20) NULL,
                        direccion NVARCHAR(300) NULL,
                        telefono VARCHAR(25) NULL,
                        correo VARCHAR(120) NULL,

                        moneda_codigo VARCHAR(10) NOT NULL
                            CONSTRAINT DF_config_moneda
                            DEFAULT ('HNL'),

                        simbolo_moneda NVARCHAR(10) NOT NULL
                            CONSTRAINT DF_config_simbolo
                            DEFAULT (N'L'),

                        porcentaje_impuesto DECIMAL(5,2) NOT NULL
                            CONSTRAINT DF_config_impuesto
                            DEFAULT (0),

                        prefijo_factura VARCHAR(20) NOT NULL
                            CONSTRAINT DF_config_prefijo
                            DEFAULT ('FAC'),

                        pie_factura NVARCHAR(500) NULL,

                        logo VARBINARY(MAX) NULL,
                        logo_nombre NVARCHAR(255) NULL,

                        smtp_host VARCHAR(150) NULL,
                        smtp_puerto INT NULL,
                        smtp_usuario VARCHAR(150) NULL,
                        smtp_tls BIT NOT NULL
                            CONSTRAINT DF_config_smtp_tls
                            DEFAULT (1),

                        nombre_remitente NVARCHAR(150) NULL,
                        ruta_respaldo_servidor NVARCHAR(500) NULL,

                        fecha_actualizacion DATETIME2(0) NOT NULL
                            CONSTRAINT DF_config_fecha
                            DEFAULT (SYSDATETIME()),

                        id_usuario_actualiza INT NULL,

                        CONSTRAINT PK_configuracion_sistema
                            PRIMARY KEY (id_configuracion),

                        CONSTRAINT CK_configuracion_unica
                            CHECK (id_configuracion = 1),

                        CONSTRAINT CK_configuracion_impuesto
                            CHECK
                            (
                                porcentaje_impuesto
                                BETWEEN 0 AND 100
                            ),

                        CONSTRAINT CK_configuracion_smtp_puerto
                            CHECK
                            (
                                smtp_puerto IS NULL
                                OR smtp_puerto
                                   BETWEEN 1 AND 65535
                            ),

                        CONSTRAINT FK_configuracion_usuario
                            FOREIGN KEY (id_usuario_actualiza)
                            REFERENCES dbo.usuarios(id_usuario)
                    );
                END;

                IF NOT EXISTS
                (
                    SELECT 1
                    FROM dbo.configuracion_sistema
                    WHERE id_configuracion = 1
                )
                BEGIN
                    INSERT INTO dbo.configuracion_sistema
                    (
                        id_configuracion,
                        nombre_empresa,
                        rtn,
                        direccion,
                        telefono,
                        correo,
                        moneda_codigo,
                        simbolo_moneda,
                        porcentaje_impuesto,
                        prefijo_factura,
                        pie_factura,
                        smtp_host,
                        smtp_puerto,
                        smtp_usuario,
                        smtp_tls,
                        nombre_remitente,
                        ruta_respaldo_servidor
                    )
                    VALUES
                    (
                        1,
                        N'Inversiones Rodríguez',
                        NULL,
                        N'Calle continua al restaurante La Sierra, '
                        + N'pasando por el cuarto túmulo.',
                        '9605-6666',
                        NULL,
                        'HNL',
                        N'L',
                        0,
                        'FAC',
                        N'Gracias por su compra.',
                        'smtp.gmail.com',
                        587,
                        NULL,
                        1,
                        N'Inversiones Rodríguez',
                        N'C:\\SIGIR\\Respaldos'
                    );
                END;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             Statement sentencia =
                     conexion.createStatement()) {

            sentencia.execute(sql);
        }
    }

    public ConfiguracionSistema obtener()
            throws SQLException {

        asegurarEstructura();

        String sql = """
                SELECT
                    c.id_configuracion,
                    c.nombre_empresa,
                    c.rtn,
                    c.direccion,
                    c.telefono,
                    c.correo,
                    c.moneda_codigo,
                    c.simbolo_moneda,
                    c.porcentaje_impuesto,
                    c.prefijo_factura,
                    c.pie_factura,
                    c.logo,
                    c.logo_nombre,
                    c.smtp_host,
                    c.smtp_puerto,
                    c.smtp_usuario,
                    c.smtp_tls,
                    c.nombre_remitente,
                    c.ruta_respaldo_servidor,
                    c.fecha_actualizacion,
                    c.id_usuario_actualiza,
                    u.nombre_completo
                        AS nombre_usuario_actualiza
                FROM dbo.configuracion_sistema AS c
                LEFT JOIN dbo.usuarios AS u
                    ON u.id_usuario =
                       c.id_usuario_actualiza
                WHERE c.id_configuracion = ?;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    ID_CONFIGURACION
            );

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                if (!resultado.next()) {
                    throw new SQLException(
                            "No existe la configuración principal."
                    );
                }

                return mapear(resultado);
            }
        }
    }

    public void guardar(
            ConfiguracionSistema configuracion,
            int idUsuario) throws SQLException {

        asegurarEstructura();

        String sql = """
                UPDATE dbo.configuracion_sistema
                SET
                    nombre_empresa = ?,
                    rtn = ?,
                    direccion = ?,
                    telefono = ?,
                    correo = ?,
                    moneda_codigo = ?,
                    simbolo_moneda = ?,
                    porcentaje_impuesto = ?,
                    prefijo_factura = ?,
                    pie_factura = ?,
                    logo = ?,
                    logo_nombre = ?,
                    smtp_host = ?,
                    smtp_puerto = ?,
                    smtp_usuario = ?,
                    smtp_tls = ?,
                    nombre_remitente = ?,
                    ruta_respaldo_servidor = ?,
                    fecha_actualizacion =
                        SYSDATETIME(),
                    id_usuario_actualiza = ?
                WHERE id_configuracion = ?;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setNString(
                    1,
                    configuracion.getNombreEmpresa()
            );

            textoNulo(
                    sentencia,
                    2,
                    configuracion.getRtn(),
                    Types.VARCHAR
            );

            textoNulo(
                    sentencia,
                    3,
                    configuracion.getDireccion(),
                    Types.NVARCHAR
            );

            textoNulo(
                    sentencia,
                    4,
                    configuracion.getTelefono(),
                    Types.VARCHAR
            );

            textoNulo(
                    sentencia,
                    5,
                    configuracion.getCorreo(),
                    Types.VARCHAR
            );

            sentencia.setString(
                    6,
                    configuracion.getMonedaCodigo()
            );

            sentencia.setNString(
                    7,
                    configuracion.getSimboloMoneda()
            );

            sentencia.setBigDecimal(
                    8,
                    configuracion.getPorcentajeImpuesto()
            );

            sentencia.setString(
                    9,
                    configuracion.getPrefijoFactura()
            );

            textoNulo(
                    sentencia,
                    10,
                    configuracion.getPieFactura(),
                    Types.NVARCHAR
            );

            byte[] logo =
                    configuracion.getLogo();

            if (logo == null
                    || logo.length == 0) {

                sentencia.setNull(
                        11,
                        Types.VARBINARY
                );
            } else {
                sentencia.setBytes(11, logo);
            }

            textoNulo(
                    sentencia,
                    12,
                    configuracion.getLogoNombre(),
                    Types.NVARCHAR
            );

            textoNulo(
                    sentencia,
                    13,
                    configuracion.getSmtpHost(),
                    Types.VARCHAR
            );

            if (configuracion.getSmtpPuerto() <= 0) {
                sentencia.setNull(
                        14,
                        Types.INTEGER
                );
            } else {
                sentencia.setInt(
                        14,
                        configuracion.getSmtpPuerto()
                );
            }

            textoNulo(
                    sentencia,
                    15,
                    configuracion.getSmtpUsuario(),
                    Types.VARCHAR
            );

            sentencia.setBoolean(
                    16,
                    configuracion.isSmtpTls()
            );

            textoNulo(
                    sentencia,
                    17,
                    configuracion.getNombreRemitente(),
                    Types.NVARCHAR
            );

            textoNulo(
                    sentencia,
                    18,
                    configuracion.getRutaRespaldoServidor(),
                    Types.NVARCHAR
            );

            sentencia.setInt(19, idUsuario);
            sentencia.setInt(
                    20,
                    ID_CONFIGURACION
            );

            if (sentencia.executeUpdate() == 0) {
                throw new SQLException(
                        "No fue posible actualizar la configuración."
                );
            }
        }
    }

    public String obtenerDiagnosticoConexion()
            throws SQLException {

        String sql = """
                SELECT
                    @@SERVERNAME AS servidor,
                    DB_NAME() AS base_datos,
                    SUSER_SNAME() AS usuario_sql,
                    HOST_NAME() AS equipo_cliente,
                    APP_NAME() AS aplicacion,
                    CONNECTIONPROPERTY('local_net_address')
                        AS ip_servidor,
                    CONNECTIONPROPERTY('local_tcp_port')
                        AS puerto,
                    CONNECTIONPROPERTY('client_net_address')
                        AS ip_cliente,
                    CONNECTIONPROPERTY('net_transport')
                        AS transporte,
                    CONNECTIONPROPERTY('encrypt_option')
                        AS cifrado;
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            if (!resultado.next()) {
                return "No se obtuvo información.";
            }

            return """
                    Servidor SQL: %s
                    Base de datos: %s
                    Usuario SQL: %s
                    Equipo cliente: %s
                    Aplicación: %s
                    IP del servidor: %s
                    Puerto TCP: %s
                    IP del cliente: %s
                    Transporte: %s
                    Cifrado: %s
                    """.formatted(
                    valor(resultado, "servidor"),
                    valor(resultado, "base_datos"),
                    valor(resultado, "usuario_sql"),
                    valor(resultado, "equipo_cliente"),
                    valor(resultado, "aplicacion"),
                    valor(resultado, "ip_servidor"),
                    valor(resultado, "puerto"),
                    valor(resultado, "ip_cliente"),
                    valor(resultado, "transporte"),
                    valor(resultado, "cifrado")
            );
        }
    }

    public String crearRespaldo(
            String carpetaServidor,
            int idUsuario) throws SQLException {

        asegurarDueno(idUsuario);

        String carpeta =
                limpiarCarpeta(carpetaServidor);

        String fecha =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd_HHmmss"
                        )
                );

        String archivo =
                carpeta
                + "\\SIGIR_"
                + fecha
                + ".bak";

        String rutaSql =
                escaparSql(archivo);

        String sql = """
                BACKUP DATABASE [SIGIR]
                TO DISK = N'%s'
                WITH
                    COPY_ONLY,
                    INIT,
                    CHECKSUM,
                    STATS = 10;
                """.formatted(rutaSql);

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             Statement sentencia =
                     conexion.createStatement()) {

            sentencia.setQueryTimeout(0);
            sentencia.execute(sql);
        }

        return archivo;
    }

    public void verificarRespaldo(
            String archivoServidor,
            int idUsuario) throws SQLException {

        asegurarDueno(idUsuario);

        String archivo =
                validarRutaBak(
                        archivoServidor
                );

        String sql = """
                RESTORE VERIFYONLY
                FROM DISK = N'%s'
                WITH CHECKSUM;
                """.formatted(
                escaparSql(archivo)
        );

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             Statement sentencia =
                     conexion.createStatement()) {

            sentencia.setQueryTimeout(0);
            sentencia.execute(sql);
        }
    }

    public void restaurarRespaldo(
            String archivoServidor,
            int idUsuario) throws SQLException {

        asegurarDueno(idUsuario);

        String archivo =
                validarRutaBak(
                        archivoServidor
                );

        String sql = """
                USE [master];

                BEGIN TRY
                    ALTER DATABASE [SIGIR]
                    SET SINGLE_USER
                    WITH ROLLBACK IMMEDIATE;

                    RESTORE DATABASE [SIGIR]
                    FROM DISK = N'%s'
                    WITH
                        REPLACE,
                        RECOVERY,
                        CHECKSUM,
                        STATS = 10;

                    ALTER DATABASE [SIGIR]
                    SET MULTI_USER;
                END TRY
                BEGIN CATCH
                    IF DB_ID(N'SIGIR') IS NOT NULL
                    BEGIN
                        ALTER DATABASE [SIGIR]
                        SET MULTI_USER
                        WITH ROLLBACK IMMEDIATE;
                    END;

                    THROW;
                END CATCH;
                """.formatted(
                escaparSql(archivo)
        );

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             Statement sentencia =
                     conexion.createStatement()) {

            sentencia.setQueryTimeout(0);
            sentencia.execute(sql);
        }

        estructuraAsegurada = false;
    }

    private void asegurarDueno(
            int idUsuario) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM dbo.usuarios AS u
                INNER JOIN dbo.roles AS r
                    ON r.id_rol = u.id_rol
                WHERE u.id_usuario = ?
                  AND u.estado = 'ACTIVO'
                  AND r.nombre = 'DUENO';
                """;

        try (Connection conexion =
                     ConexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idUsuario);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                if (!resultado.next()
                        || resultado.getInt(1) == 0) {

                    throw new SQLException(
                            "Solo el dueño activo puede "
                            + "realizar respaldos o restauraciones."
                    );
                }
            }
        }
    }

    private ConfiguracionSistema mapear(
            ResultSet resultado) throws SQLException {

        ConfiguracionSistema configuracion =
                new ConfiguracionSistema();

        configuracion.setIdConfiguracion(
                resultado.getInt(
                        "id_configuracion"
                )
        );

        configuracion.setNombreEmpresa(
                resultado.getString(
                        "nombre_empresa"
                )
        );

        configuracion.setRtn(
                resultado.getString("rtn")
        );

        configuracion.setDireccion(
                resultado.getString("direccion")
        );

        configuracion.setTelefono(
                resultado.getString("telefono")
        );

        configuracion.setCorreo(
                resultado.getString("correo")
        );

        configuracion.setMonedaCodigo(
                resultado.getString(
                        "moneda_codigo"
                )
        );

        configuracion.setSimboloMoneda(
                resultado.getString(
                        "simbolo_moneda"
                )
        );

        configuracion.setPorcentajeImpuesto(
                resultado.getBigDecimal(
                        "porcentaje_impuesto"
                )
        );

        configuracion.setPrefijoFactura(
                resultado.getString(
                        "prefijo_factura"
                )
        );

        configuracion.setPieFactura(
                resultado.getString("pie_factura")
        );

        configuracion.setLogo(
                resultado.getBytes("logo")
        );

        configuracion.setLogoNombre(
                resultado.getString("logo_nombre")
        );

        configuracion.setSmtpHost(
                resultado.getString("smtp_host")
        );

        configuracion.setSmtpPuerto(
                resultado.getInt("smtp_puerto")
        );

        if (resultado.wasNull()) {
            configuracion.setSmtpPuerto(0);
        }

        configuracion.setSmtpUsuario(
                resultado.getString("smtp_usuario")
        );

        configuracion.setSmtpTls(
                resultado.getBoolean("smtp_tls")
        );

        configuracion.setNombreRemitente(
                resultado.getString(
                        "nombre_remitente"
                )
        );

        configuracion.setRutaRespaldoServidor(
                resultado.getString(
                        "ruta_respaldo_servidor"
                )
        );

        Timestamp fecha =
                resultado.getTimestamp(
                        "fecha_actualizacion"
                );

        if (fecha != null) {
            configuracion.setFechaActualizacion(
                    fecha.toLocalDateTime()
            );
        }

        int idUsuario =
                resultado.getInt(
                        "id_usuario_actualiza"
                );

        if (!resultado.wasNull()) {
            configuracion.setIdUsuarioActualiza(
                    idUsuario
            );
        }

        configuracion.setNombreUsuarioActualiza(
                resultado.getString(
                        "nombre_usuario_actualiza"
                )
        );

        return configuracion;
    }

    private void textoNulo(
            PreparedStatement sentencia,
            int posicion,
            String valor,
            int tipoSql) throws SQLException {

        if (valor == null
                || valor.trim().isBlank()) {

            sentencia.setNull(
                    posicion,
                    tipoSql
            );
        } else {
            sentencia.setString(
                    posicion,
                    valor.trim()
            );
        }
    }

    private String valor(
            ResultSet resultado,
            String columna) throws SQLException {

        Object valor =
                resultado.getObject(columna);

        return valor == null
                ? "No disponible"
                : valor.toString();
    }

    private String limpiarCarpeta(
            String carpeta) {

        if (carpeta == null
                || carpeta.trim().isBlank()) {

            throw new IllegalArgumentException(
                    "Escribe la carpeta de respaldos "
                    + "del servidor SQL."
            );
        }

        String limpia =
                carpeta.trim()
                        .replace('/', '\\');

        while (limpia.endsWith("\\")) {
            limpia = limpia.substring(
                    0,
                    limpia.length() - 1
            );
        }

        if (limpia.contains("'")) {
            throw new IllegalArgumentException(
                    "La ruta no puede contener comillas simples."
            );
        }

        return limpia;
    }

    private String validarRutaBak(
            String archivo) {

        if (archivo == null
                || archivo.trim().isBlank()) {

            throw new IllegalArgumentException(
                    "Escribe la ruta completa del archivo .bak "
                    + "en el servidor SQL."
            );
        }

        String limpia =
                archivo.trim()
                        .replace('/', '\\');

        if (!limpia.toLowerCase()
                .endsWith(".bak")) {

            throw new IllegalArgumentException(
                    "El respaldo debe terminar en .bak."
            );
        }

        if (limpia.contains("'")) {
            throw new IllegalArgumentException(
                    "La ruta no puede contener comillas simples."
            );
        }

        return limpia;
    }

    private String escaparSql(String valor) {
        return valor.replace("'", "''");
    }
}
