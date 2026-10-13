package ir.crm.catalog.vendor;

import ir.crm.catalog.vendor.dto.VendorCreateRequest;
import ir.crm.catalog.vendor.dto.VendorResponse;
import ir.crm.catalog.vendor.dto.VendorUpdateRequest;
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
public class VendorService {

    private final VendorRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<VendorResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : VendorSpecification.pageable(params);
        Specification<Vendor> spec = VendorSpecification.build(params);
        return repository.findAll(spec, effective).map(VendorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public VendorResponse get(Long id) {
        return VendorMapper.toResponse(find(id));
    }

    public VendorResponse create(VendorCreateRequest request) {
        Vendor vendor = VendorMapper.toEntity(request);
        Vendor saved = repository.save(vendor);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return VendorMapper.toResponse(saved);
    }

    public VendorResponse update(Long id, VendorUpdateRequest request) {
        Vendor vendor = find(id);
        VendorMapper.updateEntity(vendor, request);
        Vendor saved = repository.save(vendor);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return VendorMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Vendor vendor = find(id);
        repository.delete(vendor);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, vendor.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Vendor> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(v -> new LookupResponse(String.valueOf(v.getId()), v.getName()))
                .toList();
    }

    public Vendor find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("vendor", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "vendors", id, changeType, text, null, Instant.now()
        ));
    }
}
