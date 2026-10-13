package ir.crm.catalog.product;

import ir.crm.catalog.product.dto.ProductCreateRequest;
import ir.crm.catalog.product.dto.ProductResponse;
import ir.crm.catalog.product.dto.ProductUpdateRequest;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.web.GlobalExceptionHandler;
import ir.crm.shared.web.LookupResponse;
import ir.crm.support.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({ProductControllerTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("Product Controller Tests")
class ProductControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/products";
    private static final long NON_EXISTENT_ID = 999999L;

    @MockitoBean
    private ProductService productService;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    private ProductResponse sampleResponse(Long id, String name, BigDecimal price) {
        return new ProductResponse(
                String.valueOf(id),
                name,
                "SKU-" + id,
                ProductCategory.HARDWARE,
                new LookupResponse("1", "Vendor 1"),
                price,
                10,
                true,
                "Test description",
                Instant.now(),
                Instant.now()
        );
    }

    private ProductCreateRequest createRequest(String name) {
        return new ProductCreateRequest(
                name,
                "SKU-NEW",
                ProductCategory.HARDWARE,
                1L,
                BigDecimal.valueOf(199.99),
                15,
                true,
                "New Product Description"
        );
    }

    // ============================== GET /api/v1/products ==============================

    @Nested
    @DisplayName("GET " + BASE_URL)
    class GetAllProducts {

        @Test
        @WithMockUser(authorities = "products:view")
        @DisplayName("Should return a page of products")
        void shouldReturnProducts() throws Exception {
            List<ProductResponse> items = List.of(
                    sampleResponse(1L, "Product 1", BigDecimal.valueOf(100)),
                    sampleResponse(2L, "Product 2", BigDecimal.valueOf(200)),
                    sampleResponse(3L, "Product 3", BigDecimal.valueOf(300)),
                    sampleResponse(4L, "Product 4", BigDecimal.valueOf(400))
            );
            Page<ProductResponse> page = new PageImpl<>(items, PageRequest.of(0, 10), 4);
            Mockito.when(productService.list(any(), any(Pageable.class))).thenReturn(page);

            mockMvc().perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(4)));
        }

        @Test
        @WithMockUser(authorities = "products:view")
        @DisplayName("Should filter by name")
        void shouldFilterByName() throws Exception {
            ProductResponse matched = sampleResponse(1L, "Apple iPhone 16 Pro", BigDecimal.valueOf(999));
            Page<ProductResponse> page = new PageImpl<>(List.of(matched), PageRequest.of(0, 10), 1);
            Mockito.when(productService.list(any(), any(Pageable.class))).thenReturn(page);

            mockMvc().perform(get(BASE_URL).param("search", "iphone"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Apple iPhone 16 Pro"));
        }

        @Test
        @WithMockUser(authorities = "products:view")
        @DisplayName("Should return an empty page when nothing matches")
        void shouldReturnEmptyPage() throws Exception {
            Page<ProductResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            Mockito.when(productService.list(any(), any(Pageable.class))).thenReturn(emptyPage);

            mockMvc().perform(get(BASE_URL).param("search", "non-existent-term"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 when user lacks authority")
        void shouldReturnForbidden() throws Exception {
            mockMvc().perform(get(BASE_URL)).andExpect(status().isForbidden());
        }
    }

    // ============================== GET /api/v1/products/{id} ==============================

    @Nested
    @DisplayName("GET " + BASE_URL + "/{id}")
    class GetProductById {

        @Test
        @WithMockUser(authorities = "products:view")
        @DisplayName("Should return the product when exists")
        void shouldReturnProduct() throws Exception {
            long id = 1L;
            ProductResponse response = sampleResponse(id, "Test product", BigDecimal.valueOf(150));
            Mockito.when(productService.get(id)).thenReturn(response);

            mockMvc().perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(String.valueOf(id)))
                    .andExpect(jsonPath("$.name").value("Test product"))
                    .andExpect(jsonPath("$.price").value(150));
        }

        @Test
        @WithMockUser(authorities = "products:view")
        @DisplayName("Should return 404 when product does not exist")
        void shouldReturnNotFound() throws Exception {
            Mockito.when(productService.get(NON_EXISTENT_ID))
                    .thenThrow(new ResourceNotFoundException("product", NON_EXISTENT_ID));

            mockMvc().perform(get(BASE_URL + "/{id}", NON_EXISTENT_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 when user lacks authority")
        void shouldReturnForbidden() throws Exception {
            mockMvc().perform(get(BASE_URL + "/{id}", 1L)).andExpect(status().isForbidden());
        }
    }

    // ============================== POST /api/v1/products ==============================

    @Nested
    @DisplayName("POST " + BASE_URL)
    class CreateProduct {

        @Test
        @WithMockUser(authorities = "products:create")
        @DisplayName("Should create a product and return 201 with location header")
        void shouldCreateProduct() throws Exception {
            ProductCreateRequest req = createRequest("New Test Product");
            ProductResponse res = sampleResponse(42L, "New Test Product", BigDecimal.valueOf(199.99));
            Mockito.when(productService.create(any(ProductCreateRequest.class))).thenReturn(res);

            mockMvc().perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper().writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", startsWith(BASE_URL + "/42")))
                    .andExpect(jsonPath("$.id").value("42"))
                    .andExpect(jsonPath("$.name").value("New Test Product"));
        }

        @Test
        @WithMockUser(authorities = "products:create")
        @DisplayName("Should return 400 when validation fails")
        void shouldReturnBadRequestWhenValidationFails() throws Exception {
            // Price is null and name is blank
            ProductCreateRequest invalid = new ProductCreateRequest("", null, null, null, null, null, null, null);

            mockMvc().perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper().writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 when user lacks authority")
        void shouldReturnForbidden() throws Exception {
            ProductCreateRequest req = createRequest("Product Without Perms");
            mockMvc().perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper().writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    // ============================== PUT /api/v1/products/{id} ==============================

    @Nested
    @DisplayName("PUT " + BASE_URL + "/{id}")
    class UpdateProduct {

        @Test
        @WithMockUser(authorities = "products:edit")
        @DisplayName("Should update product and return 200")
        void shouldUpdateProduct() throws Exception {
            long id = 1L;
            ProductUpdateRequest req = new ProductUpdateRequest("Updated Name", "SKU-UPD", ProductCategory.HARDWARE, 1L, BigDecimal.valueOf(250), 20, true, "Updated");
            ProductResponse res = sampleResponse(id, "Updated Name", BigDecimal.valueOf(250));
            Mockito.when(productService.update(eq(id), any(ProductUpdateRequest.class))).thenReturn(res);

            mockMvc().perform(put(BASE_URL + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper().writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"))
                    .andExpect(jsonPath("$.price").value(250));
        }

        @Test
        @WithMockUser(authorities = "products:edit")
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturnNotFoundWhenUpdatingNonExistent() throws Exception {
            ProductUpdateRequest req = new ProductUpdateRequest("Updated Name", "SKU-UPD", ProductCategory.HARDWARE, 1L, BigDecimal.valueOf(250), 20, true, "Updated");
            Mockito.when(productService.update(eq(NON_EXISTENT_ID), any(ProductUpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("product", NON_EXISTENT_ID));

            mockMvc().perform(put(BASE_URL + "/{id}", NON_EXISTENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper().writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ============================== DELETE /api/v1/products/{id} ==============================

    @Nested
    @DisplayName("DELETE " + BASE_URL + "/{id}")
    class DeleteProduct {

        @Test
        @WithMockUser(authorities = "products:delete")
        @DisplayName("Should delete product and return 204 No Content")
        void shouldDeleteProduct() throws Exception {
            mockMvc().perform(delete(BASE_URL + "/{id}", 1L))
                    .andExpect(status().isNoContent());

            Mockito.verify(productService).delete(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 when user lacks authority")
        void shouldReturnForbidden() throws Exception {
            mockMvc().perform(delete(BASE_URL + "/{id}", 1L))
                    .andExpect(status().isForbidden());
        }
    }

    // ============================== GET /api/v1/products/lookup ==============================

    @Nested
    @DisplayName("GET " + BASE_URL + "/lookup")
    class LookupProducts {

        @Test
        @WithMockUser(authorities = "products:view")
        @DisplayName("Should return lookup list")
        void shouldReturnLookupList() throws Exception {
            List<LookupResponse> lookups = List.of(
                    new LookupResponse("1", "Product 1"),
                    new LookupResponse("2", "Product 2")
            );
            Mockito.when(productService.lookup(any(), eq(20))).thenReturn(lookups);

            mockMvc().perform(get(BASE_URL + "/lookup"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value("1"))
                    .andExpect(jsonPath("$[0].label").value("Product 1"));
        }
    }
}
