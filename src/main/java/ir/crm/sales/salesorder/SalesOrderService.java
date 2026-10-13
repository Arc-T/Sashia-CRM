package ir.crm.sales.salesorder;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationRepository;
import ir.crm.sales.quote.Quote;
import ir.crm.sales.quote.QuoteRepository;
import ir.crm.sales.salesorder.dto.SalesOrderCreateRequest;
import ir.crm.sales.salesorder.dto.SalesOrderResponse;
import ir.crm.sales.salesorder.dto.SalesOrderUpdateRequest;
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
public class SalesOrderService {

    private final SalesOrderRepository repository;
    private final OrganizationRepository organizationRepository;
    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<SalesOrderResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : SalesOrderSpecification.pageable(params);
        Specification<SalesOrder> spec = SalesOrderSpecification.build(params);
        return repository.findAll(spec, effective).map(SalesOrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse get(Long id) {
        return SalesOrderMapper.toResponse(find(id));
    }

    public SalesOrderResponse create(SalesOrderCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        Quote quote = resolveQuote(request.quote());
        User user = resolveUser(request.assignedTo());
        SalesOrder order = SalesOrderMapper.toEntity(request, org, quote, user);
        SalesOrder saved = repository.save(order);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getSubject());
        return SalesOrderMapper.toResponse(saved);
    }

    public SalesOrderResponse update(Long id, SalesOrderUpdateRequest request) {
        SalesOrder order = find(id);
        Organization org = resolveOrganization(request.organization());
        Quote quote = resolveQuote(request.quote());
        User user = resolveUser(request.assignedTo());
        SalesOrderMapper.updateEntity(order, request, org, quote, user);
        SalesOrder saved = repository.save(order);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getSubject());
        return SalesOrderMapper.toResponse(saved);
    }

    public void delete(Long id) {
        SalesOrder order = find(id);
        repository.delete(order);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, order.getSubject());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<SalesOrder> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("number")), pattern),
                    cb.like(cb.lower(root.get("subject")), pattern)
            );
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(o -> new LookupResponse(String.valueOf(o.getId()), o.getNumber() + " - " + o.getSubject()))
                .toList();
    }

    public SalesOrder find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("sales_order", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) return null;
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("sales_order.organization", id));
    }

    private Quote resolveQuote(Long id) {
        if (id == null) return null;
        return quoteRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("sales_order.quote", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("sales_order.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "sales_orders", id, changeType, text, null, Instant.now()
        ));
    }
}
