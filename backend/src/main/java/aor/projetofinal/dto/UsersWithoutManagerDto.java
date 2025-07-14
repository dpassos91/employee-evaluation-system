package aor.projetofinal.dto;

import java.util.List;

/**
 * DTO representing users who do not have a manager assigned.
 * Includes the count and the list of affected users.
 */
public class UsersWithoutManagerDto {
    private int numberOfUsersWithoutManager;
    private List<UserDto> usersWithoutManager;

    public UsersWithoutManagerDto() {}

    //constructor with parameters
    public UsersWithoutManagerDto(int numberOfUsersWithoutManager, List<UserDto> usersWithoutManager) {
        this.numberOfUsersWithoutManager = numberOfUsersWithoutManager;
        this.usersWithoutManager = usersWithoutManager;
    }

    // Getters and Setters
    public int getNumberOfUsersWithoutManager() {
        return numberOfUsersWithoutManager;
    }

    public void setNumberOfUsersWithoutManager(int numberOfUsersWithoutManager) {
        this.numberOfUsersWithoutManager = numberOfUsersWithoutManager;
    }

    public List<UserDto> getUsersWithoutManager() {
        return usersWithoutManager;
    }

    public void setUsersWithoutManager(List<UserDto> usersWithoutManager) {
        this.usersWithoutManager = usersWithoutManager;
    }



}
