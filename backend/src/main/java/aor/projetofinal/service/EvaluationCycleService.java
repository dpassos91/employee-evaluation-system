package aor.projetofinal.service;


import aor.projetofinal.bean.EvaluationBean;
import aor.projetofinal.bean.EvaluationCycleBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dto.CreateCycleDto;
import aor.projetofinal.dto.UsersManagingThemselvesDto;
import aor.projetofinal.dto.UsersWithIncompleteEvaluationsDto;
import aor.projetofinal.dto.UsersWithoutManagerDto;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.util.DateValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/evaluations-cycles")
public class EvaluationCycleService {

    private static final Logger logger = LogManager.getLogger(EvaluationCycleService.class);


    @Inject
    private SessionTokenDao sessionTokenDao;

    @Inject
    UserBean userBean;

    @Inject
    private EvaluationBean evaluationBean;

    @Inject
    private EvaluationCycleBean evaluationCycleBean;


    /**
     * Creates a new evaluation cycle, if and only if all preconditions are met.
     *
     * Preconditions checked:
     * - Valid session token
     * - User is an administrator
     * - End date is valid and in the future
     * - No active evaluation cycle exists
     * - All confirmed users have managers assigned
     * - No user is managing themselves
     * - All evaluations from previous cycle are completed
     *
     * If any condition fails, a corresponding error response (HTTP 400, 401, 403, or 409)
     * is returned with detailed information or DTO.
     *
     * @param dto   DTO containing the end date for the new evaluation cycle.
     * @param token Session token of the user making the request (provided via HTTP header).
     * @return HTTP Response indicating success (201 Created) or the reason for rejection.
     */
    @POST
    @Path("/create-cycle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEvaluationCycle(CreateCycleDto dto,
                                          @HeaderParam("sessionToken") String token){


        // check if the session token is valid
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity cycleCreator = tokenEntity.getUser();


        // check if the user is an admin
        if (!cycleCreator.getRole().getName().equalsIgnoreCase("admin")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can create new cycles.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // check if the cycle's end date is valid
        if (!DateValidator.isValidFutureDate(dto.getEndDate())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Cycle's end date invalid or in the past\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        if (evaluationCycleBean.findActiveCycle() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"There currently is an active cycle.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // verify if there are users whose accounts are confrimed but don't have a manager assigned
        UsersWithoutManagerDto usersWithoutManagerDto = userBean.listConfirmedUsersWithoutManager();

        if (usersWithoutManagerDto.getNumberOfUsersWithoutManager() > 0) {
            logger.warn("User: {} | IP: {} - Found {} confirmed users without assigned managers.",
                    RequestContext.getAuthor(), RequestContext.getIp(),
                    usersWithoutManagerDto.getNumberOfUsersWithoutManager());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(usersWithoutManagerDto)  // replies with the respective and complete DTO
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // verify if any user is managing themselves
        UsersManagingThemselvesDto selfManagingDto = userBean.listUsersManagingThemselves();

        if (selfManagingDto.getNumberOfUsers() > 0) {
            logger.warn("User: {} | IP: {} - Found {} users managing themselves.",
                    RequestContext.getAuthor(), RequestContext.getIp(),
                    selfManagingDto.getNumberOfUsers());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(selfManagingDto)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }





        // verify if there are evaluations yet to be completed from the previous cycle
        UsersWithIncompleteEvaluationsDto incompleteDto = evaluationBean.listUsersWithIncompleteEvaluationsFromLastCycle();

        if (incompleteDto.getTotalUsersWithIncompleteEvaluations() > 0) {
            logger.warn("User: {} | IP: {} - Found {} users with incomplete evaluations.",
                    RequestContext.getAuthor(), RequestContext.getIp(),
                    incompleteDto.getTotalUsersWithIncompleteEvaluations());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(incompleteDto)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        //create a new evaluation cycle
        evaluationCycleBean.createCycleAndCreateBlankEvaluations(dto.getEndDate());

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"New evaluation cycle successfully created.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();

    }




    /**
     * Retrieves a list of users who still have evaluations in progress in the current cycle.
     *
     * <p>This endpoint is restricted to administrators only. It checks if the requester
     * has a valid session and the "ADMIN" role before returning data.</p>
     *
     * <p>The response contains a {@link UsersWithIncompleteEvaluationsDto} object with the
     * list of users and the total number of incomplete evaluations.</p>
     *
     * @param token The session token of the authenticated user (provided via HTTP header).
     * @return HTTP 200 OK with JSON body of users with incomplete evaluations if successful;
     *         HTTP 401 Unauthorized if the token is invalid;
     *         HTTP 403 Forbidden if the requester is not an admin.
     */
    @GET
    @Path("/list-incomplete-evaluations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIncompleteEvaluations(@HeaderParam("sessionToken") String token) {
        // Validate session token
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("User: {} | IP: {} - Unauthorized access to /list-incomplete-evaluations (invalid token).",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        // Check admin role
        if (!requester.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden: non-admin attempted to access /list-incomplete-evaluations.",
                    requester.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can access this information.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UsersWithIncompleteEvaluationsDto dto = evaluationBean.listUsersWithIncompleteEvaluationsFromLastCycle();
        return Response.ok(dto).build();
    }


    /**
     * Retrieves a list of confirmed users who do not have a manager assigned.
     *
     * <p>This endpoint is restricted to administrators only. It validates the session token
     * and verifies that the requester has the "ADMIN" role.</p>
     *
     * <p>The response contains a {@link UsersWithoutManagerDto} object with the list of users
     * and the total count.</p>
     *
     * @param token The session token of the authenticated user (provided via HTTP header).
     * @return HTTP 200 OK with JSON body of users without managers if successful;
     *         HTTP 401 Unauthorized if the token is invalid;
     *         HTTP 403 Forbidden if the requester is not an admin.
     */
    @GET
    @Path("/list-users-withouth-manager")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersWithoutManagers(@HeaderParam("sessionToken") String token) {
        // Validate session token
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("User: {} | IP: {} - Unauthorized access to /list-users-withouth-manager (invalid token).",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        // Check admin role
        if (!requester.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden: non-admin attempted to access /list-users-withouth-manager.",
                    requester.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can access this information.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UsersWithoutManagerDto dto = userBean.listConfirmedUsersWithoutManager();
        return Response.ok(dto).build();
    }






}
