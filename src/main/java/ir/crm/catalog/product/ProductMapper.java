package ir.crm.catalog.product;

import ir.crm.catalog.product.dto.ProductCreateRequest;
import ir.crm.catalog.product.dto.ProductResponse;
import ir.crm.catalog.product.dto.ProductUpdateRequest;
import ir.crm.catalog.vendor.Vendor;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toEntity(ProductCreateRequest r, Vendor vendor) {
        Product e = new Product();
        e.setName(r.name().trim());
        e.setSku(trimOrNull(r.sku()));
        e.setCategory(r.category());
        e.setVendor(vendor);
        e.setPrice(r.price());
        e.setStock(r.stock());
        e.setActive(Boolean.TRUE.equals(r.active()));
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(Product e, ProductUpdateRequest r, Vendor vendor) {
        e.setName(r.name().trim());
        e.setSku(trimOrNull(r.sku()));
        e.setCategory(r.category());
        e.setVendor(vendor);
        e.setPrice(r.price());
        e.setStock(r.stock());
        e.setActive(Boolean.TRUE.equals(r.active()));
        e.setDescription(trimOrNull(r.description()));
    }

    public static ProductResponse toResponse(Product e) {
        if (e == null) return null;
        LookupResponse vendorLookup = e.getVendor() != null
                ? new LookupResponse(String.valueOf(e.getVendor().getId()), e.getVendor().getName())
                : null;
        return new ProductResponse(
                String.valueOf(e.getId()),
                e.getName(),
                e.getSku(),
                e.getCategory(),
                vendorLookup,
                e.getPrice(),
                e.getStock(),
                e.isActive(),
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<ProductResponse> toResponses(List<Product> entities) {
        return entities.stream().map(ProductMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
