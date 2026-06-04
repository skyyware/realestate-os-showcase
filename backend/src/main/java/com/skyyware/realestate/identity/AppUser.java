package com.skyyware.realestate.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String organizationName;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String identityProvider;

    private String externalSubject;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant activatedAt;

    protected AppUser() {
    }

    public AppUser(String email, String fullName, String organizationName) {
        this.id = UUID.randomUUID();
        this.email = email.toLowerCase();
        this.fullName = fullName;
        this.organizationName = organizationName;
        this.status = UserStatus.PENDING;
        this.role = UserRole.OWNER_ADMIN;
        this.identityProvider = "local";
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String fullName() {
        return fullName;
    }

    public String organizationName() {
        return organizationName;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public UserStatus status() {
        return status;
    }

    public UserRole role() {
        return role;
    }

    public void updateProfile(String fullName, String organizationName) {
        this.fullName = fullName;
        this.organizationName = organizationName;
    }

    public void activate(String passwordHash) {
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
        this.activatedAt = Instant.now();
    }

    public String identityProvider() {
        return identityProvider;
    }

    public String externalSubject() {
        return externalSubject;
    }

    public void linkExternalIdentity(String identityProvider, String externalSubject) {
        this.identityProvider = identityProvider;
        this.externalSubject = externalSubject;
        this.status = UserStatus.ACTIVE;
        if (this.activatedAt == null) {
            this.activatedAt = Instant.now();
        }
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public static AppUser external(String email, String fullName, String organizationName, String identityProvider, String externalSubject, UserRole role) {
        AppUser user = new AppUser(email, fullName, organizationName);
        user.linkExternalIdentity(identityProvider, externalSubject);
        user.updateRole(role);
        return user;
    }
}
