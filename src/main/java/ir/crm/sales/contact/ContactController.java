package ir.crm.sales.contact;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.sales.contact.dto.ContactCreateRequest;
import ir.crm.sales.contact.dto.ContactResponse;
import ir.crm.sales.contact.dto.ContactUpdateRequest;
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

/** REST API of "مخاطبین". Permissions are "contacts:view|create|edit|delete" ("*" = administrator). */
@RestController
@RequestMapping("/api/v1/contacts")
@Tag(name = "مخاطبین")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','contacts:view')")
    public ResponseEntity<Page<ContactResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','contacts:view','contacts:create','contacts:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','contacts:view')")
    public ResponseEntity<ContactResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','contacts:create')")
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactCreateRequest request) {
        ContactResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/contacts/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','contacts:edit')")
    public ResponseEntity<ContactResponse> update(@PathVariable Long id, @Valid @RequestBody ContactUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','contacts:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','contacts:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
