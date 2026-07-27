package sigir.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final String PREFIJO = "PBKDF2_SHA256";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    private static final int ITERACIONES = 210_000;
    private static final int TAMANO_SALT_BYTES = 16;
    private static final int TAMANO_HASH_BITS = 256;

    private PasswordUtil() {
    }

    public static boolean verificar(
            char[] contrasena,
            String hashAlmacenado) {

        if (contrasena == null
                || hashAlmacenado == null
                || hashAlmacenado.isBlank()) {

            return false;
        }

        try {
            String[] partes = hashAlmacenado.split("\\$");

            if (partes.length != 4) {
                return false;
            }

            if (!PREFIJO.equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = hexadecimalABytes(partes[2]);
            byte[] hashEsperado = hexadecimalABytes(partes[3]);

            byte[] hashCalculado = calcularHash(
                    contrasena,
                    salt,
                    iteraciones,
                    hashEsperado.length * 8
            );

            boolean coincide = MessageDigest.isEqual(
                    hashEsperado,
                    hashCalculado
            );

            Arrays.fill(hashCalculado, (byte) 0);
            return coincide;

        } catch (NumberFormatException ex) {
            return false;

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No fue posible verificar la contraseña.",
                    ex
            );
        }
    }

    public static String generarHash(char[] contrasena) {

        if (contrasena == null || contrasena.length == 0) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacía."
            );
        }

        try {
            byte[] salt = new byte[TAMANO_SALT_BYTES];
            new SecureRandom().nextBytes(salt);

            byte[] hash = calcularHash(
                    contrasena,
                    salt,
                    ITERACIONES,
                    TAMANO_HASH_BITS
            );

            String resultado =
                    PREFIJO
                    + "$" + ITERACIONES
                    + "$" + bytesAHexadecimal(salt)
                    + "$" + bytesAHexadecimal(hash);

            Arrays.fill(hash, (byte) 0);
            return resultado;

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No fue posible generar el hash.",
                    ex
            );
        }
    }

    private static byte[] calcularHash(
            char[] contrasena,
            byte[] salt,
            int iteraciones,
            int tamanoBits) throws Exception {

        PBEKeySpec especificacion = new PBEKeySpec(
                contrasena,
                salt,
                iteraciones,
                tamanoBits
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(ALGORITMO);

            return fabrica
                    .generateSecret(especificacion)
                    .getEncoded();

        } finally {
            especificacion.clearPassword();
        }
    }

    private static String bytesAHexadecimal(byte[] datos) {
        StringBuilder resultado = new StringBuilder(
                datos.length * 2
        );

        for (byte dato : datos) {
            resultado.append(
                    String.format("%02x", dato & 0xff)
            );
        }

        return resultado.toString();
    }

    private static byte[] hexadecimalABytes(String hexadecimal) {

        if (hexadecimal == null
                || hexadecimal.length() % 2 != 0) {

            throw new IllegalArgumentException(
                    "El valor hexadecimal no es válido."
            );
        }

        byte[] resultado = new byte[hexadecimal.length() / 2];

        for (int i = 0; i < hexadecimal.length(); i += 2) {
            resultado[i / 2] = (byte) Integer.parseInt(
                    hexadecimal.substring(i, i + 2),
                    16
            );
        }

        return resultado;
    }
}