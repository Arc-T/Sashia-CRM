package ir.crm.productivity.event;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.productivity.event.dto.EventCreateRequest;
import ir.crm.productivity.event.dto.EventResponse;
import ir.crm.productivity.event.dto.EventUpdateRequest;
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
public class EventService {

    private final EventRepository repository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<EventResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : EventSpecification.pageable(params);
        Specification<Event> spec = EventSpecification.build(params);
        return repository.findAll(spec, effective).map(EventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) {
        return EventMapper.toResponse(find(id));
    }

    public EventResponse create(EventCreateRequest request) {
        Contact contact = resolveContact(request.contact());
        User user = resolveUser(request.assignedTo());
        Event event = EventMapper.toEntity(request, contact, user);
        Event saved = repository.save(event);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getTitle());
        return EventMapper.toResponse(saved);
    }

    public EventResponse update(Long id, EventUpdateRequest request) {
        Event event = find(id);
        Contact contact = resolveContact(request.contact());
        User user = resolveUser(request.assignedTo());
        EventMapper.updateEntity(event, request, contact, user);
        Event saved = repository.save(event);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getTitle());
        return EventMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Event event = find(id);
        repository.delete(event);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, event.getTitle());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Event> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("location")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(e -> new LookupResponse(String.valueOf(e.getId()), e.getTitle()))
                .toList();
    }

    public Event find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("event", id));
    }

    private Contact resolveContact(Long id) {
        if (id == null) return null;
        return contactRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("event.contact", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("event.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "events", id, changeType, text, null, Instant.now()
        ));
    }
}
