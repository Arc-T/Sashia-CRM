package ir.crm.support.faq;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class FaqSpecification {

    private FaqSpecification() {}

    public static Specification<Faq> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (params == null || params.isEmpty()) {
                return cb.conjunction();
            }

            String search = params.get("search");
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("question")), pattern),
                        cb.like(cb.lower(root.get("answer")), pattern)
                ));
            }

            if (params.containsKey("question") && !params.get("question").isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("question")), "%" + params.get("question").trim().toLowerCase() + "%"));
            }
            if (params.containsKey("category") && !params.get("category").isBlank()) {
                try {
                    FaqCategory cat = FaqCategory.valueOf(params.get("category").trim());
                    predicates.add(cb.equal(root.get("category"), cat));
                } catch (IllegalArgumentException ignored) {}
            }
            if (params.containsKey("status") && !params.get("status").isBlank()) {
                try {
                    FaqStatus st = FaqStatus.valueOf(params.get("status").trim());
                    predicates.add(cb.equal(root.get("status"), st));
                } catch (IllegalArgumentException ignored) {}
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
