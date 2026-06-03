package com.skyyware.realestate.mail;

import com.skyyware.realestate.config.RealEstateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mail().from());
        message.setTo(email);
        message.setSubject("RealEstate OS Zugang aktivieren");
        message.setText("""
                Hallo %s,

                willkommen bei RealEstate OS. Bitte vergib dein Passwort über diesen Link:

                %s

                Der Link ist 48 Stunden gültig. Wenn du dich nicht registriert hast, kannst du diese E-Mail ignorieren.

                Viele Grüße
                Sascha Dobrochynskyy
                """.formatted(name, setupLink));
        mailSender.send(message);
        return new MailDelivery(true, null);
    }

    public record MailDelivery(boolean emailSent, String localSetupLink) {
    }
}
