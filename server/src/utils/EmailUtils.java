package utils;

import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import listener.Application;

public class EmailUtils {

  public static void sendValidationEmail(String email, String validationCode) {
    Thread t = new Thread(new EmailRunnable(email, "Validation",
        "Your validation code is: " + validationCode));
    t.run();
  }

  private static class EmailRunnable implements Runnable {

    private String email;
    private String subject;
    private String body;

    public EmailRunnable(String email, String subject, String body) {
      this.email = email;
      this.subject = subject;
      this.body = body;
    }

    @Override
    public void run() {
      sendEmail(email, subject, body);
    }

  }

  public static void sendEmail(String to, String subject, String body) {

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("webappsalpha@gmail.com",
                "webappsalpha34");
          }
        });

    MimeMessage message = new MimeMessage(session);
    try {
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(body);
      Transport.send(message);
    } catch (MessagingException e) {
      Application.logger.log(Level.SEVERE,
          "Failed to send message: " + e.getMessage());
    }
  }

}
