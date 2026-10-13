package ir.crm.sales.lead.dto;

import ir.crm.shared.web.LookupResponse;

public record LeadConversionResponse(LookupResponse organization, LookupResponse contact, LookupResponse opportunity) {}
