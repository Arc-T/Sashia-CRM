package ir.crm.sales.contact;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.contact.dto.ContactCreateRequest;
import ir.crm.sales.contact.dto.ContactResponse;
import ir.crm.sales.contact.dto.ContactUpdateRequest;
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
public class ContactService {

    private final ContactRepository repository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<ContactResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : ContactSpecification.pageable(params);
        Specification<Contact> spec = ContactSpecification.build(params);
        return repository.findAll(spec, effective).map(ContactMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContactResponse get(Long id) {
        return ContactMapper.toResponse(find(id));
    }

    public ContactResponse create(ContactCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        Contact contact = ContactMapper.toEntity(request, org, user);
        Contact saved = repository.save(contact);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return ContactMapper.toResponse(saved);
    }

    public ContactResponse update(Long id, ContactUpdateRequest request) {
        Contact contact = find(id);
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        ContactMapper.updateEntity(contact, request, org, user);
        Contact saved = repository.save(contact);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return ContactMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Contact contact = find(id);
        repository.delete(contact);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, contact.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Contact> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(c -> new LookupResponse(String.valueOf(c.getId()), c.getName()))
                .toList();
    }

    public Contact find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("contact", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("contact.organization", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("contact.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "contacts", id, changeType, text, null, Instant.now()
        ));
    }
}
