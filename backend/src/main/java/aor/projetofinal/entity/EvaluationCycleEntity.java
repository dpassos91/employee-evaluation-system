package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluation_cycles")
public class EvaluationCycleEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    // Relação de One to Many com evaluations
    @OneToMany(mappedBy = "cycle")
    private List<EvaluationEntity> evaluationEntities;

    // Construtor vazio
    public EvaluationCycleEntity() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<EvaluationEntity> getEvaluations() { return evaluationEntities; }
    public void setEvaluations(List<EvaluationEntity> evaluationEntities) { this.evaluationEntities = evaluationEntities; }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvaluationCycleEntity)) return false;
        EvaluationCycleEntity that = (EvaluationCycleEntity) o;
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
        return "EvaluationCycleEntity{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                '}';
    }
}
