package ir.crm.shared.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parsed list-endpoint query string: paging, free-text search, sorting and per-field equality filters.
 * Unknown filter names are carried through and ignored later by the module's QueryRules whitelist.
 */
public record ListQuery(int page, int pageSize, String search, String sortField, boolean ascending,
                        Map<String, String> filters) {

    public static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> RESERVED = Set.of("page", "pageSize", "search", "q", "sortField", "sortOrder");

    public static ListQuery from(Map<String, String> params) {
        int page = Math.max(parseInt(params.get("page"), 1), 1);
        int size = Math.min(Math.max(parseInt(params.get("pageSize"), 10), 1), MAX_PAGE_SIZE);
        String search = params.getOrDefault("search", params.get("q"));
        Map<String, String> filters = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            if (!RESERVED.contains(k) && v != null && !v.isBlank()) filters.put(k, v);
        });
        return new ListQuery(page, size, search, params.get("sortField"), !"descend".equals(params.get("sortOrder")), filters);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
