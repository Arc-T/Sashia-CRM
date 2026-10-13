package ir.crm.shared.exception;

import org.springframework.http.HttpStatus;

/** Stable machine-readable error codes. Each maps to a Persian message key in messages.properties. */
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "error.validation"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "error.malformed"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "error.invalidParameter"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "error.unauthorized"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "error.invalidCredentials"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "error.accountDisabled"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "error.accessDenied"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.notFound"),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.routeNotFound"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "error.methodNotAllowed"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "error.notAcceptable"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "error.unsupportedMediaType"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "error.duplicate"),
    REFERENCE_IN_USE(HttpStatus.CONFLICT, "error.referenceInUse"),
    DATA_INTEGRITY(HttpStatus.CONFLICT, "error.dataIntegrity"),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "error.concurrentModification"),
    INVALID_REFERENCE(HttpStatus.UNPROCESSABLE_ENTITY, "error.invalidReference"),
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "error.businessRule"),
    HTTP_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.generic"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }

    public HttpStatus status() { return status; }
    public String messageKey() { return messageKey; }

    /** Best-effort mapping for exceptions raised by Spring MVC itself. */
    public static ErrorCode forStatus(int status) {
        return switch (status) {
            case 400 -> MALFORMED_REQUEST;
            case 401 -> UNAUTHORIZED;
            case 403 -> ACCESS_DENIED;
            case 404 -> ROUTE_NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            default -> HTTP_ERROR;
        };
    }
}
