package ir.crm.identity.team;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.identity.team.dto.TeamCreateRequest;
import ir.crm.identity.team.dto.TeamResponse;
import ir.crm.identity.team.dto.TeamUpdateRequest;
import ir.crm.shared.web.BulkDeleteRequest;
import ir.crm.shared.web.LookupResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

/** REST API of "تیم‌ها". Permissions are "teams:view|create|edit|delete" ("*" = administrator). */
@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "تیم‌ها")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','teams:view')")
    public ResponseEntity<Page<TeamResponse>> list(
            @RequestParam Map<String, String> params,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','teams:view','teams:create','teams:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','teams:view')")
    public ResponseEntity<TeamResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','teams:create')")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamCreateRequest request) {
        TeamResponse created = service.create(request);
        URI location = URI.create("/api/v1/teams/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','teams:edit')")
    public ResponseEntity<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','teams:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','teams:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
