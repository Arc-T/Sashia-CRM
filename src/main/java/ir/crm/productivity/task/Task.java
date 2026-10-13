package ir.crm.productivity.task;

import ir.crm.identity.user.User;
import ir.crm.sales.opportunity.Opportunity;
import ir.crm.shared.domain.BaseEntity;
import ir.crm.support.supportcase.SupportCase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 32)
    private TaskPriority priority;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_case_id")
    private SupportCase supportCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "description", length = 2000)
    private String description;

    // Domain helpers
    public boolean isOverdue() {
        return this.dueDate != null && LocalDate.now().isAfter(this.dueDate) && this.status != TaskStatus.DONE;
    }

    public void markCompleted() {
        this.status = TaskStatus.DONE;
    }

    public void reopen() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public Long getOpportunityId() {
        return this.opportunity != null ? this.opportunity.getId() : null;
    }

    public Long getSupportCaseId() {
        return this.supportCase != null ? this.supportCase.getId() : null;
    }

    public Long getAssignedToId() {
        return this.assignedTo != null ? this.assignedTo.getId() : null;
    }
}
