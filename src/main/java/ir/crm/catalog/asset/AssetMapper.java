package ir.crm.catalog.asset;

import ir.crm.catalog.asset.dto.AssetCreateRequest;
import ir.crm.catalog.asset.dto.AssetResponse;
import ir.crm.catalog.asset.dto.AssetUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class AssetMapper {

    private AssetMapper() {}

    public static Asset toEntity(AssetCreateRequest r, Organization org) {
        Asset e = new Asset();
        e.setName(r.name().trim());
        e.setSerial(trimOrNull(r.serial()));
        e.setOrganization(org);
        e.setStatus(r.status());
        e.setPurchaseDate(r.purchaseDate());
        e.setWarrantyEnd(r.warrantyEnd());
        return e;
    }

    public static void updateEntity(Asset e, AssetUpdateRequest r, Organization org) {
        e.setName(r.name().trim());
        e.setSerial(trimOrNull(r.serial()));
        e.setOrganization(org);
        e.setStatus(r.status());
        e.setPurchaseDate(r.purchaseDate());
        e.setWarrantyEnd(r.warrantyEnd());
    }

    public static AssetResponse toResponse(Asset e) {
        if (e == null) return null;
        LookupResponse orgLookup = e.getOrganization() != null
                ? new LookupResponse(String.valueOf(e.getOrganization().getId()), e.getOrganization().getName())
                : null;
        return new AssetResponse(
                String.valueOf(e.getId()),
                e.getName(),
                e.getSerial(),
                orgLookup,
                e.getStatus(),
                e.getPurchaseDate(),
                e.getWarrantyEnd(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<AssetResponse> toResponses(List<Asset> entities) {
        return entities.stream().map(AssetMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
