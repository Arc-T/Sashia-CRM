package ir.crm.shared;

import static org.assertj.core.api.Assertions.assertThat;

import ir.crm.shared.exception.ErrorCode;
import ir.crm.shared.security.ModuleCatalog;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

/** Guards the "Persian keys everywhere" requirement. */
class MessagesTest {

    private static final Pattern PERSIAN = Pattern.compile("[\\u0600-\\u06FF]");
    private final ResourceBundleMessageSource source = new ResourceBundleMessageSource();

    MessagesTest() {
        source.setBasenames("messages", "messages-fields", "messages-enums");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
    }

    @Test
    void everyErrorCodeHasAPersianMessage() {
        for (ErrorCode code : ErrorCode.values()) {
            String text = source.getMessage(code.messageKey(), new Object[] {"x", "y"}, Locale.ENGLISH);
            assertThat(text).as(code.name()).matches(s -> PERSIAN.matcher(s).find());
        }
    }

    @Test
    void defaultBeanValidationMessagesAreOverriddenInPersian() {
        for (String key : new String[] {"NotNull", "NotBlank", "NotEmpty", "Size", "Min", "Max", "Email", "Pattern",
                "PositiveOrZero", "Digits"}) {
            String text = source.getMessage("jakarta.validation.constraints." + key + ".message", null, Locale.ENGLISH);
            assertThat(PERSIAN.matcher(text).find()).as(key).isTrue();
        }
        assertThat(source.getMessage("validation.phone", null, Locale.ENGLISH)).contains("شمارهٔ تلفن");
    }

    @Test
    void everyModuleHasAPersianNounAndEnumLabelsExist() {
        assertThat(ModuleCatalog.KEYS).hasSize(27);
        assertThat(source.getMessage("resource.lead", null, Locale.ENGLISH)).isEqualTo("سرنخ");
        assertThat(source.getMessage("enum.LeadStatus.QUALIFIED", null, Locale.ENGLISH)).isEqualTo("واجد شرایط");
        assertThat(source.getMessage("field.lead.assignedTo", null, Locale.ENGLISH)).isEqualTo("مسئول");
    }
}
