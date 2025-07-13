package aor.projetofinal.service;

import aor.projetofinal.bean.ProfileBean;
import aor.projetofinal.bean.SettingsBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.SettingsEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.ProfileDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/settings")
public class SettingsService {


    @Inject
    private SessionTokenDao sessionTokenDao;

    @Inject
    private SettingsBean settingsBean;



    private static final Logger logger = LogManager.getLogger(SettingsService.class);


    /**
     * Retrieves the system timeout configuration values for confirmation tokens,
     * recovery tokens, and session tokens. Only accessible by administrators.
     *
     * Performs session validation and role-based access control. Logs all access attempts,
     * including unauthorized and forbidden actions, with audit metadata from RequestContext.
     *
     * @param token The session token provided in the request header.
     * @return A Response containing the timeout values wrapped in a SettingsDTO, or an error message if unauthorized.
     */
    @GET
    @Path("/timeouts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimeouts(@HeaderParam("sessionToken") String token) {
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);

        if (session == null || session.getUser() == null) {
            logger.warn("IP: {} - Unauthorized access attempt to settings (invalid or missing session token).",
                    RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired or invalid.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity user = session.getUser();
        String role = user.getRole().getName().toUpperCase();

        if (!role.equals("ADMIN")) {
            logger.warn("User: {} | IP: {} - Access denied to settings (not an admin).",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only administrators can access settings.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Retrieved system timeout settings.",
                RequestContext.getAuthor(), RequestContext.getIp());

        SettingsEntity settings = settingsBean.getSettings();

        SettingsDTO dto = new SettingsDTO();
        dto.setConfirmationTokenTimeout(settings.getConfirmationTokenTimeout());
        dto.setRecoveryTokenTimeout(settings.getRecoveryTokenTimeout());
        dto.setSessionTokenTimeout(settings.getSessionTokenTimeout());

        return Response.ok(dto).build();
    }



    /**
     * Updates the confirmation token timeout (in minutes) in the system settings.
     *
     * Only accessible by administrators with a valid session token.
     *
     * @param token The session token from the request header.
     * @param dto The settings DTO containing the new timeout value.
     * @return HTTP response indicating success or failure.
     */
    @PUT
    @Path("/confirmation-timeout")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateConfirmationTimeout(@HeaderParam("sessionToken") String token, int minutes) {
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);
        if (session == null || session.getUser() == null) {
            logger.warn("IP: {} - Unauthorized attempt to update confirmation timeout.", RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired or invalid.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity user = session.getUser();
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            logger.warn("User: {} | IP: {} - Forbidden: tried to update confirmation timeout.", user.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only administrators can update this setting.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = settingsBean.updateConfirmationTokenTimeout(minutes);
        if (success) {
            logger.info("User: {} | IP: {} - Successfully updated confirmation timeout to {} minutes.",
                    RequestContext.getAuthor(), RequestContext.getIp(), minutes);
            return Response.ok("{\"message\": \"Confirmation timeout updated successfully.\"}").build();
        } else {
            logger.error("User: {} | IP: {} - Failed to update confirmation timeout.", RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Failed to update confirmation timeout.\"}")
                    .build();
        }
    }



    /**
     * Updates the recovery token timeout (in minutes) in the system settings.
     */
    @PUT
    @Path("/recovery-timeout")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRecoveryTimeout(@HeaderParam("sessionToken") String token, int minutes) {
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);
        if (session == null || session.getUser() == null) {
            logger.warn("IP: {} - Unauthorized attempt to update recovery timeout.", RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired or invalid.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity user = session.getUser();
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            logger.warn("User: {} | IP: {} - Forbidden: tried to update recovery timeout.", user.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only administrators can update this setting.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = settingsBean.updateRecoveryTokenTimeout(minutes);
        if (success) {
            logger.info("User: {} | IP: {} - Successfully updated recovery timeout to {} minutes.",
                    RequestContext.getAuthor(), RequestContext.getIp(), minutes);
            return Response.ok("{\"message\": \"Recovery timeout updated successfully.\"}").build();
        } else {
            logger.error("User: {} | IP: {} - Failed to update recovery timeout.", RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Failed to update recovery timeout.\"}")
                    .build();
        }
    }



    /**
     * Updates the session token timeout (in minutes) in the system settings.
     */
    @PUT
    @Path("/session-timeout")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSessionTimeout(@HeaderParam("sessionToken") String token, int minutes) {
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);
        if (session == null || session.getUser() == null) {
            logger.warn("IP: {} - Unauthorized attempt to update session timeout.", RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired or invalid.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity user = session.getUser();
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            logger.warn("User: {} | IP: {} - Forbidden: tried to update session timeout.", user.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only administrators can update this setting.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean success = settingsBean.updateSessionTimeoutMinutes(minutes);
        if (success) {
            logger.info("User: {} | IP: {} - Successfully updated session timeout to {} minutes.",
                    RequestContext.getAuthor(), RequestContext.getIp(), minutes);
            return Response.ok("{\"message\": \"Session timeout updated successfully.\"}").build();
        } else {
            logger.error("User: {} | IP: {} - Failed to update session timeout.", RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Failed to update session timeout.\"}")
                    .build();
        }
    }








}