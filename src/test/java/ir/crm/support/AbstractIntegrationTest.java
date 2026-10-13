package ir.crm.support;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.mysql.MySQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Boots the full application against a real MySQL (Testcontainers) with Flyway migrations applied and
 * Hibernate validating the schema. One container is shared by every IT class in the JVM.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("crm").withUsername("crm").withPassword("crm")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> MYSQL.getJdbcUrl() + "?useUnicode=true&characterEncoding=utf8&connectionTimeZone=UTC");
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired protected MockMvc mvc;

    protected static String unique(String prefix) { return prefix + "-" + UUID.randomUUID().toString().substring(0, 8); }

    protected String adminToken() throws Exception { return login("admin", "Admin@12345"); }

    protected String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected ResultActions doGet(String path, String token) throws Exception {
        return mvc.perform(auth(get(path), token));
    }

    protected ResultActions doPost(String path, String token, String json) throws Exception {
        return mvc.perform(auth(post(path), token).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions doPut(String path, String token, String json) throws Exception {
        return mvc.perform(auth(put(path), token).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions doDelete(String path, String token) throws Exception {
        return mvc.perform(auth(delete(path), token));
    }

    /** Creates a record and returns its id. */
    protected String create(String path, String token, String json) throws Exception {
        String body = doPost(path, token, json).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b, String token) {
        return token == null ? b : b.header("Authorization", "Bearer " + token);
    }
}
