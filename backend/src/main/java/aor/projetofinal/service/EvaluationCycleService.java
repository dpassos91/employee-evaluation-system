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
        evaluationCycleBean.createCycle(dto.getEndDate());

        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"New evaluation cycle successfully created.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();






    }






}
