package ir.crm.catalog.serviceoffering;

import ir.crm.catalog.serviceoffering.dto.ServiceOfferingCreateRequest;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingResponse;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingUpdateRequest;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
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
public class ServiceOfferingService {

    private final ServiceOfferingRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<ServiceOfferingResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : ServiceOfferingSpecification.pageable(params);
        Specification<ServiceOffering> spec = ServiceOfferingSpecification.build(params);
        return repository.findAll(spec, effective).map(ServiceOfferingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceOfferingResponse get(Long id) {
        return ServiceOfferingMapper.toResponse(find(id));
    }

    public ServiceOfferingResponse create(ServiceOfferingCreateRequest request) {
        ServiceOffering offering = ServiceOfferingMapper.toEntity(request);
        ServiceOffering saved = repository.save(offering);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return ServiceOfferingMapper.toResponse(saved);
    }

    public ServiceOfferingResponse update(Long id, ServiceOfferingUpdateRequest request) {
        ServiceOffering offering = find(id);
        ServiceOfferingMapper.updateEntity(offering, request);
        ServiceOffering saved = repository.save(offering);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return ServiceOfferingMapper.toResponse(saved);
    }

    public void delete(Long id) {
        ServiceOffering offering = find(id);
        repository.delete(offering);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, offering.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<ServiceOffering> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(s -> new LookupResponse(String.valueOf(s.getId()), s.getName()))
                .toList();
    }

    public ServiceOffering find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("serviceoffering", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "services", id, changeType, text, null, Instant.now()
        ));
    }
}
