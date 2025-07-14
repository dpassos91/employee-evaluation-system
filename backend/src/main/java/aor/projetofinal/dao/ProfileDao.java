package aor.projetofinal.dao;


import aor.projetofinal.context.RequestContext;
import aor.projetofinal.util.StringUtils;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
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
public class ProfileDao {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = LogManager.getLogger(ProfileDao.class);

    /**
     * Counts the number of confirmed user profiles that match the given filters:
     * employee name, usual workplace, and manager's email.
     *
     * @param employeeName Filter for employee's name (first or last).
     * @param workplace Filter for usual workplace.
     * @param managerEmail Filter for manager's email.
     * @return The total number of profiles matching the filters.
     */
    public long countProfilesWithFilters(String employeeName, UsualWorkPlaceEnum workplace, String managerEmail) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM ProfileEntity p WHERE 1=1");

        // Apenas utilizadores com conta confirmada
        jpql.append(" AND p.user.confirmed = true");

        String normalizedEmployeeName = null;

        if (employeeName != null && !employeeName.isBlank()) {
            normalizedEmployeeName = StringUtils.normalize(employeeName);
            jpql.append(" AND CONCAT(' ', p.normalizedFirstName, ' ', p.normalizedLastName, ' ') LIKE CONCAT('% ', :employeeName, ' %')");
        }

        if (workplace != null) {
            jpql.append(" AND p.usualWorkplace = :workplace");
        }

        if (managerEmail != null && !managerEmail.isBlank()) {
            jpql.append(" AND p.user.manager.email = :managerEmail");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (employeeName != null && !employeeName.isBlank()) {
            query.setParameter("employeeName", normalizedEmployeeName);
        }

        if (workplace != null) {
            query.setParameter("workplace", workplace);
        }

        if (managerEmail != null && !managerEmail.isBlank()) {
            query.setParameter("managerEmail", managerEmail);
        }

        long result = query.getSingleResult();

        logger.info("User: {} | IP: {} - Counted {} profiles with filters [name='{}', workplace={}, managerEmail='{}'].",
                RequestContext.getAuthor(), RequestContext.getIp(), result, employeeName, workplace, managerEmail);

        return result;
    }


    public void create(ProfileEntity profile) {
        em.persist(profile);
    }


    //pretende receber email do gestor do frontend, visto que ppode haver gestores com nomes iguais , e o email é unico

    /**
     * Retrieves a paginated list of confirmed user profiles matching the specified filters:
     * employee name, usual workplace, and manager's email. Results are ordered by first and last name.
     *
     * @param employeeName Filter for employee's name (first or last).
     * @param workplace Filter for usual workplace.
     * @param managerEmail Filter for manager's email.
     * @param page The page number to retrieve (1-based index).
     * @return A list of ProfileEntity objects matching the filters for the requested page.
     */
    public List<ProfileEntity> findProfilesWithFiltersPaginated(String employeeName, UsualWorkPlaceEnum workplace, String managerEmail, int page) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT p FROM ProfileEntity p WHERE 1=1");

            // Only confirmed users
            jpql.append(" AND p.user.confirmed = true");

            String normalizedEmployeeName = null;

            if (employeeName != null && !employeeName.isBlank()) {
                normalizedEmployeeName = StringUtils.normalize(employeeName);
                jpql.append(" AND CONCAT(' ', p.normalizedFirstName, ' ', p.normalizedLastName, ' ') LIKE CONCAT('% ', :employeeName, ' %')");
            }

            if (workplace != null) {
                jpql.append(" AND p.usualWorkplace = :workplace");
            }

            if (managerEmail != null && !managerEmail.isBlank()) {
                jpql.append(" AND p.user.manager.email = :managerEmail");
            }

            // Order by name
            jpql.append(" ORDER BY p.firstName ASC, p.lastName ASC");

            TypedQuery<ProfileEntity> query = em.createQuery(jpql.toString(), ProfileEntity.class);

            if (employeeName != null && !employeeName.isBlank()) {
                query.setParameter("employeeName", normalizedEmployeeName);
            }

            if (workplace != null) {
                query.setParameter("workplace", workplace);
            }

            if (managerEmail != null && !managerEmail.isBlank()) {
                query.setParameter("managerEmail", managerEmail);
            }

            int pageSize = 10;
            int offset = (page > 0 ? page - 1 : 0) * pageSize;

            query.setFirstResult(offset);
            query.setMaxResults(pageSize);

            List<ProfileEntity> results = query.getResultList();

            logger.info("User: {} | IP: {} - Retrieved {} profiles for page {} with filters [name='{}', workplace={}, managerEmail='{}'].",
                    RequestContext.getAuthor(), RequestContext.getIp(), results.size(), page, employeeName, workplace, managerEmail);

            return results;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No profiles found for filters [name='{}', workplace={}, managerEmail='{}'].",
                    RequestContext.getAuthor(), RequestContext.getIp(), employeeName, workplace, managerEmail);

            return Collections.emptyList();
        }
    }



    //para produzir lista de perfis a exportar por Excel/csv
    /**
     * Retrieves all confirmed user profiles that match the provided filters:
     * employee name, usual workplace, and manager's email.
     * Results are sorted alphabetically by first and last name.
     *
     * @param employeeName Filter for employee's name (first or last).
     * @param workplace Filter for usual workplace.
     * @param managerEmail Filter for manager's email.
     * @return A list of ProfileEntity objects matching the filters.
     */
    public List<ProfileEntity> findProfilesWithFilters(String employeeName, UsualWorkPlaceEnum workplace, String managerEmail) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT p FROM ProfileEntity p WHERE 1=1");

            // Apenas utilizadores com conta confirmada
            jpql.append(" AND p.user.confirmed = true");

            String normalizedEmployeeName = null;

            if (employeeName != null && !employeeName.isBlank()) {
                normalizedEmployeeName = StringUtils.normalize(employeeName);
                jpql.append(" AND CONCAT(' ', p.normalizedFirstName, ' ', p.normalizedLastName, ' ') LIKE CONCAT('% ', :employeeName, ' %')");
            }

            if (workplace != null) {
                jpql.append(" AND p.usualWorkplace = :workplace");
            }

            if (managerEmail != null && !managerEmail.isBlank()) {
                jpql.append(" AND p.user.manager.email = :managerEmail");
            }

            // Ordenar por nome
            jpql.append(" ORDER BY p.firstName ASC, p.lastName ASC");

            TypedQuery<ProfileEntity> query = em.createQuery(jpql.toString(), ProfileEntity.class);

            if (employeeName != null && !employeeName.isBlank()) {
                query.setParameter("employeeName", normalizedEmployeeName);
            }

            if (workplace != null) {
                query.setParameter("workplace", workplace);
            }

            if (managerEmail != null && !managerEmail.isBlank()) {
                query.setParameter("managerEmail", managerEmail);
            }

            List<ProfileEntity> results = query.getResultList();

            logger.info("User: {} | IP: {} - Retrieved {} profiles with filters [name='{}', workplace={}, managerEmail='{}'].",
                    RequestContext.getAuthor(), RequestContext.getIp(), results.size(), employeeName, workplace, managerEmail);

            return results;
        } catch (NoResultException e) {
            logger.warn("User: {} | IP: {} - No profiles found for filters [name='{}', workplace={}, managerEmail='{}'].",
                    RequestContext.getAuthor(), RequestContext.getIp(), employeeName, workplace, managerEmail);

            return Collections.emptyList();
        }
    }


    /**
 * Finds the ProfileEntity associated with the given user ID, loading the User entity eagerly.
 * This ensures the profile always has access to userId, email, and role for DTO conversion.
 *
 * @param userId The unique ID of the user whose profile is being requested.
 * @return The ProfileEntity with User loaded, or null if not found.
 */
public ProfileEntity findByUserId(int userId) {
    try {
        TypedQuery<ProfileEntity> query = em.createQuery(
            "SELECT p FROM ProfileEntity p JOIN FETCH p.user WHERE p.user.id = :userId",
            ProfileEntity.class
        );
        query.setParameter("userId", userId);
        return query.getSingleResult();
    } catch (NoResultException e) {
        return null;
    }
}
    
        public void save(ProfileEntity profile) {
        em.merge(profile);
    }
}
