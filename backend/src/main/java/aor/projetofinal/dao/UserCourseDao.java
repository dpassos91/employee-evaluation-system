package aor.projetofinal.dao;

import aor.projetofinal.entity.UserCourseEntity;
import aor.projetofinal.entity.UserCourseIdEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object (DAO) for UserCourseEntity.
 * Provides database operations for user-course participation records,
 * including registration, history queries, and filtering by date or course.
 */
@ApplicationScoped
public class UserCourseDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * Deletes a user-course participation record by its composite ID.
     *
     * @param id The composite UserCourseIdEntity (userId + courseId).
     */
    @Transactional
    public void deleteById(UserCourseIdEntity id) {
        UserCourseEntity uc = em.find(UserCourseEntity.class, id);
        if (uc != null) {
            em.remove(uc);
        }
    }

    /**
     * Finds all user-course records for a given course.
     *
     * @param courseId The course ID.
     * @return List of UserCourseEntity records for the course.
     */
    public List<UserCourseEntity> findByCourseId(int courseId) {
        return em.createQuery(
                "SELECT uc FROM UserCourseEntity uc WHERE uc.course.id = :courseId", UserCourseEntity.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    /**
     * Finds all user-course records for a given user.
     *
     * @param userId The user ID.
     * @return List of UserCourseEntity records for the user.
     */
    public List<UserCourseEntity> findByUserId(int userId) {
        return em.createQuery(
                "SELECT uc FROM UserCourseEntity uc WHERE uc.user.id = :userId", UserCourseEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Finds all user-course records for a user within a specific year.
     * Useful for calculating the total training hours per year.
     *
     * @param userId The user ID.
     * @param year The year to filter by.
     * @return List of UserCourseEntity records for the user in the given year.
     */
    public List<UserCourseEntity> findByUserIdAndYear(int userId, int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        return em.createQuery(
                "SELECT uc FROM UserCourseEntity uc WHERE uc.user.id = :userId AND uc.participationDate >= :start AND uc.participationDate < :end",
                UserCourseEntity.class)
                .setParameter("userId", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    /**
     * Finds a user-course participation record by its composite ID.
     *
     * @param id The composite UserCourseIdEntity (userId + courseId).
     * @return The UserCourseEntity if found, or null.
     */
    public UserCourseEntity findById(UserCourseIdEntity id) {
        return em.find(UserCourseEntity.class, id);
    }

    /**
     * Persists a new user-course participation record.
     *
     * @param userCourse The UserCourseEntity to persist.
     */
    @Transactional
    public void save(UserCourseEntity userCourse) {
        em.persist(userCourse);
    }
}

