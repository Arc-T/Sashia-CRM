package ir.crm.shared.web;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> items, long total, int page, int pageSize, int totalPages) {

    public static <T> PageResponse<T> of(List<T> items, Page<?> page) {
        return new PageResponse<>(items, page.getTotalElements(), page.getNumber() + 1, page.getSize(), page.getTotalPages());
    }
}
