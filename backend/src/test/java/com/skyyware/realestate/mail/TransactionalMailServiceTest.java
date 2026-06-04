package com.skyyware.realestate.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skyyware.realestate.config.RealEstateProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class TransactionalMailServiceTest {
    @Test
    void usesProductNameAsSenderDisplayName() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        RealEstateProperties properties = new RealEstateProperties(
                "https://realestate.stage.dev",
                "https://realestate.stage.dev",
                new RealEstateProperties.Security("test-secret-test-secret-test-secret"),
                new RealEstateProperties.Identity("local", "", "realestate-os"),
                new RealEstateProperties.Mail("admin@stage.dev", "Real Estate OS", true)
        );

        TransactionalMailService service = new TransactionalMailService(mailSender, properties);

        service.sendPasswordSetup(
                "stage-debug@skyyware.com",
                "Stage Debug",
                "https://realestate.stage.dev/set-password?token=test"
        );

        InternetAddress from = (InternetAddress) mimeMessage.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("admin@stage.dev");
        assertThat(from.getPersonal()).isEqualTo("Real Estate OS");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void reportsDeliveryProblemsAsUserFacingValidationError() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("recipient domain rejected"))
                .when(mailSender)
                .send(any(MimeMessage.class));

        RealEstateProperties properties = new RealEstateProperties(
                "https://realestate.stage.dev",
                "https://realestate.stage.dev",
                new RealEstateProperties.Security("test-secret-test-secret-test-secret"),
                new RealEstateProperties.Identity("local", "", "realestate-os"),
                new RealEstateProperties.Mail("admin@stage.dev", "Real Estate OS", true)
        );

        TransactionalMailService service = new TransactionalMailService(mailSender, properties);

        assertThatThrownBy(() -> service.sendPasswordSetup(
                "stage-debug@skyyware.cpm",
                "Stage Debug",
                "https://realestate.stage.dev/set-password?token=test"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Aktivierungslink konnte nicht gesendet werden. Bitte E-Mail-Adresse prüfen.");
    }
}
