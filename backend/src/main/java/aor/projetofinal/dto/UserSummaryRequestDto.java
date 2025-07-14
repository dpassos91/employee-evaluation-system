package aor.projetofinal.dto;




/**
 * DTO representing the userID as an int for user summary requests, sent from the frontend
 */
public class UserSummaryRequestDto {
    private int userId;

    public UserSummaryRequestDto() {}

    public UserSummaryRequestDto(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
