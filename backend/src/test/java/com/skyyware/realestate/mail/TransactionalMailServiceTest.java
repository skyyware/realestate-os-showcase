package com.skyyware.realestate.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.skyyware.realestate.config.RealEstateProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class TransactionalMailServiceTest {
    @Test
    void reportsDeliveryProblemsAsUserFacingValidationError() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("recipient domain rejected"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        RealEstateProperties properties = new RealEstateProperties(
                "https://realestate.stage.dev",
                "https://realestate.stage.dev",
                new RealEstateProperties.Security("test-secret-test-secret-test-secret"),
                new RealEstateProperties.Mail("admin@stage.dev", true)
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
