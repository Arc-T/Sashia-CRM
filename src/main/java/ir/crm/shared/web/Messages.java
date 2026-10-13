package ir.crm.shared.web;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/** Thin wrapper over MessageSource using the request locale (Persian by default). */
@Component
public class Messages {

    private final MessageSource messageSource;

    public Messages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String getOrDefault(String key, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, LocaleContextHolder.getLocale());
    }

    /** Already-interpolated default messages (Bean Validation) win; otherwise resolve through the codes. */
    public String resolve(MessageSourceResolvable resolvable) {
        String direct = resolvable.getDefaultMessage();
        return direct != null ? direct : messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
    }
}
