package ir.crm.shared.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;

/** A lookup field points at a record that does not exist. */
public class InvalidReferenceException extends BusinessException {

    public InvalidReferenceException(String fieldKey, Object id) {
        super(ErrorCode.INVALID_REFERENCE,
                new DefaultMessageSourceResolvable(new String[] {"field." + fieldKey}, fieldKey), id);
    }
}
