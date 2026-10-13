package ir.crm.identity.user;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.identity.user.dto.UserCreateRequest;
import ir.crm.identity.user.dto.UserResponse;
import ir.crm.identity.user.dto.UserUpdateRequest;
import ir.crm.shared.web.BulkDeleteRequest;
import ir.crm.shared.web.LookupResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST API of "کاربران". Permissions are "users:view|create|edit|delete" ("*" = administrator). */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "کاربران")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','users:view')")
    public ResponseEntity<Page<UserResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','users:view','users:create','users:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','users:view')")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','users:create')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','users:edit')")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','users:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','users:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
