package ir.crm.reporting;

import ir.crm.shared.web.Messages;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ir.crm.reporting.ReportingPeriods.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final DSLContext dsl;
    private final Messages messages;

    public DashboardResponse dashboard() {
        YearMonth month = currentMonth();
        LocalDate start = month.atDay(1);
        LocalDate next = month.plusMonths(1).atDay(1);
        LocalDate prev = month.minusMonths(1).atDay(1);

        double revenue = paidRevenue(start, next);
        double revenuePrev = paidRevenue(prev, start);
        long leads = createdBetween("leads", start, next);
        long leadsPrev = createdBetween("leads", prev, start);
        long oppsOpen = dsl.fetchCount(DSL.table("opportunities"),
                DSL.field("stage").notIn("WON", "LOST"));
        long oppsNew = createdBetween("opportunities", start, next);
        long oppsPrev = createdBetween("opportunities", prev, start);
        long casesOpen = dsl.fetchCount(DSL.table("cases"),
                DSL.field("status").notIn("RESOLVED", "CLOSED"));
        long casesNew = createdBetween("cases", start, next);
        long casesPrev = createdBetween("cases", prev, start);

        List<DashboardResponse.Kpi> kpis = List.of(
                new DashboardResponse.Kpi("revenue", messages.get("dashboard.kpi.revenue"), revenue, pctChange(revenue, revenuePrev), "money", "money"),
                new DashboardResponse.Kpi("leads", messages.get("dashboard.kpi.leads"), leads, pctChange(leads, leadsPrev), "number", "leads"),
                new DashboardResponse.Kpi("opps", messages.get("dashboard.kpi.opportunities"), oppsOpen, pctChange(oppsNew, oppsPrev), "number", "opportunities"),
                new DashboardResponse.Kpi("cases", messages.get("dashboard.kpi.cases"), casesOpen, pctChange(casesNew, casesPrev), "number", "cases"));

        return new DashboardResponse(kpis, revenueByMonth(month), leadSources(), pipeline(), feed());
    }

    private List<SeriesPoint> revenueByMonth(YearMonth current) {
        YearMonth first = current.minusMonths(11);
        Map<YearMonth, Double> sums = new HashMap<>();
        dsl.select(DSL.field("issue_date", LocalDate.class), DSL.sum(DSL.field("total", BigDecimal.class)).as("sum_total"))
                .from(DSL.table("invoices"))
                .where(DSL.field("status").eq("PAID"))
                .and(DSL.field("issue_date").ge(first.atDay(1)))
                .groupBy(DSL.field("issue_date"))
                .fetch()
                .forEach(r -> {
                    LocalDate d = r.get(DSL.field("issue_date", LocalDate.class));
                    BigDecimal tot = (BigDecimal) r.get("sum_total");
                    if (d != null && tot != null) sums.merge(monthOf(d), tot.doubleValue(), Double::sum);
                });

        BigDecimal target = dsl.select(DSL.coalesce(DSL.sum(DSL.field("monthly_target", BigDecimal.class)), BigDecimal.ZERO))
                .from(DSL.table("teams"))
                .fetchOne(0, BigDecimal.class);
        Double targetBillions = target != null && target.signum() > 0 ? round2(target.doubleValue() / 1e9) : null;
        List<SeriesPoint> points = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            YearMonth m = first.plusMonths(i);
            points.add(new SeriesPoint(monthLabel(m), round2(sums.getOrDefault(m, 0.0) / 1e9), targetBillions));
        }
        return points;
    }

    private List<SeriesPoint> leadSources() {
        return dsl.select(DSL.field("source", String.class), DSL.count())
                .from(DSL.table("leads"))
                .where(DSL.field("source").isNotNull())
                .groupBy(DSL.field("source"))
                .orderBy(DSL.count().desc())
                .fetch(r -> SeriesPoint.of(enumLabel("LeadSource", r.get(DSL.field("source", String.class))), ((Number) r.get(1)).longValue()));
    }

    private List<DashboardResponse.PipelineStage> pipeline() {
        return dsl.select(
                DSL.field("stage", String.class),
                DSL.count(),
                DSL.coalesce(DSL.sum(DSL.field("amount", BigDecimal.class)), BigDecimal.ZERO)
        ).from(DSL.table("opportunities"))
        .where(DSL.field("stage").notIn("WON", "LOST"))
        .groupBy(DSL.field("stage"))
        .orderBy(DSL.field("stage"))
        .fetch(r -> new DashboardResponse.PipelineStage(
                r.get(DSL.field("stage", String.class)),
                enumLabel("OpportunityStage", r.get(DSL.field("stage", String.class))),
                ((Number) r.get(1)).longValue(),
                ((BigDecimal) r.get(2)).doubleValue()));
    }

    private List<DashboardResponse.FeedItem> feed() {
        return dsl.select(
                DSL.field("id", Long.class),
                DSL.field("actor", String.class),
                DSL.field("kind", String.class),
                DSL.field("text", String.class),
                DSL.field("created_at", LocalDateTime.class)
        ).from(DSL.table("activity_log"))
        .orderBy(DSL.field("created_at").desc())
        .limit(10)
        .fetch(r -> new DashboardResponse.FeedItem(
                String.valueOf(r.get(DSL.field("id", Long.class))),
                r.get(DSL.field("actor", String.class)),
                r.get(DSL.field("kind", String.class)),
                r.get(DSL.field("text", String.class)),
                r.get(DSL.field("created_at", LocalDateTime.class)) != null
                        ? r.get(DSL.field("created_at", LocalDateTime.class)).toInstant(ZoneOffset.UTC)
                        : Instant.now()));
    }

    private double paidRevenue(LocalDate from, LocalDate to) {
        BigDecimal sum = dsl.select(DSL.coalesce(DSL.sum(DSL.field("total", BigDecimal.class)), BigDecimal.ZERO))
                .from(DSL.table("invoices"))
                .where(DSL.field("status").eq("PAID"))
                .and(DSL.field("issue_date").ge(from))
                .and(DSL.field("issue_date").lt(to))
                .fetchOne(0, BigDecimal.class);
        return sum == null ? 0 : sum.doubleValue();
    }

    private long createdBetween(String tableName, LocalDate from, LocalDate to) {
        return dsl.fetchCount(DSL.table(tableName),
                DSL.field("created_at").ge(utc(from)).and(DSL.field("created_at").lt(utc(to))));
    }

    private String enumLabel(String enumClass, String key) {
        return messages.getOrDefault("enum." + enumClass + "." + key, key);
    }
}
