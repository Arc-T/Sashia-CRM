package ir.crm.sales.organization;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class OrganizationSpecification {

    private OrganizationSpecification() {}

    public static Specification<Organization> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("search") && !params.get("search").isBlank()) {
                String search = "%" + params.get("search").trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), search),
                        cb.like(cb.lower(root.get("phone")), search),
                        cb.like(cb.lower(root.get("email")), search),
                        cb.like(cb.lower(root.get("city")), search),
                        cb.like(cb.lower(root.get("address")), search)
                ));
            }

            if (params.containsKey("industry") && !params.get("industry").isBlank()) {
                try {
                    OrganizationIndustry industry = OrganizationIndustry.fromKey(params.get("industry").trim());
                    predicates.add(cb.equal(root.get("industry"), industry));
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

            if (params.containsKey("city") && !params.get("city").isBlank()) {
                predicates.add(cb.equal(root.get("city"), params.get("city").trim()));
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
            case "industry" -> "industry";
            case "phone" -> "phone";
            case "email" -> "email";
            case "website" -> "website";
            case "city" -> "city";
            case "annualRevenue" -> "annualRevenue";
            case "employees" -> "employees";
            case "assignedTo" -> "assignedTo.name";
            case "address" -> "address";
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

