package ir.crm.reporting;

import java.time.Instant;
import java.util.List;

public record DashboardResponse(List<Kpi> kpis, List<SeriesPoint> revenueByMonth, List<SeriesPoint> leadSources,
                                List<PipelineStage> pipeline, List<FeedItem> feed) {

    /** format: "number" | "money"; delta: percent change versus the previous Jalali month. */
    public record Kpi(String key, String label, double value, double delta, String format, String icon) {}

    public record PipelineStage(String key, String label, long count, double amount) {}

    public record FeedItem(String id, String user, String action, String target, Instant at) {}
}
