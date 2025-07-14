package aor.projetofinal.dao;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.UserCourseEntity;
import aor.projetofinal.entity.UserCourseIdEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


    private static final Logger logger = LogManager.getLogger(UserCourseDao.class);


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
     * Retrieves all course participations for a given user,
     * ordered by the participation date in ascending order.
     * <p>
     * This method logs the access attempt using RequestContext metadata.
     *
     * @param userId ID of the user to retrieve participation records for
     * @return List of UserCourseEntity objects for the specified user
     */
    public List<UserCourseEntity> findAllParticipationsInCoursesByUserId(int userId) {
        Logger logger = LogManager.getLogger(UserCourseDao.class);

        logger.info("User: {} | IP: {} - Fetching all course participations for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);

        return em.createQuery(
                        "SELECT uc FROM UserCourseEntity uc WHERE uc.user.id = :userId ORDER BY uc.participationDate",
                        UserCourseEntity.class)
                .setParameter("userId", userId)
                .getResultList();
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
     * Retrieves all distinct years in which the specified user has participated in training courses.
     * Results are ordered chronologically.
     *
     * @param userId ID of the user
     * @return List of distinct years (as integers) where the user has course participation
     */
    public List<Integer> findDistinctParticipationYearsByUserId(int userId) {
        Logger logger = LogManager.getLogger(UserCourseDao.class);

        logger.info("User: {} | IP: {} - Fetching distinct participation years for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);

        return em.createQuery(
                        "SELECT DISTINCT YEAR(uc.participationDate) " +
                                "FROM UserCourseEntity uc " +
                                "WHERE uc.user.id = :userId " +
                                "ORDER BY YEAR(uc.participationDate)",
                        Integer.class)
                .setParameter("userId", userId)
                .getResultList();
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

