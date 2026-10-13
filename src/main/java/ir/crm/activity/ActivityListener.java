package ir.crm.activity;

import ir.crm.shared.event.RecordChangedEvent;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ActivityListener {

    private static final Locale PERSIAN = Locale.forLanguageTag("fa-IR");

    private final ActivityLogRepository repository;
    private final MessageSource messages;

    /** Runs asynchronously after the publishing transaction commits; failures are retried via the event registry. */
    @ApplicationModuleListener
    void on(RecordChangedEvent event) {
        String kind = switch (event.type()) {
            case CREATED -> "create";
            case UPDATED -> "status";
            case DELETED -> "note";
        };
        String key = "activity." + event.type().name().toLowerCase();
        String text = messages.getMessage(key, new Object[] {event.recordLabel()}, PERSIAN);
        repository.save(new ActivityLog(event.moduleKey(), event.recordId(), kind, truncate(text), event.actor(), event.occurredAt()));
    }

    private static String truncate(String s) {
        return s.length() <= 500 ? s : s.substring(0, 497) + "...";
    }
}
