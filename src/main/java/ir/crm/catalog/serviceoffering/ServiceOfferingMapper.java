package ir.crm.catalog.serviceoffering;

import ir.crm.catalog.serviceoffering.dto.ServiceOfferingCreateRequest;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingResponse;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingUpdateRequest;
import java.util.List;

public final class ServiceOfferingMapper {

    private ServiceOfferingMapper() {}

    public static ServiceOffering toEntity(ServiceOfferingCreateRequest r) {
        ServiceOffering e = new ServiceOffering();
        e.setName(r.name().trim());
        e.setCode(trimOrNull(r.code()));
        e.setCategory(r.category());
        e.setPrice(r.price());
        e.setHours(r.hours());
        e.setActive(Boolean.TRUE.equals(r.active()));
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(ServiceOffering e, ServiceOfferingUpdateRequest r) {
        e.setName(r.name().trim());
        e.setCode(trimOrNull(r.code()));
        e.setCategory(r.category());
        e.setPrice(r.price());
        e.setHours(r.hours());
        e.setActive(Boolean.TRUE.equals(r.active()));
        e.setDescription(trimOrNull(r.description()));
    }

    public static ServiceOfferingResponse toResponse(ServiceOffering e) {
        if (e == null) return null;
        return new ServiceOfferingResponse(
                String.valueOf(e.getId()),
                e.getName(),
                e.getCode(),
                e.getCategory(),
                e.getPrice(),
                e.getHours(),
                e.isActive(),
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<ServiceOfferingResponse> toResponses(List<ServiceOffering> entities) {
        return entities.stream().map(ServiceOfferingMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
