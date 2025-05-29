package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class EvaluationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "evaluation_date", nullable = false)
    private LocalDateTime date;

    // Relação Many to One com ciclos
    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private EvaluationCycleEntity cycle;

    // Relação Many to One com users (avaliado)
    @ManyToOne
    @JoinColumn(name = "evaluated_user_id", nullable = false)
    private UserEntity evaluated;

    // Relação Many to One com users (avaliador)
    @ManyToOne
    @JoinColumn(name = "evaluator_user_id", nullable = false)
    private UserEntity evaluator;

    // Construtor vazio
    public EvaluationEntity() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public EvaluationCycleEntity getCycle() { return cycle; }
    public void setCycle(EvaluationCycleEntity cycle) { this.cycle = cycle; }

    public UserEntity getEvaluated() { return evaluated; }
    public void setEvaluated(UserEntity evaluated) { this.evaluated = evaluated; }

    public UserEntity getEvaluator() { return evaluator; }
    public void setEvaluator(UserEntity evaluator) { this.evaluator = evaluator; }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvaluationEntity)) return false;
        EvaluationEntity that = (EvaluationEntity) o;
        return id != null && id.equals(that.id);
    }

    // hash
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // toString
    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", rating=" + rating +
                ", date=" + date +
                '}';
    }
}
