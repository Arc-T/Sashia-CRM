package ir.crm.identity.role;

import ir.crm.identity.role.dto.RoleCreateRequest;
import ir.crm.identity.role.dto.RoleResponse;
import ir.crm.identity.role.dto.RoleUpdateRequest;
import ir.crm.identity.user.UserRepository;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.DuplicateResourceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.security.CurrentUser;
import ir.crm.shared.web.LookupResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository repository;
    private final UserRepository users;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<RoleResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : RoleSpecification.pageable(params);
        Specification<Role> spec = RoleSpecification.build(params);
        Page<Role> page = repository.findAll(spec, effectivePageable);

        Set<Long> ids = page.getContent().stream().map(Role::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Long> counts = new HashMap<>();
        if (!ids.isEmpty()) {
            users.countByRoleIds(ids).forEach(c -> counts.put(c.getRoleId(), c.getTotal()));
        }

        List<RoleResponse> responses = RoleMapper.toResponses(page.getContent(), counts);
        return new PageImpl<>(responses, effectivePageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RoleResponse get(Long id) {
        Role role = find(id);
        long count = users.countByRoleId(id);
        return RoleMapper.toResponse(role, count);
    }

    public RoleResponse create(RoleCreateRequest request) {
        validateCreate(request);
        Role role = RoleMapper.toEntity(request);
        Role saved = repository.save(role);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return RoleMapper.toResponse(saved, 0L);
    }

    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = find(id);
        validateUpdate(id, request);
        RoleMapper.updateEntity(role, request);
        Role saved = repository.save(role);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        long count = users.countByRoleId(id);
        return RoleMapper.toResponse(saved, count);
    }

    public void delete(Long id) {
        Role role = find(id);
        beforeDelete(role);
        repository.delete(role);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, role.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Pageable pageable = PageRequest.of(0, size, Sort.by("name"));
        Specification<Role> spec = RoleSpecification.search(search);
        return repository.findAll(spec, pageable)
                .map(r -> new LookupResponse(String.valueOf(r.getId()), r.getName()))
                .getContent();
    }

    private void validateCreate(RoleCreateRequest r) {
        if (repository.existsByNameIgnoreCase(r.name().trim())) {
            throw new DuplicateResourceException("role.name", r.name());
        }
    }

    private void validateUpdate(Long id, RoleUpdateRequest r) {
        if (repository.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) {
            throw new DuplicateResourceException("role.name", r.name());
        }
    }

    private void beforeDelete(Role entity) {
        long assigned = users.countByRoleId(entity.getId());
        if (assigned > 0) {
            throw new BusinessRuleException("rule.role.inUse", entity.getName(), assigned);
        }
    }

    private Role find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("role", id));
    }

    private void publishEvent(Long id, ChangeType type, String label) {
        eventPublisher.publishEvent(new RecordChangedEvent("roles", id, type, label,
                CurrentUser.username().orElse("system"), Instant.now()));
    }
}
