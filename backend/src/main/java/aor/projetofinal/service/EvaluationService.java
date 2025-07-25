package aor.projetofinal.service;


import aor.projetofinal.bean.EvaluationBean;
import aor.projetofinal.bean.EvaluationCycleBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateEnum;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.util.PdfExportUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;


@Path("/evaluations")
public class EvaluationService {

    private static final Logger logger = LogManager.getLogger(ProfileService.class);

    @Inject
    private EvaluationBean evaluationBean;

    @Inject
    private EvaluationCycleBean evaluationCycleBean;

    @Inject
    UserBean userBean;

    @Inject
    private UserDao userDao;

    @Inject
    private EvaluationDao evaluationDao;


    @Inject
    private SessionTokenDao sessionTokenDao;



    /**
     * Closes all evaluations in the currently active evaluation cycle.
     *
     * Preconditions:
     * - The request must include a valid session token.
     * - The user must have the ADMIN role.
     * - All evaluations in the active cycle must be in the EVALUATED state.
     *
     * If any of these conditions fail, the request is rejected with an appropriate
     * HTTP status (401 Unauthorized, 403 Forbidden, or 409 Conflict).
     *
     * Upon success, this method invokes the logic to close all evaluations and deactivate the cycle.
     *
     * @param token The session token of the requesting user, passed in the header.
     * @return HTTP Response indicating the result of the operation.
     */
    @PUT
    @Path("/close-all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeAllEvaluations(@HeaderParam("sessionToken") String token) {

        // validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("Unauthorized attempt to close evaluations. IP: {}", RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        logger.info("User: {} | IP: {} - Attempting to bulk close evaluations.",
                RequestContext.getAuthor(), RequestContext.getIp());

        // only an admin can close evaluations in bulk
        if (!currentUser.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden: Non-admin tried to bulk close evaluations.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can close evaluations in bulk.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validate if all evaluations are EVALUATED
        if (!evaluationBean.areAllEvaluationsInActiveCycleEvaluated()) {
            logger.warn("User: {} | IP: {} - Not all evaluations are in EVALUATED state.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Not all evaluations are in EVALUATED state. Cannot proceed with bulk close.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // close all evaluations in the current cycle
        logger.info("User: {} | IP: {} - Proceeding with bulk closure of evaluations.",
                RequestContext.getAuthor(), RequestContext.getIp());

        evaluationCycleBean.bulkCloseEvaluationsAndCycle();

        logger.info("User: {} | IP: {} - Bulk closure completed successfully.",
                RequestContext.getAuthor(), RequestContext.getIp());

        return Response.ok()
                .entity("{\"message\": \"Bulk close completed.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }










    /**
     * Closes a single evaluation by its ID, only if it is in the 'EVALUATED' state.
     *
     * Preconditions:
     * - The request must include a valid session token.
     * - The user must have the ADMIN role.
     * - The evaluation must exist and be in 'EVALUATED' state.
     *
     * After closing the evaluation, this method checks whether the associated cycle
     * can also be closed.
     *
     * @param evaluationId The ID of the evaluation to be closed.
     * @param token The session token of the requesting user (from HTTP header).
     * @return HTTP Response indicating the result of the operation.
     */
    @PUT
    @Path("/close/{evaluationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeEvaluation(@PathParam("evaluationId") Long evaluationId,
                                    @HeaderParam("sessionToken") String token) {

        // validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("Unauthorized attempt to close evaluation ID {}. IP: {}", evaluationId, RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        logger.info("User: {} | IP: {} - Attempting to close evaluation ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);

        // only admins can close evaluations
        if (!currentUser.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden: Non-admin tried to close evaluation ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can close evaluations.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // find the evaluation by ID
        EvaluationEntity evaluation = evaluationBean.findEvaluationById(evaluationId);
        if (evaluation == null) {
            logger.warn("User: {} | IP: {} - Evaluation ID {} not found.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // verify if the evaluation state is 'EVALUATED'
        if (evaluation.getState() != EvaluationStateEnum.EVALUATED) {
            logger.warn("User: {} | IP: {} - Evaluation ID {} is not in 'EVALUATED' state.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Only evaluations in 'EVALUATED' state can be closed.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Close the evaluation and check if the cycle can be closed
        evaluationCycleBean.closeEvaluationAndCheckCycle(evaluation);

        logger.info("User: {} | IP: {} - Evaluation ID {} closed successfully.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);

        return Response.ok()
                .entity("{\"message\": \"Evaluation closed successfully.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }



    /**
     * Exports all evaluations matching the given filters into a downloadable CSV file.
     * Only accessible to Admins and Managers.
     *
     * @param token          Session token of the authenticated user
     * @param name           Optional name filter of evaluated user
     * @param state          Optional evaluation state
     * @param grade          Optional grade filter
     * @param cycleEndString Optional exact end date of evaluation cycle (yyyy-MM-dd)
     * @return CSV file as HTTP attachment
     */
    @GET
    @Path("/export-csv")
    @Produces("text/csv")
    public Response exportEvaluationsToCsv(
            @HeaderParam("sessionToken") String token,
            @QueryParam("name") String name,
            @QueryParam("state") EvaluationStateEnum state,
            @QueryParam("grade") Integer grade,
            @QueryParam("cycleEnd") String cycleEndString
    ) {
        // 1. Validate session
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);
        if (session == null || session.getUser() == null) {
            logger.warn("Unauthorized CSV export attempt.");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Session expired or invalid.")
                    .build();
        }

        UserEntity requester = session.getUser();
        String roleName = requester.getRole().getName().toUpperCase();
        if (!roleName.equals("ADMIN") && !roleName.equals("MANAGER")) {
            logger.warn("User: {} | IP: {} - Access denied for evaluation CSV export.",
                    requester.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Only administrators and managers can export evaluations.")
                    .build();
        }

        // 2. Parse optional cycle end date
        LocalDate cycleEnd = null;
        if (cycleEndString != null && !cycleEndString.isBlank()) {
            try {
                cycleEnd = LocalDate.parse(cycleEndString);
            } catch (DateTimeParseException e) {
                logger.warn("User: {} | IP: {} - Invalid cycle end date: {}", requester.getEmail(), RequestContext.getIp(), cycleEndString);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid date format. Use yyyy-MM-dd.")
                        .build();
            }
        }

        // 3. Fetch all matching evaluations (no pagination)
        List<EvaluationEntity> results = evaluationDao.findEvaluationsWithFiltersPaginated(
                name, state, grade, cycleEnd, requester, 1, Integer.MAX_VALUE // fetch all
        );

        List<FlatEvaluationDto> dtos = results.stream()
                .map(JavaConversionUtil::convertEvaluationToFlatDto)
                .toList();

        logger.info("User: {} | IP: {} - Exported {} evaluations to CSV.",
                requester.getEmail(), RequestContext.getIp(), dtos.size());

        // 4. Build CSV content
        String csv = JavaConversionUtil.buildCsvFromEvaluations(dtos);

        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=evaluations_export.csv")
                .type("text/csv")
                .build();
    }


    /**
     * Exports a single closed evaluation to a PDF file.
     * Only accessible to the evaluated user or an admin.
     *
     * @param sessionToken The session token of the requester
     * @param id           The ID of the evaluation to export
     * @return A PDF file or appropriate error response
     */
    @GET
    @Path("/export-pdf")
    @Produces("application/pdf")
    public Response exportEvaluationToPdf(
            @HeaderParam("sessionToken") String sessionToken,
            @QueryParam("id") Long id
    ) {
        // 1. Validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Session expired or invalid.")
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        // 2. Load evaluation
        EvaluationEntity evaluation = evaluationDao.findById(id);
        if (evaluation == null) {
            logger.warn("User: {} | IP: {} - Tried to export non-existent evaluation ID {}.",
                    requester.getEmail(), RequestContext.getIp(), id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Evaluation not found.")
                    .build();
        }

        // 3. Validate that it's closed and cycle inactive
        boolean isClosed = evaluation.getState() == EvaluationStateEnum.CLOSED;
        boolean cycleEnded = evaluation.getCycle() != null && !evaluation.getCycle().isActive();

        if (!isClosed || !cycleEnded) {
            logger.warn("User: {} | IP: {} - Tried to export incomplete evaluation ID {}.",
                    requester.getEmail(), RequestContext.getIp(), id);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Evaluation is not yet closed.")
                    .build();
        }

        // 4. Validate permission: admin or self
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("ADMIN");
        boolean isSelf = evaluation.getEvaluated().getEmail().equalsIgnoreCase(requester.getEmail());
        boolean isManager = evaluation.getEvaluated().getManager() != null &&
                evaluation.getEvaluated().getManager().getId() == requester.getId();

        if (!isSelf && !isManager && !isAdmin){
            logger.warn("User: {} | IP: {} - Unauthorized export attempt of evaluation ID {}.",
                    requester.getEmail(), RequestContext.getIp(), id);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You are not authorized to access this evaluation.")
                    .build();
        }

        // 5. Generate PDF (you will implement this part)
        byte[] pdfBytes = PdfExportUtil.buildEvaluationPdf(evaluation); // <- implementa isto à parte

        return Response.ok(pdfBytes)
                .header("Content-Disposition", "attachment; filename=evaluation_" + id + ".pdf")
                .type("application/pdf")
                .build();
    }


    /**
     * Public endpoint to retrieve all possible evaluation states (IN_EVALUATION, EVALUATED, CLOSED).
     * Useful for UI filter dropdowns or forms. This endpoint does not require authentication.
     *
     * @return HTTP 200 with wrapped list of evaluation states.
     */
    @GET
    @Path("/states")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEvaluationStates() {
        List<EvaluationStateDto> list = evaluationBean.getAllEvaluationStates();
        EvaluationStatesDto wrapper = new EvaluationStatesDto(list);

        logger.info("IP: {} - Publicly fetched {} evaluation states.",
                RequestContext.getIp(),
                list.size()
        );

        return Response.ok(wrapper)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


    /**
     * Retrieves a paginated list of closed evaluations for a given user,
     * applying optional filters (grade, cycle number, cycle end date).
     *
     * @param sessionToken The session token of the requesting user.
     * @param email Email of the evaluated user.
     * @param page Page number for pagination (default is 1).
     * @param grade Optional filter by grade (1 to 4).
     * @param cycle Optional filter by cycle number.
     * @param cycleEndDate Optional filter by cycle end date (yyyy-MM-dd).
     * @return Paginated list of evaluations, or error if access is denied or data is invalid.
     */
    @GET
    @Path("/history-with-filters")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluationHistoryWithFilters(
            @HeaderParam("sessionToken") String sessionToken,
            @QueryParam("userId") int userId,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("grade") Integer grade,
            @QueryParam("cycle") Integer cycle,
            @QueryParam("cycleEndDate") String cycleEndDate
    ) {
        logger.info("User: {} | IP: {} - Requesting filtered evaluation history for user {}.",
                RequestContext.getAuthor(), RequestContext.getIp(),  RequestContext.getIp());

        // Validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("IP: {} - Unauthorized access attempt (invalid or expired session).", RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        // Load evaluated user
        UserEntity evaluated = userDao.findById(userId);

        if (evaluated == null) {
            logger.warn("User: {} | IP: {} - Attempted to access history of non-existent user {}.",
                    requester.getEmail(), RequestContext.getIp(), userId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluated user not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check access rights

        boolean isSelf = requester.getId() == userId;
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("ADMIN");
        boolean isManager = evaluated.getManager() != null &&
                evaluated.getManager().getId() == requester.getId();

        if (!isSelf && !isManager && !isAdmin) {
            logger.warn("User: {} | IP: {} - Access denied to evaluation history of {}.",
                    requester.getEmail(), RequestContext.getIp(), evaluated.getEmail());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"You are not allowed to view this user's evaluation history.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Load paginated history

        PaginatedEvaluationHistoryDto dto = evaluationBean.getFilteredEvaluationHistory(
                evaluated, page, grade, cycle, cycleEndDate
        );

        logger.info("User: {} | IP: {} - Successfully fetched filtered evaluation history of {} (page {}).",
                requester.getEmail(), RequestContext.getIp(), evaluated.getEmail(), page);

        return Response.ok(dto, MediaType.APPLICATION_JSON).build();
    }














    /**
     * Lists evaluations filtered by name, state, grade, and cycle end date,
     * returning results paginated in a lightweight DTO format.
     * Access is restricted to Admins and Managers only.
     *
     * @param token           Session token of the authenticated user
     * @param name            Optional partial name of the evaluated user
     * @param state           Optional evaluation state filter
     * @param grade           Optional evaluation grade filter (1-4)
     * @param cycleEndString  Optional exact cycle end date in yyyy-MM-dd format
     * @param page            Page number for pagination (1-based)
     * @return A paginated list of filtered evaluations, or appropriate error response
     */
    @GET
    @Path("/list-by-filters")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listEvaluationsFiltered(
            @HeaderParam("sessionToken") String token,
            @QueryParam("name") String name,
            @QueryParam("state") EvaluationStateEnum state,
            @QueryParam("grade") Integer grade,
            @QueryParam("cycleEnd") String cycleEndString,
            @QueryParam("page") @DefaultValue("1") int page
    ) {
        // 1. Validate session
        SessionTokenEntity session = sessionTokenDao.findBySessionToken(token);
        if (session == null || session.getUser() == null) {
            logger.warn("Unauthorized access attempt to evaluations list.");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired or invalid.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = session.getUser();

        // 2. Role check
        String roleName = requester.getRole().getName().toUpperCase();
        if (!roleName.equals("ADMIN") && !roleName.equals("MANAGER")) {
            logger.warn("User: {} | IP: {} - Access denied: not admin or manager.",
                    requester.getEmail(), RequestContext.getIp());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only administrators and managers can access the evaluations list.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Requested filtered evaluations list.", requester.getEmail(), RequestContext.getIp());

        // 3. Parse cycle end date
        LocalDate cycleEnd = null;
        if (cycleEndString != null && !cycleEndString.isBlank()) {
            try {
                cycleEnd = LocalDate.parse(cycleEndString);
            } catch (DateTimeParseException e) {
                logger.warn("User: {} | IP: {} - Invalid cycle end date: {}", requester.getEmail(), RequestContext.getIp(), cycleEndString);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid date format for cycle end. Use yyyy-MM-dd.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        }

        // 4. Load filtered paginated evaluations
        PaginatedEvaluationsDto paginated = evaluationBean.findEvaluationsWithFiltersPaginated(
                name, state, grade, cycleEnd, requester, page
        );

        logger.info("User: {} | IP: {} - Returned {} evaluations on page {}.",
                requester.getEmail(), RequestContext.getIp(), paginated.getEvaluations().size(), page);

        return Response.ok(paginated)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }




    /**
     * Returns the list of possible evaluation grade options available in the system.
     *
     * Accessible only to users with ADMIN or MANAGER roles. Requires a valid session token.
     *
     * Logs:
     * - Unauthorized or expired session access attempts
     * - Forbidden access for users without proper role
     * - Successful retrieval of options
     *
     * @param sessionToken The session token provided in the request header.
     * @return A JSON response containing the list of {@link EvaluationOptionsDto}, or an error response.
     */
    @GET
    @Path("/list-evaluation-options")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listEvaluationOptions(@HeaderParam("sessionToken") String sessionToken) {

// Sessionj validation
        SessionStatusDto sessionStatusDto = userBean.validateAndRefreshSessionToken(sessionToken);
        if (sessionStatusDto == null) {
            logger.warn("User: {} | IP: {} - Invalid or expired session when trying to access user profile.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(401)
                    .entity("{\"message\": \"Expired session. Log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // Only a manager or admin can access this endpoint and get this infpo
        SessionTokenEntity sessionTokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        UserEntity currentUser = sessionTokenEntity.getUser();


        if (currentUser == null) {
            logger.warn("User: {} | IP: {} - Attempted to access a non-existent user profile.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(404)
                    .entity("{\"message\": \"User not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if  (!currentUser.getRole().getName().equalsIgnoreCase("admin") &&
                !currentUser.getRole().getName().equalsIgnoreCase("manager"))  {
            logger.warn("User: {} | IP: {} - Not authorized to access this information.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(403)
                    .entity("{\"message\": \"Not authorized to access this information..\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        List <EvaluationOptionsDto> options= evaluationBean.listEvaluationOptions();

        logger.info("User: {} | IP: {} - Evaluation grade Options retrieved successfully.",
                RequestContext.getAuthor(), RequestContext.getIp());



        return Response.ok(options).build();
    }




    /**
     * Loads the evaluation for a specific user in the current active cycle.
     *
     * This endpoint is accessible to:
     * - ADMINs
     * - The manager of the evaluated user
     * - The evaluated user themselves (only if the cycle and evaluation are CLOSED)
     *
     * Validates:
     * - Session token
     * - User and evaluation existence
     * - Role-based access
     * - Whether the cycle is active or closed
     *
     * Logs access attempts, permission issues, and successful evaluation loads.
     *
     * @param evaluatedUserId ID of the user whose evaluation is to be loaded.
     * @param token Session token from the request header.
     * @return A JSON response containing an {@link UpdateEvaluationDto} or an appropriate error message.
     */
    @GET
    @Path("/load-evaluation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadEvaluation(@QueryParam("userId") int evaluatedUserId,
                                   @HeaderParam("sessionToken") String token) {

        // 1. Validate and refresh session token if close to expiration
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // valdiate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity evaluator = tokenEntity.getUser();

        // validates the existence of the evaluated user
        UserEntity evaluated = userDao.findById(evaluatedUserId);
        if (evaluated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluated user not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }



        // verifies permissions
        boolean isAdmin = evaluator.getRole().getName().equalsIgnoreCase("admin");
        boolean isManager = evaluated.getManager() != null &&
                evaluated.getManager().getId() == evaluator.getId();

        boolean isEvaluatedUser = evaluator.getId() == evaluated.getId();

        if (!isAdmin && !isManager && !isEvaluatedUser) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"You are not authorized to view this evaluation.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // gets the current active evaluation cycle
        EvaluationCycleEntity cycle = evaluationCycleBean.findActiveCycle();
        if (cycle == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"No active evaluation cycle found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Loading evaluation for user {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluatedUserId);


        // gets the correct evaluation to load
        EvaluationEntity evaluation = evaluationBean.findEvaluationByCycleAndUser(cycle, evaluated);
        if (evaluation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found for this user in current cycle.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // check if the cycle is close so that the evaluated user can see his evaluation


        boolean isEvaluationClosed = evaluation.getState() == EvaluationStateEnum.CLOSED;
        boolean isCycleClosed = !evaluation.getCycle().isActive();

        if (isEvaluatedUser && (!isEvaluationClosed || !isCycleClosed)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"You can only view your evaluation after the cycle has officially ended.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // build UpdateEvaluationDto to send to the frontend

        UpdateEvaluationDto dto = evaluationBean.buildEvaluationDtoCorrespondingToTheEvaluation(evaluation);

        logger.info("User: {} | IP: {} - Evaluation loaded successfully for user {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluatedUserId);


        return Response.ok()
                .entity(Map.of(
                        "message", "Evaluation data loaded successfully.",
                        "evaluation", dto
                ))
                .type(MediaType.APPLICATION_JSON)
                .build();

    }

    /**
     * Reopens a previously evaluated evaluation for editing by reverting its state to IN_EVALUATION.
     *
     * Preconditions:
     * - The session token must be valid.
     * - The user must have ADMIN role.
     * - The evaluation must exist and be currently in the EVALUATED state.
     *
     * @param evaluationId The ID of the evaluation to revert.
     * @param token The session token from the request header.
     * @return HTTP response indicating success or failure reason.
     */
    @PUT
    @Path("/reopen-for-editing/{evaluationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reopenEvaluationForEditing(@PathParam("evaluationId") Long evaluationId,
                                               @HeaderParam("sessionToken") String token) {

        // 1. Validate and refresh session token if close to expiration
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        // Validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("Unauthorized attempt to reopen evaluation ID {}. IP: {}", evaluationId, RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        logger.info("User: {} | IP: {} - Attempting to revert evaluation ID {} to IN_EVALUATION.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);

        // Verify admin role
        if (!currentUser.getRole().getName().equalsIgnoreCase("admin")) {
            logger.warn("User: {} | IP: {} - Forbidden: Non-admin tried to reopen evaluation ID {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can reopen evaluations for editing.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Find evaluation by ID
        EvaluationEntity evaluation = evaluationBean.findEvaluationById(evaluationId);
        if (evaluation == null) {
            logger.warn("User: {} | IP: {} - Evaluation ID {} not found.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Ensure evaluation is in EVALUATED state
        if (evaluation.getState() != EvaluationStateEnum.EVALUATED) {
            logger.warn("User: {} | IP: {} - Evaluation ID {} is not in EVALUATED state.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Only evaluations in EVALUATED state can be reverted for editing.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Revert evaluation state
        boolean reverted = evaluationBean.revertEvaluationToInEvaluation(evaluation);
        if (!reverted) {
            logger.warn("User: {} | IP: {} - Failed to revert evaluation ID {} state.",
                    RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Only evaluations in EVALUATED state can be reverted for editing.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("User: {} | IP: {} - Evaluation ID {} reverted to IN_EVALUATION successfully.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluationId);

        return Response.ok()
                .entity("{\"message\": \"Evaluation successfully reverted to IN_EVALUATION.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }




    /**
     * Updates an evaluation with grade and feedback based on the provided DTO.
     *
     * Preconditions:
     * - Valid session token.
     * - Active evaluation cycle exists.
     * - Evaluated user exists and has an evaluation in the current cycle.
     * - Evaluation is not closed.
     * - Evaluator is admin or the manager of the evaluated user.
     * - Evaluation is still in progress (IN_EVALUATION state).
     *
     * @param dto The DTO containing updated evaluation data.
     * @param token The session token from the request header.
     * @return HTTP response indicating success or failure.
     */
    @PUT
    @Path("/update-evaluation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvaluation(UpdateEvaluationDto dto,
                                     @HeaderParam("sessionToken") String token) {


        // 1. Validate and refresh session token if close to expiration
        SessionStatusDto sessionStatus = userBean.validateAndRefreshSessionToken(token);

        if (sessionStatus == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Session expired. Please, log in again.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("Unauthorized update attempt. IP: {}", RequestContext.getIp());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity evaluator = tokenEntity.getUser();

        // Check active evaluation cycle
        EvaluationCycleEntity cycle = evaluationCycleBean.findActiveCycle();
        if (cycle == null) {
            logger.warn("User: {} | IP: {} - No active evaluation cycle when updating evaluation.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"There is no evaluation cycle currently opened.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Find evaluated user by email
        UserEntity evaluated = userDao.findByEmail(dto.getEvaluatedEmail());
        if (evaluated == null) {
            logger.warn("User: {} | IP: {} - Evaluated user '{}' not found.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getEvaluatedEmail());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Couldn't find evaluated user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Find evaluation for current cycle and evaluated user
        EvaluationEntity evaluation = evaluationBean.findEvaluationByCycleAndUser(cycle, evaluated);
        if (evaluation == null) {
            logger.warn("User: {} | IP: {} - Evaluation not found for user '{}' in current cycle.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getEvaluatedEmail());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found for this user in current cycle.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (evaluation.getState() == EvaluationStateEnum.CLOSED) {
            logger.warn("User: {} | IP: {} - Attempt to modify closed evaluation for user '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getEvaluatedEmail());
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"This evaluation is closed and cannot be modified.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check if evaluator is allowed
        boolean isAdmin = evaluator.getRole().getName().equalsIgnoreCase("admin");
        boolean isManagerOfEvaluated = evaluated.getManager() != null &&
                evaluated.getManager().getEmail().equalsIgnoreCase(evaluator.getEmail());

        if (!isAdmin && !isManagerOfEvaluated) {
            logger.warn("User: {} | IP: {} - Not authorized to evaluate user '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getEvaluatedEmail());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"You are not allowed to evaluate this user.\"}")
                    .build();
        }

        // Check if evaluation is still to fill
        if (evaluation.getState() != EvaluationStateEnum.IN_EVALUATION) {
            logger.warn("User: {} | IP: {} - Attempt to update completed or closed evaluation for user '{}'.",
                    RequestContext.getAuthor(), RequestContext.getIp(), dto.getEvaluatedEmail());
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"This evaluation is already completed or closed.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Update evaluation
        evaluationBean.updateEvaluationWithGradeAndFeedback(dto, evaluation, evaluator);

        logger.info("User: {} | IP: {} - Evaluation updated successfully for user '{}'.",
                RequestContext.getAuthor(), RequestContext.getIp(), dto.getEvaluatedEmail());

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"Evaluation successfully updated.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }





}
