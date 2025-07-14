package aor.projetofinal.dto;

import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;

/**
 * DTO representing a user who does not have a manager assigned.
 * Used for listing or assigning managers.
 */
public class NoManagerDto {
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private UsualWorkPlaceEnum usualWorkPlace;

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public UsualWorkPlaceEnum getUsualWorkPlace() {
        return usualWorkPlace;
    }

    public void setUsualWorkPlace(UsualWorkPlaceEnum usualWorkPlace) {
        this.usualWorkPlace = usualWorkPlace;
    }
}
