package ir.crm.shared.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;

public class ResourceNotFoundException extends BusinessException {

    /** @param resourceKey suffix of the "resource.{key}" message (e.g. "lead"); the Persian noun is resolved lazily */
    public ResourceNotFoundException(String resourceKey, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                new DefaultMessageSourceResolvable(new String[] {"resource." + resourceKey}, resourceKey), id);
    }
}
