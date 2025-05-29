package aor.projetofinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RolePermissionIdEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "permission_id")
    private Long permissionId;

    // Construtor vazio
    public RolePermissionIdEntity() {}

    public RolePermissionIdEntity(Long roleId, Long permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    // Getters e Setters
    public Long getRoleId() {
        return roleId;
    }
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }
    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionIdEntity)) return false;
        RolePermissionIdEntity that = (RolePermissionIdEntity) o;
        return Objects.equals(roleId, that.roleId) &&
                Objects.equals(permissionId, that.permissionId);
    }

    // hash
    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }

    // toString
    @Override
    public String toString() {
        return "RolePermissionIdEntity{" +
                "roleId=" + roleId +
                ", permissionId=" + permissionId +
                '}';
    }
}

