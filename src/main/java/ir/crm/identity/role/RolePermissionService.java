package ir.crm.identity.role;

import ir.crm.identity.role.dto.RolePermissionsResponse;
import ir.crm.identity.role.dto.RolePermissionsUpdateRequest;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.security.ModuleCatalog;
import ir.crm.shared.security.PermissionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads and replaces the module/action permission matrix of a role. */
@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleRepository roles;

    @Transactional(readOnly = true)
    public RolePermissionsResponse get(Long roleId) {
        Role role = load(roleId);
        Set<String> perms = role.getPermissionStrings();
        Map<String, List<String>> matrix = new LinkedHashMap<>();
        for (String module : ModuleCatalog.KEYS) {
            List<String> actions = new ArrayList<>();
            for (PermissionAction a : PermissionAction.values()) {
                if (role.getKind() == RoleKind.ADMIN || perms.contains(module + ":" + a.key())) {
                    actions.add(a.key());
                }
            }
            matrix.put(module, actions);
        }
        return new RolePermissionsResponse(String.valueOf(roleId), matrix);
    }

    @Transactional
    public RolePermissionsResponse replace(Long roleId, RolePermissionsUpdateRequest request) {
        Role role = load(roleId);
        if (role.getKind() == RoleKind.ADMIN) throw new BusinessRuleException("rule.role.adminImmutable");
        Set<String> granted = new HashSet<>();
        request.matrix().forEach((module, actions) -> {
            if (!ModuleCatalog.KEYS.contains(module)) throw new BusinessRuleException("rule.permission.unknownModule", module);
            for (String action : actions == null ? List.<String>of() : actions) {
                if (PermissionAction.fromKey(action).isEmpty()) throw new BusinessRuleException("rule.permission.unknownAction", action);
                granted.add(module + ":" + action);
            }
        });
        role.getPermissions().clear();
        for (String p : granted) {
            role.getPermissions().add(new RolePermission(role.getId(), p, role));
        }
        roles.saveAndFlush(role);
        return get(roleId);
    }

    /** Permission authorities for a JWT. Admin roles receive the wildcard. */
    @Transactional(readOnly = true)
    public List<String> authoritiesOf(Role role) {
        if (role.getKind() == RoleKind.ADMIN) return List.of("*");
        return role.getPermissionStrings().stream().sorted().toList();
    }

    private Role load(Long id) {
        return roles.findById(id).orElseThrow(() -> new ResourceNotFoundException("role", id));
    }
}
