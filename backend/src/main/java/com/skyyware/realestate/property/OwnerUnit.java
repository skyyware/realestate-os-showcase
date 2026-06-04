package com.skyyware.realestate.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "owner_unit")
public class OwnerUnit {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private String unitLabel;

    @Column(nullable = false)
    private BigDecimal shareValue;

    @Column(nullable = false)
    private BigDecimal votingWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OccupancyType occupancyType;

    protected OwnerUnit() {
    }

    public OwnerUnit(PropertyAsset property, String ownerName, String ownerEmail, String unitLabel, BigDecimal shareValue, BigDecimal votingWeight, OccupancyType occupancyType) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail.trim().toLowerCase();
        this.unitLabel = unitLabel;
        this.shareValue = shareValue;
        this.votingWeight = votingWeight;
        this.occupancyType = occupancyType;
    }

    public UUID id() {
        return id;
    }

    public String ownerName() {
        return ownerName;
    }

    public String ownerEmail() {
        return ownerEmail;
    }

    public String unitLabel() {
        return unitLabel;
    }

    public BigDecimal shareValue() {
        return shareValue;
    }

    public BigDecimal votingWeight() {
        return votingWeight;
    }

    public OccupancyType occupancyType() {
        return occupancyType;
    }
}
