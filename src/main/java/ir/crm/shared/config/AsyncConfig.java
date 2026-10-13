package ir.crm.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Needed by @ApplicationModuleListener, which runs listeners asynchronously after the publisher commits. */
@Configuration
@EnableAsync
class AsyncConfig {}
