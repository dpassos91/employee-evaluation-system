package aor.projetofinal.dto;

/**
 * DTO used to assign a manager to a user.
 * Contains the email of the user and the email of the manager being assigned.
 */
public class AssignManagerDto {
    private String userEmail;
    private String managerEmail;

    public AssignManagerDto() {
        // Default constructor
    }

    public AssignManagerDto(String userEmail, String managerEmail) {
        this.userEmail = userEmail;
        this.managerEmail = managerEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }
}
