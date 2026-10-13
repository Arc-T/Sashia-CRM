package ir.crm.identity.role.dto;

import ir.crm.identity.role.RoleKind;
import java.time.Instant;

public record RoleResponse(String id, String name, RoleKind kind, long userCount, String description,
                           Instant createdAt, Instant updatedAt) {}
