/**
 * Shared kernel: base entity, error model, CRUD infrastructure and cross-module SPI.
 * Declared OPEN so every application module may use all of its sub-packages.
 */
@ApplicationModule(displayName = "Shared kernel", type = ApplicationModule.Type.OPEN)
package ir.crm.shared;

import org.springframework.modulith.ApplicationModule;
