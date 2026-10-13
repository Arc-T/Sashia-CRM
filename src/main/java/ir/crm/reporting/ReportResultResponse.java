package ir.crm.reporting;

import java.util.List;
import java.util.Map;

public record ReportResultResponse(List<Summary> summary, List<SeriesPoint> chart, List<Column> columns,
                                   List<Map<String, Object>> rows) {

    public record Summary(String label, String value) {}

    public record Column(String key, String title) {}
}
