package aor.projetofinal.bean;

import aor.projetofinal.dao.CourseDao;
import aor.projetofinal.dao.UserCourseDao;
import aor.projetofinal.dto.CreateUserCourseDto;
import aor.projetofinal.dto.UserCourseDto;
import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.UserCourseEntity;
import aor.projetofinal.entity.UserCourseIdEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

    /**
     * Registers a user's participation in a course.
     * Only allows association if the course exists and is active, and if the user is not already registered.
     *
     * @param dto The DTO containing user, course, and participation date.
     * @throws IllegalArgumentException if validation fails.
     */
    @Transactional
    public void addUserCourse(CreateUserCourseDto dto) {
        // Validate: course must exist and be active
        CourseEntity course = courseDao.findById(dto.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course does not exist.");
        }
        if (!course.isActive()) {
            throw new IllegalArgumentException("Course is not active.");
        }

        // Validate: user-course association must not already exist
        UserCourseIdEntity id = new UserCourseIdEntity(dto.getUserId(), dto.getCourseId());
        if (userCourseDao.findById(id) != null) {
            throw new IllegalArgumentException("User is already registered for this course.");
        }

        // Create new user-course entity
        UserCourseEntity uc = new UserCourseEntity();
        uc.setId(id);
        uc.setParticipationDate(dto.getParticipationDate());
        // Set UserEntity and CourseEntity references as needed in your project

        userCourseDao.save(uc);
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
}

