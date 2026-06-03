package com.skyyware.realestate.identity;

import com.skyyware.realestate.activity.ActivityEvent;
import com.skyyware.realestate.activity.ActivityEventRepository;
import com.skyyware.realestate.config.RealEstateProperties;
import com.skyyware.realestate.finance.FinanceEvent;
import com.skyyware.realestate.finance.FinanceEventRepository;
import com.skyyware.realestate.mail.TransactionalMailService;
import com.skyyware.realestate.property.OwnerUnit;
import com.skyyware.realestate.property.OwnerUnitRepository;
import com.skyyware.realestate.property.PropertyAsset;
import com.skyyware.realestate.property.PropertyAssetRepository;
import com.skyyware.realestate.security.JwtService;
import com.skyyware.realestate.task.TaskPriority;
import com.skyyware.realestate.task.WorkTask;
import com.skyyware.realestate.task.WorkTaskRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final RegistrationTokenRepository tokens;
    private final PropertyAssetRepository properties;
    private final OwnerUnitRepository ownerUnits;
    private final WorkTaskRepository tasks;
    private final FinanceEventRepository finances;
    private final ActivityEventRepository activities;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TransactionalMailService mailService;
    private final RealEstateProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository users,
            RegistrationTokenRepository tokens,
            PropertyAssetRepository properties,
            OwnerUnitRepository ownerUnits,
            WorkTaskRepository tasks,
            FinanceEventRepository finances,
            ActivityEventRepository activities,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TransactionalMailService mailService,
            RealEstateProperties appProperties
    ) {
        this.users = users;
        this.tokens = tokens;
        this.properties = properties;
        this.ownerUnits = ownerUnits;
        this.tasks = tasks;
        this.finances = finances;
        this.activities = activities;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.appProperties = appProperties;
    }

    @Transactional
    public RegistrationResult register(String email, String fullName, String organizationName) {
        String normalizedEmail = email.toLowerCase();
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
        seedWorkspaceIfEmpty(user);
        activities.save(new ActivityEvent(user, null, "ACTIVATION", "Passwort gesetzt und Demo-Workspace aktiviert."));
        return sessionFor(user);
    }

    @Transactional(readOnly = true)
    public AuthSession login(String email, String password) {
        AppUser user = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("E-Mail oder Passwort ist falsch."));
        if (user.status() != UserStatus.ACTIVE || user.passwordHash() == null || !passwordEncoder.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("E-Mail oder Passwort ist falsch.");
        }
        return sessionFor(user);
    }

    private AuthSession sessionFor(AppUser user) {
        return new AuthSession(jwtService.issue(user), new UserView(user.email(), user.fullName(), user.organizationName(), user.role().name()));
    }

    private void seedWorkspaceIfEmpty(AppUser user) {
        if (!properties.findByOwnerOrderByCreatedAtAsc(user).isEmpty()) {
            return;
        }
        PropertyAsset property = properties.save(new PropertyAsset(
                user,
                "WEG Sonnenweg 12",
                "Sonnenweg 12",
                "Stuttgart",
                18,
                new BigDecimal("24350.75"),
                new BigDecimal("58764.20")
        ));
        ownerUnits.save(new OwnerUnit(property, user.fullName(), "Einheit 07", new BigDecimal("84.50")));
        ownerUnits.save(new OwnerUnit(property, "Frauke Schneider", "Einheit 03", new BigDecimal("63.00")));
        ownerUnits.save(new OwnerUnit(property, "Max Mustermann", "Einheit 12", new BigDecimal("71.25")));
        tasks.save(new WorkTask(property, "Wirtschaftsplan 2026 beschließen", "Beschlussvorlage prüfen und zur digitalen Abstimmung freigeben.", TaskPriority.HIGH, LocalDate.now().plusDays(6)));
        tasks.save(new WorkTask(property, "Heizung Wartung freigeben", "Eingangsrechnung aus KI-Erfassung mit Angebot abgleichen.", TaskPriority.MEDIUM, LocalDate.now().plusDays(2)));
        tasks.save(new WorkTask(property, "Eigentümerversammlung vorbereiten", "Tagesordnung finalisieren und Einladungspaket versenden.", TaskPriority.URGENT, LocalDate.now().plusDays(12)));
        finances.save(new FinanceEvent(property, "Hausgeld Juni", new BigDecimal("12850.00"), "Einnahme", LocalDate.now().minusDays(1), "BOOKED"));
        finances.save(new FinanceEvent(property, "Heizung Wartung", new BigDecimal("-695.20"), "Instandhaltung", LocalDate.now().minusDays(2), "REVIEW"));
        finances.save(new FinanceEvent(property, "Gartenpflege", new BigDecimal("-320.00"), "Dienstleistung", LocalDate.now().minusDays(5), "BOOKED"));
        activities.save(new ActivityEvent(user, property, "WORKSPACE", "Demo-Portfolio mit Aufgaben, Finanzen und Einheiten erstellt."));
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
