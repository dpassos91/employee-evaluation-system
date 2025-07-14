package aor.projetofinal.dao;


import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class SessionTokenDao {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = LogManager.getLogger(SessionTokenDao.class);


    public void delete(SessionTokenEntity sessionToken) {
        try {
            em.remove(em.contains(sessionToken) ? sessionToken : em.merge(sessionToken));
        } catch (Exception e) {
            logger.error("Erro ao remover o sessionToken.", e);
        }
    }

    /**
     * Deletes all session tokens associated with the specified user ID.
     *
     * @param userId The ID of the user whose session tokens should be deleted.
     */
    public void deleteByUserId(int userId) {
        int deletedCount = em.createQuery("DELETE FROM SessionTokenEntity s WHERE s.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        logger.info("User: {} | IP: {} - Deleted {} session tokens for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), deletedCount, userId);
    }


    /**
     * Finds a session token entity by its token value.
     *
     * @param token The session token string to search for.
     * @return The corresponding SessionTokenEntity, or null if not found.
     */
    public SessionTokenEntity findBySessionToken(String token) {
        try {
            SessionTokenEntity result = em.createQuery(
                            "SELECT s FROM SessionTokenEntity s WHERE s.tokenValue = :token", SessionTokenEntity.class)
                    .setParameter("token", token)
                    .getSingleResult();

            logger.info("User: {} | IP: {} - Session token found for token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), token);

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No session token found for token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), token);
            return null;
        }
    }



    /**
     * Retrieves all session tokens that have expired.
     *
     * @param now The current timestamp used to compare with expiryDate.
     * @return A list of expired SessionTokenEntity instances.
     */
    public List<SessionTokenEntity> findExpiredSessionTokens(LocalDateTime now) {
        List<SessionTokenEntity> results = em.createQuery(
                        "SELECT s FROM SessionTokenEntity s WHERE s.expiryDate <= :now", SessionTokenEntity.class)
                .setParameter("now", now)
                .getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} expired session tokens as of {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), results.size(), now);

        return results;
    }


    public void persist(SessionTokenEntity sessionToken) {
        em.persist(sessionToken);
    }

    public void save(SessionTokenEntity sessionTokenEntity) {
        em.merge(sessionTokenEntity);
    }




}
