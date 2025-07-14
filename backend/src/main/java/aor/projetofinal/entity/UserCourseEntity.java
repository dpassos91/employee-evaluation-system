package aor.projetofinal.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing the participation of a user in a course.
 * This is a many-to-many association between users and courses with additional metadata.
 */
@Entity
@Table(name = "user_courses")
public class UserCourseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UserCourseIdEntity id = new UserCourseIdEntity();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    @Column(name = "participation_date", nullable = false)
    private LocalDateTime participationDate;

    // Construtor vazio
    public UserCourseEntity() {}

    // Getters e setters
    public UserCourseIdEntity getId() {
        return id;
    }
    public void setId(UserCourseIdEntity id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CourseEntity getCourse() {
        return course;
    }
    public void setCourse(CourseEntity course) {
        this.course = course;
    }

    public LocalDateTime getParticipationDate() {
        return participationDate;
    }
    public void setParticipationDate(LocalDateTime participationDate) {
        this.participationDate = participationDate;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseEntity)) return false;
        UserCourseEntity that = (UserCourseEntity) o;
        return Objects.equals(id, that.id);
    }

    // hash
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "UserCourseEntity{" +
                "user=" + (user != null ? user.getId() : null) +
                ", course=" + (course != null ? course.getId() : null) +
                ", participationDate=" + participationDate +
                '}';
    }
}

