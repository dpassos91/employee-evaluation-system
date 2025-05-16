package aor.projetofinal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
public class NotificationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

//vamos considerar por agora que uma notificação em especifico é enviada a um unico user
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean is_read;


    @Column (name="message", nullable = false, unique = false, length = 65535, columnDefinition = "TEXT")
    private String message;

    @Column(name = "type", nullable = false)
    private String type;

}
