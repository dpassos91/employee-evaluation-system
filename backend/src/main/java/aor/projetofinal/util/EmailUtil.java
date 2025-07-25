package aor.projetofinal.util;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;


public class EmailUtil {

    public static void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = "grupo7.exemplo@gmail.com"; // o nosso email
        final String password = "cdrjtpkfmzvsbasc";     // usa App Password do Gmail

        //configurações do servidor SMTP do Gmail

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587"); // porta TLS
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // STARTTLS
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        Session session = Session.getInstance(props, auth);
        session.setDebug(true);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(subject);
            msg.setText(body);
            //Envia a mensagem através da sessão SMTP.
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


}
