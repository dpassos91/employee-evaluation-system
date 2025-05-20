package aor.projetofinal.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_course")
public class UserCourseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UserCourseIdEntity id = new UserCourseId();

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

    public UserCourseEntity() {}

    // Getters e setters

    public UserCourseId getId() {
        return id;
    }

    public void setId(UserCourseId id) {
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

    // equals e hashCode com base no id composto (utéis para testes)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseEntity)) return false;
        UserCourseEntity that = (UserCourseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Útil para testes
    @Override
    public String toString() {
        return "UserCourseEntity{" +
                "user=" + (user != null ? user.getId() : null) +
                ", course=" + (course != null ? course.getId() : null) +
                ", participationDate=" + participationDate +
                '}';
    }
}

