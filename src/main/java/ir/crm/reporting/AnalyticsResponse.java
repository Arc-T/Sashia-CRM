package ir.crm.reporting;

import java.util.List;

public record AnalyticsResponse(List<SeriesPoint> funnel, List<TrendPoint> trend, List<TeamPerformance> team,
                                List<SeriesPoint> forecast, List<Insight> insights) {

    public record TrendPoint(String label, long leads, long won) {}

    public record TeamPerformance(String user, long deals, double revenue, double quotaPct, long calls) {}

    /** tone: good | warn | info */
    public record Insight(String title, String text, String tone) {}
}
