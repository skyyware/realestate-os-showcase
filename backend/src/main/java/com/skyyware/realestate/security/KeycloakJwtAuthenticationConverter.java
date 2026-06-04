package com.skyyware.realestate.security;

import com.skyyware.realestate.config.RealEstateProperties;
import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.identity.ExternalIdentityService;
import com.skyyware.realestate.identity.UserRole;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final ExternalIdentityService identities;
    private final RealEstateProperties properties;

    public KeycloakJwtAuthenticationConverter(ExternalIdentityService identities, RealEstateProperties properties) {
        this.identities = identities;
        this.properties = properties;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        String email = claim(source, "email", source.getSubject() + "@keycloak.local");
        String name = claim(source, "name", claim(source, "preferred_username", email));
        String organization = claim(source, "organization", claim(source, "company", "Keycloak Workspace"));
        UserRole role = roleFrom(source);
        AppUser user = identities.resolve("keycloak", source.getSubject(), email, name, organization, role);
        JwtPrincipal principal = new JwtPrincipal(user.id(), user.email(), user.fullName(), role.name());
        return new UsernamePasswordAuthenticationToken(principal, source, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private UserRole roleFrom(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        Object realmRoles = claimMap(jwt, "realm_access").get("roles");
        if (realmRoles instanceof Collection<?> collection) {
            collection.forEach(role -> roles.add(String.valueOf(role)));
        }
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resources) {
            Object client = resources.get(properties.identity().keycloakClientId());
            if (client instanceof Map<?, ?> clientAccess && clientAccess.get("roles") instanceof Collection<?> clientRoles) {
                clientRoles.forEach(role -> roles.add(String.valueOf(role)));
            }
        }
        Set<String> normalized = roles.stream().map(role -> role.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
        if (normalized.contains("property_manager") || normalized.contains("property-manager")) {
            return UserRole.PROPERTY_MANAGER;
        }
        if (normalized.contains("board_member") || normalized.contains("board-member")) {
            return UserRole.BOARD_MEMBER;
        }
        return UserRole.OWNER_ADMIN;
    }

    private static String claim(Jwt jwt, String name, String fallback) {
        Object value = jwt.getClaims().get(name);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
    }

    private static Map<?, ?> claimMap(Jwt jwt, String name) {
        Object value = jwt.getClaims().get(name);
        return value instanceof Map<?, ?> map ? map : Map.of();
    }
}
