package ir.crm.shared.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BulkDeleteRequest(@NotEmpty @Size(max = 500) List<Long> ids) {}
