package ir.crm.productivity.email;

import ir.crm.productivity.email.dto.EmailCreateRequest;
import ir.crm.productivity.email.dto.EmailResponse;
import ir.crm.productivity.email.dto.EmailUpdateRequest;
import ir.crm.sales.contact.Contact;
import ir.crm.sales.contact.ContactRepository;
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
public class EmailService {

    private final EmailRepository repository;
    private final ContactRepository contactRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<EmailResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : EmailSpecification.pageable(params);
        Specification<Email> spec = EmailSpecification.build(params);
        return repository.findAll(spec, effective).map(EmailMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EmailResponse get(Long id) {
        return EmailMapper.toResponse(find(id));
    }

    public EmailResponse create(EmailCreateRequest request) {
        Contact contact = resolveContact(request.contact());
        Email email = EmailMapper.toEntity(request, contact);
        Email saved = repository.save(email);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getSubject());
        return EmailMapper.toResponse(saved);
    }

    public EmailResponse update(Long id, EmailUpdateRequest request) {
        Email email = find(id);
        Contact contact = resolveContact(request.contact());
        EmailMapper.updateEntity(email, request, contact);
        Email saved = repository.save(email);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getSubject());
        return EmailMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Email email = find(id);
        repository.delete(email);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, email.getSubject());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Email> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("subject")), pattern),
                    cb.like(cb.lower(root.get("toAddress")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(e -> new LookupResponse(String.valueOf(e.getId()), e.getSubject()))
                .toList();
    }

    public Email find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("email", id));
    }

    private Contact resolveContact(Long id) {
        if (id == null) return null;
        return contactRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("email.contact", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "emails", id, changeType, text, null, Instant.now()
        ));
    }
}
