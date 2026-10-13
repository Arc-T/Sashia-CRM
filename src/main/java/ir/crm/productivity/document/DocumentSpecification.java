package ir.crm.productivity.document;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class DocumentSpecification {

    private DocumentSpecification() {}

    public static Specification<Document> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (params == null || params.isEmpty()) {
                return cb.conjunction();
            }

            String search = params.get("search");
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern)
                ));
            }

            if (params.containsKey("title") && !params.get("title").isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + params.get("title").trim().toLowerCase() + "%"));
            }
            if (params.containsKey("type") && !params.get("type").isBlank()) {
                try {
                    DocumentType t = DocumentType.valueOf(params.get("type").trim());
                    predicates.add(cb.equal(root.get("type"), t));
                } catch (IllegalArgumentException ignored) {}
            }
            if (params.containsKey("organization") && !params.get("organization").isBlank()) {
                try {
                    Long orgId = Long.parseLong(params.get("organization").trim());
                    predicates.add(cb.equal(root.get("organization").get("id"), orgId));
                } catch (NumberFormatException ignored) {}
            }
            if (params.containsKey("assignedTo") && !params.get("assignedTo").isBlank()) {
                try {
                    Long userId = Long.parseLong(params.get("assignedTo").trim());
                    predicates.add(cb.equal(root.get("assignedTo").get("id"), userId));
                } catch (NumberFormatException ignored) {}
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort sort(String sortField, boolean ascending) {
        if (sortField == null || sortField.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return ascending ? Sort.by(Sort.Direction.ASC, sortField) : Sort.by(Sort.Direction.DESC, sortField);
    }

    public static Pageable pageable(Map<String, String> params) {
        int page = 0;
        int pageSize = 20;
        if (params != null) {
            if (params.containsKey("page")) {
                try { page = Math.max(0, Integer.parseInt(params.get("page"))); } catch (NumberFormatException ignored) {}
            }
            if (params.containsKey("pageSize")) {
                try { pageSize = Math.min(100, Math.max(1, Integer.parseInt(params.get("pageSize")))); } catch (NumberFormatException ignored) {}
            }
            String sortField = params.get("sortField");
            boolean ascending = "ascend".equalsIgnoreCase(params.get("sortOrder")) || "asc".equalsIgnoreCase(params.get("sortOrder"));
            return PageRequest.of(page, pageSize, sort(sortField, ascending));
        }
        return PageRequest.of(page, pageSize, sort(null, false));
    }
}
