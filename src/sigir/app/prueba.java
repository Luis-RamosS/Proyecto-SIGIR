package sigir.app;

public class prueba {

    public static void main(String[] args) {

        String usuario =
                System.getenv("SIGIR_SMTP_USER");

        String clave =
                System.getenv("SIGIR_SMTP_PASSWORD");

        System.out.println(
                "Correo configurado: " + usuario
        );

        System.out.println(
                "Clave encontrada: "
                + (clave != null && !clave.isBlank())
        );
    }
}