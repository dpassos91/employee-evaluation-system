package aor.projetofinal.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) used to associate a course with a user.
 * Represents the registration of a user's participation in a specific course on a given date.
 */
public class CreateUserCourseDto {

    /** Identifier of the user who attended the course. */
    private int userId;

    /** Identifier of the course attended. */
    private int courseId;

    /** Date and time when the user participated in the course. */
    private LocalDateTime participationDate;

    /** Default constructor. */
    public CreateUserCourseDto() {}

    // Getters and setters

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getParticipationDate() {
        return participationDate;
    }

    public void setParticipationDate(LocalDateTime participationDate) {
        this.participationDate = participationDate;
    }
}
