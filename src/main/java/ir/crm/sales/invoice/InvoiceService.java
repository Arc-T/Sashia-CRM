package ir.crm.sales.invoice;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.invoice.dto.InvoiceCreateRequest;
import ir.crm.sales.invoice.dto.InvoiceResponse;
import ir.crm.sales.invoice.dto.InvoiceUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationRepository;
import ir.crm.sales.salesorder.SalesOrder;
import ir.crm.sales.salesorder.SalesOrderRepository;
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
public class InvoiceService {

    private final InvoiceRepository repository;
    private final OrganizationRepository organizationRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : InvoiceSpecification.pageable(params);
        Specification<Invoice> spec = InvoiceSpecification.build(params);
        return repository.findAll(spec, effective).map(InvoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return InvoiceMapper.toResponse(find(id));
    }

    public InvoiceResponse create(InvoiceCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        SalesOrder so = resolveSalesOrder(request.salesOrder());
        User user = resolveUser(request.assignedTo());
        Invoice invoice = InvoiceMapper.toEntity(request, org, so, user);
        Invoice saved = repository.save(invoice);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getSubject());
        return InvoiceMapper.toResponse(saved);
    }

    public InvoiceResponse update(Long id, InvoiceUpdateRequest request) {
        Invoice invoice = find(id);
        Organization org = resolveOrganization(request.organization());
        SalesOrder so = resolveSalesOrder(request.salesOrder());
        User user = resolveUser(request.assignedTo());
        InvoiceMapper.updateEntity(invoice, request, org, so, user);
        Invoice saved = repository.save(invoice);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getSubject());
        return InvoiceMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Invoice invoice = find(id);
        repository.delete(invoice);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, invoice.getSubject());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<Invoice> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("number")), pattern),
                    cb.like(cb.lower(root.get("subject")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(i -> new LookupResponse(String.valueOf(i.getId()), i.getNumber() + " - " + i.getSubject()))
                .toList();
    }

    public Invoice find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("invoice", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("invoice.organization", id));
    }

    private SalesOrder resolveSalesOrder(Long id) {
        if (id == null) return null;
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("invoice.salesOrder", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("invoice.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "invoices", id, changeType, text, null, Instant.now()
        ));
    }
}
