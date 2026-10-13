package ir.crm.sales.organization;

import ir.crm.identity.user.User;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "industry", length = 32)
    private OrganizationIndustry industry;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "annual_revenue", precision = 19, scale = 2)
    private BigDecimal annualRevenue;

    @Column(name = "employees")
    private Integer employees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "address", length = 255)
    private String address;

    // Domain helpers
    public boolean isEnterprise() {
        return this.employees != null && this.employees >= 250;
    }

    public boolean hasAnnualRevenue() {
        return this.annualRevenue != null && this.annualRevenue.compareTo(BigDecimal.ZERO) > 0;
    }

    public Long getAssignedToId() {
        return this.assignedTo != null ? this.assignedTo.getId() : null;
    }
}
