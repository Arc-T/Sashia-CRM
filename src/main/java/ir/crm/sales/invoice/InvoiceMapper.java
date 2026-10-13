package ir.crm.sales.invoice;

import ir.crm.identity.user.User;
import ir.crm.sales.invoice.dto.InvoiceCreateRequest;
import ir.crm.sales.invoice.dto.InvoiceResponse;
import ir.crm.sales.invoice.dto.InvoiceUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.sales.salesorder.SalesOrder;
import ir.crm.shared.web.LookupResponse;

public final class InvoiceMapper {

    private InvoiceMapper() {}

    public static InvoiceResponse toResponse(Invoice entity) {
        if (entity == null) return null;
        LookupResponse org = entity.getOrganization() != null
                ? new LookupResponse(String.valueOf(entity.getOrganization().getId()), entity.getOrganization().getName())
                : null;
        LookupResponse so = entity.getSalesOrder() != null
                ? new LookupResponse(String.valueOf(entity.getSalesOrder().getId()), entity.getSalesOrder().getSubject())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new InvoiceResponse(
                String.valueOf(entity.getId()),
                entity.getNumber(),
                entity.getSubject(),
                org,
                entity.getStatus(),
                so,
                entity.getIssueDate(),
                entity.getDueDate(),
                entity.getTotal(),
                assignedTo,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static Invoice toEntity(InvoiceCreateRequest request, Organization organization, SalesOrder salesOrder, User assignedTo) {
        if (request == null) return null;
        return Invoice.builder()
                .number(request.number().trim())
                .subject(request.subject().trim())
                .organization(organization)
                .status(request.status())
                .salesOrder(salesOrder)
                .issueDate(request.issueDate())
                .dueDate(request.dueDate())
                .total(request.total())
                .assignedTo(assignedTo)
                .build();
    }

    public static void updateEntity(Invoice entity, InvoiceUpdateRequest request, Organization organization, SalesOrder salesOrder, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setNumber(request.number().trim());
        entity.setSubject(request.subject().trim());
        entity.setOrganization(organization);
        entity.setStatus(request.status());
        entity.setSalesOrder(salesOrder);
        entity.setIssueDate(request.issueDate());
        entity.setDueDate(request.dueDate());
        entity.setTotal(request.total());
        entity.setAssignedTo(assignedTo);
    }
}
