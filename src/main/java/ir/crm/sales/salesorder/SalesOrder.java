package ir.crm.sales.salesorder;

import ir.crm.identity.user.User;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.quote.Quote;
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
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder extends BaseEntity {

    @Column(name = "number", nullable = false, length = 255)
    private String number;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", length = 32)
    private SalesOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // Domain helpers
    public boolean isOverdue() {
        return this.dueDate != null && LocalDate.now().isAfter(this.dueDate);
    }

    public boolean isApproved() {
        return this.status == SalesOrderStatus.APPROVED;
    }

    public boolean isDelivered() {
        return this.status == SalesOrderStatus.DELIVERED;
    }

    public Long getOrganizationId() {
        return this.organization != null ? this.organization.getId() : null;
    }

    public Long getQuoteId() {
        return this.quote != null ? this.quote.getId() : null;
    }

    public Long getAssignedToId() {
        return this.assignedTo != null ? this.assignedTo.getId() : null;
    }
}
