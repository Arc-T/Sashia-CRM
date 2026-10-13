package ir.crm.shared.exception;

/** Base type for expected, client-correctable failures. Rendered by GlobalExceptionHandler. */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode errorCode() { return errorCode; }
    public Object[] args() { return args; }
}
