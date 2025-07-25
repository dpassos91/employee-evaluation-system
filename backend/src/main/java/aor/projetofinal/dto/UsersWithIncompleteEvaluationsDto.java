package aor.projetofinal.dto;

import java.util.List;


/**
 * DTO representing users who have not yet completed their evaluations.
 * Includes the total number of such users and the list itself.
 */
public class UsersWithIncompleteEvaluationsDto {
    private int totalUsersWithIncompleteEvaluations;
    private List<UserDto> users;

    public int getTotalUsersWithIncompleteEvaluations() {
        return totalUsersWithIncompleteEvaluations;
    }

    public void setTotalUsersWithIncompleteEvaluations(int totalUsersWithIncompleteEvaluations) {
        this.totalUsersWithIncompleteEvaluations = totalUsersWithIncompleteEvaluations;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
}
