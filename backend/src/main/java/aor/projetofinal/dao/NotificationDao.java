package aor.projetofinal.dao;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.NotificationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.NotificationEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

import java.util.EnumMap;
import java.util.List;

/**
 * Data Access Object for NotificationEntity.
 * Provides basic CRUD and custom queries for notifications.
 * All logs use RequestContext for author and IP.
 */
@Stateless
public class NotificationDao {

    private static final Logger logger = LogManager.getLogger(NotificationDao.class);

    @PersistenceContext(unitName = "grupo7")
    private EntityManager em;

    /**
     * Counts the number of unread notifications for a given user, grouped by notification type.
     * Returns a map where all NotificationType values are present, even if count is zero.
     *
     * @param user the user entity to count unread notifications for
     * @return a map of NotificationType to unread count
     */
    public Map<NotificationEnum, Integer> countUnreadByType(UserEntity user) {
        // JPQL query to group unread notifications by type
        List<Object[]> results = em.createQuery(
                "SELECT n.type, COUNT(n) FROM NotificationEntity n " +
                        "WHERE n.user = :user AND n.read = false GROUP BY n.type", Object[].class)
                .setParameter("user", user)
                .getResultList();

        // Use EnumMap for efficient mapping with enums
        Map<NotificationEnum, Integer> counts = new EnumMap<>(NotificationEnum.class);

        // Initialize all types with zero to ensure a consistent response
        for (NotificationEnum type : NotificationEnum.values()) {
            counts.put(type, 0);
        }
        // Populate map with actual counts from query
        for (Object[] row : results) {
            NotificationEnum type = (NotificationEnum) row[0];
            Long count = (Long) row[1];
            counts.put(type, count.intValue());
        }
        logger.info("User: {} | IP: {} - Unread notification counts by type: {} for UserId: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                counts,
                user != null ? user.getId() : null);
        return counts;
    }

    /**
 * Finds all unread notifications for a specific user, excluding a specified notification type.
 * This method is typically used to retrieve only non-MESSAGE notifications for UI dropdowns.
 *
 * @param user the recipient user
 * @param excludeType the notification type to exclude (e.g., NotificationType.MESSAGE)
 * @return list of unread NotificationEntity objects excluding the specified type
 */
public List<NotificationEntity> findUnreadByUserExcludingType(UserEntity user, NotificationEnum excludeType) {
    return em.createQuery(
        "SELECT n FROM NotificationEntity n WHERE n.user = :user AND n.read = false AND n.type <> :excludeType ORDER BY n.createdAt DESC",
        NotificationEntity.class
    )
    .setParameter("user", user)
    .setParameter("excludeType", excludeType)
    .getResultList();
}

    /**
     * Persists a new NotificationEntity.
     *
     * @param notification the NotificationEntity to persist
     */
    public void save(NotificationEntity notification) {
        try {
            em.persist(notification);
            logger.info("User: {} | IP: {} - Notification persisted. NotificationId: {}, UserId: {}",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    notification.getId(),
                    notification.getUser() != null ? notification.getUser().getId() : null);
        } catch (Exception e) {
            logger.error("User: {} | IP: {} - Error persisting notification: {}",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all notifications for a specific user, ordered by newest first.
     *
     * @param user the recipient user
     * @return list of notifications
     */
    public List<NotificationEntity> findByUser(UserEntity user) {
        List<NotificationEntity> notifications = em.createQuery(
                "SELECT n FROM NotificationEntity n WHERE n.user = :user ORDER BY n.createdAt DESC",
                NotificationEntity.class
        ).setParameter("user", user)
         .getResultList();
        logger.info("User: {} | IP: {} - Fetched {} notifications for UserId: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                notifications.size(),
                user != null ? user.getId() : null);
        return notifications;
    }

    /**
     * Finds unread notifications for a specific user.
     *
     * @param user the recipient user
     * @return list of unread notifications
     */
    public List<NotificationEntity> findUnreadByUser(UserEntity user) {
        List<NotificationEntity> notifications = em.createQuery(
                "SELECT n FROM NotificationEntity n WHERE n.user = :user AND n.read = false ORDER BY n.createdAt DESC",
                NotificationEntity.class
        ).setParameter("user", user)
         .getResultList();
        logger.info("User: {} | IP: {} - Fetched {} unread notifications for UserId: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                notifications.size(),
                user != null ? user.getId() : null);
        return notifications;
    }

    /**
     * Marks all notifications as read for a given user.
     *
     * @param user the user
     * @return number of notifications updated
     */
    public int markAllAsRead(UserEntity user) {
        int updated = em.createQuery(
                "UPDATE NotificationEntity n SET n.read = true WHERE n.user = :user AND n.read = false"
        ).setParameter("user", user)
         .executeUpdate();
        logger.info("User: {} | IP: {} - Marked {} notifications as read for UserId: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                updated,
                user != null ? user.getId() : null);
        return updated;
    }

/**
 * Marks all MESSAGE-type notifications as read for a given user.
 * This method is typically used when the user opens the chat/messages section,
 * ensuring that the badge/count for message notifications is reset to zero.
 *
 * @param user the user whose MESSAGE notifications should be marked as read
 * @return the number of notifications updated in the database
 */
public int markAllMessageNotificationsAsRead(UserEntity user) {
    // Perform a bulk update for all unread MESSAGE-type notifications for this user
    int updated = em.createQuery(
        "UPDATE NotificationEntity n SET n.read = true " +
        "WHERE n.user = :user AND n.read = false AND n.type = :type"
    )
    .setParameter("user", user)
    .setParameter("type", NotificationEnum.MESSAGE)
    .executeUpdate();
    return updated;
}
    
    /**
     * Finds a notification by its id.
     *
     * @param id the notification id
     * @return the NotificationEntity or null if not found
     */
    public NotificationEntity findById(int id) {
        NotificationEntity notification = em.find(NotificationEntity.class, id);
        logger.info("User: {} | IP: {} - Searched for NotificationId: {}. Found: {}.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                id,
                notification != null);
        return notification;
    }

    /**
     * Updates a notification entity.
     *
     * @param notification the notification to update
     */
    public void update(NotificationEntity notification) {
        try {
            em.merge(notification);
            logger.info("User: {} | IP: {} - Notification updated. NotificationId: {}, UserId: {}",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    notification.getId(),
                    notification.getUser() != null ? notification.getUser().getId() : null);
        } catch (Exception e) {
            logger.error("User: {} | IP: {} - Error updating notification: {}",
                    RequestContext.getAuthor(),
                    RequestContext.getIp(),
                    e.getMessage());
            throw e;
        }
    }
}

