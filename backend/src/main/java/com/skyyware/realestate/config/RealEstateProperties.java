package com.skyyware.realestate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "realestate")
public record RealEstateProperties(
        String publicBaseUrl,
        String apiBaseUrl,
        Security security,
        Mail mail
) {
    public record Security(String jwtSecret) {
    }

    public record Mail(String from, boolean enabled) {
    }
}
