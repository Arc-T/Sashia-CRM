package ir.crm.catalog.inventoryitem;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class InventoryItemSpecification {

    private InventoryItemSpecification() {}

    public static Specification<InventoryItem> build(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (params == null || params.isEmpty()) {
                return cb.conjunction();
            }

            String search = params.get("search");
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("sku")), pattern)
                ));
            }

            if (params.containsKey("sku") && !params.get("sku").isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("sku")), "%" + params.get("sku").trim().toLowerCase() + "%"));
            }
            if (params.containsKey("product") && !params.get("product").isBlank()) {
                try {
                    Long productId = Long.parseLong(params.get("product").trim());
                    predicates.add(cb.equal(root.get("product").get("id"), productId));
                } catch (NumberFormatException ignored) {}
            }
            if (params.containsKey("warehouse") && !params.get("warehouse").isBlank()) {
                try {
                    InventoryItemWarehouse wh = InventoryItemWarehouse.valueOf(params.get("warehouse").trim());
                    predicates.add(cb.equal(root.get("warehouse"), wh));
                } catch (IllegalArgumentException ignored) {}
            }
            if (params.containsKey("status") && !params.get("status").isBlank()) {
                try {
                    InventoryItemStatus st = InventoryItemStatus.valueOf(params.get("status").trim());
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
