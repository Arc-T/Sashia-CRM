/**
 * Read-only dashboards, analytics and reports.
 * Design note: this module reads the other modules' tables through plain SQL (JdbcClient) instead of
 * depending on their entities, so it stays decoupled in code while accepting coupling to the schema.
 */
@ApplicationModule(displayName = "Reporting", allowedDependencies = "shared")
package ir.crm.reporting;

import org.springframework.modulith.ApplicationModule;
