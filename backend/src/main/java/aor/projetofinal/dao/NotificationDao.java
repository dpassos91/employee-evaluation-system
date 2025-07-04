package aor.projetofinal.dao;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.entity.NotificationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.NotificationType;
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
    public Map<NotificationType, Integer> countUnreadByType(UserEntity user) {
        // JPQL query to group unread notifications by type
        List<Object[]> results = em.createQuery(
                "SELECT n.type, COUNT(n) FROM NotificationEntity n " +
                        "WHERE n.user = :user AND n.read = false GROUP BY n.type", Object[].class)
                .setParameter("user", user)
                .getResultList();

        // Use EnumMap for efficient mapping with enums
        Map<NotificationType, Integer> counts = new EnumMap<>(NotificationType.class);

        // Initialize all types with zero to ensure a consistent response
        for (NotificationType type : NotificationType.values()) {
            counts.put(type, 0);
        }
        // Populate map with actual counts from query
        for (Object[] row : results) {
            NotificationType type = (NotificationType) row[0];
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

