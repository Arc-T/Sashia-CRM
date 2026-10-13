package ir.crm.projects.project;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.projects.project.dto.ProjectCreateRequest;
import ir.crm.projects.project.dto.ProjectResponse;
import ir.crm.projects.project.dto.ProjectUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationRepository;
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
public class ProjectService {

    private final ProjectRepository repository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : ProjectSpecification.pageable(params);
        Specification<Project> spec = ProjectSpecification.build(params);
        return repository.findAll(spec, effective).map(ProjectMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return ProjectMapper.toResponse(find(id));
    }

    public ProjectResponse create(ProjectCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        Project project = ProjectMapper.toEntity(request, org, user);
        Project saved = repository.save(project);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return ProjectMapper.toResponse(saved);
    }

    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        Project project = find(id);
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        ProjectMapper.updateEntity(project, request, org, user);
        Project saved = repository.save(project);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return ProjectMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Project project = find(id);
        repository.delete(project);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, project.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Project> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(p -> new LookupResponse(String.valueOf(p.getId()), p.getName()))
                .toList();
    }

    public Project find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("project", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("project.organization", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("project.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "projects", id, changeType, text, null, Instant.now()
        ));
    }
}
