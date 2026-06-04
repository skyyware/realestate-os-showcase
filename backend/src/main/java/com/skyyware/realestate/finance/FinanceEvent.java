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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "finance_event")
public class FinanceEvent {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private PropertyAsset property;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanceEventType eventType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationKey allocationKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_unit_id")
    private OwnerUnit ownerUnit;

    @Column(nullable = false)
    private LocalDate bookedOn;

    private LocalDate dueDate;

    private LocalDate paidOn;

    private String counterparty;

    private String invoiceNumber;

    private String documentReference;

    @Column(nullable = false)
    private String status;

    protected FinanceEvent() {
    }

    public FinanceEvent(
            PropertyAsset property,
            String label,
            FinanceEventType eventType,
            BigDecimal amount,
            String category,
            AllocationKey allocationKey,
            OwnerUnit ownerUnit,
            LocalDate bookedOn,
            LocalDate dueDate,
            LocalDate paidOn,
            String counterparty,
            String invoiceNumber,
            String documentReference,
            String status
    ) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.label = label;
        this.eventType = eventType;
        this.amount = amount;
        this.category = category;
        this.allocationKey = allocationKey;
        this.ownerUnit = ownerUnit;
        this.bookedOn = bookedOn;
        this.dueDate = dueDate;
        this.paidOn = paidOn;
        this.counterparty = counterparty;
        this.invoiceNumber = invoiceNumber;
        this.documentReference = documentReference;
        this.status = status;
    }

    public UUID id() {
        return id;
    }

    public String label() {
        return label;
    }

    public BigDecimal amount() {
        return amount;
    }

    public FinanceEventType eventType() {
        return eventType;
    }

    public String category() {
        return category;
    }

    public AllocationKey allocationKey() {
        return allocationKey;
    }

    public OwnerUnit ownerUnit() {
        return ownerUnit;
    }

    public LocalDate bookedOn() {
        return bookedOn;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public LocalDate paidOn() {
        return paidOn;
    }

    public String counterparty() {
        return counterparty;
    }

    public String invoiceNumber() {
        return invoiceNumber;
    }

    public String documentReference() {
        return documentReference;
    }

    public String status() {
        return status;
    }
}
