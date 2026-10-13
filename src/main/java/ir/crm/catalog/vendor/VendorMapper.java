package ir.crm.catalog.vendor;

import ir.crm.catalog.vendor.dto.VendorCreateRequest;
import ir.crm.catalog.vendor.dto.VendorResponse;
import ir.crm.catalog.vendor.dto.VendorUpdateRequest;
import java.util.List;

public final class VendorMapper {

    private VendorMapper() {}

    public static Vendor toEntity(VendorCreateRequest r) {
        Vendor e = new Vendor();
        e.setName(r.name().trim());
        e.setPhone(trimOrNull(r.phone()));
        e.setEmail(trimOrNull(r.email()));
        e.setCity(trimOrNull(r.city()));
        e.setWebsite(trimOrNull(r.website()));
        return e;
    }

    public static void updateEntity(Vendor e, VendorUpdateRequest r) {
        e.setName(r.name().trim());
        e.setPhone(trimOrNull(r.phone()));
        e.setEmail(trimOrNull(r.email()));
        e.setCity(trimOrNull(r.city()));
        e.setWebsite(trimOrNull(r.website()));
    }

    public static VendorResponse toResponse(Vendor e) {
        if (e == null) return null;
        return new VendorResponse(
                String.valueOf(e.getId()),
                e.getName(),
                e.getPhone(),
                e.getEmail(),
                e.getCity(),
                e.getWebsite(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<VendorResponse> toResponses(List<Vendor> entities) {
        return entities.stream().map(VendorMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
