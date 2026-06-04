package com.skyyware.realestate.mail;

import com.skyyware.realestate.config.RealEstateProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class TransactionalMailService {
    private static final Logger log = LoggerFactory.getLogger(TransactionalMailService.class);

    private final JavaMailSender mailSender;
    private final RealEstateProperties properties;

    public TransactionalMailService(JavaMailSender mailSender, RealEstateProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public MailDelivery sendPasswordSetup(String email, String name, String setupLink) {
        if (!properties.mail().enabled()) {
            log.info("Mail disabled. Password setup link for {}: {}", email, setupLink);
            return new MailDelivery(false, setupLink);
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(
                    properties.mail().from(),
                    properties.mail().fromName(),
                    StandardCharsets.UTF_8.name()
            ));
            helper.setTo(email);
            helper.setSubject("RealEstate OS Zugang aktivieren");
            helper.setText("""
                Hallo %s,

                willkommen bei RealEstate OS. Bitte vergib dein Passwort über diesen Link:

                %s

                Der Link ist 48 Stunden gültig. Wenn du dich nicht registriert hast, kannst du diese E-Mail ignorieren.

                Viele Grüße
                RealEstate OS
                """.formatted(name, setupLink));
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new IllegalStateException("Password setup mail could not be composed.", exception);
        }
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("Password setup mail could not be sent to {}: {}", email, exception.getMessage());
            throw new IllegalArgumentException("Aktivierungslink konnte nicht gesendet werden. Bitte E-Mail-Adresse prüfen.");
        }
        return new MailDelivery(true, null);
    }

    public MailDelivery sendPasswordReset(String email, String name, String setupLink) {
        if (!properties.mail().enabled()) {
            log.info("Mail disabled. Password reset link for {}: {}", email, setupLink);
            return new MailDelivery(false, setupLink);
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(
                    properties.mail().from(),
                    properties.mail().fromName(),
                    StandardCharsets.UTF_8.name()
            ));
            helper.setTo(email);
            helper.setSubject("RealEstate OS Passwort zurücksetzen");
            helper.setText("""
                Hallo %s,

                für deinen RealEstate OS Zugang wurde ein neues Passwort angefordert.
                Bitte vergib dein Passwort über diesen Link:

                %s

                Der Link ist 2 Stunden gültig. Wenn du diese Anfrage nicht ausgelöst hast, kannst du diese E-Mail ignorieren.

                Viele Grüße
                RealEstate OS
                """.formatted(name, setupLink));
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new IllegalStateException("Password reset mail could not be composed.", exception);
        }
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("Password reset mail could not be sent to {}: {}", email, exception.getMessage());
            throw new IllegalArgumentException("Reset-Link konnte nicht gesendet werden. Bitte E-Mail-Adresse prüfen.");
        }
        return new MailDelivery(true, null);
    }

    public MailDelivery sendCommunityInvitation(String email, String name, String propertyName, String roleLabel, String workspaceUrl) {
        if (!properties.mail().enabled()) {
            log.info("Mail disabled. Community invitation for {} to {}: {}", email, propertyName, workspaceUrl);
            return new MailDelivery(false, workspaceUrl);
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(
                    properties.mail().from(),
                    properties.mail().fromName(),
                    StandardCharsets.UTF_8.name()
            ));
            helper.setTo(email);
            helper.setSubject("RealEstate OS Einladung zu " + propertyName);
            helper.setText("""
                Hallo %s,

                du wurdest in RealEstate OS zur WEG "%s" eingeladen.
                Rolle: %s

                Öffne deinen Arbeitsraum hier:
                %s

                Viele Grüße
                RealEstate OS
                """.formatted(name, propertyName, roleLabel, workspaceUrl));
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new IllegalStateException("Community invitation mail could not be composed.", exception);
        }
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("Community invitation mail could not be sent to {}: {}", email, exception.getMessage());
            throw new IllegalArgumentException("Einladung konnte nicht gesendet werden. Bitte E-Mail-Adresse prüfen.");
        }
        return new MailDelivery(true, null);
    }

    public record MailDelivery(boolean emailSent, String localSetupLink) {
    }
}
