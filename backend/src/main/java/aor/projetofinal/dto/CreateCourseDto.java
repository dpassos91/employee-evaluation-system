package aor.projetofinal.dto;

import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;

/**
 * Data Transfer Object (DTO) used for creating a new training course.
 * All fields are required except the active flag, which is set to true by default.
 */
public class CreateCourseDto {

    /** Name of the course (required). */
    private String name;

    /** Total duration of the course in hours (required). */
    private double timeSpan;

    /** Detailed description of the course content (required). */
    private String description;

    /** Link to the course resource or e-learning platform (required). */
    private String link;

    /** Language in which the course is provided (required). */
    private LanguageEnum language;

    /** Category (area) of the course (required). */
    private CourseCategoryEnum courseCategory;

    /** Indicates whether the course is active (optional, defaults to true). */
    private boolean active = true;

    /** Default constructor. */
    public CreateCourseDto() {}

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(double timeSpan) {
        this.timeSpan = timeSpan;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

