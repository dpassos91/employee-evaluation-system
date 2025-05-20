package aor.projetofinal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "is_confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "confirmation_token_expiry")
    private LocalDateTime confirmationTokenExpiry;

    @Column(name = "recovery_token")
    private String recoveryToken;

    @Column(name = "recovery_token_expiry")
    private LocalDateTime recoveryTokenExpiry;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relacionamento One to Many com userCourses
    @OneToMany(mappedBy = "user")
    private List<UserCourseEntity> userCourses;

    // Relacionamento Many to One com roles
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    // Construtor vazio
    public UserEntity() {}

    // Getters e Setters
    public LocalDateTime getConfirmationTokenExpiry() {
        return confirmationTokenExpiry;
    }
    public void setConfirmationTokenExpiry(LocalDateTime confirmationTokenExpiry) {
        this.confirmationTokenExpiry = confirmationTokenExpiry;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public RoleEntity getRole() {
        return role;
    }
    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active= active;
    }

    public LocalDateTime getRecoveryTokenExpiry() {
        return recoveryTokenExpiry;
    }
    public void setRecoveryTokenExpiry(LocalDateTime recoveryTokenExpiry) {
        this.recoveryTokenExpiry = recoveryTokenExpiry;
    }

    public String getRecoveryToken() {
        return recoveryToken;
    }
    public void setRecoveryToken(String recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }
    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Convenience method to get all courses the user is enrolled in.
     * This ignores participation dates — use getUserCourses() directly if you need full details.
     */

    /* TODO
    Propositadamente comentado, ver se nos faz sentido de futuro.
    O que faz: obtém formações do utilizador (sem data)

    @Transient
    public List<CourseEntity> getEnrolledCourses() {
        return userCourses == null ? List.of() :
                userCourses.stream()
                        .map(UserCourseEntity::getCourse)
                        .toList();
    }*/

    // Útil para testes
    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", confirmed=" + confirmed +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", role=" + (role != null ? role.getName() : "null") +
                '}';
    }

    // Útil para testes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id == that.id;
    }

    // Útil para testes
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
