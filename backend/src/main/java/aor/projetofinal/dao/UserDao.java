package aor.projetofinal.dao;

import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.SettingsEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
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


    /**
     * Counts the total number of active, confirmed users without an assigned manager,
     * optionally filtered by name and office.
     * <p>
     * This count excludes users with the ADMIN role.
     *
     * @param name   Full or partial name of the user to match (normalized and case-insensitive).
     * @param office Office location (as enum) to filter results; may be null.
     * @return The total number of users matching the given filters.
     */
    public long countConfirmedUsersWithoutManagerFiltered(String name, UsualWorkPlaceEnum office) {
        logger.info("User: {} | IP: {} - Counting users without manager with filters. Name: '{}', Office: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), name, office);

        StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM UserEntity u WHERE 1=1");
        jpql.append(" AND u.confirmed = true AND u.active = true AND u.manager IS NULL");
        jpql.append(" AND LOWER(u.role.name) <> 'admin'");

        if (name != null && !name.isBlank()) {
            jpql.append(" AND CONCAT(' ', LOWER(u.profile.normalizedFirstName), ' ', LOWER(u.profile.normalizedLastName)) LIKE CONCAT('%', :name, '%')");
        }

        if (office != null) {
            jpql.append(" AND u.profile.usualWorkplace = :office");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (name != null && !name.isBlank()) {
            query.setParameter("name", name.toLowerCase());
        }

        if (office != null) {
            query.setParameter("office", office);
        }

        return query.getSingleResult();
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




    /**
     * Retrieves a paginated list of active, confirmed users without an assigned manager,
     * filtered by name and office (if provided).
     * <p>
     * This query excludes users with the ADMIN role and applies filters on normalized name fields and workplace.
     *
     * @param name     Full or partial name of the user to search (normalized and case-insensitive).
     * @param office   Office location (as enum) to filter results; may be null.
     * @param page     Page number for pagination (1-based).
     * @param pageSize Number of users per page.
     * @return A list of UserEntity objects matching the provided filters and pagination.
     */
    public List<UserEntity> findConfirmedUsersWithoutManagerFiltered(String name, UsualWorkPlaceEnum office, int page, int pageSize) {
        logger.info("User: {} | IP: {} - Fetching paginated users without manager with filters. Name: '{}', Office: {}, Page: {}, PageSize: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), name, office, page, pageSize);

        StringBuilder jpql = new StringBuilder("SELECT u FROM UserEntity u WHERE 1=1");
        jpql.append(" AND u.confirmed = true AND u.active = true AND u.manager IS NULL");
        jpql.append(" AND LOWER(u.role.name) <> 'admin'");

        if (name != null && !name.isBlank()) {
            jpql.append(" AND CONCAT(' ', LOWER(u.profile.normalizedFirstName), ' ', LOWER(u.profile.normalizedLastName)) LIKE CONCAT('%', :name, '%')");
        }

        if (office != null) {
            jpql.append(" AND u.profile.usualWorkplace = :office");
        }

        jpql.append(" ORDER BY u.profile.firstName ASC, u.profile.lastName ASC");

        TypedQuery<UserEntity> query = em.createQuery(jpql.toString(), UserEntity.class);

        if (name != null && !name.isBlank()) {
            query.setParameter("name", name.toLowerCase());
        }

        if (office != null) {
            query.setParameter("office", office);
        }

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
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
                "SELECT u FROM UserEntity u " +
                        "WHERE u.confirmed = true AND u.manager IS NULL AND LOWER(u.role.name) <> 'admin'",
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

    public List<UserEntity> findSuitableManager(UserEntity excluded, UserEntity formerManager) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.confirmed = true AND u.active = true " +
                        "AND LOWER(u.role.name) <> 'admin' " +
                        "AND u.email <> :excludedEmail " +
                        "AND u.email <> :formerManagerEmail",
                UserEntity.class
        );
        query.setParameter("excludedEmail", excluded.getEmail());
        query.setParameter("formerManagerEmail", formerManager.getEmail());
        return query.getResultList();
    }

    public List<UserEntity> findUsersByManager(UserEntity manager) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.manager = :manager " +
                        "AND u.confirmed = true AND u.active = true " +
                        "AND LOWER(u.role.name) <> 'admin'",
                UserEntity.class
        );
        query.setParameter("manager", manager);
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
