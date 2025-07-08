package aor.projetofinal.bean;

import aor.projetofinal.dao.CourseDao;
import aor.projetofinal.dto.CourseDto;
import aor.projetofinal.dto.CreateCourseDto;
import aor.projetofinal.dto.UpdateCourseDto;
import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.enums.LanguageEnum;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic bean for managing courses.
 * Handles creation, update, listing, inactivation, and filtering.
 */
@ApplicationScoped
public class CourseBean {

    @Inject
    private CourseDao courseDao;

    /**
     * Creates a new course from a CreateCourseDto.
     *
     * @param dto The DTO with course data.
     */
    @Transactional
    public void createCourse(CreateCourseDto dto) {
        CourseEntity course = new CourseEntity();
        course.setName(dto.getName());
        course.setTimeSpan(dto.getTimeSpan());
        course.setDescription(dto.getDescription());
        course.setLink(dto.getLink());
        course.setLanguage(dto.getLanguage());
        course.setCourseCategory(dto.getCourseCategory());
        course.setActive(dto.isActive());
        courseDao.save(course);
    }

    /**
     * Updates an existing course.
     *
     * @param dto The DTO with updated course data.
     */
    @Transactional
    public void updateCourse(UpdateCourseDto dto) {
        CourseEntity course = courseDao.findById(dto.getId());
        if (course == null) throw new IllegalArgumentException("Course not found");

        course.setName(dto.getName());
        course.setTimeSpan(dto.getTimeSpan());
        course.setDescription(dto.getDescription());
        course.setLink(dto.getLink());
        course.setLanguage(dto.getLanguage());
        course.setCourseCategory(dto.getCourseCategory());
        course.setActive(dto.isActive());
        courseDao.update(course);
    }

    /**
     * Inactivates (soft deletes) a course by its ID.
     *
     * @param id The course ID.
     */
    @Transactional
    public void deactivateCourse(int id) {
        courseDao.deactivate(id);
    }

    /**
     * Returns a course DTO by its ID.
     *
     * @param id The course ID.
     * @return The CourseDto, or null if not found.
     */
    public CourseDto getCourseById(int id) {
        CourseEntity entity = courseDao.findById(id);
        return entity != null ? toDto(entity) : null;
    }

    /**
     * Lists all courses (active and inactive) as DTOs.
     *
     * @return List of CourseDto.
     */
    public List<CourseDto> listAllCourses() {
        return courseDao.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Lists all active courses as DTOs.
     *
     * @return List of CourseDto.
     */
    public List<CourseDto> listActiveCourses() {
        return courseDao.findActive().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Filters courses by optional criteria and returns as DTOs.
     *
     * @param name       Partial or full name (nullable).
     * @param minTime    Minimum duration (nullable).
     * @param maxTime    Maximum duration (nullable).
     * @param language   Language (nullable).
     * @param category   Category (nullable).
     * @param active     Active state (nullable).
     * @return List of CourseDto matching the filters.
     */
    public List<CourseDto> filterCourses(String name, Double minTime, Double maxTime,
                                         LanguageEnum language, CourseCategoryEnum category, Boolean active) {
        return courseDao.findByFilters(name, minTime, maxTime, language, category, active)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Converts a CourseEntity to a CourseDto.
     *
     * @param course The entity to convert.
     * @return The DTO.
     */
    public CourseDto toDto(CourseEntity course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setTimeSpan(course.getTimeSpan());
        dto.setDescription(course.getDescription());
        dto.setLink(course.getLink());
        dto.setLanguage(course.getLanguage());
        dto.setCourseCategory(course.getCourseCategory());
        dto.setActive(course.isActive());
        return dto;
    }
}
