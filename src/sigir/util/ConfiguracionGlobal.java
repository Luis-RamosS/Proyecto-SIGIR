package sigir.util;

import java.math.BigDecimal;
import java.sql.SQLException;
import sigir.dao.ConfiguracionDAO;
import sigir.modelo.ConfiguracionSistema;

public final class ConfiguracionGlobal {

    private static volatile ConfiguracionSistema actual;

    private ConfiguracionGlobal() {
    }

    public static ConfiguracionSistema obtener() {
        ConfiguracionSistema configuracion = actual;

        if (configuracion != null) {
            return configuracion;
        }

        synchronized (ConfiguracionGlobal.class) {
            if (actual == null) {
                try {
                    actual = new ConfiguracionDAO()
                            .obtener();
                } catch (SQLException ex) {
                    actual = crearPredeterminada();
                }
            }

            return actual;
        }
    }

    public static synchronized void recargar()
            throws SQLException {

        actual = new ConfiguracionDAO().obtener();
    }

    public static synchronized void establecer(
            ConfiguracionSistema configuracion) {

        actual = configuracion;
    }

    public static String nombreEmpresa() {
        return texto(
                obtener().getNombreEmpresa(),
                "Inversiones Rodríguez"
        );
    }

    public static String simboloMoneda() {
        return texto(
                obtener().getSimboloMoneda(),
                "L"
        );
    }

    public static BigDecimal impuestoPorcentaje() {
        return obtener().getPorcentajeImpuesto();
    }

    private static ConfiguracionSistema
            crearPredeterminada() {

        ConfiguracionSistema configuracion =
                new ConfiguracionSistema();

        configuracion.setNombreEmpresa(
                "Inversiones Rodríguez"
        );

        configuracion.setTelefono(
                "9605-6666"
        );

        configuracion.setMonedaCodigo("HNL");
        configuracion.setSimboloMoneda("L");
        configuracion.setPrefijoFactura("FAC");

        return configuracion;
    }

    private static String texto(
            String valor,
            String predeterminado) {

        return valor == null
                || valor.isBlank()
                        ? predeterminado
                        : valor;
    }
}
