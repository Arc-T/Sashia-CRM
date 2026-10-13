package ir.crm.sales.lead;

import ir.crm.identity.user.User;
import ir.crm.marketing.campaign.Campaign;
import ir.crm.sales.lead.dto.LeadCreateRequest;
import ir.crm.sales.lead.dto.LeadResponse;
import ir.crm.sales.lead.dto.LeadUpdateRequest;
import ir.crm.shared.web.LookupResponse;

public final class LeadMapper {

    private LeadMapper() {}

    public static LeadResponse toResponse(Lead entity) {
        if (entity == null) return null;
        LookupResponse campaign = entity.getCampaign() != null
                ? new LookupResponse(String.valueOf(entity.getCampaign().getId()), entity.getCampaign().getName())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new LeadResponse(
                String.valueOf(entity.getId()),
                entity.getName(),
                entity.getCompany(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getSource(),
                entity.getRating(),
                entity.getIndustry(),
                campaign,
                entity.getCity(),
                entity.getExpectedRevenue(),
                assignedTo,
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static Lead toEntity(LeadCreateRequest request, Campaign campaign, User assignedTo) {
        if (request == null) return null;
        return Lead.builder()
                .name(request.name().trim())
                .company(trimToNull(request.company()))
                .phone(request.phone().trim())
                .email(trimToNull(request.email()))
                .status(request.status())
                .source(request.source())
                .rating(request.rating())
                .industry(request.industry())
                .campaign(campaign)
                .city(trimToNull(request.city()))
                .expectedRevenue(request.expectedRevenue())
                .assignedTo(assignedTo)
                .description(trimToNull(request.description()))
                .build();
    }

    public static void updateEntity(Lead entity, LeadUpdateRequest request, Campaign campaign, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setName(request.name().trim());
        entity.setCompany(trimToNull(request.company()));
        entity.setPhone(request.phone().trim());
        entity.setEmail(trimToNull(request.email()));
        entity.setStatus(request.status());
        entity.setSource(request.source());
        entity.setRating(request.rating());
        entity.setIndustry(request.industry());
        entity.setCampaign(campaign);
        entity.setCity(trimToNull(request.city()));
        entity.setExpectedRevenue(request.expectedRevenue());
        entity.setAssignedTo(assignedTo);
        entity.setDescription(trimToNull(request.description()));
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
