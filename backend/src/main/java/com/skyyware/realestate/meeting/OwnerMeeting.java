package com.skyyware.realestate.meeting;

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
@Table(name = "owner_meeting")
public class OwnerMeeting {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String agenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected OwnerMeeting() {
    }

    public OwnerMeeting(PropertyAsset property, String title, LocalDate meetingDate, String location, String agenda, MeetingStatus status) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.title = title;
        this.meetingDate = meetingDate;
        this.location = location;
        this.agenda = agenda;
        this.status = status;
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

    public LocalDate meetingDate() {
        return meetingDate;
    }

    public String location() {
        return location;
    }

    public String agenda() {
        return agenda;
    }

    public MeetingStatus status() {
        return status;
    }
}
