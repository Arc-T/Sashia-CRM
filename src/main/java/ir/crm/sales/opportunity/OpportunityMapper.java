package ir.crm.sales.opportunity;

import ir.crm.identity.user.User;
import ir.crm.sales.contact.Contact;
import ir.crm.sales.opportunity.dto.OpportunityCreateRequest;
import ir.crm.sales.opportunity.dto.OpportunityResponse;
import ir.crm.sales.opportunity.dto.OpportunityUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.web.LookupResponse;

public final class OpportunityMapper {

    private OpportunityMapper() {}

    public static OpportunityResponse toResponse(Opportunity entity) {
        if (entity == null) return null;
        LookupResponse org = entity.getOrganization() != null
                ? new LookupResponse(String.valueOf(entity.getOrganization().getId()), entity.getOrganization().getName())
                : null;
        LookupResponse contact = entity.getContact() != null
                ? new LookupResponse(String.valueOf(entity.getContact().getId()), entity.getContact().getName())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new OpportunityResponse(
                String.valueOf(entity.getId()),
                entity.getName(),
                org,
                contact,
                entity.getStage(),
                entity.getAmount(),
                entity.getProbability(),
                entity.getCloseDate(),
                entity.getSource(),
                assignedTo,
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static Opportunity toEntity(OpportunityCreateRequest request, Organization organization, Contact contact, User assignedTo) {
        if (request == null) return null;
        return Opportunity.builder()
                .name(request.name().trim())
                .organization(organization)
                .contact(contact)
                .stage(request.stage())
                .amount(request.amount())
                .probability(request.probability())
                .closeDate(request.closeDate())
                .source(request.source())
                .assignedTo(assignedTo)
                .description(trimToNull(request.description()))
                .build();
    }

    public static void updateEntity(Opportunity entity, OpportunityUpdateRequest request, Organization organization, Contact contact, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setName(request.name().trim());
        entity.setOrganization(organization);
        entity.setContact(contact);
        entity.setStage(request.stage());
        entity.setAmount(request.amount());
        entity.setProbability(request.probability());
        entity.setCloseDate(request.closeDate());
        entity.setSource(request.source());
        entity.setAssignedTo(assignedTo);
        entity.setDescription(trimToNull(request.description()));
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
