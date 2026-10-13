package ir.crm.sales.servicecontract;

import ir.crm.identity.user.User;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "service_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceContract extends BaseEntity {

    @Column(name = "number", nullable = false, length = 255)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", length = 32)
    private ServiceContractStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "value", precision = 19, scale = 2)
    private BigDecimal value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // Domain helpers
    public boolean isActive() {
        return this.status == ServiceContractStatus.ACTIVE;
    }

    public boolean isExpired() {
        return this.endDate != null && LocalDate.now().isAfter(this.endDate);
    }

    public boolean isUnderWarranty() {
        LocalDate today = LocalDate.now();
        return this.startDate != null && this.endDate != null
                && !today.isBefore(this.startDate) && !today.isAfter(this.endDate);
    }

    public Long getOrganizationId() {
        return this.organization != null ? this.organization.getId() : null;
    }

    public Long getAssignedToId() {
        return this.assignedTo != null ? this.assignedTo.getId() : null;
    }
}
