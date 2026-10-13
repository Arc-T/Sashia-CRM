package ir.crm.support.supportcase;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportCase extends BaseEntity {

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SupportCaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private SupportCasePriority priority;

    @Column(name = "number", length = 255)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "description", length = 2000)
    private String description;

    // Domain helpers
    public boolean isOpen() {
        return this.status != SupportCaseStatus.CLOSED && this.status != SupportCaseStatus.RESOLVED;
    }

    public void resolve() {
        this.status = SupportCaseStatus.RESOLVED;
    }

    public void close() {
        this.status = SupportCaseStatus.CLOSED;
    }

    public String getTitle() {
        return this.subject;
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
