package ir.crm.identity.user.dto;

import ir.crm.identity.user.UserStatus;
import ir.crm.shared.web.Patterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** {@code password} is optional: without it the account has no usable password until an admin sets one. */
public record UserCreateRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Pattern(regexp = Patterns.PHONE, message = "{validation.phone}") String phone,
        @NotNull Long role,
        Long team,
        @NotNull UserStatus status,
        @Size(min = 8, max = 100, message = "{validation.password.size}") String password) {}
