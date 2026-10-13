package ir.crm.support.supportcase;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.contact.Contact;
import ir.crm.sales.contact.ContactRepository;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationRepository;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.InvalidReferenceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.LookupResponse;
import ir.crm.support.supportcase.dto.SupportCaseCreateRequest;
import ir.crm.support.supportcase.dto.SupportCaseResponse;
import ir.crm.support.supportcase.dto.SupportCaseUpdateRequest;
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
public class SupportCaseService {

    private final SupportCaseRepository repository;
    private final OrganizationRepository organizationRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<SupportCaseResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : SupportCaseSpecification.pageable(params);
        Specification<SupportCase> spec = SupportCaseSpecification.build(params);
        return repository.findAll(spec, effective).map(SupportCaseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupportCaseResponse get(Long id) {
        return SupportCaseMapper.toResponse(find(id));
    }

    public SupportCaseResponse create(SupportCaseCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        Contact contact = resolveContact(request.contact());
        User user = resolveUser(request.assignedTo());
        SupportCase sc = SupportCaseMapper.toEntity(request, org, contact, user);
        SupportCase saved = repository.save(sc);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getSubject());
        return SupportCaseMapper.toResponse(saved);
    }

    public SupportCaseResponse update(Long id, SupportCaseUpdateRequest request) {
        SupportCase sc = find(id);
        Organization org = resolveOrganization(request.organization());
        Contact contact = resolveContact(request.contact());
        User user = resolveUser(request.assignedTo());
        SupportCaseMapper.updateEntity(sc, request, org, contact, user);
        SupportCase saved = repository.save(sc);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getSubject());
        return SupportCaseMapper.toResponse(saved);
    }

    public void delete(Long id) {
        SupportCase sc = find(id);
        repository.delete(sc);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, sc.getSubject());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<SupportCase> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("subject")), pattern),
                    cb.like(cb.lower(root.get("number")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(c -> new LookupResponse(String.valueOf(c.getId()), c.getSubject()))
                .toList();
    }

    public SupportCase find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("supportcase", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("supportcase.organization", id));
    }

    private Contact resolveContact(Long id) {
        if (id == null) return null;
        return contactRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("supportcase.contact", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("supportcase.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "cases", id, changeType, text, null, Instant.now()
        ));
    }
}
