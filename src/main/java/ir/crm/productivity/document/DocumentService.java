package ir.crm.productivity.document;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.productivity.document.dto.DocumentCreateRequest;
import ir.crm.productivity.document.dto.DocumentResponse;
import ir.crm.productivity.document.dto.DocumentUpdateRequest;
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
public class DocumentService {

    private final DocumentRepository repository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<DocumentResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : DocumentSpecification.pageable(params);
        Specification<Document> spec = DocumentSpecification.build(params);
        return repository.findAll(spec, effective).map(DocumentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(Long id) {
        return DocumentMapper.toResponse(find(id));
    }

    public DocumentResponse create(DocumentCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        Document doc = DocumentMapper.toEntity(request, org, user);
        Document saved = repository.save(doc);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getTitle());
        return DocumentMapper.toResponse(saved);
    }

    public DocumentResponse update(Long id, DocumentUpdateRequest request) {
        Document doc = find(id);
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        DocumentMapper.updateEntity(doc, request, org, user);
        Document saved = repository.save(doc);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getTitle());
        return DocumentMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Document doc = find(id);
        repository.delete(doc);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, doc.getTitle());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Document> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(d -> new LookupResponse(String.valueOf(d.getId()), d.getTitle()))
                .toList();
    }

    public Document find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("document", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("document.organization", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("document.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "documents", id, changeType, text, null, Instant.now()
        ));
    }
}
