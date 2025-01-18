package com.github.romanqed.course.email;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

// TODO Replace it with real mailer somehow (or not)
public final class LocalMailerStub implements Mailer {
    private static final String HOST = "mail.smtp.host";
    private static final String PORT = "mail.smtp.port";
    private static final String MAIL_SERVER = System.getenv("MAIL_SERVER");

    @Override
    public void send(String address, String message) {
        var props = (Properties) System.getProperties().clone();
        props.setProperty(HOST, MAIL_SERVER);
        props.setProperty(PORT, "8025");
        var session = Session.getDefaultInstance(props);
        try {
            var email = new MimeMessage(session);
            email.setFrom(new InternetAddress("app@mail.com"));
            email.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
            email.setSubject("Login code");
            email.setText(message);
            Transport.send(email);
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
