package ir.crm.marketing.campaign;

import ir.crm.identity.user.User;
import ir.crm.marketing.campaign.dto.CampaignCreateRequest;
import ir.crm.marketing.campaign.dto.CampaignResponse;
import ir.crm.marketing.campaign.dto.CampaignUpdateRequest;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class CampaignMapper {

    private CampaignMapper() {}

    public static Campaign toEntity(CampaignCreateRequest r, User user) {
        Campaign e = new Campaign();
        e.setName(r.name().trim());
        e.setType(r.type());
        e.setStatus(r.status());
        e.setBudget(r.budget());
        e.setActualCost(r.actualCost());
        e.setExpectedLeads(r.expectedLeads());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(Campaign e, CampaignUpdateRequest r, User user) {
        e.setName(r.name().trim());
        e.setType(r.type());
        e.setStatus(r.status());
        e.setBudget(r.budget());
        e.setActualCost(r.actualCost());
        e.setExpectedLeads(r.expectedLeads());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
    }

    public static CampaignResponse toResponse(Campaign e) {
        if (e == null) return null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new CampaignResponse(
                String.valueOf(e.getId()),
                e.getName(),
                e.getType(),
                e.getStatus(),
                e.getBudget(),
                e.getActualCost(),
                e.getExpectedLeads(),
                e.getStartDate(),
                e.getEndDate(),
                userLookup,
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<CampaignResponse> toResponses(List<Campaign> entities) {
        return entities.stream().map(CampaignMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
