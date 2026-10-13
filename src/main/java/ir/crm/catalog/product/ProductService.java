package ir.crm.catalog.product;

import ir.crm.catalog.product.dto.ProductCreateRequest;
import ir.crm.catalog.product.dto.ProductResponse;
import ir.crm.catalog.product.dto.ProductUpdateRequest;
import ir.crm.catalog.vendor.Vendor;
import ir.crm.catalog.vendor.VendorRepository;
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
public class ProductService {

    private final ProductRepository repository;
    private final VendorRepository vendorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : ProductSpecification.pageable(params);
        Specification<Product> spec = ProductSpecification.build(params);
        return repository.findAll(spec, effective).map(ProductMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductMapper.toResponse(find(id));
    }

    public ProductResponse create(ProductCreateRequest request) {
        Vendor vendor = resolveVendor(request.vendor());
        Product product = ProductMapper.toEntity(request, vendor);
        Product saved = repository.save(product);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return ProductMapper.toResponse(saved);
    }

    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = find(id);
        Vendor vendor = resolveVendor(request.vendor());
        ProductMapper.updateEntity(product, request, vendor);
        Product saved = repository.save(product);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return ProductMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Product product = find(id);
        repository.delete(product);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, product.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Product> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("sku")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(p -> new LookupResponse(String.valueOf(p.getId()), p.getName()))
                .toList();
    }

    public Product find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("product", id));
    }

    private Vendor resolveVendor(Long vendorId) {
        if (vendorId == null) return null;
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new InvalidReferenceException("product.vendor", vendorId));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "products", id, changeType, text, null, Instant.now()
        ));
    }
}
