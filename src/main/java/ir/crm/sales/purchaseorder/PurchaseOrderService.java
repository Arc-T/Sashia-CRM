package ir.crm.sales.purchaseorder;

import ir.crm.catalog.vendor.Vendor;
import ir.crm.catalog.vendor.VendorRepository;
import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.purchaseorder.dto.PurchaseOrderCreateRequest;
import ir.crm.sales.purchaseorder.dto.PurchaseOrderResponse;
import ir.crm.sales.purchaseorder.dto.PurchaseOrderUpdateRequest;
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
public class PurchaseOrderService {

    private final PurchaseOrderRepository repository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : PurchaseOrderSpecification.pageable(params);
        Specification<PurchaseOrder> spec = PurchaseOrderSpecification.build(params);
        return repository.findAll(spec, effective).map(PurchaseOrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        return PurchaseOrderMapper.toResponse(find(id));
    }

    public PurchaseOrderResponse create(PurchaseOrderCreateRequest request) {
        Vendor vendor = resolveVendor(request.vendor());
        User user = resolveUser(request.assignedTo());
        PurchaseOrder order = PurchaseOrderMapper.toEntity(request, vendor, user);
        PurchaseOrder saved = repository.save(order);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getNumber());
        return PurchaseOrderMapper.toResponse(saved);
    }

    public PurchaseOrderResponse update(Long id, PurchaseOrderUpdateRequest request) {
        PurchaseOrder order = find(id);
        Vendor vendor = resolveVendor(request.vendor());
        User user = resolveUser(request.assignedTo());
        PurchaseOrderMapper.updateEntity(order, request, vendor, user);
        PurchaseOrder saved = repository.save(order);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getNumber());
        return PurchaseOrderMapper.toResponse(saved);
    }

    public void delete(Long id) {
        PurchaseOrder order = find(id);
        repository.delete(order);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, order.getNumber());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("number")), pattern);
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(p -> new LookupResponse(String.valueOf(p.getId()), p.getNumber()))
                .toList();
    }

    public PurchaseOrder find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("purchase_order", id));
    }

    private Vendor resolveVendor(Long id) {
        if (id == null) {
            throw new InvalidReferenceException("purchase_order.vendor", null);
        }
        return vendorRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("purchase_order.vendor", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("purchase_order.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "purchase_orders", id, changeType, text, null, Instant.now()
        ));
    }
}
