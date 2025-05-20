package aor.projetofinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCourseIdEntity implements Serializable {

    @Column(name = "user_id")
    private int userId;

    @Column(name = "course_id")
    private int courseId;

    public UserCourseId() {}

    public UserCourseId(int userId, int courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

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

    // Útil para testes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseId)) return false;
        UserCourseId that = (UserCourseId) o;
        return userId == that.userId && courseId == that.courseId;
    }

    // Útil para testes
    @Override
    public int hashCode() {
        return Objects.hash(userId, courseId);
    }
}

