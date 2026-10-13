package ir.crm.sales.lead;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.sales.lead.dto.LeadCreateRequest;
import ir.crm.sales.lead.dto.LeadResponse;
import ir.crm.sales.lead.dto.LeadUpdateRequest;
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

/** REST API of "سرنخ‌ها". Permissions are "leads:view|create|edit|delete" ("*" = administrator). */
@RestController
@RequestMapping("/api/v1/leads")
@Tag(name = "سرنخ‌ها")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','leads:view')")
    public ResponseEntity<Page<LeadResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','leads:view','leads:create','leads:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','leads:view')")
    public ResponseEntity<LeadResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','leads:create')")
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadCreateRequest request) {
        LeadResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/leads/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','leads:edit')")
    public ResponseEntity<LeadResponse> update(@PathVariable Long id, @Valid @RequestBody LeadUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','leads:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','leads:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
