package com.skyyware.realestate.finance;

import com.skyyware.realestate.property.PropertyAsset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate bookedOn;

    @Column(nullable = false)
    private String status;

    protected FinanceEvent() {
    }

    public FinanceEvent(PropertyAsset property, String label, BigDecimal amount, String category, LocalDate bookedOn, String status) {
        this.id = UUID.randomUUID();
        this.property = property;
        this.label = label;
        this.amount = amount;
        this.category = category;
        this.bookedOn = bookedOn;
        this.status = status;
    }

    public String label() {
        return label;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String category() {
        return category;
    }

    public LocalDate bookedOn() {
        return bookedOn;
    }

    public String status() {
        return status;
    }
}
