package ir.crm.catalog.serviceoffering;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingCreateRequest;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingResponse;
import ir.crm.catalog.serviceoffering.dto.ServiceOfferingUpdateRequest;
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
@RequestMapping("/api/v1/services")
@Tag(name = "خدمات")
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','services:view')")
    public ResponseEntity<Page<ServiceOfferingResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','services:view','services:create','services:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','services:view')")
    public ResponseEntity<ServiceOfferingResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','services:create')")
    public ResponseEntity<ServiceOfferingResponse> create(@Valid @RequestBody ServiceOfferingCreateRequest request) {
        ServiceOfferingResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/services/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','services:edit')")
    public ResponseEntity<ServiceOfferingResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceOfferingUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','services:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','services:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
