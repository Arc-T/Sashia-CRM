package ir.crm.sales.lead;

import io.swagger.v3.oas.annotations.tags.Tag;
import ir.crm.sales.lead.dto.LeadConversionResponse;
import ir.crm.sales.lead.dto.LeadConvertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leads/{id}/convert")
@Tag(name = "سرنخ‌ها")
@RequiredArgsConstructor
public class LeadConversionController {

    private final LeadConversionService service;

    @PostMapping
    @PreAuthorize("hasAuthority('*') or (hasAuthority('leads:edit') and hasAuthority('organizations:create')"
            + " and hasAuthority('contacts:create') and (!#request.createOpportunity() or hasAuthority('opportunities:create')))")
    public ResponseEntity<LeadConversionResponse> convert(@PathVariable Long id, @Valid @RequestBody LeadConvertRequest request) {
        return ResponseEntity.ok(service.convert(id, request));
    }
}
