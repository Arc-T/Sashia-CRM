package ir.crm.catalog.pricebook;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.catalog.pricebook.dto.PriceBookCreateRequest;
import ir.crm.catalog.pricebook.dto.PriceBookResponse;
import ir.crm.catalog.pricebook.dto.PriceBookUpdateRequest;
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
@RequestMapping("/api/v1/pricebooks")
@Tag(name = "دفترچه قیمت")
@RequiredArgsConstructor
public class PriceBookController {

    private final PriceBookService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','pricebooks:view')")
    public ResponseEntity<Page<PriceBookResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','pricebooks:view','pricebooks:create','pricebooks:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','pricebooks:view')")
    public ResponseEntity<PriceBookResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','pricebooks:create')")
    public ResponseEntity<PriceBookResponse> create(@Valid @RequestBody PriceBookCreateRequest request) {
        PriceBookResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/pricebooks/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','pricebooks:edit')")
    public ResponseEntity<PriceBookResponse> update(@PathVariable Long id, @Valid @RequestBody PriceBookUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','pricebooks:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','pricebooks:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
