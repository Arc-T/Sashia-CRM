package ir.crm.shared;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.DuplicateResourceException;
import ir.crm.shared.exception.InvalidReferenceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.GlobalExceptionHandler;
import ir.crm.shared.web.Messages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Exercises the advice in isolation (no Spring context, no database). */
class GlobalExceptionHandlerTest {

    record ProbeRequest(@NotBlank String name, @Email String email, @Min(1) int qty,
                        @JsonProperty("to") @NotBlank String toAddress) {}

    @RestController
    static class Probe {
        @GetMapping("/probe/missing") String missing() { throw new ResourceNotFoundException("lead", 7); }
        @GetMapping("/probe/duplicate") String duplicate() { throw new DuplicateResourceException("user.email", "a@b.ir"); }
        @GetMapping("/probe/reference") String reference() { throw new InvalidReferenceException("lead.assignedTo", 99); }
        @GetMapping("/probe/rule") String rule() { throw new BusinessRuleException("rule.user.cannotDeleteSelf"); }
        @GetMapping("/probe/denied") String denied() { throw new AccessDeniedException("no"); }
        @GetMapping("/probe/boom") String boom() { throw new IllegalStateException("secret internals"); }
        @GetMapping("/probe/typed/{id}") String typed(@PathVariable Long id) { return "ok"; }
        @GetMapping("/probe/inuse") String inUse() {
            throw new DataIntegrityViolationException("fk", new SQLException("child row", "23000", 1451));
        }
        @PostMapping("/probe/validate") String validate(@Valid @RequestBody ProbeRequest r) { return "ok"; }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages", "messages-fields", "messages-enums");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(source);
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new Probe())
                .setControllerAdvice(new GlobalExceptionHandler(new Messages(source)))
                .setValidator(validator).build();
    }

    @Test
    void notFoundNamesTheResourceInPersian() throws Exception {
        mvc.perform(get("/probe/missing")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/probe/missing"))
                .andExpect(jsonPath("$.message", containsString("سرنخ")))
                .andExpect(jsonPath("$.message", containsString("7")));
    }

    @Test
    void duplicateAndInvalidReferenceUseFieldLabels() throws Exception {
        mvc.perform(get("/probe/duplicate")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message", containsString("ایمیل")));
        mvc.perform(get("/probe/reference")).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_REFERENCE"))
                .andExpect(jsonPath("$.message", containsString("مسئول")));
    }

    @Test
    void businessRuleKeyIsLocalised() throws Exception {
        mvc.perform(get("/probe/rule")).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("امکان حذف حساب کاربری خودتان وجود ندارد."));
    }

    @Test
    void beanValidationProducesPersianFieldErrorsUnderTheJsonName() throws Exception {
        mvc.perform(post("/probe/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad\",\"qty\":0,\"to\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name[0]").value("این فیلد الزامی است"))
                .andExpect(jsonPath("$.errors.email[0]").value("ایمیل معتبر نیست"))
                .andExpect(jsonPath("$.errors.qty[0]", containsString("حداقل 1")))
                .andExpect(jsonPath("$.errors.to[0]").value("این فیلد الزامی است"))
                .andExpect(jsonPath("$.violations.length()").value(4));
    }

    @Test
    void malformedJsonIsABadRequest() throws Exception {
        mvc.perform(post("/probe/validate").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void wrongPathVariableTypeNamesTheParameter() throws Exception {
        mvc.perform(get("/probe/typed/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message", containsString("id")));
    }

    @Test
    void foreignKeyViolationMapsToConflict() throws Exception {
        mvc.perform(get("/probe/inuse")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REFERENCE_IN_USE"));
    }

    @Test
    void accessDeniedIs403() throws Exception {
        mvc.perform(get("/probe/denied")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("شما اجازهٔ انجام این عملیات را ندارید."));
    }

    @Test
    void unexpectedErrorsNeverLeakInternals() throws Exception {
        mvc.perform(get("/probe/boom")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(containsString("secret"))));
    }

    @Test
    void unknownRouteAndWrongMethodUseMvcStatuses() throws Exception {
        mvc.perform(get("/nope")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"));
        mvc.perform(post("/probe/missing")).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }
}
