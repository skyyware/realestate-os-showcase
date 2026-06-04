package com.skyyware.realestate.finance;

import com.skyyware.realestate.property.OwnerUnit;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "house_money_assessment")
public class HouseMoneyAssessment {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id")
    private OwnerUnit unit;

    @Column(nullable = false)
    private Integer fiscalYear;

    @Column(nullable = false)
    private BigDecimal monthlyHouseMoney;

    @Column(nullable = false)
    private BigDecimal monthlyReserveContribution;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected HouseMoneyAssessment() {
    }

    public HouseMoneyAssessment(PropertyAsset property, OwnerUnit unit, int fiscalYear, BigDecimal monthlyHouseMoney, BigDecimal monthlyReserveContribution, LocalDate validFrom, AssessmentStatus status) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.unit = unit;
        this.fiscalYear = fiscalYear;
        this.monthlyHouseMoney = monthlyHouseMoney;
        this.monthlyReserveContribution = monthlyReserveContribution;
        this.validFrom = validFrom;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public PropertyAsset property() {
        return property;
    }

    public OwnerUnit unit() {
        return unit;
    }

    public int fiscalYear() {
        return fiscalYear;
    }

    public BigDecimal monthlyHouseMoney() {
        return monthlyHouseMoney;
    }

    public BigDecimal monthlyReserveContribution() {
        return monthlyReserveContribution;
    }

    public LocalDate validFrom() {
        return validFrom;
    }

    public AssessmentStatus status() {
        return status;
    }
}
