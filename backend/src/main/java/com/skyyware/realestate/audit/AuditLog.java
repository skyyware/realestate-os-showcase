package com.skyyware.realestate.audit;

import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.property.PropertyAsset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id")
    private AppUser actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String targetType;

    private UUID targetId;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private Instant occurredAt;

    protected AuditLog() {
    }

    public AuditLog(AppUser actor, PropertyAsset property, String action, String targetType, UUID targetId, String summary) {
        this.id = UUID.randomUUID();
        this.actor = actor;
        this.property = property;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.summary = summary;
        this.occurredAt = Instant.now();
    }

    public AppUser actor() {
        return actor;
    }

    public PropertyAsset property() {
        return property;
    }

    public String action() {
        return action;
    }

    public String targetType() {
        return targetType;
    }

    public UUID targetId() {
        return targetId;
    }

    public String summary() {
        return summary;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}
