package com.skyyware.realestate.identity;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class EmailAddressPolicy {
    private static final Map<String, String> DOMAIN_SUFFIX_FIXES = Map.of(
            ".cpm", ".com",
            ".cmo", ".com",
            ".con", ".com",
            ".vom", ".com",
            ".deu", ".de"
    );

    private EmailAddressPolicy() {
    }

    static String normalize(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        suggestionFor(normalized).ifPresent(suggestion -> {
            throw new IllegalArgumentException("Bitte E-Mail-Adresse prüfen. Meintest du " + suggestion + "?");
        });
        return normalized;
    }

    static Optional<String> suggestionFor(String normalizedEmail) {
        return DOMAIN_SUFFIX_FIXES.entrySet().stream()
                .filter(entry -> normalizedEmail.endsWith(entry.getKey()))
                .findFirst()
                .map(entry -> normalizedEmail.substring(0, normalizedEmail.length() - entry.getKey().length()) + entry.getValue());
    }
}
