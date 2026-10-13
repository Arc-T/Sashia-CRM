package ir.crm.catalog.serviceoffering;

import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOffering extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", length = 255)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32)
    private ServiceOfferingCategory category;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "hours")
    private Integer hours;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "description", length = 2000)
    private String description;

    // Domain helpers
    public boolean hasHourlyEstimate() {
        return this.hours != null && this.hours > 0;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
