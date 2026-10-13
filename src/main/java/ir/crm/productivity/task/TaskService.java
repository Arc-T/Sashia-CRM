package ir.crm.productivity.task;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.productivity.task.dto.TaskCreateRequest;
import ir.crm.productivity.task.dto.TaskResponse;
import ir.crm.productivity.task.dto.TaskUpdateRequest;
import ir.crm.sales.opportunity.Opportunity;
import ir.crm.sales.opportunity.OpportunityRepository;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.InvalidReferenceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.LookupResponse;
import ir.crm.support.supportcase.SupportCase;
import ir.crm.support.supportcase.SupportCaseRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository repository;
    private final OpportunityRepository opportunityRepository;
    private final SupportCaseRepository supportCaseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : TaskSpecification.pageable(params);
        Specification<Task> spec = TaskSpecification.build(params);
        return repository.findAll(spec, effective).map(TaskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return TaskMapper.toResponse(find(id));
    }

    public TaskResponse create(TaskCreateRequest request) {
        Opportunity opp = resolveOpportunity(request.opportunity());
        SupportCase sc = resolveSupportCase(request.supportCase());
        User user = resolveUser(request.assignedTo());
        Task task = TaskMapper.toEntity(request, opp, sc, user);
        Task saved = repository.save(task);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getTitle());
        return TaskMapper.toResponse(saved);
    }

    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = find(id);
        Opportunity opp = resolveOpportunity(request.opportunity());
        SupportCase sc = resolveSupportCase(request.supportCase());
        User user = resolveUser(request.assignedTo());
        TaskMapper.updateEntity(task, request, opp, sc, user);
        Task saved = repository.save(task);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getTitle());
        return TaskMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Task task = find(id);
        repository.delete(task);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, task.getTitle());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Task> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(t -> new LookupResponse(String.valueOf(t.getId()), t.getTitle()))
                .toList();
    }

    public Task find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task", id));
    }

    private Opportunity resolveOpportunity(Long id) {
        if (id == null) return null;
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("task.opportunity", id));
    }

    private SupportCase resolveSupportCase(Long id) {
        if (id == null) return null;
        return supportCaseRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("task.case", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("task.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "tasks", id, changeType, text, null, Instant.now()
        ));
    }
}
