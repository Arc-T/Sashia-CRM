package ir.crm.identity;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import ir.crm.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class RolePermissionIT extends AbstractIntegrationTest {

    @Test
    void roleWithViewOnlyLeadsCanReadButNotWrite() throws Exception {
        String admin = adminToken();
        String roleId = create("/api/v1/roles", admin, "{\"name\":\"" + unique("نقش") + "\",\"kind\":\"sales\"}");
        doPut("/api/v1/roles/" + roleId + "/permissions", admin, "{\"matrix\":{\"leads\":[\"view\"]}}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.matrix.leads[0]").value("view"))
                .andExpect(jsonPath("$.matrix.contacts.length()").value(0));

        String email = unique("viewer") + "@example.ir";
        doPost("/api/v1/users", admin, "{\"name\":\"کاربر آزمایشی\",\"email\":\"" + email + "\",\"role\":" + roleId
                + ",\"status\":\"active\",\"password\":\"Passw0rd!!\"}").andExpect(status().isCreated());
        String token = login(email, "Passw0rd!!");

        doGet("/api/v1/leads", token).andExpect(status().isOk());
        doPost("/api/v1/leads", token, "{\"name\":\"x\",\"phone\":\"09123456789\",\"status\":\"new\"}")
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("شما اجازهٔ انجام این عملیات را ندارید."));
        doGet("/api/v1/contacts", token).andExpect(status().isForbidden());
        doGet("/api/v1/roles", token).andExpect(status().isForbidden());
    }

    @Test
    void adminRoleMatrixIsImmutable() throws Exception {
        String admin = adminToken();
        String body = doGet("/api/v1/roles?kind=admin", admin).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String adminRoleId = JsonPath.read(body, "$.items[0].id");
        doPut("/api/v1/roles/" + adminRoleId + "/permissions", admin, "{\"matrix\":{}}")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("دسترسی‌های نقش مدیر سیستم قابل ویرایش نیست."));
    }

    @Test
    void unknownModuleOrActionIsRejected() throws Exception {
        String admin = adminToken();
        String roleId = create("/api/v1/roles", admin, "{\"name\":\"" + unique("نقش") + "\",\"kind\":\"support\"}");
        doPut("/api/v1/roles/" + roleId + "/permissions", admin, "{\"matrix\":{\"ghosts\":[\"view\"]}}")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.message").value("ماژول «ghosts» وجود ندارد."));
        doPut("/api/v1/roles/" + roleId + "/permissions", admin, "{\"matrix\":{\"leads\":[\"fly\"]}}")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void duplicateEmailIsAConflictAndRolesInUseCannotBeDeleted() throws Exception {
        String admin = adminToken();
        String roleId = create("/api/v1/roles", admin, "{\"name\":\"" + unique("نقش") + "\",\"kind\":\"finance\"}");
        String email = unique("dup") + "@example.ir";
        String user = "{\"name\":\"تکراری\",\"email\":\"" + email + "\",\"role\":" + roleId + ",\"status\":\"active\"}";
        doPost("/api/v1/users", admin, user).andExpect(status().isCreated());
        doPost("/api/v1/users", admin, user).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
        doDelete("/api/v1/roles/" + roleId, admin).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }
}
