package ir.crm.sales.quote;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.sales.quote.dto.QuoteCreateRequest;
import ir.crm.sales.quote.dto.QuoteResponse;
import ir.crm.sales.quote.dto.QuoteUpdateRequest;
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

/** REST API of "پیش‌فاکتورها". Permissions are "quotes:view|create|edit|delete" ("*" = administrator). */
@RestController
@RequestMapping("/api/v1/quotes")
@Tag(name = "پیش‌فاکتورها")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','quotes:view')")
    public ResponseEntity<Page<QuoteResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','quotes:view','quotes:create','quotes:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','quotes:view')")
    public ResponseEntity<QuoteResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','quotes:create')")
    public ResponseEntity<QuoteResponse> create(@Valid @RequestBody QuoteCreateRequest request) {
        QuoteResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/quotes/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','quotes:edit')")
    public ResponseEntity<QuoteResponse> update(@PathVariable Long id, @Valid @RequestBody QuoteUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','quotes:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','quotes:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
