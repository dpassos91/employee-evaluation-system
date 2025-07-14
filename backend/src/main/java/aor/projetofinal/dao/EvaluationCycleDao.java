package aor.projetofinal.dao;

import aor.projetofinal.bean.EvaluationBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.enums.EvaluationStateEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class EvaluationCycleDao {

    @PersistenceContext
    private EntityManager em;


    private static final Logger logger = LogManager.getLogger(EvaluationCycleDao.class);

    /**
     * Retrieves the currently active evaluation cycle from the database.
     *
     * @return The active EvaluationCycleEntity, or null if no active cycle exists.
     */
    public EvaluationCycleEntity findActiveCycle() {
        try {
            TypedQuery<EvaluationCycleEntity> query = em.createQuery(
                    "SELECT c FROM EvaluationCycleEntity c WHERE c.active = true", EvaluationCycleEntity.class
            );
            query.setMaxResults(1);

            EvaluationCycleEntity result = query.getSingleResult();

            logger.info("User: {} | IP: {} - Active evaluation cycle retrieved with ID: {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), result.getId());

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No active evaluation cycle found.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return null;
        }
    }








    /**
     * Checks whether there is at least one active evaluation cycle in the system.
     *
     * @return True if an active cycle exists; false otherwise.
     */
    public boolean isThereAnActiveCycle() {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM EvaluationCycleEntity c WHERE c.active = true", Long.class
        );
        Long count = query.getSingleResult();

        logger.info("User: {} | IP: {} - Checked for active evaluation cycle: {} found.",
                RequestContext.getAuthor(), RequestContext.getIp(), count > 0 ? "Yes" : "No");

        return count > 0;
    }


    public void create(EvaluationCycleEntity evaluationCycle) {
        em.persist(evaluationCycle);
    }

    public void save(EvaluationCycleEntity evaluationCycle) {
        em.merge(evaluationCycle);
    }




}
