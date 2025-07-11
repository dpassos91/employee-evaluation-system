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



    @PUT
    @Path("/close-all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeAllEvaluations(@HeaderParam("sessionToken") String token) {

        // validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        // only and admin can close evaluations in bulk
        if (!currentUser.getRole().getName().equalsIgnoreCase("admin")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can close evaluations in bulk.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 3. close in bulk all evaluations in the current cycle
        evaluationCycleBean.bulkCloseEvaluationsAndCycle();

        return Response.ok()
                .entity("{\"message\": \"Bulk close completed.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }










    @PUT
    @Path("/close/{evaluationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeEvaluation(@PathParam("evaluationId") Long evaluationId,
                                    @HeaderParam("sessionToken") String token) {

        // validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        // only admins can close evaluations
        if (!currentUser.getRole().getName().equalsIgnoreCase("admin")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can close evaluations.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // find the evaluation by ID
        EvaluationEntity evaluation = evaluationBean.findEvaluationById(evaluationId);
        if (evaluation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // verify if the evaluation state is 'EVALUATED'
        if (evaluation.getState() != EvaluationStateEnum.EVALUATED) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Only evaluations in 'EVALUATED' state can be closed.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Close the evaluation and check if the cycle can be closed
        evaluationCycleBean.closeEvaluationAndCheckCycle(evaluation);

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

        if (!isAdmin && !isSelf) {
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
     * Returns the paginated history of closed evaluations for a given user.
     * Only accessible to the evaluated user themselves, their current manager, or an admin.
     *
     * @param sessionToken  The session token of the requester
     * @param email         The email of the evaluated user (whose history is being requested)
     * @param page          The page number (default = 1)
     * @return JSON response with paginated evaluation history or error
     */
    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluationHistory(
            @HeaderParam("sessionToken") String sessionToken,
            @QueryParam("email") String email,
            @QueryParam("page") @DefaultValue("1") int page
    ) {
        // 1. Validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(sessionToken);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            logger.warn("Unauthorized request to evaluation history.");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity requester = tokenEntity.getUser();

        // 2. Load evaluated user
        UserEntity evaluated = userDao.findByEmail(email);
        if (evaluated == null) {
            logger.warn("User: {} | IP: {} - Attempted to access history of non-existent user {}.",
                    requester.getEmail(), RequestContext.getIp(), email);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluated user not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 3. Check access rights
        boolean isSelf = evaluated.getEmail().equalsIgnoreCase(requester.getEmail());
        boolean isAdmin = requester.getRole().getName().equalsIgnoreCase("ADMIN");
        boolean isManager = evaluated.getManager() != null &&
                evaluated.getManager().getEmail().equalsIgnoreCase(requester.getEmail());

        if (!isSelf && !isManager && !isAdmin) {
            logger.warn("User: {} | IP: {} - Access denied to evaluation history of {}.",
                    requester.getEmail(), RequestContext.getIp(), evaluated.getEmail());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"You are not allowed to view this user's evaluation history.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 4. Load paginated history
        PaginatedEvaluationHistoryDto dto = evaluationBean.getEvaluationHistory(evaluated, page);

        logger.info("User: {} | IP: {} - Accessed evaluation history of {} (Page {}).",
                requester.getEmail(), RequestContext.getIp(), evaluated.getEmail(), page);

        return Response.ok(dto)
                .type(MediaType.APPLICATION_JSON)
                .build();
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

    @GET
    @Path("/load-evaluation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadEvaluation(@QueryParam("email") String evaluatedEmail,
                                   @HeaderParam("sessionToken") String token) {

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
        UserEntity evaluated = userDao.findByEmail(evaluatedEmail);
        if (evaluated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluated user not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }



        // verifies permissions
        boolean isAdmin = evaluator.getRole().getName().equalsIgnoreCase("admin");
        boolean isManager = evaluated.getManager() != null &&
                evaluated.getManager().getEmail().equalsIgnoreCase(evaluator.getEmail());

        if (!isAdmin && !isManager) {
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
                RequestContext.getAuthor(), RequestContext.getIp(), evaluatedEmail);


        // gets the correct evaluation to load
        EvaluationEntity evaluation = evaluationBean.findEvaluationByCycleAndUser(cycle, evaluated);
        if (evaluation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found for this user in current cycle.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // check if the cycle is close so that the evaluated user can see his evaluation

        boolean isEvaluatedUser = evaluated.getEmail().equalsIgnoreCase(evaluator.getEmail());
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
                RequestContext.getAuthor(), RequestContext.getIp(), evaluatedEmail);


        return Response.ok()
                .entity(Map.of(
                        "message", "Evaluation data loaded successfully.",
                        "evaluation", dto
                ))
                .type(MediaType.APPLICATION_JSON)
                .build();

    }

    @PUT
    @Path("/reopen-for-editing/{evaluationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reopenEvaluationForEditing(@PathParam("evaluationId") Long evaluationId,
                                               @HeaderParam("sessionToken") String token) {

        // validate session
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        UserEntity currentUser = tokenEntity.getUser();

        // verifies if it is an admin
        if (!currentUser.getRole().getName().equalsIgnoreCase("admin")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"Only admins can reopen evaluations for editing.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // get correct evaluation by ID
        EvaluationEntity evaluation = evaluationBean.findEvaluationById(evaluationId);
        if (evaluation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // can only open evaluations that are in EVALUATED state
        if (evaluation.getState() != EvaluationStateEnum.EVALUATED) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Only evaluations in EVALUATED state can be reverted for editing.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // set the evaluation state back to IN_EVALUATION
        boolean reverted = evaluationBean.revertEvaluationToInEvaluation(evaluation);
        if (!reverted) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Only evaluations in EVALUATED state can be reverted for editing.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        logger.info("Admin {} reverted evaluation ID {} to IN_EVALUATION.", currentUser.getEmail(), evaluationId);

        return Response.ok()
                .entity("{\"message\": \"Evaluation successfully reverted to IN_EVALUATION.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }




    @PUT
    @Path("/update-evaluation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvaluation(UpdateEvaluationDto dto,
                                     @HeaderParam("sessionToken") String token) {

        // check if the session token is valid
        SessionTokenEntity tokenEntity = sessionTokenDao.findBySessionToken(token);
        if (tokenEntity == null || tokenEntity.getUser() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid or expired session.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // find the manager or admin who's the evaluator
        UserEntity evaluator = tokenEntity.getUser();

        // get the current active cycle
        EvaluationCycleEntity cycle = evaluationCycleBean.findActiveCycle();
        if (cycle == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"There is no evaluation cycle currently openned.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // find the evaluated user by email
        UserEntity evaluated = userDao.findByEmail(dto.getEvaluatedEmail());
        if (evaluated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Couldn't find evaluated user.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


        //search for the correct evaluation in the current cycle
        EvaluationEntity evaluation = evaluationBean.findEvaluationByCycleAndUser(cycle, evaluated);
        if (evaluation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evaluation not found for this user in current cycle.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (evaluation.getState() == EvaluationStateEnum.CLOSED) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"This evaluation is closed and cannot be modified.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }


//verify if the evaluator is allowed to evaluate the evaluated user,
// such that he must be either an admin or the manager of the evaluated user
        boolean isAdmin = evaluator.getRole().getName().equalsIgnoreCase("admin");
        boolean isManagerOfEvaluated = evaluated.getManager() != null &&
                evaluated.getManager().getEmail().equalsIgnoreCase(evaluator.getEmail());

        if (!isAdmin && !isManagerOfEvaluated) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\": \"You are not allowed to evaluate this user.\"}")
                    .build();
        }


//check if the evaluation is still to fill
        if (evaluation.getState() != EvaluationStateEnum.IN_EVALUATION) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"This evaluation is already completed or closed.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }




        // create new evaluation based on the data received from the frontend
        evaluationBean.updateEvaluationWithGradeAndFeedback(dto, evaluation, evaluator);

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"Evaluation successfully updated.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }





}
