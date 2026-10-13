package ir.crm.sales.lead;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class LeadSpecification {

    private LeadSpecification() {}

    public static Specification<Lead> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("search") && !params.get("search").isBlank()) {
                String search = "%" + params.get("search").trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), search),
                        cb.like(cb.lower(root.get("company")), search),
                        cb.like(cb.lower(root.get("phone")), search),
                        cb.like(cb.lower(root.get("email")), search),
                        cb.like(cb.lower(root.get("city")), search)
                ));
            }

            if (params.containsKey("status") && !params.get("status").isBlank()) {
                try {
                    LeadStatus status = LeadStatus.fromKey(params.get("status").trim());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {}
            }

            if (params.containsKey("source") && !params.get("source").isBlank()) {
                try {
                    LeadSource source = LeadSource.fromKey(params.get("source").trim());
                    predicates.add(cb.equal(root.get("source"), source));
                } catch (IllegalArgumentException ignored) {}
            }

            if (params.containsKey("rating") && !params.get("rating").isBlank()) {
                try {
                    LeadRating rating = LeadRating.fromKey(params.get("rating").trim());
                    predicates.add(cb.equal(root.get("rating"), rating));
                } catch (IllegalArgumentException ignored) {}
            }

            if (params.containsKey("industry") && !params.get("industry").isBlank()) {
                try {
                    LeadIndustry industry = LeadIndustry.fromKey(params.get("industry").trim());
                    predicates.add(cb.equal(root.get("industry"), industry));
                } catch (IllegalArgumentException ignored) {}
            }

            if (params.containsKey("campaign") && !params.get("campaign").isBlank()) {
                try {
                    Long cId = Long.valueOf(params.get("campaign").trim());
                    predicates.add(cb.equal(root.get("campaign").get("id"), cId));
                } catch (NumberFormatException ignored) {}
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
            case "company" -> "company";
            case "phone" -> "phone";
            case "email" -> "email";
            case "status" -> "status";
            case "source" -> "source";
            case "rating" -> "rating";
            case "industry" -> "industry";
            case "campaign" -> "campaign.name";
            case "city" -> "city";
            case "expectedRevenue" -> "expectedRevenue";
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

