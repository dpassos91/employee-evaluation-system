package aor.projetofinal.dao;

import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class EvaluationDao {

    @PersistenceContext
    private EntityManager em;


    public void create(EvaluationEntity evaluation) {
        em.persist(evaluation);
    }


    public boolean alreadyEvaluatedAtCurrentCycle(EvaluationCycleEntity cycle, UserEntity evaluated) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(e) FROM EvaluationEntity e WHERE e.cycle = :cycle AND e.evaluated = :evaluated",
                Long.class
        );

        query.setParameter("cycle", cycle);
        query.setParameter("evaluated", evaluated);

        Long count = query.getSingleResult();
        return count > 0;
    }

    public List<EvaluationEntity> findIncompleteEvaluationsByCycle(EvaluationCycleEntity cycle) {
        TypedQuery<EvaluationEntity> query = em.createQuery(
                "SELECT e FROM EvaluationEntity e WHERE e.cycle = :cycle AND e.state <> :excludedState",
                EvaluationEntity.class
        );
        query.setParameter("cycle", cycle);
        query.setParameter("excludedState", EvaluationStateType.CLOSED);
        return query.getResultList();
    }



}
