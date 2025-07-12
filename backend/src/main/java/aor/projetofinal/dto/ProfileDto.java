package aor.projetofinal.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * ProfileDto is a comprehensive Data Transfer Object for detailed user profile views and edits.
 * Designed for profile pages (self or admin access), it exposes all relevant user and management fields,
 * including editable personal data and assignment context.
 */
public class ProfileDto implements Serializable {

    /**
     * The unique identifier of the user.
     */
    private Long userId;

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    /**
     * The user's date of birth (ISO format yyyy-MM-dd).
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * The user's address.
     */
    private String address;

    /**
     * The user's phone number.
     */
    private String phone;

    /**
     * The user's profile photograph (URL or filename).
     */
    private String photograph;

    /**
     * The user's short biography or description.
     */
    private String bio;

    /**
     * The user's usual workplace (as String, e.g. "PORTO").
     */
    private String usualWorkplace;

    /**
     * Indicates if the profile is considered complete (all mandatory fields filled).
     */
    private boolean profileComplete;

    /**
     * List of missing field names required to complete the profile (for UI validation).
     */
    private List<String> missingFields;

    /**
     * The user's email address (unique identifier for login/contact).
     */
    private String email;

    /**
     * The user's current role (e.g., "USER", "MANAGER", "ADMIN").
     */
    private String role;

    /**
     * The unique ID of the user's manager (may be null).
     */
    private Long managerId;

    /**
     * The full name of the user's manager (for display).
     */
    private String managerName;

    // Default no-args constructor
    public ProfileDto() {}

    // Getters and setters

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhotograph() { return photograph; }
    public void setPhotograph(String photograph) { this.photograph = photograph; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getUsualWorkplace() { return usualWorkplace; }
    public void setUsualWorkplace(String usualWorkplace) { this.usualWorkplace = usualWorkplace; }

    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
}
