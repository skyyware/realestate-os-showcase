package com.skyyware.realestate.document;

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
@Table(name = "property_document")
public class PropertyDocument {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private LocalDate documentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentVisibility visibility;

    @Column(nullable = false)
    private String source;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentLinkType linkedEntityType;

    private UUID linkedEntityId;

    @Column(nullable = false)
    private Instant createdAt;

    protected PropertyDocument() {
    }

    public PropertyDocument(
            PropertyAsset property,
            String title,
            String documentType,
            String fileName,
            LocalDate documentDate,
            DocumentStatus status,
            DocumentVisibility visibility,
            String source,
            String description,
            DocumentLinkType linkedEntityType,
            UUID linkedEntityId
    ) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.title = title;
        this.documentType = documentType;
        this.fileName = fileName;
        this.documentDate = documentDate;
        this.status = status;
        this.visibility = visibility;
        this.source = source;
        this.description = description;
        this.linkedEntityType = linkedEntityType;
        this.linkedEntityId = linkedEntityId;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String documentType() {
        return documentType;
    }

    public String fileName() {
        return fileName;
    }

    public LocalDate documentDate() {
        return documentDate;
    }

    public DocumentStatus status() {
        return status;
    }

    public DocumentVisibility visibility() {
        return visibility;
    }

    public String source() {
        return source;
    }

    public String description() {
        return description;
    }

    public DocumentLinkType linkedEntityType() {
        return linkedEntityType;
    }

    public UUID linkedEntityId() {
        return linkedEntityId;
    }
}
