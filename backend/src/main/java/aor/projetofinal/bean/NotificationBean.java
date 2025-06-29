package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.NotificationDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.NotificationDto;
import aor.projetofinal.entity.NotificationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.NotificationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for notifications.
 * All logs use RequestContext for author and IP.
 */
@Stateless
public class NotificationBean {

    private static final Logger logger = LogManager.getLogger(NotificationBean.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    private NotificationDao notificationDao;

    @Inject
    private UserDao userDao;

    /**
     * Creates and saves a new notification for a user.
     *
     * @param userId recipient user ID
     * @param type notification type (enum)
     * @param message notification message/content
     * @return true if notification created, false otherwise
     */
    public boolean createNotification(Integer userId, String type, String message) {
        try {
            UserEntity user = userDao.findById(userId);
            if (user == null) {
                logger.warn("User: {} | IP: {} - Attempted to create notification for non-existent user (userId: {}). Operation aborted.",
                        RequestContext.getAuthor(), RequestContext.getIp(), userId);
                return false;
            }
            NotificationEntity notification = new NotificationEntity();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setRead(false);

            // Handle type conversion
            try {
                NotificationType notifType = NotificationType.valueOf(type.toUpperCase());
                notification.setType(notifType);
            } catch (Exception e) {
                logger.warn("User: {} | IP: {} - Invalid notification type '{}'. Operation aborted.", RequestContext.getAuthor(), RequestContext.getIp(), type);
                return false;
            }

            notificationDao.save(notification);
            logger.info("User: {} | IP: {} - Notification created. UserId: {}, Type: {}, Message: '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), userId, type, message);
            return true;
        } catch (Exception e) {
            logger.error("User: {} | IP: {} - Error creating notification: {}", RequestContext.getAuthor(), RequestContext.getIp(), e.getMessage());
            return false;
        }
    }

    /**
     * Gets all notifications for a user, ordered by date.
     *
     * @param userId recipient user ID
     * @return list of NotificationDto
     */
    public List<NotificationDto> getNotificationsForUser(Integer userId) {
        UserEntity user = userDao.findById(userId);
        if (user == null) {
            logger.warn("User: {} | IP: {} - Tried to get notifications for non-existent user (userId: {}).", RequestContext.getAuthor(), RequestContext.getIp(), userId);
            return List.of();
        }
        List<NotificationEntity> entities = notificationDao.findByUser(user);
        List<NotificationDto> dtos = new ArrayList<>();
        for (NotificationEntity entity : entities) {
            dtos.add(toDto(entity));
        }
        logger.info("User: {} | IP: {} - Fetched {} notifications for userId: {}.", RequestContext.getAuthor(), RequestContext.getIp(), dtos.size(), userId);
        return dtos;
    }

    /**
     * Gets unread notifications for a user.
     *
     * @param userId recipient user ID
     * @return list of NotificationDto
     */
    public List<NotificationDto> getUnreadNotificationsForUser(Integer userId) {
        UserEntity user = userDao.findById(userId);
        if (user == null) {
            logger.warn("User: {} | IP: {} - Tried to get unread notifications for non-existent user (userId: {}).", RequestContext.getAuthor(), RequestContext.getIp(), userId);
            return List.of();
        }
        List<NotificationEntity> entities = notificationDao.findUnreadByUser(user);
        List<NotificationDto> dtos = new ArrayList<>();
        for (NotificationEntity entity : entities) {
            dtos.add(toDto(entity));
        }
        logger.info("User: {} | IP: {} - Fetched {} unread notifications for userId: {}.", RequestContext.getAuthor(), RequestContext.getIp(), dtos.size(), userId);
        return dtos;
    }

    /**
     * Marks all notifications as read for a given user.
     *
     * @param userId recipient user ID
     * @return number of notifications marked as read
     */
    public int markAllNotificationsAsRead(Integer userId) {
        UserEntity user = userDao.findById(userId);
        if (user == null) {
            logger.warn("User: {} | IP: {} - Tried to mark notifications as read for non-existent user (userId: {}).", RequestContext.getAuthor(), RequestContext.getIp(), userId);
            return 0;
        }
        int updated = notificationDao.markAllAsRead(user);
        logger.info("User: {} | IP: {} - Marked {} notifications as read for userId: {}.", RequestContext.getAuthor(), RequestContext.getIp(), updated, userId);
        return updated;
    }

    /**
     * Converts a NotificationEntity to NotificationDto.
     *
     * @param entity the NotificationEntity
     * @return NotificationDto
     */
    public NotificationDto toDto(NotificationEntity entity) {
        if (entity == null) {
            logger.warn("User: {} | IP: {} - Attempted to convert null NotificationEntity to DTO.", RequestContext.getAuthor(), RequestContext.getIp());
            return null;
        }
        NotificationDto dto = new NotificationDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(FORMATTER) : null);
        return dto;
    }
}

