package ir.crm.sales;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.crm.support.AbstractIntegrationTest;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class LeadApiIT extends AbstractIntegrationTest {

    private static String lead(String name, String status) {
        return "{\"name\":\"" + name + "\",\"phone\":\"09123456789\",\"status\":\"" + status + "\",\"source\":\"web\"}";
    }

    @Test
    void invalidPayloadReturnsPersianFieldErrors() throws Exception {
        doPost("/api/v1/leads", adminToken(), "{\"name\":\"\",\"phone\":\"123\",\"email\":\"bad\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name[0]").value("این فیلد الزامی است"))
                .andExpect(jsonPath("$.errors.phone[0]", containsString("شمارهٔ تلفن معتبر نیست")))
                .andExpect(jsonPath("$.errors.email[0]").value("ایمیل معتبر نیست"))
                .andExpect(jsonPath("$.errors.status[0]").value("این فیلد الزامی است"))
                .andExpect(jsonPath("$.violations[?(@.field=='name')].label").value(hasItem("نام و نام خانوادگی")));
    }

    @Test
    void unknownEnumValueIsAMalformedRequest() throws Exception {
        doPost("/api/v1/leads", adminToken(), lead(unique("l"), "flying"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        String token = adminToken();
        String name = unique("سرنخ");
        String id = create("/api/v1/leads", token, lead(name, "new"));

        doGet("/api/v1/leads/" + id, token).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.status").value("new"))
                .andExpect(jsonPath("$.source").value("web")).andExpect(jsonPath("$.createdAt").exists());

        doGet("/api/v1/leads?search=" + name + "&status=new&sortField=name&sortOrder=ascend&pageSize=5", token)
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items", hasSize(1))).andExpect(jsonPath("$.items[0].name").value(name));
        doGet("/api/v1/leads?search=" + name + "&status=lost", token).andExpect(jsonPath("$.total").value(0));

        doPut("/api/v1/leads/" + id, token, lead(name + "-ویرایش", "qualified")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("qualified")).andExpect(jsonPath("$.name").value(name + "-ویرایش"));

        doDelete("/api/v1/leads/" + id, token).andExpect(status().isNoContent());
        doGet("/api/v1/leads/" + id, token).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("سرنخ")));
    }

    @Test
    void lookupFieldsAreValidatedAndExpandedToLabels() throws Exception {
        String token = adminToken();
        doPost("/api/v1/leads", token, "{\"name\":\"x\",\"phone\":\"09123456789\",\"status\":\"new\",\"assignedTo\":99999999}")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("INVALID_REFERENCE"))
                .andExpect(jsonPath("$.message", containsString("مسئول")));

        String adminId = com.jayway.jsonpath.JsonPath.read(
                doGet("/api/v1/auth/me", token).andReturn().getResponse().getContentAsString(), "$.id");
        String id = create("/api/v1/leads", token, "{\"name\":\"" + unique("l") + "\",\"phone\":\"09123456789\",\"status\":\"new\",\"assignedTo\":" + adminId + "}");
        doGet("/api/v1/leads/" + id, token).andExpect(jsonPath("$.assignedTo.id").value(adminId))
                .andExpect(jsonPath("$.assignedTo.label").value("مدیر سیستم"));
        doGet("/api/v1/leads?assignedTo=" + adminId + "&pageSize=100", token).andExpect(status().isOk());
        doGet("/api/v1/users/lookup?search=مدیر", token).andExpect(jsonPath("$[0].label").value("مدیر سیستم"));
        doGet("/api/v1/leads?assignedTo=abc", token).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void bulkDeleteRemovesAllSelectedRecords() throws Exception {
        String token = adminToken();
        String a = create("/api/v1/leads", token, lead(unique("a"), "new"));
        String b = create("/api/v1/leads", token, lead(unique("b"), "new"));
        doPost("/api/v1/leads/bulk-delete", token, "{\"ids\":[" + a + "," + b + "]}").andExpect(status().isNoContent());
        doGet("/api/v1/leads/" + a, token).andExpect(status().isNotFound());
        doPost("/api/v1/leads/bulk-delete", token, "{\"ids\":[]}").andExpect(status().isBadRequest());
    }

    @Test
    void changesAppearInTheActivityTimeline() throws Exception {
        String token = adminToken();
        String name = unique("timeline");
        String id = create("/api/v1/leads", token, lead(name, "new"));
        // The timeline is written asynchronously by the activity module after the transaction commits.
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                doGet("/api/v1/leads/" + id + "/activities", token).andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].kind").value("create"))
                        .andExpect(jsonPath("$[0].text", containsString(name))));
    }
}
