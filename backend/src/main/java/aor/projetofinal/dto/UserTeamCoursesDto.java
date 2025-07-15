package aor.projetofinal.dto;

import java.util.List;

/**
 * DTO that aggregates a flat user profile and their list of attended courses.
 * Used for manager's team listing (lightweight for dashboards/tables).
 */
public class UserTeamCoursesDto {
    /**
     * Flat user profile for list display.
     */
    private FlatProfileDto user;

    /**
     * List of attended courses.
     */
    private List<UserCourseDto> courses;

    public UserTeamCoursesDto() {}

    public UserTeamCoursesDto(FlatProfileDto user, List<UserCourseDto> courses) {
        this.user = user;
        this.courses = courses;
    }

    public FlatProfileDto getUser() { return user; }
    public void setUser(FlatProfileDto user) { this.user = user; }
    public List<UserCourseDto> getCourses() { return courses; }
    public void setCourses(List<UserCourseDto> courses) { this.courses = courses; }
}
