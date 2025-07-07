package aor.projetofinal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import aor.projetofinal.entity.enums.NotificationEnum;

@Entity
@Table(name = "notifications")
public class NotificationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Uma notificação é enviada a um único utilizador, mas um utilizador pode ter várias notificações
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationEnum type;

    // Getters e Setters

    public int getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;

    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationEnum getType() {
        return type;
    }
    public void setType(NotificationEnum type) {
        this.type = type;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationEntity that = (NotificationEntity) o;
        return id == that.id;
    }

    // hash
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // toString
    @Override
    public String toString() {
        return "NotificationEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", read=" + read +
                '}';
    }
}

