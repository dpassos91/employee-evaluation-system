package aor.projetofinal.dao;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateEnum;
import aor.projetofinal.entity.enums.GradeEvaluationEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import aor.projetofinal.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class EvaluationDao {

    @PersistenceContext
    private EntityManager em;


    private static final Logger logger = LogManager.getLogger(EvaluationDao.class);


    public void create(EvaluationEntity evaluation) {
        em.persist(evaluation);
    }


    public boolean alreadyEvaluatedAtCurrentCycle(EvaluationCycleEntity cycle, UserEntity evaluated) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(e) FROM EvaluationEntity e WHERE e.cycle = :cycle AND e.evaluated = :evaluated",
                Long.class
        );

        query.setParameter("cycle", cycle);
        query.setParameter("evaluated", evaluated);

        Long count = query.getSingleResult();
        return count > 0;
    }


    /**
     * Counts the number of closed evaluations for a specific user,
     * considering only evaluations from inactive cycles.
     *
     * @param evaluated The user being evaluated.
     * @return The total number of evaluations that match the criteria.
     */
    public long countClosedByEvaluated(UserEntity evaluated) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(e) FROM EvaluationEntity e " +
                        "WHERE e.evaluated = :evaluated " +
                        "AND e.state = :state " +
                        "AND e.cycle.active = false",
                Long.class
        );

        query.setParameter("evaluated", evaluated);
        query.setParameter("state", EvaluationStateEnum.CLOSED);
        return query.getSingleResult();
    }





    /**
     * Counts the total number of evaluations matching the given filter criteria.
     * Applies the same visibility rules as in paginated queries.
     *
     * @param name       Partial name of evaluated user (nullable)
     * @param state      Evaluation state to filter by (nullable)
     * @param grade      Grade to filter by (nullable)
     * @param cycleEnd   Exact end date of cycle to filter by (nullable)
     * @param requester  The logged-in user requesting the data
     * @return The total number of matching evaluation records
     */
    public long countEvaluationsWithFilters(String name,
                                            EvaluationStateEnum state,
                                            Integer grade,
                                            LocalDate cycleEnd,
                                            UserEntity requester) {

        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM EvaluationEntity e WHERE 1=1");

        // Only confirmed & active evaluated users
        jpql.append(" AND e.evaluated.confirmed = true AND e.evaluated.active = true");

        // Only evalautions from active cycles should be returned
        jpql.append(" AND e.cycle.active = true");

        // Name filter
        if (name != null && !name.isBlank()) {
            jpql.append(" AND CONCAT(' ', LOWER(e.evaluated.profile.normalizedFirstName), ' ', LOWER(e.evaluated.profile.normalizedLastName), ' ') LIKE CONCAT('% ', :name, ' %')");
        }

        // State filter
        if (state != null) {
            jpql.append(" AND e.state = :state");
        }

        // Grade filter
        if (grade != null) {
            jpql.append(" AND e.grade = :gradeEnum");
        }

        // Cycle end date filter
        if (cycleEnd != null) {
            jpql.append(" AND e.cycle.endDate >= :cycleStartOfDay AND e.cycle.endDate < :cycleNextDay");

        }

        // Restrict visibility if not admin
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("admin");
        if (!isAdmin) {
            jpql.append(" AND e.evaluator.email = :requesterEmail");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        // Set parameters
        if (name != null && !name.isBlank()) {
            String normalizedName = StringUtils.normalize(name.toLowerCase());
            query.setParameter("name", normalizedName);
        }

        if (state != null) {
            query.setParameter("state", state);
        }

        if (grade != null) {
            GradeEvaluationEnum gradeEnum = GradeEvaluationEnum.getEnumfromGrade(grade);
            query.setParameter("gradeEnum", gradeEnum);
        }

        if (cycleEnd != null) {
            query.setParameter("cycleStartOfDay", cycleEnd.atStartOfDay());
            query.setParameter("cycleNextDay", cycleEnd.plusDays(1).atStartOfDay());
        }

        if (!isAdmin) {
            query.setParameter("requesterEmail", requester.getEmail());
        }

        return query.getSingleResult();
    }



    /**
     * Counts the total number of closed evaluations for a user,
     * optionally filtered by grade, cycle number, or cycle end date.
     *
     * @param evaluated     The evaluated user.
     * @param grade         Optional grade filter.
     * @param cycle         Optional cycle number filter.
     * @param cycleEndDate  Optional cycle end date filter (in "yyyy-MM-dd" format).
     * @return Total number of matching evaluations.
     */
    public long countFilteredClosedEvaluations(
            UserEntity evaluated, Integer grade, Integer cycle, String cycleEndDate
    ) {
        logger.info("User: {} | IP: {} - Counting filtered closed evaluations for user {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluated.getEmail());

        StringBuilder sb = new StringBuilder(
                "SELECT COUNT(e) FROM EvaluationEntity e " +
                        "WHERE e.evaluated = :evaluated " +
                        "AND e.state = :state " +
                        "AND e.cycle.active = false "
        );

        if (grade != null) sb.append("AND e.grade = :grade ");
        if (cycle != null) sb.append("AND e.cycle.id = :cycle ");
        if (cycleEndDate != null) sb.append("AND e.cycle.endDate BETWEEN :startOfDay AND :endOfDay ");

        TypedQuery<Long> query = em.createQuery(sb.toString(), Long.class);
        query.setParameter("evaluated", evaluated);
        query.setParameter("state", EvaluationStateEnum.CLOSED);

        if (grade != null) {
            GradeEvaluationEnum gradeEnum = GradeEvaluationEnum.getEnumfromGrade(grade);
            query.setParameter("grade", gradeEnum);
        }
        if (cycle != null) query.setParameter("cycle", Long.valueOf(cycle));
        if (cycleEndDate != null) {
            LocalDate parsedDate = LocalDate.parse(cycleEndDate);
            query.setParameter("startOfDay", parsedDate.atStartOfDay());
            query.setParameter("endOfDay", parsedDate.atTime(LocalTime.MAX));
        }

        long count = query.getSingleResult();

        logger.info("User: {} | IP: {} - Counted {} evaluations.",
                RequestContext.getAuthor(), RequestContext.getIp(), count);

        return count;
    }





    public void deleteEvaluation(EvaluationEntity evaluation) {
        if (em.contains(evaluation)) {
            em.remove(evaluation);
        } else {
            EvaluationEntity attached = em.merge(evaluation);
            em.remove(attached);
        }
    }


    /**
     * Retrieves all evaluations associated with the given evaluation cycle.
     *
     * This method is useful when you want to avoid LazyInitializationExceptions
     * and ensure evaluations are explicitly fetched in a separate query.
     *
     * Logs the author and IP from the request context for audit purposes.
     *
     * @param cycle The evaluation cycle for which to fetch associated evaluations.
     * @return A list of EvaluationEntity objects that belong to the given cycle.
     */
    public List<EvaluationEntity> findAllEvaluationsByCycle(EvaluationCycleEntity cycle) {
        logger.info("User: {} | IP: {} - Fetching all evaluations for cycle ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());

        List<EvaluationEntity> evaluations = em.createQuery(
                "SELECT e FROM EvaluationEntity e WHERE e.cycle = :cycle",
                EvaluationEntity.class
        ).setParameter("cycle", cycle).getResultList();

        logger.info("User: {} | IP: {} - Found {} evaluations for cycle ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluations.size(), cycle.getId());

        return evaluations;
    }





    // This version eagerly fetches the evaluation cycle and its evaluations
    public EvaluationEntity findById(Long id) {

        //avoids LazyInitializationException
        TypedQuery<EvaluationEntity> query = em.createQuery(
                "SELECT e FROM EvaluationEntity e " +
                        "JOIN FETCH e.cycle " +
                        "LEFT JOIN FETCH e.cycle.evaluationEntities " +
                        "WHERE e.id = :id",
                EvaluationEntity.class
        );
        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }



    /**
     * Returns a paginated list of evaluations where the given user is the evaluated person,
     * and the evaluations are CLOSED and from an inactive cycle.
     *
     * @param evaluated The user being evaluated.
     * @param page      Page number (1-based).
     * @param pageSize  Number of results per page.
     * @return A list of EvaluationEntity.
     */
    public List<EvaluationEntity> findClosedByEvaluatedPaginated(UserEntity evaluated, int page, int pageSize) {
        TypedQuery<EvaluationEntity> query = em.createQuery(
                "SELECT e FROM EvaluationEntity e " +
                        "WHERE e.evaluated = :evaluated " +
                        "AND e.state = :state " +
                        "AND e.cycle.active = false " +
                        "ORDER BY e.date DESC",
                EvaluationEntity.class
        );

        query.setParameter("evaluated", evaluated);
        query.setParameter("state", EvaluationStateEnum.CLOSED);

        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }




    /**
     * Retrieves all evaluations that are in the "EVALUATED" state
     * and belong to the currently active evaluation cycle.
     *
     * This method is used during the bulk closing operation, where
     * only evaluations marked as completed (EVALUATED) should be closed.
     *
     * @return List of evaluations ready to be closed in the active cycle.
     */
    public List<EvaluationEntity> findEvaluatedEvaluationsInActiveCycle() {
        logger.info("User: {} | IP: {} - Fetching all evaluations in EVALUATED state for the active cycle.",
                RequestContext.getAuthor(), RequestContext.getIp());

        try {
            List<EvaluationEntity> results = em.createQuery(
                            "SELECT e FROM EvaluationEntity e " +
                                    "JOIN e.cycle c " +
                                    "WHERE c.active = true AND e.state = :state",
                            EvaluationEntity.class
                    )
                    .setParameter("state", EvaluationStateEnum.EVALUATED)
                    .getResultList();

            logger.info("User: {} | IP: {} - Found {} evaluations in EVALUATED state for active cycle.",
                    RequestContext.getAuthor(), RequestContext.getIp(), results.size());

            return results;
        } catch (Exception ex) {
            logger.error("User: {} | IP: {} - Error while fetching EVALUATED evaluations for active cycle: {}",
                    RequestContext.getAuthor(), RequestContext.getIp(), ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }



    public EvaluationEntity findEvaluationByCycleAndUser(EvaluationCycleEntity cycle, UserEntity evaluated) {
        try {
            TypedQuery<EvaluationEntity> query = em.createQuery(
                    "SELECT e FROM EvaluationEntity e WHERE e.cycle = :cycle AND e.evaluated = :evaluated",
                    EvaluationEntity.class
            );
            query.setParameter("cycle", cycle);
            query.setParameter("evaluated", evaluated);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    /**
     * Finds a paginated list of evaluations filtered by optional criteria.
     * Only evaluations visible to the given requester (admin or manager) are returned.
     *
     * @param name       Partial name of evaluated user (nullable)
     * @param state      Evaluation state to filter by (nullable)
     * @param grade      Grade to filter by (nullable)
     * @param cycleEnd   Exact end date of cycle to filter by (nullable)
     * @param requester  The logged-in user requesting the evaluations
     * @param page       The page number (1-based)
     * @param pageSize   Max results per page
     * @return A list of EvaluationEntity matching the filters and visibility rules
     */
    public List<EvaluationEntity> findEvaluationsWithFiltersPaginated(String name,
                                                                      EvaluationStateEnum state,
                                                                      Integer grade,
                                                                      LocalDate cycleEnd,
                                                                      UserEntity requester,
                                                                      int page,
                                                                      int pageSize) {

        StringBuilder jpql = new StringBuilder("SELECT e FROM EvaluationEntity e WHERE 1=1");

        // Only confirmed & active evaluated users
        jpql.append(" AND e.evaluated.confirmed = true AND e.evaluated.active = true");

        // Only evalautions from active cycles should be returned
        jpql.append(" AND e.cycle.active = true");

        // Filter by name (normalized, case-insensitive, partial match)
        if (name != null && !name.isBlank()) {
            jpql.append(" AND CONCAT(' ', LOWER(e.evaluated.profile.normalizedFirstName), ' ', LOWER(e.evaluated.profile.normalizedLastName), ' ') LIKE CONCAT('% ', :name, ' %')");
        }

        // Filter by evaluation state
        if (state != null) {
            jpql.append(" AND e.state = :state");
        }

        // Filter by grade (1–4)
        if (grade != null) {
            jpql.append(" AND e.grade = :gradeEnum");
        }

        // Filter by cycle end date
        if (cycleEnd != null) {
            jpql.append(" AND e.cycle.endDate >= :cycleStartOfDay AND e.cycle.endDate < :cycleNextDay");

        }

        // Restrict access based on role
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("admin");
        if (!isAdmin) {
            // Managers see only their evaluations
            jpql.append(" AND e.evaluator.email = :requesterEmail");
        }

        // Order by evaluated last name
        jpql.append(" ORDER BY e.evaluated.profile.lastName ASC, e.evaluated.profile.firstName ASC");

        TypedQuery<EvaluationEntity> query = em.createQuery(jpql.toString(), EvaluationEntity.class);

        // Set query parameters
        if (name != null && !name.isBlank()) {
            String normalizedName = StringUtils.normalize(name.toLowerCase());
            query.setParameter("name", normalizedName);
        }

        if (state != null) {
            query.setParameter("state", state);
        }

        if (grade != null) {
            GradeEvaluationEnum gradeEnum = GradeEvaluationEnum.getEnumfromGrade(grade);
            query.setParameter("gradeEnum", gradeEnum);
        }

        if (cycleEnd != null) {
            query.setParameter("cycleStartOfDay", cycleEnd.atStartOfDay());
            query.setParameter("cycleNextDay", cycleEnd.plusDays(1).atStartOfDay());
        }

        if (!isAdmin) {
            query.setParameter("requesterEmail", requester.getEmail());
        }

        // Pagination
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }


    /**
     * Retrieves a paginated list of closed evaluations for a given user,
     * optionally filtered by grade, cycle number, or cycle end date.
     *
     * @param evaluated     The evaluated user.
     * @param page          The current page number (1-based).
     * @param pageSize      Number of evaluations per page.
     * @param grade         Optional grade filter.
     * @param cycle         Optional cycle number filter.
     * @param cycleEndDate  Optional cycle end date filter (in "yyyy-MM-dd" format).
     * @return List of filtered EvaluationEntity results.
     */
    public List<EvaluationEntity> findFilteredClosedEvaluations(
            UserEntity evaluated, int page, int pageSize,
            Integer grade, Integer cycle, String cycleEndDate
    ) {
        logger.info("User: {} | IP: {} - Fetching filtered closed evaluations for user {} (page {}).",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluated.getEmail(), page);

        StringBuilder sb = new StringBuilder(
                "SELECT e FROM EvaluationEntity e " +
                        "WHERE e.evaluated = :evaluated " +
                        "AND e.state = :state " +
                        "AND e.cycle.active = false "
        );

        if (grade != null) sb.append("AND e.grade = :grade ");
        if (cycle != null) sb.append("AND e.cycle.id = :cycle ");
        if (cycleEndDate != null) sb.append("AND e.cycle.endDate BETWEEN :startOfDay AND :endOfDay ");

        sb.append("ORDER BY e.date DESC");

        TypedQuery<EvaluationEntity> query = em.createQuery(sb.toString(), EvaluationEntity.class);
        query.setParameter("evaluated", evaluated);
        query.setParameter("state", EvaluationStateEnum.CLOSED);

        if (grade != null) {
            GradeEvaluationEnum gradeEnum = GradeEvaluationEnum.getEnumfromGrade(grade);
            query.setParameter("grade", gradeEnum);
        }
        if (cycle != null) query.setParameter("cycle", Long.valueOf(cycle));
        if (cycleEndDate != null) {
            LocalDate parsedDate = LocalDate.parse(cycleEndDate);
            query.setParameter("startOfDay", parsedDate.atStartOfDay());
            query.setParameter("endOfDay", parsedDate.atTime(LocalTime.MAX));
        }

        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        List<EvaluationEntity> results = query.getResultList();

        logger.info("User: {} | IP: {} - Found {} evaluations.",
                RequestContext.getAuthor(), RequestContext.getIp(), results.size());

        return results;
    }








    public List<EvaluationEntity> findIncompleteEvaluationsByCycle(EvaluationCycleEntity cycle) {
        TypedQuery<EvaluationEntity> query = em.createQuery(
                "SELECT e FROM EvaluationEntity e WHERE e.cycle = :cycle AND e.state <> :excludedState",
                EvaluationEntity.class
        );
        query.setParameter("cycle", cycle);
        query.setParameter("excludedState", EvaluationStateEnum.CLOSED);
        return query.getResultList();
    }


    public void save(EvaluationEntity evaluation) {
        em.merge(evaluation);
    }




}
