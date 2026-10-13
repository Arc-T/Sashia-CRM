package ir.crm.reporting;

import ir.crm.shared.exception.BusinessException;
import ir.crm.shared.exception.ErrorCode;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.Messages;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ir.crm.reporting.ReportingPeriods.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final DSLContext dsl;
    private final Messages messages;

    private record Definition(String id, String category, String enumName, String sql) {}

    private static final List<Definition> DEFINITIONS = List.of(
            new Definition("lead-by-source", "leads", "LeadSource",
                    "SELECT COALESCE(source, 'UNKNOWN') AS name, COUNT(*) AS cnt, COALESCE(SUM(expected_revenue), 0) AS amount FROM leads WHERE created_at >= {0} GROUP BY source"),
            new Definition("lead-by-status", "leads", "LeadStatus",
                    "SELECT status AS name, COUNT(*) AS cnt, COALESCE(SUM(expected_revenue), 0) AS amount FROM leads WHERE created_at >= {0} GROUP BY status"),
            new Definition("opp-by-stage", "sales", "OpportunityStage",
                    "SELECT stage AS name, COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS amount FROM opportunities WHERE created_at >= {0} GROUP BY stage"),
            new Definition("opp-by-source", "sales", "OpportunitySource",
                    "SELECT COALESCE(source, 'UNKNOWN') AS name, COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS amount FROM opportunities WHERE created_at >= {0} GROUP BY source"),
            new Definition("revenue-month", "finance", null,
                    "SELECT 'unused' AS name, 0 AS cnt, 0 AS amount"),
            new Definition("invoice-aging", "finance", null,
                    """
                    SELECT CASE
                             WHEN DATEDIFF(CURRENT_DATE, due_date) <= 0  THEN 'CURRENT'
                             WHEN DATEDIFF(CURRENT_DATE, due_date) <= 30 THEN '1_30'
                             WHEN DATEDIFF(CURRENT_DATE, due_date) <= 60 THEN '31_60'
                             WHEN DATEDIFF(CURRENT_DATE, due_date) <= 90 THEN '61_90'
                             ELSE 'OVER_90'
                           END AS name,
                           COUNT(*) AS cnt, COALESCE(SUM(total), 0) AS amount
                    FROM invoices WHERE status IN ('SENT','OVERDUE') GROUP BY name"""),
            new Definition("case-by-status", "support", "SupportCaseStatus",
                    "SELECT status AS name, COUNT(*) AS cnt, 0 AS amount FROM cases WHERE created_at >= {0} GROUP BY status"),
            new Definition("case-by-priority", "support", "SupportCasePriority",
                    "SELECT priority AS name, COUNT(*) AS cnt, 0 AS amount FROM cases WHERE created_at >= {0} GROUP BY priority"));

    private static final List<String> AGING_ORDER = List.of("CURRENT", "1_30", "31_60", "61_90", "OVER_90");

    public List<ReportMetaResponse> list() {
        return DEFINITIONS.stream().map(d -> new ReportMetaResponse(d.id(), messages.get("report." + d.id() + ".title"),
                d.category(), messages.get("report." + d.id() + ".description"))).toList();
    }

    public ReportResultResponse run(String id, String period) {
        Definition def = DEFINITIONS.stream().filter(d -> d.id().equals(id)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("report", id));
        YearMonth current = currentMonth();
        YearMonth from = switch (period == null ? "quarter" : period) {
            case "month" -> current;
            case "quarter" -> current.minusMonths(2);
            case "year" -> YearMonth.of(current.getYear(), 1);
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "period");
        };
        List<Row> rows = id.equals("revenue-month") ? revenueRows(from, current) : queryRows(def, from);
        return toResult(rows);
    }

    private record Row(String name, long count, double amount) {}

    private List<Row> queryRows(Definition def, YearMonth from) {
        LocalDate fromDate = from.atDay(1);
        UnaryOperator<String> label = name -> def.enumName() != null
                ? messages.getOrDefault("enum." + def.enumName() + "." + name, name) : name;

        List<Row> rows = dsl.resultQuery(def.sql(), utc(fromDate))
                .fetch(r -> {
                    String name = r.get("name", String.class);
                    long count = ((Number) r.get("cnt")).longValue();
                    BigDecimal amount = (BigDecimal) r.get("amount");
                    return new Row(label.apply(name), count, amount != null ? amount.doubleValue() : 0.0);
                });

        if (def.id().equals("invoice-aging")) return agingRows(rows);
        return rows;
    }

    private List<Row> agingRows(List<Row> raw) {
        Map<String, Row> byBucket = new HashMap<>();
        raw.forEach(r -> byBucket.put(r.name(), r));
        List<Row> out = new ArrayList<>();
        for (String bucket : AGING_ORDER) {
            Row r = byBucket.get(bucket);
            out.add(new Row(messages.get("report.aging." + bucket), r == null ? 0 : r.count(), r == null ? 0 : r.amount()));
        }
        return out;
    }

    private List<Row> revenueRows(YearMonth from, YearMonth current) {
        Map<YearMonth, double[]> months = new LinkedHashMap<>();
        for (YearMonth m = from; !m.isAfter(current); m = m.plusMonths(1)) months.put(m, new double[2]);

        dsl.select(DSL.field("issue_date", LocalDate.class), DSL.count(), DSL.sum(DSL.field("total", BigDecimal.class)))
                .from(DSL.table("invoices"))
                .where(DSL.field("status").eq("PAID"))
                .and(DSL.field("issue_date").ge(from.atDay(1)))
                .groupBy(DSL.field("issue_date"))
                .fetch()
                .forEach(r -> {
                    LocalDate d = r.get(DSL.field("issue_date", LocalDate.class));
                    long cnt = ((Number) r.get(1)).longValue();
                    BigDecimal tot = (BigDecimal) r.get(2);
                    if (d != null && tot != null) {
                        double[] acc = months.get(monthOf(d));
                        if (acc != null) {
                            acc[0] += cnt;
                            acc[1] += tot.doubleValue();
                        }
                    }
                });

        List<Row> out = new ArrayList<>();
        months.forEach((m, totals) -> out.add(new Row(monthLabel(m), (long) totals[0], totals[1])));
        return out;
    }

    private ReportResultResponse toResult(List<Row> rows) {
        long totalCount = rows.stream().mapToLong(Row::count).sum();
        double totalAmount = rows.stream().mapToDouble(Row::amount).sum();
        List<ReportResultResponse.Summary> summary = List.of(
                new ReportResultResponse.Summary("تعداد کل", String.valueOf(totalCount)),
                new ReportResultResponse.Summary("مبلغ کل", String.format("%,.0f", totalAmount))
        );
        List<SeriesPoint> chart = rows.stream()
                .map(r -> new SeriesPoint(r.name(), r.amount() > 0 ? r.amount() : (double) r.count(), null))
                .toList();
        List<ReportResultResponse.Column> columns = List.of(
                new ReportResultResponse.Column("name", "عنوان"),
                new ReportResultResponse.Column("count", "تعداد"),
                new ReportResultResponse.Column("amount", "مبلغ")
        );
        List<Map<String, Object>> mappedRows = rows.stream()
                .map(r -> Map.<String, Object>of(
                        "name", r.name(),
                        "count", r.count(),
                        "amount", r.amount()
                ))
                .toList();
        return new ReportResultResponse(summary, chart, columns, mappedRows);
    }
}
