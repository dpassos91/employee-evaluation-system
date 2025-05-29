package aor.projetofinal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_token")
public class SessionTokenEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false , updatable = false, unique = true )
    private int id;

    //um user pode ter vários sessionTokens em diferentes dispositivos
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token_value", nullable = false)
    private String tokenValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiry_date", nullable = true)
    private LocalDateTime expiryDate;


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getTokenValue() {
        return tokenValue;
    }
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionTokenEntity that = (SessionTokenEntity) o;

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
        return "SessionTokenEntity{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", sessionTokenValue='" + sessionTokenValue + '\'' +
                ", createdAt=" + createdAt +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
