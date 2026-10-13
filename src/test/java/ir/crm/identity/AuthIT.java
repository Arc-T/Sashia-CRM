package ir.crm.identity;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.crm.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class AuthIT extends AbstractIntegrationTest {

    @Test
    void adminCanLogInAndReadProfile() throws Exception {
        doPost("/api/v1/auth/login", null, "{\"username\":\"admin\",\"password\":\"Admin@12345\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.user.role").value("admin"))
                .andExpect(jsonPath("$.user.name").value("مدیر سیستم"));
        doGet("/api/v1/auth/me", adminToken()).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("admin@example.ir"));
    }

    @Test
    void wrongPasswordGivesPersian401() throws Exception {
        doPost("/api/v1/auth/login", null, "{\"username\":\"admin\",\"password\":\"wrong-password\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("نام کاربری یا رمز عبور اشتباه است."));
    }

    @Test
    void blankCredentialsAreValidationErrors() throws Exception {
        doPost("/api/v1/auth/login", null, "{\"username\":\"\",\"password\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username[0]").value("این فیلد الزامی است"))
                .andExpect(jsonPath("$.errors.password[0]").value("این فیلد الزامی است"));
    }

    @Test
    void protectedEndpointsRequireATokenAndReturnTheStandardBody() throws Exception {
        doGet("/api/v1/leads", null).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("برای دسترسی باید وارد حساب کاربری شوید یا نشست شما منقضی شده است."));
        doGet("/api/v1/leads", "not-a-jwt").andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
