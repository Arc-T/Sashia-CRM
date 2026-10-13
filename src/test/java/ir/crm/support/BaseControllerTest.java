package ir.crm.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc() {
        return mockMvc;
    }

    protected ObjectMapper objectMapper() {
        return objectMapper;
    }
}
