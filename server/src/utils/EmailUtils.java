package utils;

import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import listener.Application;

public class EmailUtils {

  public static void sendEmail(String to, String subject, String body,
      Session session) {

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
