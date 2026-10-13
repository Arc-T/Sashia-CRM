package ir.crm.sales.opportunity;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class OpportunitySpecification {

    private OpportunitySpecification() {}

    public static Specification<Opportunity> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("search") && !params.get("search").isBlank()) {
                String search = "%" + params.get("search").trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), search));
            }

            if (params.containsKey("organization") && !params.get("organization").isBlank()) {
                String val = params.get("organization").trim();
                try {
                    Long orgId = Long.valueOf(val);
                    predicates.add(cb.equal(root.get("organization").get("id"), orgId));
                } catch (NumberFormatException e) {
                    predicates.add(cb.like(cb.lower(root.get("organization").get("name")), "%" + val.toLowerCase() + "%"));
                }
            }

            if (params.containsKey("contact") && !params.get("contact").isBlank()) {
                String val = params.get("contact").trim();
                try {
                    Long contactId = Long.valueOf(val);
                    predicates.add(cb.equal(root.get("contact").get("id"), contactId));
                } catch (NumberFormatException e) {
                    predicates.add(cb.like(cb.lower(root.get("contact").get("name")), "%" + val.toLowerCase() + "%"));
                }
            }

            if (params.containsKey("stage") && !params.get("stage").isBlank()) {
                try {
                    OpportunityStage stage = OpportunityStage.fromKey(params.get("stage").trim());
                    predicates.add(cb.equal(root.get("stage"), stage));
                } catch (IllegalArgumentException ignored) {}
            }

            if (params.containsKey("source") && !params.get("source").isBlank()) {
                try {
                    OpportunitySource source = OpportunitySource.fromKey(params.get("source").trim());
                    predicates.add(cb.equal(root.get("source"), source));
                } catch (IllegalArgumentException ignored) {}
            }

            if (params.containsKey("assignedTo") && !params.get("assignedTo").isBlank()) {
                String val = params.get("assignedTo").trim();
                try {
                    Long userId = Long.valueOf(val);
                    predicates.add(cb.equal(root.get("assignedTo").get("id"), userId));
                } catch (NumberFormatException e) {
                    predicates.add(cb.like(cb.lower(root.get("assignedTo").get("name")), "%" + val.toLowerCase() + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort sort(String sortField, boolean ascending) {
        if (sortField == null || sortField.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String field = switch (sortField) {
            case "name" -> "name";
            case "organization" -> "organization.name";
            case "contact" -> "contact.name";
            case "stage" -> "stage";
            case "amount" -> "amount";
            case "probability" -> "probability";
            case "closeDate" -> "closeDate";
            case "source" -> "source";
            case "assignedTo" -> "assignedTo.name";
            case "updatedAt" -> "updatedAt";
            default -> "createdAt";
        };
        return ascending ? Sort.by(field).ascending() : Sort.by(field).descending();
    }

    public static Pageable pageable(Map<String, String> params) {
        int page = 0;
        int size = 20;
        if (params.containsKey("page")) {
            try { page = Math.max(0, Integer.parseInt(params.get("page"))); } catch (NumberFormatException ignored) {}
        }
        if (params.containsKey("pageSize")) {
            try { size = Math.max(1, Integer.parseInt(params.get("pageSize"))); } catch (NumberFormatException ignored) {}
        }
        String sortField = params.get("sortField");
        boolean ascending = "ascend".equalsIgnoreCase(params.get("sortOrder")) || "asc".equalsIgnoreCase(params.get("sortOrder"));
        return PageRequest.of(page, size, sort(sortField, ascending));
    }
}

