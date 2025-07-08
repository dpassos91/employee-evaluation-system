package aor.projetofinal.service;

import aor.projetofinal.bean.UserCourseBean;
import aor.projetofinal.dto.CreateUserCourseDto;
import aor.projetofinal.dto.UserCourseDto;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.context.RequestContext; 

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

    @POST
    public Response addUserCourse(CreateUserCourseDto dto) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Registered participation in course {} for user {}.",
                currentUser.getEmail(), RequestContext.getIp(), dto.getCourseId(), dto.getUserId());

        userCourseBean.addUserCourse(dto);
        return Response.status(Response.Status.CREATED).build();
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
}

