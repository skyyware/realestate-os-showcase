package com.skyyware.realestate.property;

import com.skyyware.realestate.identity.AppUser;
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
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "property_asset")
public class PropertyAsset {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Integer unitCount;

    @Column(nullable = false)
    private Integer fiscalYear;

    @Column(nullable = false)
    private BigDecimal cashBalance;

    @Column(nullable = false)
    private BigDecimal reserveBalance;

    @Column(nullable = false)
    private BigDecimal reserveTarget;

    @Column(nullable = false)
    private BigDecimal shareTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManagementMode managementMode;

    @Column(nullable = false)
    private Instant createdAt;

    protected PropertyAsset() {
    }

    public PropertyAsset(
            AppUser owner,
            String name,
            String address,
            String city,
            int unitCount,
            int fiscalYear,
            BigDecimal cashBalance,
            BigDecimal reserveBalance,
            BigDecimal reserveTarget,
            BigDecimal shareTotal,
            ManagementMode managementMode
    ) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.city = city;
        this.unitCount = unitCount;
        this.fiscalYear = fiscalYear;
        this.cashBalance = cashBalance;
        this.reserveBalance = reserveBalance;
        this.reserveTarget = reserveTarget;
        this.shareTotal = shareTotal;
        this.managementMode = managementMode;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public AppUser owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public String address() {
        return address;
    }

    public String city() {
        return city;
    }

    public int unitCount() {
        return unitCount;
    }

    public int fiscalYear() {
        return fiscalYear;
    }

    public BigDecimal cashBalance() {
        return cashBalance;
    }

    public BigDecimal reserveBalance() {
        return reserveBalance;
    }

    public BigDecimal reserveTarget() {
        return reserveTarget;
    }

    public BigDecimal shareTotal() {
        return shareTotal;
    }

    public ManagementMode managementMode() {
        return managementMode;
    }
}
