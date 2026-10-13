package ir.crm.identity.auth;

import jakarta.validation.constraints.NotBlank;

/** {@code username} accepts either the login name or the e-mail address. */
public record LoginRequest(@NotBlank String username, @NotBlank String password, Boolean remember) {}
