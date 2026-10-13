package ir.crm.shared.event;

import java.time.Instant;

/** Published by every CRUD service inside the writing transaction; consumed e.g. by the activity module. */
public record RecordChangedEvent(String moduleKey, Long recordId, ChangeType type, String recordLabel,
                                 String actor, Instant occurredAt) {

    public enum ChangeType { CREATED, UPDATED, DELETED }
}
