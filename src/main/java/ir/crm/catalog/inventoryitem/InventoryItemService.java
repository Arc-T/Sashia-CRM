package ir.crm.catalog.inventoryitem;

import ir.crm.catalog.inventoryitem.dto.InventoryItemCreateRequest;
import ir.crm.catalog.inventoryitem.dto.InventoryItemResponse;
import ir.crm.catalog.inventoryitem.dto.InventoryItemUpdateRequest;
import ir.crm.catalog.product.Product;
import ir.crm.catalog.product.ProductRepository;
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
public class InventoryItemService {

    private final InventoryItemRepository repository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : InventoryItemSpecification.pageable(params);
        Specification<InventoryItem> spec = InventoryItemSpecification.build(params);
        return repository.findAll(spec, effective).map(InventoryItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse get(Long id) {
        return InventoryItemMapper.toResponse(find(id));
    }

    public InventoryItemResponse create(InventoryItemCreateRequest request) {
        Product product = resolveProduct(request.product());
        InventoryItem item = InventoryItemMapper.toEntity(request, product);
        InventoryItem saved = repository.save(item);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getSku());
        return InventoryItemMapper.toResponse(saved);
    }

    public InventoryItemResponse update(Long id, InventoryItemUpdateRequest request) {
        InventoryItem item = find(id);
        Product product = resolveProduct(request.product());
        InventoryItemMapper.updateEntity(item, request, product);
        InventoryItem saved = repository.save(item);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getSku());
        return InventoryItemMapper.toResponse(saved);
    }

    public void delete(Long id) {
        InventoryItem item = find(id);
        repository.delete(item);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, item.getSku());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<InventoryItem> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("sku")), "%" + search.trim().toLowerCase() + "%");
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(i -> new LookupResponse(String.valueOf(i.getId()), i.getSku()))
                .toList();
    }

    public InventoryItem find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("inventory", id));
    }

    private Product resolveProduct(Long productId) {
        if (productId == null) return null;
        return productRepository.findById(productId)
                .orElseThrow(() -> new InvalidReferenceException("inventory.product", productId));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "inventory", id, changeType, text, null, Instant.now()
        ));
    }
}
