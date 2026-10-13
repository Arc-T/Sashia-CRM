package ir.crm.identity.auth;

import ir.crm.identity.role.RoleKind;
import ir.crm.identity.role.RoleRepository;
import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.identity.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** First-run convenience: creates user "admin" when there are no users and app.bootstrap.admin-password is set. */
@Component
class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    BootstrapAdminInitializer(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                              @Value("${app.bootstrap.admin-password:}") String adminPassword) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminPassword.isBlank() || users.count() > 0) return;
        User admin = new User();
        admin.setUsername("admin");
        admin.setName("مدیر سیستم");
        admin.setEmail("admin@example.ir");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(roles.findFirstByKind(RoleKind.ADMIN).orElseThrow());
        admin.setStatus(UserStatus.ACTIVE);
        users.save(admin);
        log.info("Bootstrap administrator 'admin' created. Change its password after first login.");
    }
}
