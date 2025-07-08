package aor.projetofinal.dao;

import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.enums.LanguageEnum;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for CourseEntity.
 * Provides database operations for training courses,
 * including CRUD and flexible filtering.
 */
@ApplicationScoped
public class CourseDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * Marks a course as inactive ("soft delete").
     *
     * @param id The course ID.
     */
    @Transactional
    public void deactivate(int id) {
        CourseEntity course = findById(id);
        if (course != null) {
            course.setActive(false);
            em.merge(course);
        }
    }

    /**
     * Retrieves all courses in the system (active and inactive).
     *
     * @return List of all CourseEntity objects.
     */
    public List<CourseEntity> findAll() {
        return em.createQuery("SELECT c FROM CourseEntity c", CourseEntity.class)
                .getResultList();
    }

    /**
     * Retrieves all active courses (can be assigned to users).
     *
     * @return List of active CourseEntity objects.
     */
    public List<CourseEntity> findActive() {
        return em.createQuery("SELECT c FROM CourseEntity c WHERE c.active = true", CourseEntity.class)
                .getResultList();
    }

    /**
     * Finds courses matching the provided filters.
     * Any filter can be null (or empty for strings), in which case it is ignored.
     *
     * @param name The course name (partial match, case insensitive).
     * @param minTimeSpan Minimum duration in hours (inclusive).
     * @param maxTimeSpan Maximum duration in hours (inclusive).
     * @param language The language (enum).
     * @param category The course category (enum).
     * @param active Only include active/inactive courses (if not null).
     * @return List of CourseEntity objects matching the filters.
     */
    public List<CourseEntity> findByFilters(String name,
                                            Double minTimeSpan,
                                            Double maxTimeSpan,
                                            LanguageEnum language,
                                            CourseCategoryEnum category,
                                            Boolean active) {

        StringBuilder jpql = new StringBuilder("SELECT c FROM CourseEntity c WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            jpql.append(" AND LOWER(c.name) LIKE ?1");
            params.add("%" + name.toLowerCase() + "%");
        }
        if (minTimeSpan != null) {
            jpql.append(" AND c.timeSpan >= ?").append(params.size() + 1);
            params.add(minTimeSpan);
        }
        if (maxTimeSpan != null) {
            jpql.append(" AND c.timeSpan <= ?").append(params.size() + 1);
            params.add(maxTimeSpan);
        }
        if (language != null) {
            jpql.append(" AND c.language = ?").append(params.size() + 1);
            params.add(language);
        }
        if (category != null) {
            jpql.append(" AND c.courseCategory = ?").append(params.size() + 1);
            params.add(category);
        }
        if (active != null) {
            jpql.append(" AND c.active = ?").append(params.size() + 1);
            params.add(active);
        }

        TypedQuery<CourseEntity> query = em.createQuery(jpql.toString(), CourseEntity.class);

        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        return query.getResultList();
    }

    /**
     * Retrieves all courses in a given category.
     *
     * @param category The course category.
     * @return List of CourseEntity objects for the category.
     */
    public List<CourseEntity> findByCategory(CourseCategoryEnum category) {
        return em.createQuery(
                "SELECT c FROM CourseEntity c WHERE c.courseCategory = :category", CourseEntity.class)
                .setParameter("category", category)
                .getResultList();
    }

    /**
     * Retrieves all courses in a given language.
     *
     * @param language The language.
     * @return List of CourseEntity objects for the language.
     */
    public List<CourseEntity> findByLanguage(LanguageEnum language) {
        return em.createQuery(
                "SELECT c FROM CourseEntity c WHERE c.language = :language", CourseEntity.class)
                .setParameter("language", language)
                .getResultList();
    }

    /**
     * Retrieves all courses with a duration within a given range.
     *
     * @param minTimeSpan Minimum duration in hours (inclusive).
     * @param maxTimeSpan Maximum duration in hours (inclusive).
     * @return List of CourseEntity objects within the duration range.
     */
    public List<CourseEntity> findByTimeSpanRange(double minTimeSpan, double maxTimeSpan) {
        return em.createQuery(
                "SELECT c FROM CourseEntity c WHERE c.timeSpan >= :min AND c.timeSpan <= :max", CourseEntity.class)
                .setParameter("min", minTimeSpan)
                .setParameter("max", maxTimeSpan)
                .getResultList();
    }

    /**
     * Finds a course by its unique ID.
     *
     * @param id The course ID.
     * @return The CourseEntity if found, or null.
     */
    public CourseEntity findById(int id) {
        return em.find(CourseEntity.class, id);
    }

    /**
     * Persists a new course in the database.
     *
     * @param course The CourseEntity to persist.
     */
    @Transactional
    public void save(CourseEntity course) {
        em.persist(course);
    }

    /**
     * Updates an existing course in the database.
     *
     * @param course The CourseEntity to update.
     * @return The managed CourseEntity.
     */
    @Transactional
    public CourseEntity update(CourseEntity course) {
        return em.merge(course);
    }
}

