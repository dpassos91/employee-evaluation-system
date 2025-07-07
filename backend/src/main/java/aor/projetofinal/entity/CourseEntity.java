package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;

/**
 * Entity representing a training course available in the system.
 * Courses can be assigned to users and are managed exclusively by administrators.
 * <p>
 * The course includes metadata such as language and category (stored as enums).
 * Each course can be assigned to multiple users (see UserCourseEntity).
 */
@Entity
@Table(name = "courses")
public class CourseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the course (auto-incremented).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private int id;

    /**
     * Name of the course. Required field.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Total duration of the course in hours. Required field.
     */
    @Column(name = "time_span", nullable = false)
    private double timeSpan;

    /**
     * Detailed description of the course content.
     */
    @Column(name = "description", nullable = false, length = 65535, columnDefinition = "TEXT")
    private String description;

    /**
     * Link to the course resource or e-learning platform.
     */
    @Column(name = "link", nullable = false)
    private String link;

    /**
     * Language in which the course is provided (stored as Enum).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private LanguageEnum language;

    /**
     * Course category (area) as defined in the requirements (stored as Enum).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "course_category", nullable = false)
    private CourseCategoryEnum courseCategory;

    /**
     * Flag indicating whether the course is active.
     * Inactive courses cannot be assigned to new users, but remain visible in user history.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    /**
     * One-to-many relationship with UserCourseEntity.
     * Represents all user enrollments in this course.
     */
    @OneToMany(mappedBy = "course")
    private List<UserCourseEntity> userCourses;

    /** Default constructor. */
    public CourseEntity() {}

    // Getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTimeSpan() { return timeSpan; }
    public void setTimeSpan(double timeSpan) { this.timeSpan = timeSpan; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LanguageEnum getLanguage() { return language; }
    public void setLanguage(LanguageEnum language) { this.language = language; }

    public CourseCategoryEnum getCourseCategory() { return courseCategory; }
    public void setCourseCategory(CourseCategoryEnum courseCategory) { this.courseCategory = courseCategory; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<UserCourseEntity> getUserCourses() { return userCourses; }
    public void setUserCourses(List<UserCourseEntity> userCourses) { this.userCourses = userCourses; }

    // equals and hashCode (based on id)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseEntity)) return false;
        CourseEntity that = (CourseEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString for debugging/logging

    @Override
    public String toString() {
        return "CourseEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", timeSpan=" + timeSpan +
                ", courseCategory=" + courseCategory +
                ", language=" + language +
                ", active=" + active +
                '}';
    }
}



