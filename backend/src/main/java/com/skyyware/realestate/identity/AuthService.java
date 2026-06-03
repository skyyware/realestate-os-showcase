package com.skyyware.realestate.identity;

import com.skyyware.realestate.activity.ActivityEvent;
import com.skyyware.realestate.activity.ActivityEventRepository;
import com.skyyware.realestate.config.RealEstateProperties;
import com.skyyware.realestate.mail.TransactionalMailService;
import com.skyyware.realestate.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final RegistrationTokenRepository tokens;
    private final ActivityEventRepository activities;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TransactionalMailService mailService;
    private final RealEstateProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository users,
            RegistrationTokenRepository tokens,
            ActivityEventRepository activities,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TransactionalMailService mailService,
            RealEstateProperties appProperties
    ) {
        this.users = users;
        this.tokens = tokens;
        this.activities = activities;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.appProperties = appProperties;
    }

    @Transactional
    public RegistrationResult register(String email, String fullName, String organizationName) {
        String normalizedEmail = EmailAddressPolicy.normalize(email);
        AppUser user = users.findByEmail(normalizedEmail)
                .map(existing -> {
                    existing.updateProfile(fullName, organizationName);
                    return existing;
                })
                .orElseGet(() -> users.save(new AppUser(normalizedEmail, fullName, organizationName)));

        String rawToken = newToken();
        tokens.save(new RegistrationToken(user, hash(rawToken), Instant.now().plusSeconds(60 * 60 * 48)));
        String setupLink = appProperties.publicBaseUrl() + "/set-password?token=" + rawToken;
        var delivery = mailService.sendPasswordSetup(user.email(), user.fullName(), setupLink);
        activities.save(new ActivityEvent(user, null, "REGISTRATION", "Zugang angelegt und Aktivierungslink vorbereitet."));
        return new RegistrationResult(delivery.emailSent(), delivery.localSetupLink());
    }

    @Transactional(readOnly = true)
    public RegistrationPreview previewRegistration(String rawToken) {
        RegistrationToken token = tokens.findByTokenHash(hash(rawToken))
                .filter(candidate -> candidate.isUsable(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Token ist ungültig oder abgelaufen."));
        AppUser user = token.user();
        return new RegistrationPreview(user.email(), user.fullName(), user.organizationName());
    }

    @Transactional
    public AuthSession setPassword(String rawToken, String password) {
        RegistrationToken token = tokens.findByTokenHash(hash(rawToken))
                .filter(candidate -> candidate.isUsable(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Token ist ungültig oder abgelaufen."));
        AppUser user = token.user();
        user.activate(passwordEncoder.encode(password));
        token.markUsed();
        activities.save(new ActivityEvent(user, null, "ACTIVATION", "Passwort gesetzt. Workspace ist bereit für die erste Immobilie."));
        return sessionFor(user);
    }

    @Transactional(readOnly = true)
    public AuthSession login(String email, String password) {
        AppUser user = users.findByEmail(EmailAddressPolicy.normalize(email))
                .orElseThrow(() -> new IllegalArgumentException("E-Mail oder Passwort ist falsch."));
        if (user.status() != UserStatus.ACTIVE || user.passwordHash() == null || !passwordEncoder.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("E-Mail oder Passwort ist falsch.");
        }
        return sessionFor(user);
    }

    private AuthSession sessionFor(AppUser user) {
        return new AuthSession(jwtService.issue(user), new UserView(user.email(), user.fullName(), user.organizationName(), user.role().name()));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    public record RegistrationResult(boolean emailSent, String localSetupLink) {
    }

    public record RegistrationPreview(String email, String fullName, String organizationName) {
    }

    public record AuthSession(String accessToken, UserView user) {
    }

    public record UserView(String email, String fullName, String organizationName, String role) {
    }
}
