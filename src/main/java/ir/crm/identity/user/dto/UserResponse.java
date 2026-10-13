package ir.crm.identity.user.dto;

import ir.crm.identity.user.UserStatus;
import ir.crm.shared.web.LookupResponse;
import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        String username,
        String email,
        String phone,
        LookupResponse role,
        LookupResponse team,
        UserStatus status,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt) {}
