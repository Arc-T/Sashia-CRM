package ir.crm.identity.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Bound from {@code app.security.*}. A missing/short JWT secret fails the startup with a clear message. */
@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(@Valid @NotNull Jwt jwt, @Valid @NotNull Cors cors) {

    public record Jwt(@NotBlank @Size(min = 32, message = "JWT_SECRET must be at least 32 characters") String secret,
                      @NotNull Duration expiration) {}

    public record Cors(@NotNull List<String> allowedOrigins) {}
}
