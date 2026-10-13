package ir.crm.sales.opportunity;

import ir.crm.identity.user.User;
import ir.crm.sales.contact.Contact;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "stage", nullable = false, length = 32)
    private OpportunityStage stage;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "probability")
    private Integer probability;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "source", length = 32)
    private OpportunitySource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "description", length = 2000)
    private String description;

    // Domain helpers
    public boolean isWon() {
        return this.stage == OpportunityStage.WON;
    }

    public boolean isClosed() {
        return this.stage == OpportunityStage.WON || this.stage == OpportunityStage.LOST;
    }

    public BigDecimal getExpectedValue() {
        if (this.amount == null || this.probability == null) return BigDecimal.ZERO;
        return this.amount.multiply(BigDecimal.valueOf(this.probability))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Long getOrganizationId() {
        return this.organization != null ? this.organization.getId() : null;
    }

    public Long getContactId() {
        return this.contact != null ? this.contact.getId() : null;
    }

    public Long getAssignedToId() {
        return this.assignedTo != null ? this.assignedTo.getId() : null;
    }
}
