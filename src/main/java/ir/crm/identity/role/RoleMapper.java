package ir.crm.identity.role;

import ir.crm.identity.role.dto.RoleCreateRequest;
import ir.crm.identity.role.dto.RoleResponse;
import ir.crm.identity.role.dto.RoleUpdateRequest;
import java.util.List;
import java.util.Map;

public final class RoleMapper {

    private RoleMapper() {}

    public static Role toEntity(RoleCreateRequest r) {
        return Role.builder()
                .name(r.name() != null ? r.name().trim() : null)
                .kind(r.kind())
                .description(r.description() != null && !r.description().isBlank() ? r.description().trim() : null)
                .build();
    }

    public static void updateEntity(Role e, RoleUpdateRequest r) {
        e.setName(r.name() != null ? r.name().trim() : null);
        e.setKind(r.kind());
        e.setDescription(r.description() != null && !r.description().isBlank() ? r.description().trim() : null);
    }

    public static RoleResponse toResponse(Role role, long userCount) {
        return new RoleResponse(
                String.valueOf(role.getId()),
                role.getName(),
                role.getKind(),
                userCount,
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public static RoleResponse toResponse(Role role) {
        return toResponse(role, 0L);
    }

    public static List<RoleResponse> toResponses(List<Role> roles, Map<Long, Long> counts) {
        if (roles == null) return List.of();
        return roles.stream().map(e -> toResponse(e, counts.getOrDefault(e.getId(), 0L))).toList();
    }
}
