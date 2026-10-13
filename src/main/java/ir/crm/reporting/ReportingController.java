package ir.crm.reporting;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only endpoints available to every authenticated user. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reporting")
public class ReportingController {

    private final DashboardService dashboard;
    private final AnalyticsService analytics;
    private final ReportService reports;

    ReportingController(DashboardService dashboard, AnalyticsService analytics, ReportService reports) {
        this.dashboard = dashboard;
        this.analytics = analytics;
        this.reports = reports;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() { return dashboard.dashboard(); }

    @GetMapping("/analytics")
    public AnalyticsResponse analytics() { return analytics.analytics(); }

    @GetMapping("/reports")
    public List<ReportMetaResponse> reports() { return reports.list(); }

    @GetMapping("/reports/{id}")
    public ReportResultResponse report(@PathVariable String id, @RequestParam(defaultValue = "quarter") String period) {
        return reports.run(id, period);
    }
}
