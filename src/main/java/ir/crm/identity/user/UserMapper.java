package ir.crm.identity.user;

import ir.crm.identity.role.Role;
import ir.crm.identity.team.Team;
import ir.crm.identity.user.dto.UserCreateRequest;
import ir.crm.identity.user.dto.UserResponse;
import ir.crm.identity.user.dto.UserUpdateRequest;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class UserMapper {

    private UserMapper() {}

    public static User toEntity(UserCreateRequest r, Role role, Team team) {
        return User.builder()
                .name(r.name() != null ? r.name().trim() : null)
                .email(r.email() != null ? r.email().trim().toLowerCase() : null)
                .phone(r.phone() != null && !r.phone().isBlank() ? r.phone().trim() : null)
                .role(role)
                .team(team)
                .status(r.status())
                .build();
    }

    public static void updateEntity(User e, UserUpdateRequest r, Role role, Team team) {
        e.setName(r.name() != null ? r.name().trim() : null);
        e.setEmail(r.email() != null ? r.email().trim().toLowerCase() : null);
        e.setPhone(r.phone() != null && !r.phone().isBlank() ? r.phone().trim() : null);
        e.setRole(role);
        e.setTeam(team);
        e.setStatus(r.status());
    }

    public static UserResponse toResponse(User user) {
        LookupResponse roleRef = user.getRole() != null
                ? new LookupResponse(String.valueOf(user.getRole().getId()), user.getRole().getName())
                : null;
        LookupResponse teamRef = user.getTeam() != null
                ? new LookupResponse(String.valueOf(user.getTeam().getId()), user.getTeam().getName())
                : null;

        return new UserResponse(
                String.valueOf(user.getId()),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                roleRef,
                teamRef,
                user.getStatus(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static List<UserResponse> toResponses(List<User> users) {
        if (users == null) return List.of();
        return users.stream().map(UserMapper::toResponse).toList();
    }
}
