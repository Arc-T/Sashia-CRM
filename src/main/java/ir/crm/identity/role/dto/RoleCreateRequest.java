package ir.crm.identity.role.dto;

import ir.crm.identity.role.RoleKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull RoleKind kind,
        @Size(max = 2000) String description) {}
