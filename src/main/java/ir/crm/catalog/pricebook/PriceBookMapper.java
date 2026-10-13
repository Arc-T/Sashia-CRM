package ir.crm.catalog.pricebook;

import ir.crm.catalog.pricebook.dto.PriceBookCreateRequest;
import ir.crm.catalog.pricebook.dto.PriceBookResponse;
import ir.crm.catalog.pricebook.dto.PriceBookUpdateRequest;
import java.util.List;

public final class PriceBookMapper {

    private PriceBookMapper() {}

    public static PriceBook toEntity(PriceBookCreateRequest r) {
        PriceBook e = new PriceBook();
        e.setName(r.name().trim());
        e.setStatus(r.status());
        e.setDiscountPct(r.discountPct());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(PriceBook e, PriceBookUpdateRequest r) {
        e.setName(r.name().trim());
        e.setStatus(r.status());
        e.setDiscountPct(r.discountPct());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        e.setDescription(trimOrNull(r.description()));
    }

    public static PriceBookResponse toResponse(PriceBook e) {
        if (e == null) return null;
        return new PriceBookResponse(
                String.valueOf(e.getId()),
                e.getName(),
                e.getStatus(),
                e.getDiscountPct(),
                e.getValidFrom(),
                e.getValidTo(),
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<PriceBookResponse> toResponses(List<PriceBook> entities) {
        return entities.stream().map(PriceBookMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
