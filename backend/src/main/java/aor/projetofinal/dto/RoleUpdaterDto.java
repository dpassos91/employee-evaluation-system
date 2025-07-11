package aor.projetofinal.dto;

/**
 * DTO for updating a user's role and manager.
 */
public class RoleUpdaterDto {
    private String role;      
    private Integer managerId; 

    public RoleUpdaterDto() {}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }
}