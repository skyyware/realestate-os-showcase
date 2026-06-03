package com.skyyware.realestate.decision;

import com.skyyware.realestate.property.PropertyAsset;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "community_decision")
public class CommunityDecision {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1600)
    private String resolutionText;

    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(nullable = false)
    private String meetingLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus status;

    @Column(nullable = false)
    private int yesVotes;

    @Column(nullable = false)
    private int noVotes;

    @Column(nullable = false)
    private int abstentions;

    @Column(nullable = false)
    private Instant createdAt;

    protected CommunityDecision() {
    }

    public CommunityDecision(
            PropertyAsset property,
            String title,
            String resolutionText,
            LocalDate meetingDate,
            String meetingLocation,
            DecisionStatus status,
            int yesVotes,
            int noVotes,
            int abstentions
    ) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.title = title;
        this.resolutionText = resolutionText;
        this.meetingDate = meetingDate;
        this.meetingLocation = meetingLocation;
        this.status = status;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.abstentions = abstentions;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public PropertyAsset property() {
        return property;
    }

    public String title() {
        return title;
    }

    public String resolutionText() {
        return resolutionText;
    }

    public LocalDate meetingDate() {
        return meetingDate;
    }

    public String meetingLocation() {
        return meetingLocation;
    }

    public DecisionStatus status() {
        return status;
    }

    public int yesVotes() {
        return yesVotes;
    }

    public int noVotes() {
        return noVotes;
    }

    public int abstentions() {
        return abstentions;
    }

    public void transitionTo(DecisionStatus status) {
        this.status = status;
    }
}
