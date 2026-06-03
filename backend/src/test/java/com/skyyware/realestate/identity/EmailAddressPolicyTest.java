package com.skyyware.realestate.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailAddressPolicyTest {
    @Test
    void normalizesSafeAddresses() {
        assertThat(EmailAddressPolicy.normalize("  Sascha@SKYYWARE.com "))
                .isEqualTo("sascha@skyyware.com");
    }

    @Test
    void rejectsCommonTopLevelDomainTyposBeforeSmtp() {
        assertThatThrownBy(() -> EmailAddressPolicy.normalize("sascha@skyyware.cpm"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bitte E-Mail-Adresse prüfen. Meintest du sascha@skyyware.com?");
    }
}
