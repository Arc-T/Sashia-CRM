package ir.crm.identity.role;

import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 32)
    private RoleKind kind;

    @Column(name = "description", length = 2000)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RolePermission> permissions = new HashSet<>();

    // Domain helpers
    public boolean isAdmin() {
        return this.kind == RoleKind.ADMIN;
    }

    public boolean hasPermission(String permission) {
        if (isAdmin()) return true;
        return permissions != null && permissions.stream()
                .anyMatch(p -> p.getPermission().equalsIgnoreCase(permission));
    }

    public Set<String> getPermissionStrings() {
        if (permissions == null) return Set.of();
        return permissions.stream().map(RolePermission::getPermission).collect(Collectors.toSet());
    }

    public void addPermission(String permission) {
        if (this.permissions == null) this.permissions = new HashSet<>();
        this.permissions.add(new RolePermission(this.getId(), permission, this));
    }

    public void removePermission(String permission) {
        if (this.permissions != null) {
            this.permissions.removeIf(p -> p.getPermission().equalsIgnoreCase(permission));
        }
    }
}
