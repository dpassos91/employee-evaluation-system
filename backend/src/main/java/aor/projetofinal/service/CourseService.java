package aor.projetofinal.service;

import aor.projetofinal.bean.CourseBean;
import aor.projetofinal.dto.CourseDto;
import aor.projetofinal.dto.CreateCourseDto;
import aor.projetofinal.dto.UpdateCourseDto;
import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.context.RequestContext; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST Service for managing training courses.
 * Only administrators should have access to create, update, or deactivate courses.
 */
@Path("/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CourseService {

    private static final Logger logger = LogManager.getLogger(CourseService.class);

    @jakarta.inject.Inject
    private CourseBean courseBean;

    @GET
    public Response getCourses(
            @QueryParam("name") String name,
            @QueryParam("minTimeSpan") Double minTimeSpan,
            @QueryParam("maxTimeSpan") Double maxTimeSpan,
            @QueryParam("language") LanguageEnum language,
            @QueryParam("category") CourseCategoryEnum category,
            @QueryParam("active") Boolean active
    ) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Listed courses (filters: name='{}', minTimeSpan={}, maxTimeSpan={}, language={}, category={}, active={}).",
                currentUser.getEmail(), RequestContext.getIp(), name, minTimeSpan, maxTimeSpan, language, category, active);

        List<CourseDto> courses = courseBean.filterCourses(
                name, minTimeSpan, maxTimeSpan, language, category, active
        );
        return Response.ok(courses).build();
    }

    @GET
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportCoursesCsv(
            @QueryParam("name") String name,
            @QueryParam("minTimeSpan") Double minTimeSpan,
            @QueryParam("maxTimeSpan") Double maxTimeSpan,
            @QueryParam("language") LanguageEnum language,
            @QueryParam("category") CourseCategoryEnum category,
            @QueryParam("active") Boolean active
    ) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Exported courses to CSV (filters: name='{}', minTimeSpan={}, maxTimeSpan={}, language={}, category={}, active={}).",
                currentUser.getEmail(), RequestContext.getIp(), name, minTimeSpan, maxTimeSpan, language, category, active);

        List<CourseDto> courses = courseBean.filterCourses(
                name, minTimeSpan, maxTimeSpan, language, category, active
        );
        StringBuilder csv = new StringBuilder("Name,Duration,Language,Category,Active\n");
        for (CourseDto dto : courses) {
            csv.append(dto.getName()).append(",")
               .append(dto.getTimeSpan()).append(",")
               .append(dto.getLanguage()).append(",")
               .append(dto.getCourseCategory()).append(",")
               .append(dto.isActive()).append("\n");
        }
        return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=\"courses.csv\"")
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getCourseById(@PathParam("id") int id) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Fetched course with id {}.",
                currentUser.getEmail(), RequestContext.getIp(), id);

        CourseDto dto = courseBean.getCourseById(id);
        if (dto == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(dto).build();
    }

    @POST
    public Response createCourse(CreateCourseDto dto) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Created new course '{}'.",
                currentUser.getEmail(), RequestContext.getIp(), dto.getName());

        courseBean.createCourse(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCourse(@PathParam("id") int id, UpdateCourseDto dto) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Updated course with id {}.",
                currentUser.getEmail(), RequestContext.getIp(), id);

        dto.setId(id);
        courseBean.updateCourse(dto);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deactivateCourse(@PathParam("id") int id) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Deactivated course with id {}.",
                currentUser.getEmail(), RequestContext.getIp(), id);

        courseBean.deactivateCourse(id);
        return Response.noContent().build();
    }
}


