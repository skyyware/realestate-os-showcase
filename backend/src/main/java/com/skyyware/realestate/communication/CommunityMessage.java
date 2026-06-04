package com.skyyware.realestate.communication;

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
@Table(name = "community_message")
public class CommunityMessage {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String audience;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    protected CommunityMessage() {
    }

    public CommunityMessage(PropertyAsset property, String audience, String subject, String message, String status) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.audience = audience;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String audience() {
        return audience;
    }

    public String subject() {
        return subject;
    }

    public String message() {
        return message;
    }

    public String status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
