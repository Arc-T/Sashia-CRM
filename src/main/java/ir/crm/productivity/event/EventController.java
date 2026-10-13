package ir.crm.productivity.event;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.productivity.event.dto.EventCreateRequest;
import ir.crm.productivity.event.dto.EventResponse;
import ir.crm.productivity.event.dto.EventUpdateRequest;
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
@RequestMapping("/api/v1/events")
@Tag(name = "رویدادها")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','events:view')")
    public ResponseEntity<Page<EventResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','events:view','events:create','events:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','events:view')")
    public ResponseEntity<EventResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','events:create')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest request) {
        EventResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/events/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','events:edit')")
    public ResponseEntity<EventResponse> update(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','events:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','events:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
