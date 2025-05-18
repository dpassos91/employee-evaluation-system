package aor.projetofinal.dao;

import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    public UserEntity findByEmail(String email) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void create(UserEntity user) {
        em.persist(user);
    }

    public long countAdmins() {
        return em.createQuery(
                        "SELECT COUNT(u) FROM UserEntity u WHERE u.role.name = :roleName", Long.class)
                .setParameter("roleName", "ADMIN")
                .getSingleResult();
    }
}
