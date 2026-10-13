package ir.crm.sales.organization;

import ir.crm.identity.user.User;
import ir.crm.sales.organization.dto.OrganizationCreateRequest;
import ir.crm.sales.organization.dto.OrganizationResponse;
import ir.crm.sales.organization.dto.OrganizationUpdateRequest;
import ir.crm.shared.web.LookupResponse;

public final class OrganizationMapper {

    private OrganizationMapper() {}

    public static OrganizationResponse toResponse(Organization entity) {
        if (entity == null) return null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new OrganizationResponse(
                String.valueOf(entity.getId()),
                entity.getName(),
                entity.getIndustry(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getWebsite(),
                entity.getCity(),
                entity.getAnnualRevenue(),
                entity.getEmployees(),
                assignedTo,
                entity.getAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static Organization toEntity(OrganizationCreateRequest request, User assignedTo) {
        if (request == null) return null;
        return Organization.builder()
                .name(request.name().trim())
                .industry(request.industry())
                .phone(trimToNull(request.phone()))
                .email(trimToNull(request.email()))
                .website(trimToNull(request.website()))
                .city(trimToNull(request.city()))
                .annualRevenue(request.annualRevenue())
                .employees(request.employees())
                .assignedTo(assignedTo)
                .address(trimToNull(request.address()))
                .build();
    }

    public static void updateEntity(Organization entity, OrganizationUpdateRequest request, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setName(request.name().trim());
        entity.setIndustry(request.industry());
        entity.setPhone(trimToNull(request.phone()));
        entity.setEmail(trimToNull(request.email()));
        entity.setWebsite(trimToNull(request.website()));
        entity.setCity(trimToNull(request.city()));
        entity.setAnnualRevenue(request.annualRevenue());
        entity.setEmployees(request.employees());
        entity.setAssignedTo(assignedTo);
        entity.setAddress(trimToNull(request.address()));
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
