package ir.crm.identity.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByUsernameIgnoreCase(String username);

    long countByRoleId(Long roleId);

    interface RoleCount { Long getRoleId(); long getTotal(); }

    interface TeamCount { Long getTeamId(); long getTotal(); }

    @Query("select u.roleId as roleId, count(u) as total from User u where u.roleId in :ids group by u.roleId")
    List<RoleCount> countByRoleIds(@Param("ids") Collection<Long> ids);

    @Query("select u.teamId as teamId, count(u) as total from User u where u.teamId in :ids group by u.teamId")
    List<TeamCount> countByTeamIds(@Param("ids") Collection<Long> ids);
}
