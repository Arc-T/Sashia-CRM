package ir.crm.shared.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import ir.crm.shared.exception.BusinessException;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.RecordComponent;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Single place where every failure becomes an {@link ApiErrorResponse} with a Persian message.
 * Security filters delegate here too (see identity's RestAuthenticationEntryPoint), so clients always see one shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /** MySQL vendor codes. */
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;
    private static final int MYSQL_ROW_IS_REFERENCED = 1451;
    private static final int MYSQL_NO_REFERENCED_ROW = 1452;

    private final Messages messages;

    public GlobalExceptionHandler(Messages messages) {
        this.messages = messages;
    }

    // ---- domain errors

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> business(BusinessException ex, HttpServletRequest req) {
        log.warn("Business error {} on {}: {}", ex.errorCode(), req.getRequestURI(), ex.getMessage());
        return build(ex.errorCode(), ex.errorCode().status(), req, null, null, ex.args());
    }

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ApiErrorResponse> rule(BusinessRuleException ex, HttpServletRequest req) {
        ErrorCode code = ErrorCode.BUSINESS_RULE_VIOLATION;
        return respond(code.status(), code, messages.get(ex.messageKey(), ex.args()), req, null, null, null);
    }

    // ---- validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> invalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        BindingResult result = ex.getBindingResult();
        String resource = resourceOf(result.getObjectName());
        List<FieldViolation> violations = new ArrayList<>();
        for (FieldError fe : result.getFieldErrors()) {
            String field = apiName(result.getTarget(), fe.getField());
            violations.add(new FieldViolation(field, label(resource, field), messages.resolve(fe)));
        }
        for (ObjectError oe : result.getGlobalErrors()) {
            violations.add(new FieldViolation("_global", null, messages.resolve(oe)));
        }
        return validation(req, violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiErrorResponse> invalidParameters(HandlerMethodValidationException ex, HttpServletRequest req) {
        List<FieldViolation> violations = ex.getAllErrors().stream()
                .map(e -> new FieldViolation("_parameter", null, messages.resolve(e))).toList();
        return validation(req, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<FieldViolation> violations = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String path = v.getPropertyPath().toString();
            violations.add(new FieldViolation(path.substring(path.lastIndexOf('.') + 1), null, v.getMessage()));
        }
        return validation(req, violations);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> unreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.debug("Unreadable body on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(ErrorCode.MALFORMED_REQUEST, HttpStatus.BAD_REQUEST, req, null, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorResponse> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST, req, null, null, ex.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiErrorResponse> missingParameter(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST, req, null, null, ex.getParameterName());
    }

    // ---- security

    @ExceptionHandler(DisabledException.class)
    ResponseEntity<ApiErrorResponse> disabled(DisabledException ex, HttpServletRequest req) {
        return build(ErrorCode.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN, req, null, null);
    }

    @ExceptionHandler(LockedException.class)
    ResponseEntity<ApiErrorResponse> locked(LockedException ex, HttpServletRequest req) {
        return build(ErrorCode.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN, req, null, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiErrorResponse> unauthenticated(AuthenticationException ex, HttpServletRequest req) {
        return build(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, req, null, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> denied(AccessDeniedException ex, HttpServletRequest req) {
        return build(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, req, null, null);
    }

    // ---- persistence

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> integrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ErrorCode code = ErrorCode.DATA_INTEGRITY;
        if (ex.getMostSpecificCause() instanceof SQLException sql) {
            code = switch (sql.getErrorCode()) {
                case MYSQL_ROW_IS_REFERENCED -> ErrorCode.REFERENCE_IN_USE;
                case MYSQL_NO_REFERENCED_ROW -> ErrorCode.DATA_INTEGRITY;
                case MYSQL_DUPLICATE_ENTRY -> ErrorCode.DUPLICATE_RESOURCE;
                default -> ErrorCode.DATA_INTEGRITY;
            };
        }
        log.warn("Data integrity violation on {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(code, code.status(), req, null, null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ApiErrorResponse> optimistic(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        return build(ErrorCode.CONCURRENT_MODIFICATION, HttpStatus.CONFLICT, req, null, null);
    }

    // ---- everything else

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> fallback(Exception ex, HttpServletRequest req) {
        // Spring MVC's own exceptions (404/405/406/415...) implement ErrorResponse and carry the right status.
        if (ex instanceof org.springframework.web.ErrorResponse er && er.getStatusCode().value() < 500) {
            int status = er.getStatusCode().value();
            ErrorCode code = ErrorCode.forStatus(status);
            return build(code, HttpStatus.valueOf(status), req, null, null);
        }
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("Unhandled error [{}] on {} {}", traceId, req.getMethod(), req.getRequestURI(), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                messages.get(ErrorCode.INTERNAL_ERROR.messageKey(), traceId), req, traceId, null, null);
    }

    // ---- helpers

    private ResponseEntity<ApiErrorResponse> validation(HttpServletRequest req, List<FieldViolation> violations) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        violations.forEach(v -> errors.computeIfAbsent(v.field(), k -> new ArrayList<>()).add(v.message()));
        return build(ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, req, errors, violations);
    }

    private ResponseEntity<ApiErrorResponse> build(ErrorCode code, HttpStatus status, HttpServletRequest req,
                                                   Map<String, List<String>> errors, List<FieldViolation> violations,
                                                   Object... args) {
        return respond(status, code, messages.get(code.messageKey(), args), req, null, errors, violations);
    }

    private ResponseEntity<ApiErrorResponse> respond(HttpStatus status, ErrorCode code, String message, HttpServletRequest req,
                                                     String traceId, Map<String, List<String>> errors,
                                                     List<FieldViolation> violations) {
        ApiErrorResponse body = new ApiErrorResponse(Instant.now(), status.value(), code.name(), message,
                req.getRequestURI(), traceId, errors, violations);
        return ResponseEntity.status(status).body(body);
    }

    /** "leadCreateRequest" -> "lead" (matches the generated "field.lead.name" label keys). */
    static String resourceOf(String objectName) {
        String s = objectName.replaceAll("(Create|Update)?Request$", "");
        return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /** Records may expose a field under another JSON name (@JsonProperty); clients bind errors by that name. */
    static String apiName(Object target, String field) {
        if (target != null && target.getClass().isRecord()) {
            for (RecordComponent rc : target.getClass().getRecordComponents()) {
                if (!rc.getName().equals(field)) continue;
                JsonProperty json = rc.getAccessor().getAnnotation(JsonProperty.class);
                if (json != null && !json.value().isEmpty()) return json.value();
            }
        }
        return field;
    }

    private String label(String resource, String field) {
        return messages.getOrDefault("field." + resource + "." + field, field);
    }
}
