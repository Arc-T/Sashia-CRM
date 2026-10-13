package ir.crm.shared.exception;

/** A domain rule was violated; the Persian message is looked up under {@code messageKey}. */
public class BusinessRuleException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] args;

    public BusinessRuleException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String messageKey() { return messageKey; }
    public Object[] args() { return args; }
}
