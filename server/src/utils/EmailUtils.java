package utils;

import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import listener.Application;

public class EmailUtils {

  public static void sendEmail(String to, String subject, String body) {

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("webappsalpha@gmail.com", "webappsalpha34");
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

  public static void main(String[] arg) {
    // sets SMTP server properties
    Properties properties = new Properties();
    properties.put("mail.smtp.host", "smtp.gmail.com");
    properties.put("mail.smtp.port", "587");
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");

    // creates a new session with an authenticator
    Authenticator auth = new Authenticator() {
      public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("webappsbeta@gmail.com","webapps12345");
      }
    };

    Session session = Session.getInstance(properties, auth);
    EmailUtils.sendEmail("bradley45@blueyonder.co.uk", "This is a test", "Hello", session);
  }

}
