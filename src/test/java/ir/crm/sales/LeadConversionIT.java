package ir.crm.sales;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.crm.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class LeadConversionIT extends AbstractIntegrationTest {

    @Test
    void convertingALeadCreatesOrganizationContactAndOpportunityOnce() throws Exception {
        String token = adminToken();
        String company = unique("شرکت");
        String id = create("/api/v1/leads", token, "{\"name\":\"علی رضایی\",\"company\":\"" + company
                + "\",\"phone\":\"09123456789\",\"status\":\"qualified\",\"source\":\"referral\",\"industry\":\"tech\",\"expectedRevenue\":250000000}");

        doPost("/api/v1/leads/" + id + "/convert", token, "{\"createOpportunity\":true}").andExpect(status().isOk())
                .andExpect(jsonPath("$.organization.label").value(company))
                .andExpect(jsonPath("$.contact.label").value("علی رضایی"))
                .andExpect(jsonPath("$.opportunity.label").value(company));

        doGet("/api/v1/leads/" + id, token).andExpect(jsonPath("$.status").value("converted"));
        doGet("/api/v1/organizations?search=" + company, token).andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].industry").value("tech"));
        doGet("/api/v1/opportunities?search=" + company, token).andExpect(jsonPath("$.items[0].stage").value("qualification"))
                .andExpect(jsonPath("$.items[0].amount").value(250000000));

        doPost("/api/v1/leads/" + id + "/convert", token, "{\"createOpportunity\":false}")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.message").value("این سرنخ قبلاً تبدیل شده است."));
    }

    @Test
    void convertingAMissingLeadIs404() throws Exception {
        doPost("/api/v1/leads/987654321/convert", adminToken(), "{\"createOpportunity\":false}")
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
