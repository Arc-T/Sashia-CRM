package ir.crm.sales.organization;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.organization.dto.OrganizationCreateRequest;
import ir.crm.sales.organization.dto.OrganizationResponse;
import ir.crm.sales.organization.dto.OrganizationUpdateRequest;
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
public class OrganizationService {

    private final OrganizationRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<OrganizationResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : OrganizationSpecification.pageable(params);
        Specification<Organization> spec = OrganizationSpecification.build(params);
        return repository.findAll(spec, effective).map(OrganizationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse get(Long id) {
        return OrganizationMapper.toResponse(find(id));
    }

    public OrganizationResponse create(OrganizationCreateRequest request) {
        User user = resolveUser(request.assignedTo());
        Organization org = OrganizationMapper.toEntity(request, user);
        Organization saved = repository.save(org);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return OrganizationMapper.toResponse(saved);
    }

    public OrganizationResponse update(Long id, OrganizationUpdateRequest request) {
        Organization org = find(id);
        User user = resolveUser(request.assignedTo());
        OrganizationMapper.updateEntity(org, request, user);
        Organization saved = repository.save(org);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return OrganizationMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Organization org = find(id);
        repository.delete(org);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, org.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Organization> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(o -> new LookupResponse(String.valueOf(o.getId()), o.getName()))
                .toList();
    }

    public Organization find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("organization", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("organization.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "organizations", id, changeType, text, null, Instant.now()
        ));
    }
}
