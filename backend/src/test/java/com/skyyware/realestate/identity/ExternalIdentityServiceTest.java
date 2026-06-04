package com.skyyware.realestate.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("ci")
@SpringBootTest
class ExternalIdentityServiceTest {
    @Autowired
    private ExternalIdentityService externalIdentities;

    @Autowired
    private AppUserRepository users;

    @Test
    void resolvesKeycloakSubjectToActiveLocalUserBoundary() {
        AppUser created = externalIdentities.resolve(
                "keycloak",
                "subject-123",
                "owner@example.com",
                "Owner Example",
                "WEG Stuttgart",
                UserRole.PROPERTY_MANAGER
        );

        AppUser resolvedAgain = externalIdentities.resolve(
                "keycloak",
                "subject-123",
                "owner@example.com",
                "Owner Example Updated",
                "WEG Stuttgart",
                UserRole.PROPERTY_MANAGER
        );

        assertThat(resolvedAgain.id()).isEqualTo(created.id());
        assertThat(resolvedAgain.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(resolvedAgain.identityProvider()).isEqualTo("keycloak");
        assertThat(resolvedAgain.externalSubject()).isEqualTo("subject-123");
        assertThat(resolvedAgain.fullName()).isEqualTo("Owner Example Updated");
        assertThat(users.findByEmail("owner@example.com")).isPresent();
    }
}
