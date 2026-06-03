package com.skyyware.realestate.document;

import com.skyyware.realestate.property.PropertyAsset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false)
    private Instant createdAt;

    protected PropertyDocument() {
    }

    public PropertyDocument(PropertyAsset property, String title, String documentType, String fileName, LocalDate documentDate) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.title = title;
        this.documentType = documentType;
        this.fileName = fileName;
        this.documentDate = documentDate;
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
}
