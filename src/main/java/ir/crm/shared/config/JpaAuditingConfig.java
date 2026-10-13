package ir.crm.shared.config;

import ir.crm.shared.security.CurrentUser;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class JpaAuditingConfig {

    @Bean
    AuditorAware<String> auditorProvider() {
        return () -> Optional.of(CurrentUser.username().orElse("system"));
    }
}
