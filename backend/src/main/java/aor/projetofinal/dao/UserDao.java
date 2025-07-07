package aor.projetofinal.dao;

import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.SettingsEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@ApplicationScoped
public class UserDao {

    @PersistenceContext
    private EntityManager em;


    private static final Logger logger = LogManager.getLogger(UserDao.class);

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


    public List<UserEntity> findConfirmedUsersWithManager() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE u.active = true " +
                        "AND u.confirmed = true " +
                        "AND u.manager IS NOT NULL " +
                        "AND LOWER(u.role.name) <> 'admin'",
                UserEntity.class
        );
        return query.getResultList();
    }



    public List<UserEntity> findConfirmedUsersWithoutManager() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.confirmed = true AND u.manager IS NULL",
                UserEntity.class
        );
        return query.getResultList();
    }

    /**
     * Retrieves all users who are active, confirmed, and not assigned the ADMIN role.
     * This is used to populate the manager assignment dropdown in the UI.
     *
     * @return A list of UserEntity objects representing eligible non-admin users.
     */
    public List<UserEntity> findNonAdminActiveConfirmedUsersForDropDownMenu() {
        logger.info("User: {} | IP: {} - Fetching non-admin, active, confirmed users for dropdown menu.",
                RequestContext.getAuthor(), RequestContext.getIp());

        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE " +
                        "u.active = true AND u.confirmed = true AND LOWER(u.role.name) <> 'admin'",
                UserEntity.class
        );

        List<UserEntity> resultList = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} users for dropdown menu.",
                RequestContext.getAuthor(), RequestContext.getIp(), resultList.size());

        return resultList;
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


    public List<UserEntity> findUsersManagingThemselves() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE u.confirmed = true AND u.active = true " +
                        "AND u.manager IS NOT NULL " +
                        "AND u = u.manager",
                UserEntity.class
        );
        return query.getResultList();
    }



    public void save(UserEntity user) {
        em.merge(user);
    }

}
