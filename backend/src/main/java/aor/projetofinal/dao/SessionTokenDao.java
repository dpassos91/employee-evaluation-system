package aor.projetofinal.dao;


import aor.projetofinal.entity.SessionTokenEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class SessionTokenDao {

    @PersistenceContext
    private EntityManager em;

    public void persist(SessionTokenEntity sessionToken) {
        em.persist(sessionToken);
    }

    public SessionTokenEntity findBySessionToken(String token) {
        return em.createQuery("SELECT s FROM SessionTokenEntity s WHERE s.tokenValue = :token", SessionTokenEntity.class)
                .setParameter("token", token)
                .getSingleResult();
    }

    public void delete(SessionTokenEntity sessionToken) {
        em.remove(em.contains(sessionToken) ? sessionToken : em.merge(sessionToken));
    }

    public void deleteByUserId(int userId) {
        em.createQuery("DELETE FROM SessionTokenEntity s WHERE s.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }






}
