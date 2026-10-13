package ir.crm.identity.role;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.identity.role.dto.RolePermissionsResponse;
import ir.crm.identity.role.dto.RolePermissionsUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles/{id}/permissions")
@Tag(name = "Roles")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','roles:view')")
    public ResponseEntity<RolePermissionsResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('*','roles:edit')")
    public ResponseEntity<RolePermissionsResponse> replace(@PathVariable Long id, @Valid @RequestBody RolePermissionsUpdateRequest request) {
        return ResponseEntity.ok(service.replace(id, request));
    }
}
