package ir.crm.sales.servicecontract;

import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.organization.OrganizationRepository;
import ir.crm.sales.servicecontract.dto.ServiceContractCreateRequest;
import ir.crm.sales.servicecontract.dto.ServiceContractResponse;
import ir.crm.sales.servicecontract.dto.ServiceContractUpdateRequest;
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
public class ServiceContractService {

    private final ServiceContractRepository repository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<ServiceContractResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effective = (pageable != null && pageable.isPaged()) ? pageable : ServiceContractSpecification.pageable(params);
        Specification<ServiceContract> spec = ServiceContractSpecification.build(params);
        return repository.findAll(spec, effective).map(ServiceContractMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceContractResponse get(Long id) {
        return ServiceContractMapper.toResponse(find(id));
    }

    public ServiceContractResponse create(ServiceContractCreateRequest request) {
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        ServiceContract contract = ServiceContractMapper.toEntity(request, org, user);
        ServiceContract saved = repository.save(contract);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getNumber());
        return ServiceContractMapper.toResponse(saved);
    }

    public ServiceContractResponse update(Long id, ServiceContractUpdateRequest request) {
        ServiceContract contract = find(id);
        Organization org = resolveOrganization(request.organization());
        User user = resolveUser(request.assignedTo());
        ServiceContractMapper.updateEntity(contract, request, org, user);
        ServiceContract saved = repository.save(contract);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getNumber());
        return ServiceContractMapper.toResponse(saved);
    }

    public void delete(Long id) {
        ServiceContract contract = find(id);
        repository.delete(contract);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, contract.getNumber());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Specification<ServiceContract> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("number")), pattern);
        };
        return repository.findAll(spec, PageRequest.of(0, size)).stream()
                .map(s -> new LookupResponse(String.valueOf(s.getId()), s.getNumber()))
                .toList();
    }

    public ServiceContract find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("service_contract", id));
    }

    private Organization resolveOrganization(Long id) {
        if (id == null) {
            throw new InvalidReferenceException("service_contract.organization", null);
        }
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("service_contract.organization", id));
    }

    private User resolveUser(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidReferenceException("service_contract.assignedTo", id));
    }

    private void publishEvent(Long id, ChangeType changeType, String text) {
        eventPublisher.publishEvent(new RecordChangedEvent(
                "service_contracts", id, changeType, text, null, Instant.now()
        ));
    }
}
