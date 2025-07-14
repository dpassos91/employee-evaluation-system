package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;


/**
 * Entity representing the many-to-many association between roles and permissions.
 * Uses a composite key defined in {@link RolePermissionIdEntity}.
 */
@Entity
@Table(name = "role_permission")
public class RolePermissionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private RolePermissionIdEntity id = new RolePermissionIdEntity();

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private PermissionEntity permission;

    // Construtor vazio
    public RolePermissionEntity() {}

    public RolePermissionEntity(RoleEntity role, PermissionEntity permission) {
        this.role = role;
        this.permission = permission;
        this.id = new RolePermissionIdEntity(role.getId(), permission.getId());
    }

    // Getters e Setters
    public RolePermissionIdEntity getId() {
        return id;
    }
    public void setId(RolePermissionIdEntity id) {
        this.id = id;
    }

    public RoleEntity getRole() {
        return role;
    }
    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public PermissionEntity getPermission() {
        return permission;
    }
    public void setPermission(PermissionEntity permission) {
        this.permission = permission;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionEntity)) return false;
        RolePermissionEntity that = (RolePermissionEntity) o;
        return Objects.equals(id, that.id);
    }

    // hash
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "RolePermissionEntity{" +
                "role=" + (role != null ? role.getName() : null) +
                ", permission=" + (permission != null ? permission.getAction() : null) +
                '}';
    }
}

