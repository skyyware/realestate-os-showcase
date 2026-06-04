package com.skyyware.realestate.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalIdentityService {
    private final AppUserRepository users;

    public ExternalIdentityService(AppUserRepository users) {
        this.users = users;
    }

    @Transactional
    public AppUser resolve(String identityProvider, String externalSubject, String email, String fullName, String organizationName, UserRole role) {
        String normalizedEmail = EmailAddressPolicy.normalize(email);
        return users.findByIdentityProviderAndExternalSubject(identityProvider, externalSubject)
                .map(user -> updateProfile(user, fullName, organizationName, role))
                .orElseGet(() -> users.findByEmail(normalizedEmail)
                        .map(user -> {
                            user.linkExternalIdentity(identityProvider, externalSubject);
                            return updateProfile(user, fullName, organizationName, role);
                        })
                        .orElseGet(() -> users.save(AppUser.external(normalizedEmail, fullName, organizationName, identityProvider, externalSubject, role))));
    }

    private AppUser updateProfile(AppUser user, String fullName, String organizationName, UserRole role) {
        user.updateProfile(fullName, organizationName);
        user.updateRole(role);
        return user;
    }
}
