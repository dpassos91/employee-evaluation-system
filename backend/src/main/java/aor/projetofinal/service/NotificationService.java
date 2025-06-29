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
    public Response getMyNotifications(@Context SecurityContext securityContext) {
        try {
            Integer userId = getCurrentUserId(securityContext);
            if (userId == null) {
                logger.warn("User: {} | IP: {} - Attempted to get notifications while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
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
    public Response getMyUnreadNotifications(@Context SecurityContext securityContext) {
        try {
            Integer userId = getCurrentUserId(securityContext);
            if (userId == null) {
                logger.warn("User: {} | IP: {} - Attempted to get unread notifications while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
            List<NotificationDto> notifications = notificationBean.getUnreadNotificationsForUser(userId);
            logger.info("User: {} | IP: {} - Fetched {} unread notifications.",
                    RequestContext.getAuthor(), RequestContext.getIp(), notifications.size());
            return Response.ok(notifications).build();
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
    public Response markAllAsRead(@Context SecurityContext securityContext) {
        try {
            Integer userId = getCurrentUserId(securityContext);
            if (userId == null) {
                logger.warn("User: {} | IP: {} - Attempted to mark notifications as read while not authenticated.",
                        RequestContext.getAuthor(), RequestContext.getIp());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"User not authenticated.\"}")
                        .build();
            }
            int updated = notificationBean.markAllNotificationsAsRead(userId);
            logger.info("User: {} | IP: {} - Marked {} notifications as read.",
                    RequestContext.getAuthor(), RequestContext.getIp(), updated);
            return Response.ok("{\"updated\":" + updated + "}").build();
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * Helper to extract the current user ID from the SecurityContext or RequestContext.
     * Adapt this to your authentication logic if needed.
     */
    private Integer getCurrentUserId(SecurityContext securityContext) {
        if (RequestContext.getCurrentUser() != null) {
            return RequestContext.getCurrentUser().getId();
        }
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            try {
                return Integer.parseInt(securityContext.getUserPrincipal().getName());
            } catch (NumberFormatException ignore) {}
        }
        return null;
    }
}
