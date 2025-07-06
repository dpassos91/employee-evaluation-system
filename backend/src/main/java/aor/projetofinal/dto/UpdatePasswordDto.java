package aor.projetofinal.dto;

/**
 * DTO used for changing the user's password (when user is authenticated).
 * Includes both current and new passwords.
 */
public class UpdatePasswordDto {
    private String currentPassword; // Current password (to be validated)
    private String newPassword;     // New password to set

    public String getCurrentPassword() {
        return currentPassword;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
