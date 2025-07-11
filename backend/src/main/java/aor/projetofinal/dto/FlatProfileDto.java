package aor.projetofinal.dto;

import java.io.Serializable;

/**
 * FlatProfileDto is a lightweight Data Transfer Object for listing user profiles in REST API responses.
 * It exposes only simple fields (no entity references).
 */
public class FlatProfileDto implements Serializable {
    /**
     * The unique ID of the user.
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
     * The user's email address.
     */
    private String email;

    /**
     * The user's usual workplace (as String).
     */
    private String usualWorkplace;

    /**
     * The full name of the user's manager (may be empty).
     */
    private String managerName;

    /**
     * The unique ID of the user's manager (may be null).
     */
    private Long managerId;

    /**
     * The URL of the user's profile photo (avatar).
     */
    private String photograph;

    /**
     * The user's role (e.g., "USER", "MANAGER", "ADMIN").
     */
    private String role;

    // Default no-args constructor
    public FlatProfileDto() {}

    /**
     * Full constructor for FlatProfileDto.
     * 
     * @param userId        The unique user ID.
     * @param firstName     User's first name.
     * @param lastName      User's last name.
     * @param email         User's email address.
     * @param usualWorkplace User's usual workplace.
     * @param managerName   Full name of the user's manager.
     * @param managerId     Unique ID of the user's manager.
     * @param photograph    URL of the user's profile photo.
     * @param role          The user's role ("USER", "MANAGER", "ADMIN").
     */
    public FlatProfileDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String usualWorkplace,
        String managerName,
        Long managerId,
        String photograph,
        String role
    ) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.usualWorkplace = usualWorkplace;
        this.managerName = managerName;
        this.managerId = managerId;
        this.photograph = photograph;
        this.role = role;
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsualWorkplace() { return usualWorkplace; }
    public void setUsualWorkplace(String usualWorkplace) { this.usualWorkplace = usualWorkplace; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getPhotograph() { return photograph; }
    public void setPhotograph(String photograph) { this.photograph = photograph; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

