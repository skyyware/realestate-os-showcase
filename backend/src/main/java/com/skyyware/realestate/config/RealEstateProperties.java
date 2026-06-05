package com.skyyware.realestate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "realestate")
public record RealEstateProperties(
        String publicBaseUrl,
        String apiBaseUrl,
        Security security,
        Identity identity,
        Mail mail,
        DocumentStorage documentStorage
) {
    public record Security(String jwtSecret) {
    }

    public record Identity(String mode, String keycloakIssuerUri, String keycloakClientId) {
        public boolean keycloakEnabled() {
            return "keycloak".equalsIgnoreCase(mode) && keycloakIssuerUri != null && !keycloakIssuerUri.isBlank();
        }
    }

    public record Mail(String from, String fromName, boolean enabled) {
    }

    public record DocumentStorage(String rootPath, long maxFileSizeBytes) {
    }
}
