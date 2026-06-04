package com.skyyware.realestate.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private String unitLabel;

    @Column(nullable = false)
    private BigDecimal shareValue;

    protected OwnerUnit() {
    }

    public OwnerUnit(PropertyAsset property, String ownerName, String unitLabel, BigDecimal shareValue) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.ownerName = ownerName;
        this.unitLabel = unitLabel;
        this.shareValue = shareValue;
    }

    public UUID id() {
        return id;
    }

    public String ownerName() {
        return ownerName;
    }

    public String unitLabel() {
        return unitLabel;
    }

    public BigDecimal shareValue() {
        return shareValue;
    }
}
