package ir.crm.projects.project;

import ir.crm.identity.user.User;
import ir.crm.projects.project.dto.ProjectCreateRequest;
import ir.crm.projects.project.dto.ProjectResponse;
import ir.crm.projects.project.dto.ProjectUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class ProjectMapper {

    private ProjectMapper() {}

    public static Project toEntity(ProjectCreateRequest r, Organization org, User user) {
        Project e = new Project();
        e.setName(r.name().trim());
        e.setOrganization(org);
        e.setStatus(r.status());
        e.setProgress(r.progress());
        e.setPriority(r.priority());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setBudget(r.budget());
        e.setAssignedTo(user);
        return e;
    }

    public static void updateEntity(Project e, ProjectUpdateRequest r, Organization org, User user) {
        e.setName(r.name().trim());
        e.setOrganization(org);
        e.setStatus(r.status());
        e.setProgress(r.progress());
        e.setPriority(r.priority());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setBudget(r.budget());
        e.setAssignedTo(user);
    }

    public static ProjectResponse toResponse(Project e) {
        if (e == null) return null;
        LookupResponse orgLookup = e.getOrganization() != null
                ? new LookupResponse(String.valueOf(e.getOrganization().getId()), e.getOrganization().getName())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new ProjectResponse(
                String.valueOf(e.getId()),
                e.getName(),
                orgLookup,
                e.getStatus(),
                e.getProgress(),
                e.getPriority(),
                e.getStartDate(),
                e.getEndDate(),
                e.getBudget(),
                userLookup,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<ProjectResponse> toResponses(List<Project> entities) {
        return entities.stream().map(ProjectMapper::toResponse).toList();
    }
}
