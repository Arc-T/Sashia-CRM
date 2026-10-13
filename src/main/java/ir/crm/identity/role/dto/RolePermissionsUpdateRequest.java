package ir.crm.identity.role.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record RolePermissionsUpdateRequest(@NotNull Map<String, List<String>> matrix) {}
