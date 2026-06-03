package com.skyyware.realestate.activity;

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
@Table(name = "activity_event")
public class ActivityEvent {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private Instant createdAt;

    protected ActivityEvent() {
    }

    public ActivityEvent(AppUser user, PropertyAsset property, String eventType, String summary) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.property = property;
        this.eventType = eventType;
        this.summary = summary;
        this.createdAt = Instant.now();
    }

    public String eventType() {
        return eventType;
    }

    public String summary() {
        return summary;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
