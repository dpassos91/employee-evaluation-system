package aor.projetofinal.service;

import aor.projetofinal.bean.NotificationBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.NotificationDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.NotificationType;
import java.util.Map;

import java.util.List;

/**
 * REST service for notification-related operations.
 * All logs use RequestContext for author and IP context.
 */
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    @Inject
    private NotificationBean notificationBean;

    /**
     * Gets all notifications for the current user.
     *
     * @return list of NotificationDto
     */
    @GET
    public Response getMyNotifications() {
        try {
            if (RequestContext.getCurrentUser() == null) {
                logger.warn("User: {} | IP: {} - Attempted to get notifications while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
            Integer userId = RequestContext.getCurrentUser().getId();
            List<NotificationDto> notifications = notificationBean.getNotificationsForUser(userId);
            logger.info("User: {} | IP: {} - Fetched {} notifications.",
                    RequestContext.getAuthor(), RequestContext.getIp(), notifications.size());
            return Response.ok(notifications).build();
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * Gets all unread notifications for the current user.
     *
     * @return list of NotificationDto
     */
    @GET
    @Path("/unread")
    public Response getMyUnreadNotifications() {
        try {
            if (RequestContext.getCurrentUser() == null) {
                logger.warn("User: {} | IP: {} - Attempted to get unread notifications while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
            Integer userId = RequestContext.getCurrentUser().getId();
            List<NotificationDto> notifications = notificationBean.getUnreadNotificationsForUser(userId);
            logger.info("User: {} | IP: {} - Fetched {} unread notifications.",
                    RequestContext.getAuthor(), RequestContext.getIp(), notifications.size());
            return Response.ok(notifications).build();
        } finally {
            RequestContext.clear();
        }
    }

        /**
     * Gets the count of unread notifications for the current user, grouped by notification type.
     *
     * @return HTTP 200 com um JSON (mapa tipo -> count), ou 401 se não autenticado.
     */
    @GET
    @Path("/unread/count-by-type")
    public Response getUnreadNotificationCountsByType() {
        try {
            if (RequestContext.getCurrentUser() == null) {
                logger.warn("User: {} | IP: {} - Attempted to get unread notification counts while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
            Integer userId = RequestContext.getCurrentUser().getId();
            Map<NotificationType, Integer> counts = notificationBean.countUnreadNotificationsByType(userId);
            logger.info("User: {} | IP: {} - Unread notification counts: {}", RequestContext.getAuthor(), RequestContext.getIp(), counts);
            return Response.ok(counts).build();
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * Marks all notifications as read for the current user.
     *
     * @return JSON with number of notifications updated
     */
    @PUT
    @Path("/read")
    public Response markAllAsRead() {
        try {
            if (RequestContext.getCurrentUser() == null) {
                logger.warn("User: {} | IP: {} - Attempted to mark notifications as read while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
            Integer userId = RequestContext.getCurrentUser().getId();
            int updated = notificationBean.markAllNotificationsAsRead(userId);
            logger.info("User: {} | IP: {} - Marked {} notifications as read.",
                    RequestContext.getAuthor(), RequestContext.getIp(), updated);
            return Response.ok("{\"updated\":" + updated + "}").build();
        } finally {
            RequestContext.clear();
        }
    }

    /**
 * Marks all MESSAGE-type notifications as read for the current user.
 * This endpoint should be called when the user enters the chat/messages section,
 * ensuring that the message notification badge/count is reset.
 *
 * @return a JSON object indicating the number of notifications updated
 */
@PUT
@Path("/read/message")
public Response markAllMessageNotificationsAsRead() {
    UserEntity user = RequestContext.getCurrentUser();
    int updated = notificationBean.markAllMessageNotificationsAsRead(user.getId());
    return Response.ok("{\"updated\":" + updated + "}").build();
}

}
