package ir.crm.sales.salesorder;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.sales.salesorder.dto.SalesOrderCreateRequest;
import ir.crm.sales.salesorder.dto.SalesOrderResponse;
import ir.crm.sales.salesorder.dto.SalesOrderUpdateRequest;
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

/** REST API of "سفارش‌های فروش". Permissions are "sales_orders:view|create|edit|delete" ("*" = administrator). */
@RestController
@RequestMapping("/api/v1/sales-orders")
@Tag(name = "سفارش‌های فروش")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('*','sales_orders:view')")
    public ResponseEntity<Page<SalesOrderResponse>> list(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(service.list(params, pageable));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('*','sales_orders:view','sales_orders:create','sales_orders:edit')")
    public ResponseEntity<List<LookupResponse>> lookup(@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.lookup(search, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','sales_orders:view')")
    public ResponseEntity<SalesOrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('*','sales_orders:create')")
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody SalesOrderCreateRequest request) {
        SalesOrderResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/sales-orders/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','sales_orders:edit')")
    public ResponseEntity<SalesOrderResponse> update(@PathVariable Long id, @Valid @RequestBody SalesOrderUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('*','sales_orders:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('*','sales_orders:delete')")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        service.deleteAll(request.ids());
        return ResponseEntity.noContent().build();
    }
}
