package sigir.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class HorarioVentaRapidaUtil {

    public static final LocalTime HORA_INICIO = LocalTime.of(7, 50);
    public static final LocalTime HORA_FIN = LocalTime.of(11, 0);

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("hh:mm a");

    private HorarioVentaRapidaUtil() {
    }

    public static boolean estaHabilitadaAhora() {
        return estaHabilitada(LocalTime.now());
    }

    public static boolean estaHabilitada(LocalTime hora) {
        if (hora == null) {
            return false;
        }
        return !hora.isBefore(HORA_INICIO)
                && hora.isBefore(HORA_FIN);
    }

    public static String descripcionHorario() {
        return HORA_INICIO.format(FORMATO)
                + " a "
                + HORA_FIN.format(FORMATO);
    }
}
