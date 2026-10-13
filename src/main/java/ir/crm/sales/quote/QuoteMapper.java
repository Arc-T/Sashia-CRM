package ir.crm.sales.quote;

import ir.crm.identity.user.User;
import ir.crm.sales.opportunity.Opportunity;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.quote.dto.QuoteCreateRequest;
import ir.crm.sales.quote.dto.QuoteResponse;
import ir.crm.sales.quote.dto.QuoteUpdateRequest;
import ir.crm.shared.web.LookupResponse;

public final class QuoteMapper {

    private QuoteMapper() {}

    public static QuoteResponse toResponse(Quote entity) {
        if (entity == null) return null;
        LookupResponse org = entity.getOrganization() != null
                ? new LookupResponse(String.valueOf(entity.getOrganization().getId()), entity.getOrganization().getName())
                : null;
        LookupResponse opp = entity.getOpportunity() != null
                ? new LookupResponse(String.valueOf(entity.getOpportunity().getId()), entity.getOpportunity().getName())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new QuoteResponse(
                String.valueOf(entity.getId()),
                entity.getNumber(),
                entity.getSubject(),
                org,
                entity.getStatus(),
                opp,
                entity.getValidUntil(),
                entity.getTotal(),
                assignedTo,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static Quote toEntity(QuoteCreateRequest request, Organization organization, Opportunity opportunity, User assignedTo) {
        if (request == null) return null;
        return Quote.builder()
                .number(request.number().trim())
                .subject(request.subject().trim())
                .organization(organization)
                .status(request.status())
                .opportunity(opportunity)
                .validUntil(request.validUntil())
                .total(request.total())
                .assignedTo(assignedTo)
                .build();
    }

    public static void updateEntity(Quote entity, QuoteUpdateRequest request, Organization organization, Opportunity opportunity, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setNumber(request.number().trim());
        entity.setSubject(request.subject().trim());
        entity.setOrganization(organization);
        entity.setStatus(request.status());
        entity.setOpportunity(opportunity);
        entity.setValidUntil(request.validUntil());
        entity.setTotal(request.total());
        entity.setAssignedTo(assignedTo);
    }
}
