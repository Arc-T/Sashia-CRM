package ir.crm.sales.opportunity;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.contact.Contact;
import ir.crm.sales.contact.ContactRepository;
import ir.crm.sales.opportunity.dto.OpportunityCreateRequest;
import ir.crm.sales.opportunity.dto.OpportunityResponse;
import ir.crm.sales.opportunity.dto.OpportunityUpdateRequest;
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
public class OpportunityService {

    private final OpportunityRepository repository;
    private final OrganizationRepository organizationRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<OpportunityResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : OpportunitySpecification.pageable(params);
        Specification<Opportunity> spec = OpportunitySpecification.build(params);
        return repository.findAll(spec, effective).map(OpportunityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OpportunityResponse get(Long id) {
        return OpportunityMapper.toResponse(find(id));
    }

    public OpportunityResponse create(OpportunityCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        Contact contact = resolveContact(request.contact());
        User user = resolveUser(request.assignedTo());
        Opportunity opp = OpportunityMapper.toEntity(request, org, contact, user);
        Opportunity saved = repository.save(opp);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return OpportunityMapper.toResponse(saved);
    }

    public OpportunityResponse update(Long id, OpportunityUpdateRequest request) {
        Opportunity opp = find(id);
        Organization org = resolveOrganization(request.organization());
        Contact contact = resolveContact(request.contact());
        User user = resolveUser(request.assignedTo());
        OpportunityMapper.updateEntity(opp, request, org, contact, user);
        Opportunity saved = repository.save(opp);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return OpportunityMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Opportunity opp = find(id);
        repository.delete(opp);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, opp.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Opportunity> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(o -> new LookupResponse(String.valueOf(o.getId()), o.getName()))
                .toList();
    }

    public Opportunity find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("opportunity", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) {
            throw new InvalidReferenceException("opportunity.organization", null);
        }
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("opportunity.organization", id));
    }

    private Contact resolveContact(Long id) {
        if (id == null) return null;
        return contactRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("opportunity.contact", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("opportunity.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "opportunities", id, changeType, text, null, Instant.now()
        ));
    }
}
