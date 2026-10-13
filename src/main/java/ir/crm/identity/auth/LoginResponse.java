package ir.crm.identity.auth;

public record LoginResponse(String token, AuthUserResponse user) {}
