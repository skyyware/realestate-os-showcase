package com.skyyware.realestate.property;

import com.skyyware.realestate.identity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_member")
public class CommunityMember {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    private Instant invitedAt;

    private Instant acceptedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected CommunityMember() {
    }

    private CommunityMember(PropertyAsset property, AppUser user, String fullName, String email, CommunityRole role, MemberStatus status) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.user = user;
        this.fullName = fullName;
        this.email = normalizeEmail(email);
        this.role = role;
        this.status = status;
        this.createdAt = Instant.now();
        if (status == MemberStatus.INVITED) {
            this.invitedAt = this.createdAt;
        }
        if (status == MemberStatus.ACTIVE) {
            this.acceptedAt = this.createdAt;
        }
    }

    public static CommunityMember active(PropertyAsset property, AppUser user, CommunityRole role) {
        return new CommunityMember(property, user, user.fullName(), user.email(), role, MemberStatus.ACTIVE);
    }

    public static CommunityMember invited(PropertyAsset property, String fullName, String email, CommunityRole role) {
        return new CommunityMember(property, null, fullName, email, role, MemberStatus.INVITED);
    }

    public UUID id() {
        return id;
    }

    public PropertyAsset property() {
        return property;
    }

    public AppUser user() {
        return user;
    }

    public String fullName() {
        return fullName;
    }

    public String email() {
        return email;
    }

    public CommunityRole role() {
        return role;
    }

    public MemberStatus status() {
        return status;
    }

    public Instant invitedAt() {
        return invitedAt;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void updateInvite(String fullName, String email, CommunityRole role) {
        this.fullName = fullName;
        this.email = normalizeEmail(email);
        this.role = role;
        if (this.status != MemberStatus.ACTIVE) {
            this.status = MemberStatus.INVITED;
            this.invitedAt = Instant.now();
        }
    }

    public void attachUser(AppUser user) {
        this.user = user;
        this.status = MemberStatus.ACTIVE;
        this.acceptedAt = Instant.now();
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

