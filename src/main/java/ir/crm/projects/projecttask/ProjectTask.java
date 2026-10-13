package ir.crm.projects.projecttask;

import ir.crm.identity.user.User;
import ir.crm.projects.project.Project;
import ir.crm.shared.domain.BaseEntity;
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
@Table(name = "project_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTask extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private ProjectTaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 32)
    private ProjectTaskPriority priority;

    @Column(name = "hours")
    private Integer hours;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // Domain helpers
    public boolean isOverdue() {
        return this.dueDate != null && LocalDate.now().isAfter(this.dueDate) && this.status != ProjectTaskStatus.DONE;
    }

    public void complete() {
        this.status = ProjectTaskStatus.DONE;
    }

    public Long getProjectId() {
        return this.project != null ? this.project.getId() : null;
    }

    public Long getAssignedToId() {
        return this.assignedTo != null ? this.assignedTo.getId() : null;
    }
}
