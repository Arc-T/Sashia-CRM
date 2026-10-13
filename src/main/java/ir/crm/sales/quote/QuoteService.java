package ir.crm.sales.quote;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.opportunity.Opportunity;
import ir.crm.sales.opportunity.OpportunityRepository;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationRepository;
import ir.crm.sales.quote.dto.QuoteCreateRequest;
import ir.crm.sales.quote.dto.QuoteResponse;
import ir.crm.sales.quote.dto.QuoteUpdateRequest;
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
public class QuoteService {

    private final QuoteRepository repository;
    private final OrganizationRepository organizationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<QuoteResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : QuoteSpecification.pageable(params);
        Specification<Quote> spec = QuoteSpecification.build(params);
        return repository.findAll(spec, effective).map(QuoteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public QuoteResponse get(Long id) {
        return QuoteMapper.toResponse(find(id));
    }

    public QuoteResponse create(QuoteCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        Opportunity opp = resolveOpportunity(request.opportunity());
        User user = resolveUser(request.assignedTo());
        Quote quote = QuoteMapper.toEntity(request, org, opp, user);
        Quote saved = repository.save(quote);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getSubject());
        return QuoteMapper.toResponse(saved);
    }

    public QuoteResponse update(Long id, QuoteUpdateRequest request) {
        Quote quote = find(id);
        Organization org = resolveOrganization(request.organization());
        Opportunity opp = resolveOpportunity(request.opportunity());
        User user = resolveUser(request.assignedTo());
        QuoteMapper.updateEntity(quote, request, org, opp, user);
        Quote saved = repository.save(quote);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getSubject());
        return QuoteMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Quote quote = find(id);
        repository.delete(quote);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, quote.getSubject());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Quote> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("number")), pattern),
                    cb.like(cb.lower(root.get("subject")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(q -> new LookupResponse(String.valueOf(q.getId()), q.getNumber() + " - " + q.getSubject()))
                .toList();
    }

    public Quote find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("quote", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("quote.organization", id));
    }

    private Opportunity resolveOpportunity(Long id) {
        if (id == null) return null;
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("quote.opportunity", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("quote.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "quotes", id, changeType, text, null, Instant.now()
        ));
    }
}
