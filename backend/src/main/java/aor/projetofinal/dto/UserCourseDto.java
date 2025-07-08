package aor.projetofinal.dto;

import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a user's participation in a specific course.
 * Used for listing the user's training history with detailed course information.
 */
public class UserCourseDto {

    /** Identifier of the user who participated in the course. */
    private int userId;

    /** Identifier of the course attended by the user. */
    private int courseId;

    /** Name of the course attended. */
    private String courseName;

    /** Total duration of the course in hours. */
    private double timeSpan;

    /** Language in which the course was provided. */
    private LanguageEnum language;

    /** Category (area) of the course. */
    private CourseCategoryEnum courseCategory;

    /** Date and time when the user participated in the course. */
    private LocalDateTime participationDate;

    /** Default constructor. */
    public UserCourseDto() {}

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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(double timeSpan) {
        this.timeSpan = timeSpan;
    }

    public LanguageEnum getLanguage() {
        return language;
    }

    public void setLanguage(LanguageEnum language) {
        this.language = language;
    }

    public CourseCategoryEnum getCourseCategory() {
        return courseCategory;
    }

    public void setCourseCategory(CourseCategoryEnum courseCategory) {
        this.courseCategory = courseCategory;
    }

    public LocalDateTime getParticipationDate() {
        return participationDate;
    }

    public void setParticipationDate(LocalDateTime participationDate) {
        this.participationDate = participationDate;
    }
}
