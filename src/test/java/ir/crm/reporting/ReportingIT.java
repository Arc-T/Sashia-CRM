package ir.crm.reporting;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import ir.crm.support.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Runs every dashboard/analytics/report query against real MySQL so SQL/schema drift is caught. */
class ReportingIT extends AbstractIntegrationTest {

    @Test
    void dashboardHasFourKpisAndTwelveMonths() throws Exception {
        doGet("/api/v1/dashboard", adminToken()).andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis", hasSize(4))).andExpect(jsonPath("$.kpis[0].key").value("revenue"))
                .andExpect(jsonPath("$.revenueByMonth", hasSize(12))).andExpect(jsonPath("$.pipeline", hasSize(5)));
    }

    @Test
    void analyticsQueriesRun() throws Exception {
        doGet("/api/v1/analytics", adminToken()).andExpect(status().isOk())
                .andExpect(jsonPath("$.funnel", hasSize(5))).andExpect(jsonPath("$.trend", hasSize(9)))
                .andExpect(jsonPath("$.forecast", hasSize(4))).andExpect(jsonPath("$.insights").isNotEmpty());
    }

    @Test
    void everyReportRunsForEveryPeriod() throws Exception {
        String token = adminToken();
        String body = doGet("/api/v1/reports", token).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(body, "$[*].id");
        org.assertj.core.api.Assertions.assertThat(ids).hasSize(8);
        for (String id : ids) {
            for (String period : List.of("month", "quarter", "year")) {
                doGet("/api/v1/reports/" + id + "?period=" + period, token).andExpect(status().isOk())
                        .andExpect(jsonPath("$.columns", hasSize(4))).andExpect(jsonPath("$.summary", hasSize(3)));
            }
        }
    }

    @Test
    void unknownReportAndPeriodAreRejected() throws Exception {
        String token = adminToken();
        doGet("/api/v1/reports/nope", token).andExpect(status().isNotFound());
        doGet("/api/v1/reports/lead-source?period=decade", token).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
