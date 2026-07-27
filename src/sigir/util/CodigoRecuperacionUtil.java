package sigir.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

public final class CodigoRecuperacionUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );

    private CodigoRecuperacionUtil() {
    }

    public static String generarCodigo() {
        return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
    }

    public static String generarHashSha256(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor no puede ser nulo.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexadecimal = new StringBuilder(hash.length * 2);

            for (byte dato : hash) {
                hexadecimal.append(
                        String.format(Locale.ROOT, "%02X", dato & 0xFF)
                );
            }

            return hexadecimal.toString();

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 no está disponible en esta instalación de Java.",
                    ex
            );
        }
    }

    public static String normalizarCorreo(String correo) {
        return correo == null
                ? ""
                : correo.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean esCorreoValido(String correo) {
        String correoNormalizado = normalizarCorreo(correo);

        return !correoNormalizado.isBlank()
                && correoNormalizado.length() <= 254
                && PATRON_CORREO.matcher(correoNormalizado).matches();
    }

    public static String ocultarCorreo(String correo) {
        String correoNormalizado = normalizarCorreo(correo);
        int posicionArroba = correoNormalizado.indexOf('@');

        if (posicionArroba <= 1) {
            return correoNormalizado;
        }

        String usuario = correoNormalizado.substring(0, posicionArroba);
        String dominio = correoNormalizado.substring(posicionArroba);

        String visible = usuario.substring(0, Math.min(2, usuario.length()));
        return visible + "••••" + dominio;
    }
}
