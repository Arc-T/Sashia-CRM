package ir.crm.identity.role.dto;

import java.util.List;
import java.util.Map;

/** {@code matrix}: module key -> granted action keys (view/create/edit/delete/export). */
public record RolePermissionsResponse(String roleId, Map<String, List<String>> matrix) {}
