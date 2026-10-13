package ir.crm.productivity.task;

import ir.crm.identity.user.User;
import ir.crm.productivity.task.dto.TaskCreateRequest;
import ir.crm.productivity.task.dto.TaskResponse;
import ir.crm.productivity.task.dto.TaskUpdateRequest;
import ir.crm.sales.opportunity.Opportunity;
import ir.crm.shared.web.LookupResponse;
import ir.crm.support.supportcase.SupportCase;
import java.util.List;

public final class TaskMapper {

    private TaskMapper() {}

    public static Task toEntity(TaskCreateRequest r, Opportunity opp, SupportCase sc, User user) {
        Task e = new Task();
        e.setTitle(r.title().trim());
        e.setStatus(r.status());
        e.setPriority(r.priority());
        e.setDueDate(r.dueDate());
        e.setOpportunity(opp);
        e.setSupportCase(sc);
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(Task e, TaskUpdateRequest r, Opportunity opp, SupportCase sc, User user) {
        e.setTitle(r.title().trim());
        e.setStatus(r.status());
        e.setPriority(r.priority());
        e.setDueDate(r.dueDate());
        e.setOpportunity(opp);
        e.setSupportCase(sc);
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
    }

    public static TaskResponse toResponse(Task e) {
        if (e == null) return null;
        LookupResponse oppLookup = e.getOpportunity() != null
                ? new LookupResponse(String.valueOf(e.getOpportunity().getId()), e.getOpportunity().getName())
                : null;
        LookupResponse scLookup = e.getSupportCase() != null
                ? new LookupResponse(String.valueOf(e.getSupportCase().getId()), e.getSupportCase().getTitle())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new TaskResponse(
                String.valueOf(e.getId()),
                e.getTitle(),
                e.getStatus(),
                e.getPriority(),
                e.getDueDate(),
                oppLookup,
                scLookup,
                userLookup,
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<TaskResponse> toResponses(List<Task> entities) {
        return entities.stream().map(TaskMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
