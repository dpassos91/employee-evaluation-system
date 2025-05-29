package aor.projetofinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCourseIdEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "course_id")
    private int courseId;

    // Construtor vazio
    public UserCourseIdEntity() {}

    public UserCourseIdEntity(int userId, int courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    // Getters e Setters
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

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseIdEntity)) return false;
        UserCourseIdEntity that = (UserCourseIdEntity) o;
        return userId == that.userId && courseId == that.courseId;
    }


    // hash
    @Override
    public int hashCode() {
        return Objects.hash(userId, courseId);
    }

    // toString
    @Override
    public String toString() {
        return "UserCourseIdEntity{" +
                "userId=" + userId +
                ", courseId=" + courseId +
                '}';
    }
}

