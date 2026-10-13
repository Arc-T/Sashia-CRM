package ir.crm.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Uniform error body. {@code errors} (field -> messages) is what the React client binds to form items;
 * {@code violations} adds the Persian field label for summaries.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        Map<String, List<String>> errors,
        List<FieldViolation> violations) {}
