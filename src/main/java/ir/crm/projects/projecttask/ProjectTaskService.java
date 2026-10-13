package ir.crm.projects.projecttask;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.projects.project.Project;
import ir.crm.projects.project.ProjectRepository;
import ir.crm.projects.projecttask.dto.ProjectTaskCreateRequest;
import ir.crm.projects.projecttask.dto.ProjectTaskResponse;
import ir.crm.projects.projecttask.dto.ProjectTaskUpdateRequest;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.InvalidReferenceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.LookupResponse;
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
public class ProjectTaskService {

    private final ProjectTaskRepository repository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<ProjectTaskResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : ProjectTaskSpecification.pageable(params);
        Specification<ProjectTask> spec = ProjectTaskSpecification.build(params);
        return repository.findAll(spec, effective).map(ProjectTaskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectTaskResponse get(Long id) {
        return ProjectTaskMapper.toResponse(find(id));
    }

    public ProjectTaskResponse create(ProjectTaskCreateRequest request) {
        Project project = resolveProject(request.project());
        User user = resolveUser(request.assignedTo());
        ProjectTask task = ProjectTaskMapper.toEntity(request, project, user);
        ProjectTask saved = repository.save(task);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return ProjectTaskMapper.toResponse(saved);
    }

    public ProjectTaskResponse update(Long id, ProjectTaskUpdateRequest request) {
        ProjectTask task = find(id);
        Project project = resolveProject(request.project());
        User user = resolveUser(request.assignedTo());
        ProjectTaskMapper.updateEntity(task, request, project, user);
        ProjectTask saved = repository.save(task);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return ProjectTaskMapper.toResponse(saved);
    }

    public void delete(Long id) {
        ProjectTask task = find(id);
        repository.delete(task);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, task.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<ProjectTask> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(t -> new LookupResponse(String.valueOf(t.getId()), t.getName()))
                .toList();
    }

    public ProjectTask find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("projecttask", id));
    }

    private Project resolveProject(Long id) {
        if (id == null) return null;
        return projectRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("projecttask.project", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("projecttask.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "project_tasks", id, changeType, text, null, Instant.now()
        ));
    }
}
