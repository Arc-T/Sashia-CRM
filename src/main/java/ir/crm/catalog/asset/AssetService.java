package ir.crm.catalog.asset;

import ir.crm.catalog.asset.dto.AssetCreateRequest;
import ir.crm.catalog.asset.dto.AssetResponse;
import ir.crm.catalog.asset.dto.AssetUpdateRequest;
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
public class AssetService {

    private final AssetRepository repository;
    private final OrganizationRepository organizationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<AssetResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : AssetSpecification.pageable(params);
        Specification<Asset> spec = AssetSpecification.build(params);
        return repository.findAll(spec, effective).map(AssetMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AssetResponse get(Long id) {
        return AssetMapper.toResponse(find(id));
    }

    public AssetResponse create(AssetCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        Asset asset = AssetMapper.toEntity(request, org);
        Asset saved = repository.save(asset);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return AssetMapper.toResponse(saved);
    }

    public AssetResponse update(Long id, AssetUpdateRequest request) {
        Asset asset = find(id);
        Organization org = resolveOrganization(request.organization());
        AssetMapper.updateEntity(asset, request, org);
        Asset saved = repository.save(asset);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return AssetMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Asset asset = find(id);
        repository.delete(asset);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, asset.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Asset> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("serial")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(a -> new LookupResponse(String.valueOf(a.getId()), a.getName()))
                .toList();
    }

    public Asset find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("asset", id));
    }

    private Organization resolveOrganization(Long orgId) {
        if (orgId == null) return null;
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new InvalidReferenceException("asset.organization", orgId));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "assets", id, changeType, text, null, Instant.now()
        ));
    }
}
