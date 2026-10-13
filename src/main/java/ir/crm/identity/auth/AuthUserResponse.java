package ir.crm.identity.auth;

public record AuthUserResponse(String id, String name, String email, String role, String avatarColor) {}
