package ir.crm.projects.projecttask;

import ir.crm.identity.user.User;
import ir.crm.projects.project.Project;
import ir.crm.projects.projecttask.dto.ProjectTaskCreateRequest;
import ir.crm.projects.projecttask.dto.ProjectTaskResponse;
import ir.crm.projects.projecttask.dto.ProjectTaskUpdateRequest;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class ProjectTaskMapper {

    private ProjectTaskMapper() {}

    public static ProjectTask toEntity(ProjectTaskCreateRequest r, Project project, User user) {
        ProjectTask e = new ProjectTask();
        e.setName(r.name().trim());
        e.setProject(project);
        e.setStatus(r.status());
        e.setPriority(r.priority());
        e.setHours(r.hours());
        e.setDueDate(r.dueDate());
        e.setAssignedTo(user);
        return e;
    }

    public static void updateEntity(ProjectTask e, ProjectTaskUpdateRequest r, Project project, User user) {
        e.setName(r.name().trim());
        e.setProject(project);
        e.setStatus(r.status());
        e.setPriority(r.priority());
        e.setHours(r.hours());
        e.setDueDate(r.dueDate());
        e.setAssignedTo(user);
    }

    public static ProjectTaskResponse toResponse(ProjectTask e) {
        if (e == null) return null;
        LookupResponse projectLookup = e.getProject() != null
                ? new LookupResponse(String.valueOf(e.getProject().getId()), e.getProject().getName())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new ProjectTaskResponse(
                String.valueOf(e.getId()),
                e.getName(),
                projectLookup,
                e.getStatus(),
                e.getPriority(),
                e.getHours(),
                e.getDueDate(),
                userLookup,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<ProjectTaskResponse> toResponses(List<ProjectTask> entities) {
        return entities.stream().map(ProjectTaskMapper::toResponse).toList();
    }
}
