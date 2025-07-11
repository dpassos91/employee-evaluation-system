package aor.projetofinal.dto;

/**
 * DTO used to populate dropdown menus with eligible users (non-admin, active, confirmed).
 */
public class UsersDropdownMenuDto {
    private int id;
    private String email;
    private String firstName;
    private String lastName;

    public UsersDropdownMenuDto() {}

    public UsersDropdownMenuDto(int id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
