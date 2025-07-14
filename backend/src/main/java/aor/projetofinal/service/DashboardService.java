package aor.projetofinal.service;

import aor.projetofinal.bean.DashboardBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.dto.DashboardDto;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.context.RequestContext;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST service for providing dashboard summary data to authenticated users.
 * Aggregates metrics and quick info for users, managers, and admins.
 */
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardService {

    private static final Logger logger = LogManager.getLogger(DashboardService.class);

    @Inject
    private DashboardBean dashboardBean;

    @Inject
    private UserBean userBean;

    /**
     * Returns the dashboard summary for the authenticated user.
     * Sets request context for audit logs and security tracking.
     *
     * @param sessionToken The session token identifying the user.
     * @return HTTP 200 and dashboard summary if authorized; HTTP 401 if unauthorized.
     */
    @GET
    @Path("/summary")
    public Response getDashboardSummary(@HeaderParam("sessionToken") String sessionToken) {
        // Identify authenticated user using session token
        UserEntity currentUser = userBean.findUserBySessionToken(sessionToken);

        // Set request context for enterprise-level logging (author and IP)
        if (currentUser != null) {
            RequestContext.setAuthor(currentUser.getEmail());
        }
        // You should implement this method to extract the real client IP from the request, if required
        RequestContext.setIp(RequestContext.getIp());

        // Log access to dashboard summary (enterprise logging)
        logger.info("User: {} | IP: {} - Requested dashboard summary.",
                RequestContext.getAuthor(), RequestContext.getIp());

        // Handle unauthorized access
        if (currentUser == null) {
            logger.warn("Unauthorized dashboard summary request. Invalid or expired session token. IP: {}",
                    RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Aggregate dashboard summary data
        DashboardDto dto = dashboardBean.getDashboardForUser(currentUser);

        // Log successful delivery
        logger.info("User: {} | IP: {} - Dashboard summary delivered successfully.",
                RequestContext.getAuthor(), RequestContext.getIp());

        return Response.ok(dto).build();
    }
}
