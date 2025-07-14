package aor.projetofinal.dto;

/**
 * DTO for resetting a user's password.
 * Contains only the new password to be set.
 */
public class ResetPasswordDto {
     private String password;
     
    // getters e setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
