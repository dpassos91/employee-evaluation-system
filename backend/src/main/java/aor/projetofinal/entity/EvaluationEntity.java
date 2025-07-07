package aor.projetofinal.entity;

import aor.projetofinal.entity.enums.EvaluationStateType;
import aor.projetofinal.entity.enums.GradeEvaluationType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
//the unique constraint ensures that a user can only be evaluated once per cycle, so that
// tehre cannot exist two lines at this table with the same combination of cycle_id and evaluated_user_id
//cosntrains works as a validation at the database level
@Table(
        name = "evaluations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cycle_id", "evaluated_user_id"})
        }
)
public class EvaluationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = true)
    private GradeEvaluationType grade;

    @Column(name = "feedback", nullable = true, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "evaluation_date", nullable = true)
    private LocalDateTime date;


    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private EvaluationStateType state;

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
    public EvaluationEntity() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GradeEvaluationType getGrade() {
        return grade;
    }

    public void setGrade(GradeEvaluationType grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public EvaluationStateType getState() {
        return state;
    }

    public void setState(EvaluationStateType state) {
        this.state = state;
    }

    public EvaluationCycleEntity getCycle() {
        return cycle;
    }

    public void setCycle(EvaluationCycleEntity cycle) {
        this.cycle = cycle;
    }

    public UserEntity getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(UserEntity evaluated) {
        this.evaluated = evaluated;
    }

    public UserEntity getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(UserEntity evaluator) {
        this.evaluator = evaluator;
    }

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
                ", rating=" + grade +
                ", date=" + date +
                '}';
    }
}
