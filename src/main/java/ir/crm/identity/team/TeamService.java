package ir.crm.identity.team;

import ir.crm.identity.team.dto.TeamCreateRequest;
import ir.crm.identity.team.dto.TeamResponse;
import ir.crm.identity.team.dto.TeamUpdateRequest;
import ir.crm.identity.user.User;
import ir.crm.identity.user.UserRepository;
import ir.crm.shared.event.RecordChangedEvent;
import ir.crm.shared.event.RecordChangedEvent.ChangeType;
import ir.crm.shared.exception.InvalidReferenceException;
import ir.crm.shared.exception.ResourceNotFoundException;
import ir.crm.shared.security.CurrentUser;
import ir.crm.shared.web.LookupResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository repository;
    private final UserRepository users;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<TeamResponse> list(Map<String, String> params, Pageable pageable) {
        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : TeamSpecification.pageable(params);
        Specification<Team> spec = TeamSpecification.build(params);
        Page<Team> page = repository.findAll(spec, effectivePageable);

        Set<Long> ids = page.getContent().stream().map(Team::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Long> counts = new HashMap<>();
        if (!ids.isEmpty()) {
            users.countByTeamIds(ids).forEach(c -> counts.put(c.getTeamId(), c.getTotal()));
        }

        List<TeamResponse> responses = TeamMapper.toResponses(page.getContent(), counts);
        return new PageImpl<>(responses, effectivePageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TeamResponse get(Long id) {
        Team team = find(id);
        long count = users.countByTeamIds(List.of(id)).stream().mapToLong(UserRepository.TeamCount::getTotal).sum();
        return TeamMapper.toResponse(team, count);
    }

    public TeamResponse create(TeamCreateRequest request) {
        User leader = request.leader() != null
                ? users.findById(request.leader()).orElseThrow(() -> new InvalidReferenceException("team.leader", request.leader()))
                : null;
        Team team = TeamMapper.toEntity(request, leader);
        Team saved = repository.save(team);
        publishEvent(saved.getId(), ChangeType.CREATED, saved.getName());
        return TeamMapper.toResponse(saved, 0L);
    }

    public TeamResponse update(Long id, TeamUpdateRequest request) {
        Team team = find(id);
        User leader = request.leader() != null
                ? users.findById(request.leader()).orElseThrow(() -> new InvalidReferenceException("team.leader", request.leader()))
                : null;
        TeamMapper.updateEntity(team, request, leader);
        Team saved = repository.save(team);
        publishEvent(saved.getId(), ChangeType.UPDATED, saved.getName());
        long count = users.countByTeamIds(List.of(id)).stream().mapToLong(UserRepository.TeamCount::getTotal).sum();
        return TeamMapper.toResponse(saved, count);
    }

    public void delete(Long id) {
        Team team = find(id);
        repository.delete(team);
        repository.flush();
        publishEvent(id, ChangeType.DELETED, team.getName());
    }

    public void deleteAll(Collection<Long> ids) {
        ids.stream().distinct().forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public List<LookupResponse> lookup(String search, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Pageable pageable = PageRequest.of(0, size, Sort.by("name"));
        Specification<Team> spec = TeamSpecification.search(search);
        return repository.findAll(spec, pageable)
                .map(t -> new LookupResponse(String.valueOf(t.getId()), t.getName()))
                .getContent();
    }

    private Team find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("team", id));
    }

    private void publishEvent(Long id, ChangeType type, String label) {
        eventPublisher.publishEvent(new RecordChangedEvent("teams", id, type, label,
                CurrentUser.username().orElse("system"), Instant.now()));
    }
}
