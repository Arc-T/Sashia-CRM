package ir.crm.identity.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordResetRequest(
        @NotBlank @Size(min = 8, max = 100, message = "{validation.password.size}") String newPassword) {}
