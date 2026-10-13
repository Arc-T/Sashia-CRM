package ir.crm.sales.lead;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.marketing.campaign.Campaign;
import ir.crm.marketing.campaign.CampaignRepository;
import ir.crm.sales.lead.dto.LeadCreateRequest;
import ir.crm.sales.lead.dto.LeadResponse;
import ir.crm.sales.lead.dto.LeadUpdateRequest;
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
public class LeadService {

    private final LeadRepository repository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<LeadResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : LeadSpecification.pageable(params);
        Specification<Lead> spec = LeadSpecification.build(params);
        return repository.findAll(spec, effective).map(LeadMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public LeadResponse get(Long id) {
        return LeadMapper.toResponse(find(id));
    }

    public LeadResponse create(LeadCreateRequest request) {
        Campaign campaign = resolveCampaign(request.campaign());
        User user = resolveUser(request.assignedTo());
        Lead lead = LeadMapper.toEntity(request, campaign, user);
        Lead saved = repository.save(lead);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return LeadMapper.toResponse(saved);
    }

    public LeadResponse update(Long id, LeadUpdateRequest request) {
        Lead lead = find(id);
        Campaign campaign = resolveCampaign(request.campaign());
        User user = resolveUser(request.assignedTo());
        LeadMapper.updateEntity(lead, request, campaign, user);
        Lead saved = repository.save(lead);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return LeadMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Lead lead = find(id);
        repository.delete(lead);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, lead.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Lead> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("company")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(l -> new LookupResponse(String.valueOf(l.getId()), l.getName()))
                .toList();
    }

    public Lead find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("lead", id));
    }

    private Campaign resolveCampaign(Long id) {
        if (id == null) return null;
        return campaignRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("lead.campaign", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("lead.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "leads", id, changeType, text, null, Instant.now()
        ));
    }
}
