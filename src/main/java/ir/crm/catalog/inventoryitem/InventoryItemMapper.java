package ir.crm.catalog.inventoryitem;

import ir.crm.catalog.inventoryitem.dto.InventoryItemCreateRequest;
import ir.crm.catalog.inventoryitem.dto.InventoryItemResponse;
import ir.crm.catalog.inventoryitem.dto.InventoryItemUpdateRequest;
import ir.crm.catalog.product.Product;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class InventoryItemMapper {

    private InventoryItemMapper() {}

    public static InventoryItem toEntity(InventoryItemCreateRequest r, Product product) {
        InventoryItem e = new InventoryItem();
        e.setSku(r.sku().trim());
        e.setProduct(product);
        e.setWarehouse(r.warehouse());
        e.setQuantity(r.quantity());
        e.setReorderLevel(r.reorderLevel());
        e.setStatus(r.status());
        e.updateStockStatus();
        return e;
    }

    public static void updateEntity(InventoryItem e, InventoryItemUpdateRequest r, Product product) {
        e.setSku(r.sku().trim());
        e.setProduct(product);
        e.setWarehouse(r.warehouse());
        e.setQuantity(r.quantity());
        e.setReorderLevel(r.reorderLevel());
        e.setStatus(r.status());
        e.updateStockStatus();
    }

    public static InventoryItemResponse toResponse(InventoryItem e) {
        if (e == null) return null;
        LookupResponse productLookup = e.getProduct() != null
                ? new LookupResponse(String.valueOf(e.getProduct().getId()), e.getProduct().getName())
                : null;
        return new InventoryItemResponse(
                String.valueOf(e.getId()),
                e.getSku(),
                productLookup,
                e.getWarehouse(),
                e.getQuantity(),
                e.getReorderLevel(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<InventoryItemResponse> toResponses(List<InventoryItem> entities) {
        return entities.stream().map(InventoryItemMapper::toResponse).toList();
    }
}
