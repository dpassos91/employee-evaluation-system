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

    /**
     * Counts the number of users with the ADMIN role.
     *
     * @return The total number of users assigned to the ADMIN role.
     */
    public long countAdmins() {
        long count = em.createQuery(
                        "SELECT COUNT(u) FROM UserEntity u WHERE u.role.name = :roleName", Long.class)
                .setParameter("roleName", "ADMIN")
                .getSingleResult();

        logger.info("User: {} | IP: {} - Counted {} admin users.",
                RequestContext.getAuthor(), RequestContext.getIp(), count);

        return count;
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

    /**
     * Finds a user by their confirmation token.
     *
     * @param confirmToken The confirmation token to search for.
     * @return The corresponding UserEntity, or null if not found.
     */
    public UserEntity findByConfirmToken(String confirmToken) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.confirmationToken = :token", UserEntity.class);
            query.setParameter("token", confirmToken);

            UserEntity result = query.getSingleResult();

            logger.info("User: {} | IP: {} - User found with confirmation token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), confirmToken);

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No user found with confirmation token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), confirmToken);
            return null;
        }
    }


    /**
     * Finds a user by their unique ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The corresponding UserEntity, or null if not found or on error.
     */
    public UserEntity findById(int id) {
        try {
            UserEntity result = em.find(UserEntity.class, id);

            if (result != null) {
                logger.info("User: {} | IP: {} - User found with ID {}.",
                        RequestContext.getAuthor(), RequestContext.getIp(), id);
            } else {
                logger.warn("User: {} | IP: {} - No user found with ID {}.",
                        RequestContext.getAuthor(), RequestContext.getIp(), id);
            }

            return result;
        } catch (Exception e) {
            logger.error("User: {} | IP: {} - Error retrieving user with ID {}: {}",
                    RequestContext.getAuthor(), RequestContext.getIp(), id, e.getMessage());
            return null;
        }
    }


    /**
     * Finds a user by their email address.
     *
     * @param email The email of the user to search for.
     * @return The corresponding UserEntity, or null if not found.
     */
    public UserEntity findByEmail(String email) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class);
            query.setParameter("email", email);

            UserEntity result = query.getSingleResult();

            logger.info("User: {} | IP: {} - User found with email '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No user found with email '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), email);
            return null;
        }
    }


    /**
     * Finds a user by their password recovery token.
     *
     * @param recoveryToken The recovery token to search for.
     * @return The corresponding UserEntity, or null if not found.
     */
    public UserEntity findByRecoveryToken(String recoveryToken) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.recoveryToken = :token", UserEntity.class);
            query.setParameter("token", recoveryToken);

            UserEntity result = query.getSingleResult();

            logger.info("User: {} | IP: {} - User found with recovery token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), recoveryToken);

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No user found with recovery token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), recoveryToken);
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




    /**
     * Retrieves the user associated with a given session token.
     *
     * @param token The session token string.
     * @return The corresponding UserEntity, or null if not found.
     */
    public UserEntity findBySessionToken(String token) {
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT st.user FROM SessionTokenEntity st WHERE st.tokenValue = :token", UserEntity.class);
            query.setParameter("token", token);

            UserEntity result = query.getSingleResult();

            logger.info("User: {} | IP: {} - User found by session token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), token);

            return result;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No user found for session token '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), token);
            return null;
        }
    }



    /**
     * Retrieves all active and confirmed users who have a manager assigned and are not administrators.
     *
     * @return A list of confirmed UserEntity objects with managers assigned.
     */
    public List<UserEntity> findConfirmedUsersWithManager() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE u.active = true " +
                        "AND u.confirmed = true " +
                        "AND u.manager IS NOT NULL " +
                        "AND LOWER(u.role.name) <> 'admin'",
                UserEntity.class
        );

        List<UserEntity> users = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} confirmed users with managers (excluding admins).",
                RequestContext.getAuthor(), RequestContext.getIp(), users.size());

        return users;
    }



    /**
     * Retrieves all confirmed users who do not have a manager assigned and are not administrators.
     *
     * @return A list of confirmed UserEntity objects without managers.
     */
    public List<UserEntity> findConfirmedUsersWithoutManager() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE u.confirmed = true AND u.manager IS NULL AND LOWER(u.role.name) <> 'admin'",
                UserEntity.class
        );

        List<UserEntity> users = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} confirmed users without managers (excluding admins).",
                RequestContext.getAuthor(), RequestContext.getIp(), users.size());

        return users;
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

    /**
     * Finds a list of confirmed and active users that can be assigned as a manager,
     * excluding the specified user and their former manager. Administrators are also excluded.
     *
     * @param excluded The user who should not be included in the result.
     * @param formerManager The previous manager to be excluded from the result.
     * @return A list of suitable UserEntity candidates for manager assignment.
     */
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

        List<UserEntity> results = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} suitable manager candidates excluding '{}' and former manager '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), results.size(),
                excluded.getEmail(), formerManager.getEmail());

        return results;
    }


    /**
     * Retrieves all confirmed and active users managed by the given manager,
     * excluding users with the ADMIN role.
     *
     * @param manager The manager whose direct reports are to be retrieved.
     * @return A list of UserEntity objects managed by the specified manager.
     */
    public List<UserEntity> findUsersByManager(UserEntity manager) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE u.manager = :manager " +
                        "AND u.confirmed = true AND u.active = true " +
                        "AND LOWER(u.role.name) <> 'admin'",
                UserEntity.class
        );
        query.setParameter("manager", manager);

        List<UserEntity> results = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} users managed by '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), results.size(), manager.getEmail());

        return results;
    }




    /**
     * Retrieves all confirmed and active users with the specified role.
     *
     * @param roleName The name of the role to filter users by.
     * @return A list of UserEntity objects matching the given role.
     */
    public List<UserEntity> findUsersByRole(String roleName) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE LOWER(u.role.name) = LOWER(:roleName) " +
                        "AND u.active = true " +
                        "AND u.confirmed = true",
                UserEntity.class
        );
        query.setParameter("roleName", roleName);

        List<UserEntity> results = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} users with role '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), results.size(), roleName);

        return results;
    }


    /**
     * Retrieves all confirmed and active users who are assigned as their own manager.
     *
     * @return A list of UserEntity objects managing themselves.
     */
    public List<UserEntity> findUsersManagingThemselves() {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u " +
                        "WHERE u.confirmed = true AND u.active = true " +
                        "AND u.manager IS NOT NULL " +
                        "AND u = u.manager",
                UserEntity.class
        );

        List<UserEntity> results = query.getResultList();

        logger.info("User: {} | IP: {} - Retrieved {} users managing themselves.",
                RequestContext.getAuthor(), RequestContext.getIp(), results.size());

        return results;
    }




    public void save(UserEntity user) {
        em.merge(user);
    }

}
