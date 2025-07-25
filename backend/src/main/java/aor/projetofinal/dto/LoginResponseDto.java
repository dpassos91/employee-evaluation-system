package aor.projetofinal.dto;

import java.util.List;


/**
 * DTO representing the response returned after a successful login.
 * Contains session token, user identity, role, profile completeness, and any missing profile fields.
 */
public class LoginResponseDto {
    private String sessionToken;
    private int id;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private boolean profileComplete;
    private List<String> missingFields;

    public LoginResponseDto() {}

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
}
