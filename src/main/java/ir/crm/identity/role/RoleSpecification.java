package ir.crm.identity.role;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class RoleSpecification {

    private RoleSpecification() {}

    private static final Map<String, String> SORT_MAPPINGS = Map.of(
            "name", "name",
            "kind", "kind",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    public static Sort sort(String sortField, boolean ascending) {
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort tieBreaker = Sort.by(Sort.Direction.DESC, "id");
        if (sortField == null || sortField.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt").and(tieBreaker);
        }
        String property = SORT_MAPPINGS.getOrDefault(sortField, sortField);
        return Sort.by(direction, property).and(tieBreaker);
    }

    public static Pageable pageable(Map<String, String> params) {
        int page = parseInt(params.get("page"), 1);
        int pageSize = Math.min(Math.max(parseInt(params.get("pageSize"), 20), 1), 100);
        String sortField = params.get("sortField");
        boolean ascending = !"descend".equalsIgnoreCase(params.get("sortOrder"));
        return PageRequest.of(page - 1, pageSize, sort(sortField, ascending));
    }

    public static Specification<Role> build(Map<String, String> params) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String search = params.getOrDefault("search", params.get("q"));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (params.containsKey("kind") && !params.get("kind").isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("kind"), RoleKind.fromKey(params.get("kind").trim())));
                } catch (IllegalArgumentException ignored) {}
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Role> search(String text) {
        return (root, cq, cb) -> {
            if (text == null || text.isBlank()) return cb.conjunction();
            String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    private static int parseInt(String val, int def) {
        try {
            return val != null ? Integer.parseInt(val.trim()) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
