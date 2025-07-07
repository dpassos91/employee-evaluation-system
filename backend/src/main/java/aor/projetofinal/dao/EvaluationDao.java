package aor.projetofinal.dao;

import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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


    public EvaluationEntity findById(Long id) {
        TypedQuery<EvaluationEntity> query = em.createQuery(
                "SELECT e FROM EvaluationEntity e WHERE e.id = :id",
                EvaluationEntity.class
        );
        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }






    public EvaluationEntity findEvaluationByCycleAndUser(EvaluationCycleEntity cycle, UserEntity evaluated) {
        try {
            TypedQuery<EvaluationEntity> query = em.createQuery(
                    "SELECT e FROM EvaluationEntity e WHERE e.cycle = :cycle AND e.evaluated = :evaluated",
                    EvaluationEntity.class
            );
            query.setParameter("cycle", cycle);
            query.setParameter("evaluated", evaluated);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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


    public void save(EvaluationEntity evaluation) {
        em.merge(evaluation);
    }




}
