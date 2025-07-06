package aor.projetofinal.service;


import aor.projetofinal.bean.EvaluationBean;
import aor.projetofinal.bean.EvaluationCycleBean;
import aor.projetofinal.bean.UserBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.UpdateEvaluationDto;
import aor.projetofinal.dto.EvaluationOptionsDto;
import aor.projetofinal.dto.SessionStatusDto;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.SessionTokenEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateType;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


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
    private SessionTokenDao sessionTokenDao;




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
        if (evaluation.getState() != EvaluationStateType.IN_EVALUATION) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"This evaluation is already completed or closed.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }




        // create new evaluation based on the data received from the frontend
        evaluationBean.updateEvaluationWithGradeAndFeedback(dto, evaluation, evaluator);

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"New evaluation successfully craeted.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }





}
