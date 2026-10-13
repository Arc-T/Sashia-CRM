package ir.crm.shared.web;

import java.util.Map;

/** Compact reference to another record: id + display label. */
public record LookupResponse(String id, String label) {

    public static LookupResponse of(Long id, Map<Long, String> labels) {
        return id == null ? null : new LookupResponse(String.valueOf(id), labels.getOrDefault(id, String.valueOf(id)));
    }
}
