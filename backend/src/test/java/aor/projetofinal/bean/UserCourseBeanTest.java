package aor.projetofinal.bean;

import aor.projetofinal.dao.CourseDao;
import aor.projetofinal.dao.UserCourseDao;
import aor.projetofinal.dto.CreateUserCourseDto;
import aor.projetofinal.dto.UserCourseDto;
import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.UserCourseEntity;
import aor.projetofinal.entity.UserCourseIdEntity;
import aor.projetofinal.entity.UserEntity;

import aor.projetofinal.entity.CourseEntity;
import aor.projetofinal.entity.enums.CourseCategoryEnum;
import aor.projetofinal.entity.enums.LanguageEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserCourseBeanTest {

    @Mock
    private CourseDao courseDao;

    @Mock
    private UserCourseDao userCourseDao;

    @InjectMocks
    private UserCourseBean userCourseBean;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUserCourse_success() {
        CreateUserCourseDto dto = new CreateUserCourseDto();
        dto.setUserId(1);
        dto.setCourseId(10);
        dto.setParticipationDate(LocalDateTime.now());

        CourseEntity course = new CourseEntity();
        course.setId(10);
        course.setActive(true);

        when(courseDao.findById(10)).thenReturn(course);
        when(userCourseDao.findById(new UserCourseIdEntity(1,10))).thenReturn(null);

        assertDoesNotThrow(() -> userCourseBean.addUserCourse(dto));
        verify(userCourseDao, times(1)).save(any(UserCourseEntity.class));
    }

    @Test
    void addUserCourse_throwsIfCourseNotExists() {
        CreateUserCourseDto dto = new CreateUserCourseDto();
        dto.setUserId(1);
        dto.setCourseId(10);

        when(courseDao.findById(10)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userCourseBean.addUserCourse(dto));
        assertEquals("Course does not exist.", ex.getMessage());
    }

    @Test
    void addUserCourse_throwsIfCourseNotActive() {
        CreateUserCourseDto dto = new CreateUserCourseDto();
        dto.setUserId(1);
        dto.setCourseId(10);

        CourseEntity course = new CourseEntity();
        course.setId(10);
        course.setActive(false);

        when(courseDao.findById(10)).thenReturn(course);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userCourseBean.addUserCourse(dto));
        assertEquals("Course is not active.", ex.getMessage());
    }

    @Test
    void addUserCourse_throwsIfUserAlreadyRegistered() {
        CreateUserCourseDto dto = new CreateUserCourseDto();
        dto.setUserId(1);
        dto.setCourseId(10);

        CourseEntity course = new CourseEntity();
        course.setId(10);
        course.setActive(true);

        UserCourseEntity existing = new UserCourseEntity();

        when(courseDao.findById(10)).thenReturn(course);
        when(userCourseDao.findById(new UserCourseIdEntity(1, 10))).thenReturn(existing);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userCourseBean.addUserCourse(dto));
        assertEquals("User is already registered for this course.", ex.getMessage());
    }

    @Test
    void toDto_convertsEntityToDto() {
        UserCourseEntity entity = new UserCourseEntity();

        UserEntity user = new UserEntity();
        user.setId(1);
        entity.setUser(user);

        CourseEntity course = new CourseEntity();
        course.setId(10);
        course.setName("Course Name");
        course.setTimeSpan(5.0);
        course.setLanguage(LanguageEnum.EN);
        course.setCourseCategory(CourseCategoryEnum.BACKEND);
        entity.setCourse(course);

        LocalDateTime now = LocalDateTime.now();
        entity.setParticipationDate(now);

        UserCourseDto dto = userCourseBean.toDto(entity);

        assertEquals(1, dto.getUserId());
        assertEquals(10, dto.getCourseId());
        assertEquals("Course Name", dto.getCourseName());
        assertEquals(5.0, dto.getTimeSpan());
        assertEquals(LanguageEnum.EN, dto.getLanguage());
        assertEquals(CourseCategoryEnum.BACKEND, dto.getCourseCategory());
        assertEquals(now, dto.getParticipationDate());
    }

    @Test
    void listUserCourses_returnsDtos() {
        UserCourseEntity entity = new UserCourseEntity();
        UserEntity user = new UserEntity();
        user.setId(1);
        entity.setUser(user);
        CourseEntity course = new CourseEntity();
        course.setId(10);
        course.setName("Course");
        entity.setCourse(course);
        entity.setParticipationDate(LocalDateTime.now());

        when(userCourseDao.findByUserId(1)).thenReturn(List.of(entity));

        List<UserCourseDto> dtos = userCourseBean.listUserCourses(1);

        assertEquals(1, dtos.size());
        assertEquals("Course", dtos.get(0).getCourseName());
    }

    @Test
    void listUserCoursesByYear_returnsDtos() {
        UserCourseEntity entity = new UserCourseEntity();
        UserEntity user = new UserEntity();
        user.setId(1);
        entity.setUser(user);
        CourseEntity course = new CourseEntity();
        course.setId(10);
        course.setName("Course");
        entity.setCourse(course);
        entity.setParticipationDate(LocalDateTime.now());

        when(userCourseDao.findByUserIdAndYear(1, 2023)).thenReturn(List.of(entity));

        List<UserCourseDto> dtos = userCourseBean.listUserCoursesByYear(1, 2023);

        assertEquals(1, dtos.size());
        assertEquals("Course", dtos.get(0).getCourseName());
    }
}
