package ir.crm.identity.team.dto;

import ir.crm.shared.web.LookupResponse;
import java.math.BigDecimal;
import java.time.Instant;

public record TeamResponse(String id, String name, LookupResponse leader, long members, BigDecimal monthlyTarget,
                           String description, Instant createdAt, Instant updatedAt) {}
