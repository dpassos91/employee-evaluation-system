package aor.projetofinal.dao;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.RoleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class RoleDao {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = LogManager.getLogger(RoleDao.class);


    public void create(RoleEntity role) {
        em.persist(role);
    }

    /**
     * Finds a role entity by its name.
     *
     * @param name The name of the role to find.
     * @return The matching RoleEntity, or null if no such role exists.
     */
    public RoleEntity findByName(String name) {
        try {
            TypedQuery<RoleEntity> query = em.createQuery(
                    "SELECT r FROM RoleEntity r WHERE r.name = :name", RoleEntity.class);
            query.setParameter("name", name);

            RoleEntity result = query.getSingleResult();

            logger.info("User: {} | IP: {} - Role '{}' found with ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), name, result.getId());

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No role found with name '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), name);
            return null;
        }
    }

}

