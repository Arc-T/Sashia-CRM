package ir.crm.sales.salesorder;

import ir.crm.identity.user.User;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.quote.Quote;
import ir.crm.sales.salesorder.dto.SalesOrderCreateRequest;
import ir.crm.sales.salesorder.dto.SalesOrderResponse;
import ir.crm.sales.salesorder.dto.SalesOrderUpdateRequest;
import ir.crm.shared.web.LookupResponse;

public final class SalesOrderMapper {

    private SalesOrderMapper() {}

    public static SalesOrderResponse toResponse(SalesOrder entity) {
        if (entity == null) return null;
        LookupResponse org = entity.getOrganization() != null
                ? new LookupResponse(String.valueOf(entity.getOrganization().getId()), entity.getOrganization().getName())
                : null;
        LookupResponse quote = entity.getQuote() != null
                ? new LookupResponse(String.valueOf(entity.getQuote().getId()), entity.getQuote().getSubject())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new SalesOrderResponse(
                String.valueOf(entity.getId()),
                entity.getNumber(),
                entity.getSubject(),
                org,
                entity.getStatus(),
                quote,
                entity.getDueDate(),
                entity.getTotal(),
                assignedTo,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static SalesOrder toEntity(SalesOrderCreateRequest request, Organization organization, Quote quote, User assignedTo) {
        if (request == null) return null;
        return SalesOrder.builder()
                .number(request.number().trim())
                .subject(request.subject().trim())
                .organization(organization)
                .status(request.status())
                .quote(quote)
                .dueDate(request.dueDate())
                .total(request.total())
                .assignedTo(assignedTo)
                .build();
    }

    public static void updateEntity(SalesOrder entity, SalesOrderUpdateRequest request, Organization organization, Quote quote, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setNumber(request.number().trim());
        entity.setSubject(request.subject().trim());
        entity.setOrganization(organization);
        entity.setStatus(request.status());
        entity.setQuote(quote);
        entity.setDueDate(request.dueDate());
        entity.setTotal(request.total());
        entity.setAssignedTo(assignedTo);
    }
}
