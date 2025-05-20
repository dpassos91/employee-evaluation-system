package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "courses")
public class CourseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "time_span", nullable = false)
    private double timeSpan;

    @Column(name = "description", nullable = false, length = 65535, columnDefinition = "TEXT")
    private String description;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "course_category", nullable = false)
    private String courseCategory;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    // Relação One to Many com a tabela userCourses
    @OneToMany(mappedBy = "course")
    private List<UserCourseEntity> userCourses;

    // Construtor vazio
    public CourseEntity() {}

    // Getters e Setters
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

    public String getLanguage() { return language; }

    public void setLanguage(String language) { this.language = language; }

    public String getCourseCategory() { return courseCategory; }

    public void setCourseCategory(String courseCategory) { this.courseCategory = courseCategory; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public List<UserCourseEntity> getUserCourses() {
        return userCourses;
    }

    public void setUserCourses(List<UserCourseEntity> userCourses) {
        this.userCourses = userCourses;
    }

    /**
     * Convenience method to get all users enrolled in this course.
     * Ignores participation date — use getUserCourses() directly if needed.
     */

    /*
    TODO
    Propositadamente comentado, ver se nos faz sentido de futuro.
    O que faz: obtém lista de utilizadores participantes (sem data)

    @Transient
    public List<UserEntity> getEnrolledUsers() {
        return userCourses == null ? List.of() :
                userCourses.stream()
                        .map(UserCourseEntity::getUser)
                        .toList();
    }*/

    // equals e hashCode (útil para testes)
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

    // toString (sem campos pesados como descrição longa ou users)
    @Override
    public String toString() {
        return "CourseEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", timeSpan=" + timeSpan +
                ", courseCategory='" + courseCategory + '\'' +
                ", active=" + active +
                '}';
    }
}


