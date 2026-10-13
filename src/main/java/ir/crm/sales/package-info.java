/** Sales pipeline: leads to invoices. Other modules are referenced by id only (see ir.crm.shared.lookup). */
@ApplicationModule(displayName = "Sales", allowedDependencies = "shared")
package ir.crm.sales;

import org.springframework.modulith.ApplicationModule;
