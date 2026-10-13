package ir.crm.reporting;

import ir.crm.shared.web.Messages;
import java.math.BigDecimal;
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
public class AnalyticsService {

    private final DSLContext dsl;
    private final Messages messages;

    public AnalyticsResponse analytics() {
        YearMonth month = currentMonth();
        long leads = dsl.fetchCount(DSL.table("leads"));
        long qualified = dsl.fetchCount(DSL.table("leads"),
                DSL.field("status").in("QUALIFIED", "CONVERTED"));
        long opps = dsl.fetchCount(DSL.table("opportunities"));
        long quotes = dsl.fetchCount(DSL.table("quotes"));
        long won = dsl.fetchCount(DSL.table("opportunities"),
                DSL.field("stage").eq("WON"));
        List<SeriesPoint> funnel = List.of(
                SeriesPoint.of(messages.get("analytics.funnel.leads"), leads),
                SeriesPoint.of(messages.get("analytics.funnel.qualified"), qualified),
                SeriesPoint.of(messages.get("analytics.funnel.opportunities"), opps),
                SeriesPoint.of(messages.get("analytics.funnel.quotes"), quotes),
                SeriesPoint.of(messages.get("analytics.funnel.won"), won));
        return new AnalyticsResponse(funnel, trend(month), team(month), forecast(month), insights(leads, opps));
    }

    private List<AnalyticsResponse.TrendPoint> trend(YearMonth current) {
        YearMonth first = current.minusMonths(8);
        Map<YearMonth, Long> leads = new HashMap<>();
        Map<YearMonth, Long> won = new HashMap<>();

        dsl.select(DSL.field("created_at", LocalDateTime.class))
                .from(DSL.table("leads"))
                .where(DSL.field("created_at").ge(utc(first.atDay(1))))
                .fetch(DSL.field("created_at", LocalDateTime.class))
                .forEach(t -> {
                    if (t != null) {
                        leads.merge(monthOf(t.toInstant(ZoneOffset.UTC).atZone(TEHRAN).toLocalDate()), 1L, Long::sum);
                    }
                });

        dsl.select(DSL.field("close_date", LocalDate.class))
                .from(DSL.table("opportunities"))
                .where(DSL.field("stage").eq("WON"))
                .and(DSL.field("close_date").ge(first.atDay(1)))
                .fetch(DSL.field("close_date", LocalDate.class))
                .forEach(d -> {
                    if (d != null) {
                        won.merge(monthOf(d), 1L, Long::sum);
                    }
                });

        List<AnalyticsResponse.TrendPoint> out = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            YearMonth m = first.plusMonths(i);
            out.add(new AnalyticsResponse.TrendPoint(monthLabel(m), leads.getOrDefault(m, 0L), won.getOrDefault(m, 0L)));
        }
        return out;
    }

    private List<AnalyticsResponse.TeamPerformance> team(YearMonth month) {
        var query = dsl.select(
                DSL.field("u.name", String.class).as("name"),
                DSL.count(DSL.field("o.id")).as("deals"),
                DSL.coalesce(DSL.sum(DSL.field("o.amount", BigDecimal.class)), BigDecimal.ZERO).as("revenue"),
                dsl.selectCount().from(DSL.table("events").as("e"))
                        .where(DSL.field("e.assigned_to_id").eq(DSL.field("u.id")))
                        .and(DSL.field("e.type").eq("CALL"))
                        .and(DSL.field("e.status").eq("HELD")).asField("calls"),
                DSL.coalesce(DSL.field("t.monthly_target", BigDecimal.class), BigDecimal.ZERO).as("target")
        ).from(DSL.table("users").as("u"))
        .leftJoin(DSL.table("opportunities").as("o"))
                .on(DSL.field("o.assigned_to_id").eq(DSL.field("u.id")))
                .and(DSL.field("o.stage").eq("WON"))
                .and(DSL.field("o.close_date").ge(month.atDay(1)))
                .and(DSL.field("o.close_date").lt(month.plusMonths(1).atDay(1)))
        .leftJoin(DSL.table("teams").as("t")).on(DSL.field("t.id").eq(DSL.field("u.team_id")))
        .where(DSL.field("u.status").eq("ACTIVE"))
        .groupBy(DSL.field("u.id"), DSL.field("u.name"), DSL.field("t.monthly_target"))
        .orderBy(DSL.field("revenue").desc())
        .limit(10);

        return query.fetch(r -> {
            BigDecimal revBg = (BigDecimal) r.get("revenue");
            BigDecimal tgtBg = (BigDecimal) r.get("target");
            double revenue = revBg != null ? revBg.doubleValue() : 0.0;
            double target = tgtBg != null ? tgtBg.doubleValue() : 0.0;
            double quota = target > 0 ? Math.round(revenue / target * 1000) / 10.0 : 0;
            long calls = r.get("calls") != null ? ((Number) r.get("calls")).longValue() : 0L;
            long deals = r.get("deals") != null ? ((Number) r.get("deals")).longValue() : 0L;
            return new AnalyticsResponse.TeamPerformance((String) r.get("name"), deals, revenue, quota, calls);
        });
    }

    private List<SeriesPoint> forecast(YearMonth current) {
        Map<YearMonth, Double> sums = new HashMap<>();
        dsl.select(
                DSL.field("close_date", LocalDate.class),
                DSL.sum(DSL.field("amount", BigDecimal.class).mul(DSL.coalesce(DSL.field("probability", Integer.class), 0)).div(100)).as("weighted")
        ).from(DSL.table("opportunities"))
        .where(DSL.field("stage").notIn("WON", "LOST"))
        .and(DSL.field("close_date").ge(current.atDay(1)))
        .and(DSL.field("close_date").lt(current.plusMonths(4).atDay(1)))
        .groupBy(DSL.field("close_date"))
        .fetch()
        .forEach(r -> {
            LocalDate d = r.get(DSL.field("close_date", LocalDate.class));
            BigDecimal w = (BigDecimal) r.get("weighted");
            if (d != null && w != null) sums.merge(monthOf(d), w.doubleValue(), Double::sum);
        });

        BigDecimal target = dsl.select(DSL.coalesce(DSL.sum(DSL.field("monthly_target", BigDecimal.class)), BigDecimal.ZERO))
                .from(DSL.table("teams"))
                .fetchOne(0, BigDecimal.class);
        Double targetBillions = target != null && target.signum() > 0 ? round2(target.doubleValue() / 1e9) : null;
        List<SeriesPoint> out = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            YearMonth m = current.plusMonths(i);
            out.add(new SeriesPoint(monthLabel(m), round2(sums.getOrDefault(m, 0.0) / 1e9), targetBillions));
        }
        return out;
    }

    private List<AnalyticsResponse.Insight> insights(long leads, long opps) {
        List<AnalyticsResponse.Insight> out = new ArrayList<>();
        double conversion = leads == 0 ? 0 : Math.round((double) opps / leads * 1000) / 10.0;
        out.add(new AnalyticsResponse.Insight(messages.get("analytics.insight.conversion.title"),
                messages.get("analytics.insight.conversion.text", number(conversion)), conversion >= 20 ? "good" : "info"));

        Long stale = dsl.selectCount().from(DSL.table("opportunities"))
                .where(DSL.field("stage").eq("NEGOTIATION"))
                .and(DSL.field("updated_at").lt(LocalDateTime.now(ZoneOffset.UTC).minusDays(14)))
                .fetchOne(0, Long.class);
        if (stale != null && stale > 0) {
            out.add(new AnalyticsResponse.Insight(messages.get("analytics.insight.stale.title"),
                    messages.get("analytics.insight.stale.text", number(stale)), "warning"));
        }
        return out;
    }
}
