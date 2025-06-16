package aor.projetofinal.dao;


import aor.projetofinal.entity.SessionTokenEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class SessionTokenDao {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = LogManager.getLogger(SessionTokenDao.class);


    public void persist(SessionTokenEntity sessionToken) {
        em.persist(sessionToken);
    }

    public SessionTokenEntity findBySessionToken(String token) {
        try {
            return em.createQuery("SELECT s FROM SessionTokenEntity s WHERE s.tokenValue = :token", SessionTokenEntity.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void delete(SessionTokenEntity sessionToken) {
        try {
            em.remove(em.contains(sessionToken) ? sessionToken : em.merge(sessionToken));
        } catch (Exception e) {
            logger.error("Erro ao remover o sessionToken.", e);
        }
    }

    public void deleteByUserId(int userId) {
        em.createQuery("DELETE FROM SessionTokenEntity s WHERE s.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }






}
