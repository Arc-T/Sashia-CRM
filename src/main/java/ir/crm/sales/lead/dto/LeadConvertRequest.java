package ir.crm.sales.lead.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Lead conversion options. When {@code createOpportunity} is true an opportunity is opened for the new
 * organization; its name defaults to the organization name and its amount to the lead's expected revenue.
 */
public record LeadConvertRequest(
        boolean createOpportunity,
        @Size(max = 255) String opportunityName,
        @PositiveOrZero @Digits(integer = 15, fraction = 2) BigDecimal opportunityAmount) {}
