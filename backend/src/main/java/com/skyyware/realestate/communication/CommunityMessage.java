package com.skyyware.realestate.communication;

import com.skyyware.realestate.common.WorkContextType;
import com.skyyware.realestate.property.PropertyAsset;
import com.skyyware.realestate.task.WorkTask;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkContextType sourceType;

    private UUID sourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_task_id")
    private WorkTask followUpTask;

    private LocalDate readyToSendOn;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    protected CommunityMessage() {
    }

    public CommunityMessage(
            PropertyAsset property,
            String audience,
            String subject,
            String message,
            MessageStatus status,
            MessageChannel channel,
            WorkContextType sourceType,
            UUID sourceId,
            WorkTask followUpTask,
            LocalDate readyToSendOn
    ) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.audience = audience;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.channel = channel;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.followUpTask = followUpTask;
        this.readyToSendOn = readyToSendOn;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public PropertyAsset property() {
        return property;
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

    public MessageStatus status() {
        return status;
    }

    public MessageChannel channel() {
        return channel;
    }

    public WorkContextType sourceType() {
        return sourceType;
    }

    public UUID sourceId() {
        return sourceId;
    }

    public WorkTask followUpTask() {
        return followUpTask;
    }

    public LocalDate readyToSendOn() {
        return readyToSendOn;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant sentAt() {
        return sentAt;
    }
}
