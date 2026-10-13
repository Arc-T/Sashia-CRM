package ir.crm.identity.auth;

import ir.crm.identity.role.Role;
import ir.crm.identity.role.RolePermissionService;
import ir.crm.identity.role.RoleRepository;
import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.identity.user.UserStatus;
import ir.crm.shared.exception.BusinessException;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.ErrorCode;
import ir.crm.shared.security.CurrentUser;
import java.time.Instant;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AuthService {

    private static final String[] AVATAR_COLORS = {"#4f46e5", "#0891b2", "#059669", "#d97706", "#dc2626", "#7c3aed", "#db2777"};

    private final UserRepository users;
    private final RoleRepository roles;
    private final RolePermissionService permissions;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    /** Compared against when the user does not exist so response time doesn't reveal valid usernames. */
    private final String dummyHash;

    AuthService(UserRepository users, RoleRepository roles, RolePermissionService permissions,
                PasswordEncoder passwordEncoder, JwtService jwt) {
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @Transactional
    LoginResponse login(LoginRequest request) {
        String login = request.username().trim();
        User user = users.findByUsernameIgnoreCase(login).or(() -> users.findByEmailIgnoreCase(login)).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);

        Role role = roleOf(user);
        user.setLastLoginAt(Instant.now());
        List<String> authorities = permissions.authoritiesOf(role);
        return new LoginResponse(jwt.issue(user, role, authorities), toResponse(user, role));
    }

    @Transactional(readOnly = true)
    AuthUserResponse me() {
        User user = currentUser();
        return toResponse(user, roleOf(user));
    }

    @Transactional
    void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("rule.password.wrongCurrent");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private User currentUser() {
        String username = CurrentUser.username().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return users.findByUsernameIgnoreCase(username).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private Role roleOf(User user) {
        if (user.getRole() != null) {
            return user.getRole();
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private AuthUserResponse toResponse(User user, Role role) {
        String color = AVATAR_COLORS[Math.floorMod(user.getUsername().hashCode(), AVATAR_COLORS.length)];
        return new AuthUserResponse(String.valueOf(user.getId()), user.getName(), user.getEmail(), role.getKind().key(), color);
    }
}
