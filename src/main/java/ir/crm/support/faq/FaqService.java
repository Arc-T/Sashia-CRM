package ir.crm.support.faq;

import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.LookupResponse;
import ir.crm.support.faq.dto.FaqCreateRequest;
import ir.crm.support.faq.dto.FaqResponse;
import ir.crm.support.faq.dto.FaqUpdateRequest;
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
public class FaqService {

    private final FaqRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<FaqResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : FaqSpecification.pageable(params);
        Specification<Faq> spec = FaqSpecification.build(params);
        return repository.findAll(spec, effective).map(FaqMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FaqResponse get(Long id) {
        return FaqMapper.toResponse(find(id));
    }

    public FaqResponse create(FaqCreateRequest request) {
        Faq faq = FaqMapper.toEntity(request);
        Faq saved = repository.save(faq);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getQuestion());
        return FaqMapper.toResponse(saved);
    }

    public FaqResponse update(Long id, FaqUpdateRequest request) {
        Faq faq = find(id);
        FaqMapper.updateEntity(faq, request);
        Faq saved = repository.save(faq);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getQuestion());
        return FaqMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Faq faq = find(id);
        repository.delete(faq);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, faq.getQuestion());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Faq> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("question")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(f -> new LookupResponse(String.valueOf(f.getId()), f.getQuestion()))
                .toList();
    }

    public Faq find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("faq", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "faq", id, changeType, text, null, Instant.now()
        ));
    }
}
