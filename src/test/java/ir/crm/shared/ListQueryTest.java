package ir.crm.shared;

import static org.assertj.core.api.Assertions.assertThat;

import ir.crm.shared.web.ListQuery;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ListQueryTest {

    @Test
    void appliesDefaultsAndSeparatesFilters() {
        ListQuery q = ListQuery.from(Map.of("status", "new", "assignedTo", "5"));
        assertThat(q.page()).isEqualTo(1);
        assertThat(q.pageSize()).isEqualTo(10);
        assertThat(q.filters()).containsOnlyKeys("status", "assignedTo");
        assertThat(q.ascending()).isTrue();
    }

    @Test
    void clampsPagingAndParsesSorting() {
        ListQuery q = ListQuery.from(Map.of("page", "0", "pageSize", "5000", "sortField", "name", "sortOrder", "descend", "search", "علی"));
        assertThat(q.page()).isEqualTo(1);
        assertThat(q.pageSize()).isEqualTo(ListQuery.MAX_PAGE_SIZE);
        assertThat(q.ascending()).isFalse();
        assertThat(q.sortField()).isEqualTo("name");
        assertThat(q.search()).isEqualTo("علی");
        assertThat(q.filters()).isEmpty();
    }

    @Test
    void toleratesGarbageNumbers() {
        ListQuery q = ListQuery.from(Map.of("page", "abc", "pageSize", "x"));
        assertThat(q.page()).isEqualTo(1);
        assertThat(q.pageSize()).isEqualTo(10);
    }
}
