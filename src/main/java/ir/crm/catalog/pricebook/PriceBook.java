package ir.crm.catalog.pricebook;

import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "price_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceBook extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private PriceBookStatus status;

    @Column(name = "discount_pct")
    private Integer discountPct;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "description", length = 2000)
    private String description;

    // Domain helpers
    public boolean isActiveOn(LocalDate date) {
        if (this.status != PriceBookStatus.ACTIVE) return false;
        if (date == null) date = LocalDate.now();
        boolean afterStart = (this.validFrom == null || !date.isBefore(this.validFrom));
        boolean beforeEnd = (this.validTo == null || !date.isAfter(this.validTo));
        return afterStart && beforeEnd;
    }

    public void activate() {
        this.status = PriceBookStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = PriceBookStatus.INACTIVE;
    }
}
