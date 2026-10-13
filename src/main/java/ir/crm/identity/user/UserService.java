package ir.crm.identity.user;

import ir.crm.identity.role.Role;
import ir.crm.identity.role.RoleRepository;
import ir.crm.identity.team.Team;
import ir.crm.identity.team.TeamRepository;
import ir.crm.identity.user.dto.UserCreateRequest;
import ir.crm.identity.user.dto.UserResponse;
import ir.crm.identity.user.dto.UserUpdateRequest;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.BusinessRuleException;
import ir.crm.shared.exception.DuplicateResourceException;
import ir.crm.shared.exception.InvalidReferenceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.security.CurrentUser;
import ir.crm.shared.web.LookupResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : UserSpecification.pageable(params);
        Specification<User> spec = UserSpecification.build(params);
        return repository.findAll(spec, effectivePageable).map(UserMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserMapper.toResponse(find(id));
    }

    public UserResponse create(UserCreateRequest request) {
        validateEmailUniqueness(request.email().trim(), null);
        Role role = roleRepository.findById(request.role())
                .orElseThrow(() -> new InvalidReferenceException("user.role", request.role()));
        Team team = request.team() != null
                ? teamRepository.findById(request.team()).orElseThrow(() -> new InvalidReferenceException("user.team", request.team()))
                : null;

        User user = UserMapper.toEntity(request, role, team);
        user.setUsername(uniqueUsername(user.getEmail()));
        String raw = request.password() != null ? request.password() : UUID.randomUUID().toString();
        user.updatePassword(passwordEncoder.encode(raw));
        User saved = repository.save(user);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return UserMapper.toResponse(saved);
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = find(id);
        validateEmailUniqueness(request.email().trim(), id);
        if (request.status() == UserStatus.INACTIVE && isCurrentUser(user)) {
            throw new BusinessRuleException("rule.user.cannotDeactivateSelf");
        }
        Role role = roleRepository.findById(request.role())
                .orElseThrow(() -> new InvalidReferenceException("user.role", request.role()));
        Team team = request.team() != null
                ? teamRepository.findById(request.team()).orElseThrow(() -> new InvalidReferenceException("user.team", request.team()))
                : null;

        UserMapper.updateEntity(user, request, role, team);
        User saved = repository.save(user);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        return UserMapper.toResponse(saved);
    }

    public void delete(Long id) {
        User user = find(id);
        if (isCurrentUser(user)) throw new BusinessRuleException("rule.user.cannotDeleteSelf");
        repository.delete(user);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, user.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Pageable pageable = PageRequest.of(0, size, Sort.by("name"));
        Specification<User> spec = UserSpecification.search(search);
        return repository.findAll(spec, pageable)
                .map(u -> new LookupResponse(String.valueOf(u.getId()), u.getName()))
                .getContent();
    }

    public void resetPassword(Long id, String newPassword) {
        User user = find(id);
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    private void validateEmailUniqueness(String email, Long id) {
        boolean exists = id == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (exists) {
            throw new DuplicateResourceException("user.email", email);
        }
    }

    private User find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("user", id));
    }

    private boolean isCurrentUser(User user) {
        return CurrentUser.username().map(n -> n.equalsIgnoreCase(user.getUsername())).orElse(false);
    }

    private String uniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length());
        String candidate = base;
        for (int i = 2; repository.existsByUsernameIgnoreCase(candidate); i++) candidate = base + i;
        return candidate;
    }

    private void publishEvent(Long id, ChangeType type, String label) {
        eventPublisher.publishEvent(new RecordChangedEvent("users", id, type, label,
                CurrentUser.username().orElse("system"), Instant.now()));
    }
}
