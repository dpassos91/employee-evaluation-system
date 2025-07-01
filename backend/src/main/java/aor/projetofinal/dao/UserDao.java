package aor.projetofinal.dao;

import aor.projetofinal.entity.SettingsEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class UserDao {

    @PersistenceContext
    private EntityManager em;


    public long countAdmins() {
        return em.createQuery(
                        "SELECT COUNT(u) FROM UserEntity u WHERE u.role.name = :roleName", Long.class)
                .setParameter("roleName", "ADMIN")
                .getSingleResult();
    }

    public void create(UserEntity user) {
        em.persist(user);
    }

    public UserEntity findByConfirmToken(String confirmToken) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.confirmationToken = :token", UserEntity.class);
            query.setParameter("token", confirmToken);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }    
    
    public UserEntity findById(int id) {
    try {
        return em.find(UserEntity.class, id);
    } catch (Exception e) {
        return null;
    }
}

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

    public UserEntity findByRecoveryToken(String recoveryToken) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.recoveryToken = :token", UserEntity.class);
            query.setParameter("token", recoveryToken);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

public UserEntity findBySessionToken(String token) {
    try {
        TypedQuery<UserEntity> query = em.createQuery(
            "SELECT st.user FROM SessionTokenEntity st WHERE st.tokenValue = :token", UserEntity.class);
        query.setParameter("token", token);
        return query.getSingleResult();
    } catch (NoResultException e) {
        return null;
    }
}


    public List<UserEntity> findConfirmedUsersWithoutManager() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.confirmed = true AND u.manager IS NULL",
                UserEntity.class
        );
        return query.getResultList();
    }

    public List<UserEntity> findUsersByRole(String roleName) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE LOWER(u.role.name) = LOWER(:roleName) " +
                        "AND u.active = true " +
                        "AND u.confirmed = true",
                UserEntity.class
        );
        query.setParameter("roleName", roleName);
        return query.getResultList();
    }


    public void save(UserEntity user) {
        em.merge(user);
    }

}
