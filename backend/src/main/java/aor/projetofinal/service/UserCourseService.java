package aor.projetofinal.service;

import aor.projetofinal.bean.UserBean;
import aor.projetofinal.bean.UserCourseBean;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.context.RequestContext;

import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST Service for managing user-course participation.
 * Managers or administrators can register participation.
 * Any user can consult or export their own training history.
 */
@Path("/user-courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserCourseService {

    private static final Logger logger = LogManager.getLogger(UserCourseService.class);

    @jakarta.inject.Inject
    private UserCourseBean userCourseBean;

    @Inject
    UserBean userBean;

    @Inject
    private SessionTokenDao sessionTokenDao;

    @Inject
    private UserDao userDao;




    /**
     * Registers a user's participation in a specific course.
     *
     * Accessible by:
     * - The user themselves
     * - Their direct manager
     * - An administrator
     *
     * @param dto   Data containing user ID, course ID, and participation date
     * @param token Session token for authentication and authorization
     * @return      HTTP Response indicating success or failure of registration
     */
    @POST
    public Response addUserCourse(CreateUserCourseDto dto, @HeaderParam("sessionToken") String token) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Attempting to register course {} for user {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), dto.getCourseId(), dto.getUserId());

        // 1. Validate session token
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);
        if (sessionStatus == null) {
            logger.warn("User: {} | IP: {} - Session expired while registering course for user {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 2. Check if target user exists
        UserEntity targetUser = userDao.findById(dto.getUserId());
        if (targetUser == null) {
            logger.warn("User: {} | IP: {} - Attempted to register course for non-existent user ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Target user not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 3. Authorization check: Admin, self, or user's manager
        boolean isAdmin = currentUser.getRole().getName().equalsIgnoreCase("admin");
        boolean isSelf = currentUser.getId() == targetUser.getId();
        boolean isManager = targetUser.getManager() != null &&
                targetUser.getManager().getId() == currentUser.getId();

        if (!isAdmin && !isSelf && !isManager) {
            logger.warn("User: {} | IP: {} - Unauthorized attempt to assign course to user ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Access denied. Only admins, the user, or their manager may register courses.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 4. Authorized - proceed with registration
        logger.info("User: {} | IP: {} - Authorized to assign course {} to user {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), dto.getCourseId(), dto.getUserId());

        try {
            userCourseBean.addUserCourse(dto);
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("User: {} | IP: {} - Failed to assign course: {}",
                    RequestContext.getAuthor(), RequestContext.getIp(), e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }


    @GET
    @Path("/user/{userId}")
    public Response getUserCourses(@PathParam("userId") int userId) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Fetched training history for user {}.",
                currentUser.getEmail(), RequestContext.getIp(), userId);

        List<UserCourseDto> history = userCourseBean.listUserCourses(userId);
        return Response.ok(history).build();
    }

    @GET
    @Path("/user/{userId}/export/csv")
    @Produces("text/csv")
    public Response exportUserCoursesCsv(@PathParam("userId") int userId) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Exported training history to CSV for user {}.",
                currentUser.getEmail(), RequestContext.getIp(), userId);

        List<UserCourseDto> history = userCourseBean.listUserCourses(userId);
        StringBuilder csv = new StringBuilder("Course,Duration,Language,Category,Date\n");
        for (UserCourseDto dto : history) {
            csv.append(dto.getCourseName()).append(",")
               .append(dto.getTimeSpan()).append(",")
               .append(dto.getLanguage()).append(",")
               .append(dto.getCourseCategory()).append(",")
               .append(dto.getParticipationDate()).append("\n");
        }
        return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=\"user_courses_" + userId + ".csv\"")
                .build();
    }

    @GET
    @Path("/user/{userId}/year/{year}")
    public Response getUserCoursesByYear(@PathParam("userId") int userId, @PathParam("year") int year) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Fetched training history for user {} in year {}.",
                currentUser.getEmail(), RequestContext.getIp(), userId, year);

        List<UserCourseDto> history = userCourseBean.listUserCoursesByYear(userId, year);
        return Response.ok(history).build();
    }

    @GET
    @Path("/user/{userId}/year/{year}/export/csv")
    @Produces("text/csv")
    public Response exportUserCoursesByYearCsv(@PathParam("userId") int userId, @PathParam("year") int year) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Exported training history to CSV for user {} in year {}.",
                currentUser.getEmail(), RequestContext.getIp(), userId, year);

        List<UserCourseDto> history = userCourseBean.listUserCoursesByYear(userId, year);
        StringBuilder csv = new StringBuilder("Course,Duration,Language,Category,Date\n");
        for (UserCourseDto dto : history) {
            csv.append(dto.getCourseName()).append(",")
               .append(dto.getTimeSpan()).append(",")
               .append(dto.getLanguage()).append(",")
               .append(dto.getCourseCategory()).append(",")
               .append(dto.getParticipationDate()).append("\n");
        }
        return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=\"user_courses_" + userId + "_" + year + ".csv\"")
                .build();
    }


    /**
     * REST endpoint to retrieve the distinct years in which a user has participated in courses.
     * Accessible by:
     * - the user themselves
     * - their assigned manager
     * - an administrator
     *
     * @param userId the ID of the target user
     * @param token  the session token for authentication
     * @return list of distinct years as integers (e.g., [2022, 2023, 2024])
     */
    @GET
    @Path("/user/{userId}/years")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getParticipationYearsByUser(@PathParam("userId") int userId,
                                                @HeaderParam("sessionToken") String token) {

        logger.info("User: {} | IP: {} - Requesting participation years for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);

        // 1. Validate and refresh session token
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);
        if (sessionStatus == null) {
            logger.warn("User: {} | IP: {} - Session expired while requesting participation years for user ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), userId);

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 2. Validate token and user
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("IP: {} - Invalid or expired session token when accessing participation years for user ID {}.",
                    RequestContext.getIp(), userId);

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();
        UserEntity targetUser = userDao.findById(userId);

        if (targetUser == null) {
            logger.warn("User: {} | IP: {} - Target user ID {} not found.",
                    requester.getEmail(), RequestContext.getIp(), userId);

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 3. Authorization
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("admin");
        boolean isSelf = requester.getId() == targetUser.getId();
        boolean isManager = targetUser.getManager() != null &&
                targetUser.getManager().getId() == requester.getId();

        if (!isAdmin && !isSelf && !isManager) {
            logger.warn("User: {} | IP: {} - Forbidden access to participation years for user ID {}.",
                    requester.getEmail(), RequestContext.getIp(), userId);

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Access denied. You must be admin, the user, or their manager.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 4. Build response
        logger.info("User: {} | IP: {} - Authorized access. Fetching participation years for user ID {}.",
                requester.getEmail(), RequestContext.getIp(), userId);

        List<Integer> years = userCourseBean.listParticipationYearsByUser(userId);
        return Response.ok(years).build();
    }





    /**
     * REST endpoint to retrieve the yearly training summary (total hours per year)
     * for a specific user. Accessible by:
     * - the user themselves
     * - their assigned manager
     * - an administrator
     *
     * @param dto   the request body containing the target user's ID
     * @param token the session token passed in the header for authentication
     * @return HTTP response containing a list of UserCourseYearSummaryDto or an error message
     */
    @POST
    @Path("/user/summary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserCourseSummaryByYear(UserSummaryRequestDto dto,
                                               @HeaderParam("sessionToken") String token) {

        logger.info("User: {} | IP: {} - Requesting training summary by year for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), dto.getUserId());

        // 1. Validate and refresh session token
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);
        if (sessionStatus == null) {
            logger.warn("User: {} | IP: {} - Session expired while requesting summary for user ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 2. Validate session token and retrieve user
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("IP: {} - Invalid or expired session token when accessing summary for user ID {}.",
                    RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();
        UserEntity targetUser = userDao.findById(dto.getUserId());

        if (targetUser == null) {
            logger.warn("User: {} | IP: {} - Target user ID {} not found.",
                    requester.getEmail(), RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"User not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 3. Authorization: Admin, self, or user's manager
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("admin");
        boolean isSelf = requester.getId() == targetUser.getId();
        boolean isManager = targetUser.getManager() != null &&
                targetUser.getManager().getId() == requester.getId();

        if (!isAdmin && !isSelf && !isManager) {
            logger.warn("User: {} | IP: {} - Forbidden access to training summary of user ID {}.",
                    requester.getEmail(), RequestContext.getIp(), dto.getUserId());

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Access denied. You must be admin, the user, or their manager.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 4. Build response with summary
        logger.info("User: {} | IP: {} - Authorized access. Generating training summary for user ID {}.",
                requester.getEmail(), RequestContext.getIp(), dto.getUserId());

        List<UserCourseYearSummaryDto> summary = userCourseBean.summarizeUserCoursesByYear(dto.getUserId());
        return Response.ok(summary).build();
    }






}

