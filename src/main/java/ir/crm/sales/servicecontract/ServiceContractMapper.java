package ir.crm.sales.servicecontract;

import ir.crm.identity.user.User;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.servicecontract.dto.ServiceContractCreateRequest;
import ir.crm.sales.servicecontract.dto.ServiceContractResponse;
import ir.crm.sales.servicecontract.dto.ServiceContractUpdateRequest;
import ir.crm.shared.web.LookupResponse;

public final class ServiceContractMapper {

    private ServiceContractMapper() {}

    public static ServiceContractResponse toResponse(ServiceContract entity) {
        if (entity == null) return null;
        LookupResponse org = entity.getOrganization() != null
                ? new LookupResponse(String.valueOf(entity.getOrganization().getId()), entity.getOrganization().getName())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new ServiceContractResponse(
                String.valueOf(entity.getId()),
                entity.getNumber(),
                org,
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getValue(),
                assignedTo,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ServiceContract toEntity(ServiceContractCreateRequest request, Organization organization, User assignedTo) {
        if (request == null) return null;
        return ServiceContract.builder()
                .number(request.number().trim())
                .organization(organization)
                .status(request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .value(request.value())
                .assignedTo(assignedTo)
                .build();
    }

    public static void updateEntity(ServiceContract entity, ServiceContractUpdateRequest request, Organization organization, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setNumber(request.number().trim());
        entity.setOrganization(organization);
        entity.setStatus(request.status());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setValue(request.value());
        entity.setAssignedTo(assignedTo);
    }
}
