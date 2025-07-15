package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.CourseDao;
import aor.projetofinal.dao.UserCourseDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.CreateUserCourseDto;
import aor.projetofinal.dto.UserCourseDto;
import aor.projetofinal.dto.UserCourseYearSummaryDto;
import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.UserCourseEntity;
import aor.projetofinal.entity.UserCourseIdEntity;

import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.util.JavaConversionUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic bean for managing user-course participation.
 * Handles registration, validation, and retrieval of training history.
 */
@ApplicationScoped
public class UserCourseBean {

    @Inject
    private CourseDao courseDao;

    @Inject
    private UserCourseDao userCourseDao;


    @Inject
    private UserDao userDao;



    private static final Logger logger = LogManager.getLogger(UserCourseBean.class);


    /**
     * Registers a new training participation for a user in a specific course.
     *
     * Validates that:
     * - the course exists and is active
     * - the user exists
     * - the user is not already registered for the course
     *
     * If validation passes, associates the user and course with a new UserCourseEntity and persists it.
     *
     * @param dto the data transfer object containing userId, courseId, and participationDate
     * @throws IllegalArgumentException if any validation fails
     */
    @Transactional
    public void addUserCourse(CreateUserCourseDto dto) {
        logger.info("Request from user [{}] | IP [{}] - Initiating course registration for user [{}], course [{}]",
                RequestContext.getAuthor(), RequestContext.getIp(), dto.getUserId(), dto.getCourseId());

        // 1. Validate course existence and active status
        CourseEntity course = courseDao.findById(dto.getCourseId());
        if (course == null) {
            logger.warn("Course ID [{}] not found.", dto.getCourseId());
            throw new IllegalArgumentException("Course does not exist.");
        }
        if (!course.isActive()) {
            logger.warn("Course ID [{}] is inactive.", dto.getCourseId());
            throw new IllegalArgumentException("Course is not active.");
        }

        // 2. Validate user existence
        UserEntity user = userDao.findById(dto.getUserId());
        if (user == null) {
            logger.warn("Target user ID [{}] not found.", dto.getUserId());
            throw new IllegalArgumentException("User does not exist.");
        }

        // 3. Check for existing user-course participation
        UserCourseIdEntity id = new UserCourseIdEntity(dto.getUserId(), dto.getCourseId());
        if (userCourseDao.findById(id) != null) {
            logger.warn("User [{}] already registered for course [{}].", dto.getUserId(), dto.getCourseId());
            throw new IllegalArgumentException("User is already registered for this course.");
        }

        // 4. Create and populate the new UserCourseEntity
        UserCourseEntity uc = new UserCourseEntity();
        uc.setId(id);
        uc.setParticipationDate(dto.getParticipationDate().atTime(LocalTime.MAX)); // Convert LocalDate to end of day
        uc.setUser(user);
        uc.setCourse(course);

        // 5. Persist the record
        userCourseDao.save(uc);

        logger.info("Course [{}] successfully registered for user [{}] by [{}] | IP [{}]",
                dto.getCourseId(), dto.getUserId(), RequestContext.getAuthor(), RequestContext.getIp());
    }




    /**
     * Returns the list of distinct years in which a user has participated in training courses.
     *
     * @param userId ID of the user
     * @return ordered list of years with recorded training participation
     */
    public List<Integer> listParticipationYearsByUser(int userId) {
        Logger logger = LogManager.getLogger(UserCourseBean.class);

        logger.info("User: {} | IP: {} - Getting participation years for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);

        return userCourseDao.findDistinctParticipationYearsByUserId(userId);
    }




    /**
     * Converts a UserCourseEntity to a UserCourseDto.
     *
     * @param entity The UserCourseEntity to convert.
     * @return The corresponding UserCourseDto.
     */
    public UserCourseDto toDto(UserCourseEntity entity) {
        UserCourseDto dto = new UserCourseDto();
        dto.setUserId(entity.getUser().getId());
        dto.setCourseId(entity.getCourse().getId());
        dto.setCourseName(entity.getCourse().getName());
        dto.setTimeSpan(entity.getCourse().getTimeSpan());
        dto.setLanguage(entity.getCourse().getLanguage());
        dto.setCourseCategory(entity.getCourse().getCourseCategory());
        dto.setParticipationDate(entity.getParticipationDate());
        return dto;
    }

    /**
     * Retrieves all courses attended by a user, as DTOs.
     *
     * @param userId The user ID.
     * @return List of UserCourseDto.
     */
    public List<UserCourseDto> listUserCourses(int userId) {
        return userCourseDao.findByUserId(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Retrieves all courses attended by a user in a specific year, as DTOs.
     *
     * @param userId The user ID.
     * @param year   The year.
     * @return List of UserCourseDto.
     */
    public List<UserCourseDto> listUserCoursesByYear(int userId, int year) {
        return userCourseDao.findByUserIdAndYear(userId, year).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Generates a yearly summary of training hours for a specific user.
     * <p>
     * Retrieves all course participations for the given user and converts
     * them into a list of yearly aggregates (total hours per year).
     *
     * @param userId the ID of the user whose training history is being summarized
     * @return list of UserCourseYearSummaryDto, one per year with total hours
     */
    public List<UserCourseYearSummaryDto> summarizeUserCoursesByYear(int userId) {
        Logger logger = LogManager.getLogger(UserCourseBean.class);

        logger.info("User: {} | IP: {} - Summarizing training hours per year for user ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), userId);

        List<UserCourseEntity> participations = userCourseDao.findAllParticipationsInCoursesByUserId(userId);
        return JavaConversionUtil.convertUserCourseEntityToUserCourseYearSummaryDto(participations);
    }







}

