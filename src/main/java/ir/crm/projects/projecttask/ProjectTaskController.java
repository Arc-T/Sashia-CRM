package ir.crm.projects.projecttask;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.projects.projecttask.dto.ProjectTaskCreateRequest;
import ir.crm.projects.projecttask.dto.ProjectTaskResponse;
import ir.crm.projects.projecttask.dto.ProjectTaskUpdateRequest;
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

@RestController
@RequestMapping("/api/v1/project-tasks")
@Tag(name = "وظایف پروژه")
@RequiredArgsConstructor
public class ProjectTaskController {

    private final ProjectTaskService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','projects:view')")
    public ResponseEntity<Page<ProjectTaskResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','projects:view','projects:create','projects:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','projects:view')")
    public ResponseEntity<ProjectTaskResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','projects:create')")
    public ResponseEntity<ProjectTaskResponse> create(@Valid @RequestBody ProjectTaskCreateRequest request) {
        ProjectTaskResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/project-tasks/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','projects:edit')")
    public ResponseEntity<ProjectTaskResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectTaskUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','projects:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','projects:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
