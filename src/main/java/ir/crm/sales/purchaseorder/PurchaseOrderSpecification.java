package ir.crm.sales.purchaseorder;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class PurchaseOrderSpecification {

    private PurchaseOrderSpecification() {}

    public static Specification<PurchaseOrder> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("search") && !params.get("search").isBlank()) {
                String search = "%" + params.get("search").trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("number")), search));
            }

            if (params.containsKey("vendor") && !params.get("vendor").isBlank()) {
                String val = params.get("vendor").trim();
                try {
                    Long vId = Long.valueOf(val);
                    predicates.add(cb.equal(root.get("vendor").get("id"), vId));
                } catch (NumberFormatException e) {
                    predicates.add(cb.like(cb.lower(root.get("vendor").get("name")), "%" + val.toLowerCase() + "%"));
                }
            }

            if (params.containsKey("status") && !params.get("status").isBlank()) {
                try {
                    PurchaseOrderStatus status = PurchaseOrderStatus.fromKey(params.get("status").trim());
                    predicates.add(cb.equal(root.get("status"), status));
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
            case "number" -> "number";
            case "vendor" -> "vendor.name";
            case "status" -> "status";
            case "orderDate" -> "orderDate";
            case "total" -> "total";
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

