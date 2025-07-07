package aor.projetofinal.dao;

import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class EvaluationCycleDao {

    @PersistenceContext
    private EntityManager em;

    public EvaluationCycleEntity findActiveCycle() {
        try {
            TypedQuery<EvaluationCycleEntity> query = em.createQuery(
                    "SELECT c FROM EvaluationCycleEntity c WHERE c.active = true", EvaluationCycleEntity.class
            );
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public boolean isThereAnActiveCycle() {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM EvaluationCycleEntity c WHERE c.active = true", Long.class
        );
        Long count = query.getSingleResult();
        return count > 0;
    }

    public void create(EvaluationCycleEntity evaluationCycle) {
        em.persist(evaluationCycle);
    }

    public void save(EvaluationCycleEntity evaluationCycle) {
        em.merge(evaluationCycle);
    }




}
