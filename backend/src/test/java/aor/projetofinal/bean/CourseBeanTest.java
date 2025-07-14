package aor.projetofinal.bean;

import aor.projetofinal.dao.CourseDao;
import aor.projetofinal.dto.CreateCourseDto;
import aor.projetofinal.dto.UpdateCourseDto;
import aor.projetofinal.dto.CourseDto;
import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseBean using Mockito annotations.
 */
class CourseBeanTest {

    @Mock
    private CourseDao courseDaoMock;

    @InjectMocks
    private CourseBean courseBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCourse_shouldSaveCourse() {
        CreateCourseDto dto = new CreateCourseDto();
        dto.setName("Java 101");
        dto.setTimeSpan(10.0);
        dto.setDescription("Introductory Java course");
        dto.setLink("http://java-course.com");
        dto.setLanguage(LanguageEnum.ES);
        dto.setCourseCategory(CourseCategoryEnum.BACKEND);
        dto.setActive(true);

        courseBean.createCourse(dto);

        verify(courseDaoMock, times(1)).save(any(CourseEntity.class));
    }

    @Test
    void updateCourse_shouldUpdateExistingCourse() {
        UpdateCourseDto dto = new UpdateCourseDto();
        dto.setId(1);
        dto.setName("Updated Java");
        dto.setTimeSpan(12.0);
        dto.setDescription("Updated desc");
        dto.setLink("http://updated-link.com");
        dto.setLanguage(LanguageEnum.FR);
        dto.setCourseCategory(CourseCategoryEnum.UX_UI);
        dto.setActive(false);

        CourseEntity existing = new CourseEntity();
        existing.setId(dto.getId());

        when(courseDaoMock.findById(dto.getId())).thenReturn(existing);

        courseBean.updateCourse(dto);

        verify(courseDaoMock, times(1)).update(existing);
        assertEquals(dto.getName(), existing.getName());
    }

    @Test
    void updateCourse_shouldThrowExceptionIfCourseNotFound() {
        UpdateCourseDto dto = new UpdateCourseDto();
        dto.setId(99);

        when(courseDaoMock.findById(dto.getId())).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseBean.updateCourse(dto);
        });

        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    void deactivateCourse_shouldCallDaoDeactivate() {
        int courseId = 42;
        courseBean.deactivateCourse(courseId);
        verify(courseDaoMock, times(1)).deactivate(courseId);
    }

    @Test
    void getCourseById_shouldReturnDtoWhenFound() {
        CourseEntity entity = new CourseEntity();
        entity.setId(100);
        entity.setName("Test Course");
        entity.setActive(true);
        entity.setCourseCategory(CourseCategoryEnum.FRONTEND);
        entity.setLanguage(LanguageEnum.EN);

        when(courseDaoMock.findById(100)).thenReturn(entity);

        CourseDto dto = courseBean.getCourseById(100);

        assertNotNull(dto);
        assertEquals(100, dto.getId());
        assertEquals("Test Course", dto.getName());
        assertTrue(dto.isActive());
        assertEquals(CourseCategoryEnum.FRONTEND, dto.getCourseCategory());
        assertEquals(LanguageEnum.EN, dto.getLanguage());
    }

    @Test
    void getCourseById_shouldReturnNullWhenNotFound() {
        when(courseDaoMock.findById(9999)).thenReturn(null);

        CourseDto dto = courseBean.getCourseById(9999);

        assertNull(dto);
    }

    @Test
    void listAllCourses_shouldReturnListOfDtos() {
        CourseEntity c1 = new CourseEntity();
        c1.setId(1);
        c1.setName("Course1");
        CourseEntity c2 = new CourseEntity();
        c2.setId(2);
        c2.setName("Course2");

        when(courseDaoMock.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<CourseDto> dtos = courseBean.listAllCourses();

        assertEquals(2, dtos.size());
        assertEquals("Course1", dtos.get(0).getName());
        assertEquals("Course2", dtos.get(1).getName());
    }

    @Test
    void listActiveCourses_shouldReturnOnlyActiveCourses() {
        CourseEntity activeCourse = new CourseEntity();
        activeCourse.setId(1);
        activeCourse.setName("Active Course");
        activeCourse.setActive(true);

        when(courseDaoMock.findActive()).thenReturn(List.of(activeCourse));

        List<CourseDto> dtos = courseBean.listActiveCourses();

        assertEquals(1, dtos.size());
        assertTrue(dtos.get(0).isActive());
        assertEquals("Active Course", dtos.get(0).getName());
    }

    @Test
    void filterCourses_shouldReturnFilteredList() {
        CourseEntity filteredCourse = new CourseEntity();
        filteredCourse.setId(10);
        filteredCourse.setName("Filtered Course");

        when(courseDaoMock.findByFilters("filter", 1.0, 5.0, LanguageEnum.EN, CourseCategoryEnum.UX_UI, true))
                .thenReturn(List.of(filteredCourse));

        List<CourseDto> dtos = courseBean.filterCourses("filter", 1.0, 5.0, LanguageEnum.EN, CourseCategoryEnum.UX_UI, true);

        assertEquals(1, dtos.size());
        assertEquals("Filtered Course", dtos.get(0).getName());
    }

    @Test
    void toDto_shouldConvertEntityToDto() {
        CourseEntity entity = new CourseEntity();
        entity.setId(55);
        entity.setName("DTO Course");
        entity.setTimeSpan(3.5);
        entity.setDescription("Description here");
        entity.setLink("http://course.link");
        entity.setLanguage(LanguageEnum.FR);
        entity.setCourseCategory(CourseCategoryEnum.UX_UI);
        entity.setActive(true);

        CourseDto dto = courseBean.toDto(entity);

        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getTimeSpan(), dto.getTimeSpan());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getLink(), dto.getLink());
        assertEquals(entity.getLanguage(), dto.getLanguage());
        assertEquals(entity.getCourseCategory(), dto.getCourseCategory());
        assertTrue(dto.isActive());
    }


}
