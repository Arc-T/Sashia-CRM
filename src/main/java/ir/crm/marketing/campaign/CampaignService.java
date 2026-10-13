package ir.crm.marketing.campaign;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.marketing.campaign.dto.CampaignCreateRequest;
import ir.crm.marketing.campaign.dto.CampaignResponse;
import ir.crm.marketing.campaign.dto.CampaignUpdateRequest;
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
public class CampaignService {

    private final CampaignRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<CampaignResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : CampaignSpecification.pageable(params);
        Specification<Campaign> spec = CampaignSpecification.build(params);
        return repository.findAll(spec, effective).map(CampaignMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CampaignResponse get(Long id) {
        return CampaignMapper.toResponse(find(id));
    }

    public CampaignResponse create(CampaignCreateRequest request) {
        User user = resolveUser(request.assignedTo());
        Campaign campaign = CampaignMapper.toEntity(request, user);
        Campaign saved = repository.save(campaign);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return CampaignMapper.toResponse(saved);
    }

    public CampaignResponse update(Long id, CampaignUpdateRequest request) {
        Campaign campaign = find(id);
        User user = resolveUser(request.assignedTo());
        CampaignMapper.updateEntity(campaign, request, user);
        Campaign saved = repository.save(campaign);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return CampaignMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Campaign campaign = find(id);
        repository.delete(campaign);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, campaign.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Campaign> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(c -> new LookupResponse(String.valueOf(c.getId()), c.getName()))
                .toList();
    }

    public Campaign find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("campaign", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("campaign.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "campaigns", id, changeType, text, null, Instant.now()
        ));
    }
}
