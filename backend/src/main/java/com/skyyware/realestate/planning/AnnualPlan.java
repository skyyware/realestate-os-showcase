package com.skyyware.realestate.planning;

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
import java.util.UUID;

@Entity
@Table(name = "annual_plan")
public class AnnualPlan {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private Integer fiscalYear;

    @Column(nullable = false)
    private BigDecimal houseMoneyBudget;

    @Column(nullable = false)
    private BigDecimal maintenanceBudget;

    @Column(nullable = false)
    private BigDecimal reserveContribution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnualPlanStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected AnnualPlan() {
    }

    public AnnualPlan(PropertyAsset property, int fiscalYear, BigDecimal houseMoneyBudget, BigDecimal maintenanceBudget, BigDecimal reserveContribution, AnnualPlanStatus status) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.fiscalYear = fiscalYear;
        this.houseMoneyBudget = houseMoneyBudget;
        this.maintenanceBudget = maintenanceBudget;
        this.reserveContribution = reserveContribution;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public PropertyAsset property() {
        return property;
    }

    public int fiscalYear() {
        return fiscalYear;
    }

    public BigDecimal houseMoneyBudget() {
        return houseMoneyBudget;
    }

    public BigDecimal maintenanceBudget() {
        return maintenanceBudget;
    }

    public BigDecimal reserveContribution() {
        return reserveContribution;
    }

    public AnnualPlanStatus status() {
        return status;
    }
}
