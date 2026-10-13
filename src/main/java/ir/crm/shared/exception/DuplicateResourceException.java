package ir.crm.shared.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;

public class DuplicateResourceException extends BusinessException {

    /** @param fieldKey suffix of the "field.{key}" message, e.g. "user.email" */
    public DuplicateResourceException(String fieldKey, Object value) {
        super(ErrorCode.DUPLICATE_RESOURCE,
                new DefaultMessageSourceResolvable(new String[] {"field." + fieldKey}, fieldKey), value);
    }
}
