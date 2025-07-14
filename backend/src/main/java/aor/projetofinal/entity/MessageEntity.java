package aor.projetofinal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a personal message exchanged between two users.
 * Includes sender, receiver, content, creation date, and read status.
 */
@Entity
@Table(name = "messages")
public class MessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // TODO: Avaliar com o professor se devo aplicar validações básicas @NotBlank aqui ou na camada DAO/Service
    //@NotBlank(message = "Message content cannot be blank")
    @Column (name="content", nullable = false, length = 65535, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    // Relacionamento Many to One com users
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    // Relacionamento Many to One com users
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    // Construtor vazio
    public MessageEntity() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public UserEntity getSender() {
        return sender;
    }
    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getReceiver() {
        return receiver;
    }
    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntity that = (MessageEntity) o;
        return id == that.id;
    }

    // hash
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "MessageEntity{" +
                "id=" + id +
                ", sender=" + (sender != null ? sender.getId() : "null") +
                ", receiver=" + (receiver != null ? receiver.getId() : "null") +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", isRead=" + read +
                '}';
    }
}
