package ir.crm.identity.team;

import ir.crm.identity.team.dto.TeamCreateRequest;
import ir.crm.identity.team.dto.TeamResponse;
import ir.crm.identity.team.dto.TeamUpdateRequest;
import ir.crm.identity.user.User;
import ir.crm.shared.web.LookupResponse;
import java.util.List;
import java.util.Map;

public final class TeamMapper {

    private TeamMapper() {}

    public static Team toEntity(TeamCreateRequest r, User leader) {
        return Team.builder()
                .name(r.name() != null ? r.name().trim() : null)
                .leader(leader)
                .monthlyTarget(r.monthlyTarget())
                .description(r.description() != null && !r.description().isBlank() ? r.description().trim() : null)
                .build();
    }

    public static void updateEntity(Team e, TeamUpdateRequest r, User leader) {
        e.setName(r.name() != null ? r.name().trim() : null);
        e.setLeader(leader);
        e.setMonthlyTarget(r.monthlyTarget());
        e.setDescription(r.description() != null && !r.description().isBlank() ? r.description().trim() : null);
    }

    public static TeamResponse toResponse(Team team, long memberCount) {
        LookupResponse leaderRef = team.getLeader() != null
                ? new LookupResponse(String.valueOf(team.getLeader().getId()), team.getLeader().getName())
                : null;

        return new TeamResponse(
                String.valueOf(team.getId()),
                team.getName(),
                leaderRef,
                memberCount,
                team.getMonthlyTarget(),
                team.getDescription(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    public static TeamResponse toResponse(Team team) {
        return toResponse(team, 0L);
    }

    public static List<TeamResponse> toResponses(List<Team> teams, Map<Long, Long> counts) {
        if (teams == null) return List.of();
        return teams.stream().map(e -> toResponse(e, counts.getOrDefault(e.getId(), 0L))).toList();
    }
}
