package sigir.correo;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import sigir.modelo.SolicitudRecuperacion;

public class CorreoServicio {

    public void enviarCodigo(
            SolicitudRecuperacion solicitud,
            String codigo) throws MessagingException {

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud de recuperación no puede ser nula."
            );
        }

        CorreoConfig.validarConfiguracion();

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.starttls.required", "true");
        propiedades.put("mail.smtp.host", CorreoConfig.getHost());
        propiedades.put(
                "mail.smtp.port",
                String.valueOf(CorreoConfig.getPuerto())
        );
        propiedades.put("mail.smtp.connectiontimeout", "10000");
        propiedades.put("mail.smtp.timeout", "10000");
        propiedades.put("mail.smtp.writetimeout", "10000");

        Session sesionCorreo = Session.getInstance(
                propiedades,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                CorreoConfig.getUsuario(),
                                CorreoConfig.getClaveAplicacion()
                        );
                    }
                }
        );

        MimeMessage mensaje = new MimeMessage(sesionCorreo);

        try {
            mensaje.setFrom(new InternetAddress(
                    CorreoConfig.getRemitente(),
                    "SIGIR",
                    StandardCharsets.UTF_8.name()
            ));
        } catch (UnsupportedEncodingException ex) {
            throw new MessagingException(
                    "No fue posible establecer el nombre del remitente.",
                    ex
            );
        }

        mensaje.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(solicitud.getCorreo(), false)
        );

        mensaje.setSubject(
                "Código de recuperación de SIGIR",
                StandardCharsets.UTF_8.name()
        );

        String nombre = escaparHtml(solicitud.getNombreCompleto());

        String contenido = """
                <html>
                  <body style="font-family:Arial,sans-serif;color:#1c2737;">
                    <div style="max-width:560px;margin:auto;padding:28px;
                                border:1px solid #dbe3ed;border-radius:14px;">
                      <h1 style="color:#15325b;margin-bottom:4px;">SIGIR</h1>
                      <p style="color:#64748b;margin-top:0;">
                        Recuperación de contraseña
                      </p>
                      <p>Hola, <strong>%s</strong>.</p>
                      <p>Tu código de verificación es:</p>
                      <div style="font-size:34px;font-weight:bold;
                                  letter-spacing:8px;color:#356eaf;
                                  text-align:center;padding:18px;
                                  background:#f1f5f9;border-radius:12px;">
                        %s
                      </div>
                      <p>El código vencerá en 10 minutos y solo puede usarse una vez.</p>
                      <p style="color:#64748b;font-size:13px;">
                        Si no solicitaste este cambio, ignora este mensaje.
                      </p>
                    </div>
                  </body>
                </html>
                """.formatted(nombre, codigo);

        mensaje.setContent(contenido, "text/html; charset=UTF-8");

        Transport.send(mensaje);
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "usuario";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
