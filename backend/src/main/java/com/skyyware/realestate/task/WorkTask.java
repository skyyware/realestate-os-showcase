package com.skyyware.realestate.task;

import com.skyyware.realestate.common.WorkContextType;
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
@Table(name = "work_task")
public class WorkTask {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(nullable = false)
    private String assigneeRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkContextType sourceType;

    private UUID sourceId;

    private LocalDate dueDate;

    private LocalDate reminderDate;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    protected WorkTask() {
    }

    public WorkTask(
            PropertyAsset property,
            String title,
            String description,
            TaskPriority priority,
            String assigneeRole,
            WorkContextType sourceType,
            UUID sourceId,
            LocalDate dueDate,
            LocalDate reminderDate
    ) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assigneeRole = assigneeRole;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.dueDate = dueDate;
        this.reminderDate = reminderDate;
        this.status = TaskStatus.OPEN;
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

    public String description() {
        return description;
    }

    public TaskStatus status() {
        return status;
    }

    public TaskPriority priority() {
        return priority;
    }

    public String assigneeRole() {
        return assigneeRole;
    }

    public WorkContextType sourceType() {
        return sourceType;
    }

    public UUID sourceId() {
        return sourceId;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public LocalDate reminderDate() {
        return reminderDate;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public void transitionTo(TaskStatus status) {
        this.status = status;
        this.completedAt = status == TaskStatus.DONE ? Instant.now() : null;
    }
}
