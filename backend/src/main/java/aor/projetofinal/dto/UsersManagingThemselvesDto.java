package aor.projetofinal.dto;


import java.util.List;

/**
 * DTO representing a collection of users who are assigned as their own managers.
 * Includes a count of such users and the corresponding list.
 */
public class UsersManagingThemselvesDto {
    private int numberOfUsers;
    private List<UserDto> users;

    public UsersManagingThemselvesDto() {
    }

    public UsersManagingThemselvesDto(int numberOfUsers, List<UserDto> users) {
        this.numberOfUsers = numberOfUsers;
        this.users = users;
    }


    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
}
