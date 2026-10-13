package ir.crm.sales.purchaseorder;

import ir.crm.catalog.vendor.Vendor;
import ir.crm.identity.user.User;
import ir.crm.sales.purchaseorder.dto.PurchaseOrderCreateRequest;
import ir.crm.sales.purchaseorder.dto.PurchaseOrderResponse;
import ir.crm.sales.purchaseorder.dto.PurchaseOrderUpdateRequest;
import ir.crm.shared.web.LookupResponse;

public final class PurchaseOrderMapper {

    private PurchaseOrderMapper() {}

    public static PurchaseOrderResponse toResponse(PurchaseOrder entity) {
        if (entity == null) return null;
        LookupResponse vendor = entity.getVendor() != null
                ? new LookupResponse(String.valueOf(entity.getVendor().getId()), entity.getVendor().getName())
                : null;
        LookupResponse assignedTo = entity.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(entity.getAssignedTo().getId()), entity.getAssignedTo().getFullName())
                : null;

        return new PurchaseOrderResponse(
                String.valueOf(entity.getId()),
                entity.getNumber(),
                vendor,
                entity.getStatus(),
                entity.getOrderDate(),
                entity.getTotal(),
                assignedTo,
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PurchaseOrder toEntity(PurchaseOrderCreateRequest request, Vendor vendor, User assignedTo) {
        if (request == null) return null;
        return PurchaseOrder.builder()
                .number(request.number().trim())
                .vendor(vendor)
                .status(request.status())
                .orderDate(request.orderDate())
                .total(request.total())
                .assignedTo(assignedTo)
                .description(trimToNull(request.description()))
                .build();
    }

    public static void updateEntity(PurchaseOrder entity, PurchaseOrderUpdateRequest request, Vendor vendor, User assignedTo) {
        if (entity == null || request == null) return;
        entity.setNumber(request.number().trim());
        entity.setVendor(vendor);
        entity.setStatus(request.status());
        entity.setOrderDate(request.orderDate());
        entity.setTotal(request.total());
        entity.setAssignedTo(assignedTo);
        entity.setDescription(trimToNull(request.description()));
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
