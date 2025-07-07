package aor.projetofinal.dao;

import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;


import java.util.List;

@ApplicationScoped
public class EvaluationDao {

    @PersistenceContext
    private EntityManager em;


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
                        "AND e.state = aor.projetofinal.entity.enums.EvaluationStateType.CLOSED " +
                        "AND e.cycle.active = false",
                Long.class
        );

        query.setParameter("evaluated", evaluated);
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
                                            EvaluationStateType state,
                                            Integer grade,
                                            LocalDate cycleEnd,
                                            UserEntity requester) {

        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM EvaluationEntity e WHERE 1=1");

        // Only confirmed & active evaluated users
        jpql.append(" AND e.evaluated.confirmed = true AND e.evaluated.active = true");

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
            jpql.append(" AND e.grade.grade = :grade");
        }

        // Cycle end date filter
        if (cycleEnd != null) {
            jpql.append(" AND DATE(e.cycle.endDate) = :cycleEnd");
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
            query.setParameter("grade", grade);
        }

        if (cycleEnd != null) {
            query.setParameter("cycleEnd", cycleEnd);
        }

        if (!isAdmin) {
            query.setParameter("requesterEmail", requester.getEmail());
        }

        return query.getSingleResult();
    }








    public void deleteEvaluation(EvaluationEntity evaluation) {
        if (em.contains(evaluation)) {
            em.remove(evaluation);
        } else {
            EvaluationEntity attached = em.merge(evaluation);
            em.remove(attached);
        }
    }



    public EvaluationEntity findById(Long id) {
        TypedQuery<EvaluationEntity> query = em.createQuery(
                "SELECT e FROM EvaluationEntity e WHERE e.id = :id",
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
                        "AND e.state = aor.projetofinal.entity.enums.EvaluationStateType.CLOSED " +
                        "AND e.cycle.active = false " +
                        "ORDER BY e.date DESC", // most recent evaluations first
                EvaluationEntity.class
        );

        query.setParameter("evaluated", evaluated);

        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList();
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
                                                                      EvaluationStateType state,
                                                                      Integer grade,
                                                                      LocalDate cycleEnd,
                                                                      UserEntity requester,
                                                                      int page,
                                                                      int pageSize) {

        StringBuilder jpql = new StringBuilder("SELECT e FROM EvaluationEntity e WHERE 1=1");

        // Only confirmed & active evaluated users
        jpql.append(" AND e.evaluated.confirmed = true AND e.evaluated.active = true");

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
            jpql.append(" AND e.grade.grade = :grade");
        }

        // Filter by cycle end date
        if (cycleEnd != null) {
            jpql.append(" AND DATE(e.cycle.endDate) = :cycleEnd");
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
            query.setParameter("grade", grade);
        }

        if (cycleEnd != null) {
            query.setParameter("cycleEnd", cycleEnd);
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
