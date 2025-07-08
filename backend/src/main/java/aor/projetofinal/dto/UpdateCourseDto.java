package aor.projetofinal.dto;

import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;

/**
 * Data Transfer Object (DTO) used to update an existing course.
 * Includes the course ID and all editable fields.
 */
public class UpdateCourseDto {

    /** Unique identifier of the course to update. */
    private int id;

    /** Name of the course. */
    private String name;

    /** Total duration of the course in hours. */
    private double timeSpan;

    /** Detailed description of the course content. */
    private String description;

    /** Link to the course resource or e-learning platform. */
    private String link;

    /** Language in which the course is provided. */
    private LanguageEnum language;

    /** Category (area) of the course. */
    private CourseCategoryEnum courseCategory;

    /** Indicates whether the course is active. */
    private boolean active;

    /** Default constructor. */
    public UpdateCourseDto() {}

        // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
