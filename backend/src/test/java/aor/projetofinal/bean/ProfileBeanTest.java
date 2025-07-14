package aor.projetofinal.bean;

import aor.projetofinal.dao.ProfileDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.FlatProfileDto;
import aor.projetofinal.dto.PaginatedProfilesDto;
import aor.projetofinal.dto.ProfileDto;
import aor.projetofinal.entity.ProfileEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
import aor.projetofinal.util.JavaConversionUtil;
import aor.projetofinal.util.ProfileValidator;
import aor.projetofinal.util.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileBeanTest {

    @Mock
    private ProfileDao profileDao;

    @Mock
    private UserDao userDao;

    @Mock
    private JavaConversionUtil javaConversionUtil;

    @InjectMocks
    private ProfileBean profileBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void changePhotographOnProfile_successfulUpdate() {
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");
        ProfileEntity profile = new ProfileEntity();
        user.setProfile(profile);

        boolean result = profileBean.changePhotographOnProfile(user, "newphoto.png");

        assertTrue(result);
        assertEquals("newphoto.png", profile.getPhotograph());
        verify(userDao).save(user);
    }

    @Test
    void changePhotographOnProfile_nullUser_returnsFalse() {
        boolean result = profileBean.changePhotographOnProfile(null, "photo.png");
        assertFalse(result);
    }

    @Test
    void convertToDto_nullEntity_returnsNull() {
        assertNull(profileBean.convertToDto(null));
    }


    @Test
    void findProfilesWithFilters_returnsConvertedList() {
        List<ProfileEntity> entities = List.of(new ProfileEntity());
        when(profileDao.findProfilesWithFilters("name", UsualWorkPlaceEnum.COIMBRA, "manager@example.com")).thenReturn(entities);

        // Mock static utility conversion
        try (MockedStatic<JavaConversionUtil> util = mockStatic(JavaConversionUtil.class)) {
            util.when(() -> JavaConversionUtil.convertProfileEntityListToFlatProfileDtoList(entities))
                    .thenReturn(new ArrayList<>());

            var result = profileBean.findProfilesWithFilters("name", UsualWorkPlaceEnum.COIMBRA, "manager@example.com");
            assertNotNull(result);
            util.verify(() -> JavaConversionUtil.convertProfileEntityListToFlatProfileDtoList(entities));
        }
    }

    @Test
    void findProfilesWithFiltersPaginated_returnsPaginatedDto() {
        List<ProfileEntity> entities = List.of(new ProfileEntity());
        when(profileDao.findProfilesWithFiltersPaginated("name", UsualWorkPlaceEnum.COIMBRA, "manager@example.com", 1)).thenReturn(entities);
        when(profileDao.countProfilesWithFilters("name", UsualWorkPlaceEnum.COIMBRA, "manager@example.com")).thenReturn(15L);

        try (MockedStatic<JavaConversionUtil> util = mockStatic(JavaConversionUtil.class)) {
            util.when(() -> JavaConversionUtil.convertProfileEntityListToFlatProfileDtoList(entities))
                    .thenReturn(new ArrayList<>());

            PaginatedProfilesDto result = profileBean.findProfilesWithFiltersPaginated("name", UsualWorkPlaceEnum.COIMBRA, "manager@example.com", 1);

            assertNotNull(result);
            assertEquals(15L, result.getTotalCount());
            assertEquals(2, result.getTotalPages());
            util.verify(() -> JavaConversionUtil.convertProfileEntityListToFlatProfileDtoList(entities));
        }
    }



    @Test
    void updateProfile_userNotFound_returnsFalse() {
        when(userDao.findByEmail("unknown@example.com")).thenReturn(null);
        ProfileDto dto = new ProfileDto();
        boolean result = profileBean.updateProfile(dto, "unknown@example.com");
        assertFalse(result);
    }

    @Test
    void updateProfile_invalidUsualWorkplace_returnsFalse() {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        when(userDao.findByEmail(email)).thenReturn(user);
        ProfileDto dto = new ProfileDto();
        dto.setUsualWorkplace("INVALID");

        boolean result = profileBean.updateProfile(dto, email);
        assertFalse(result);
    }

    @Test
    void updateProfile_missingUsualWorkplace_returnsFalse() {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        when(userDao.findByEmail(email)).thenReturn(user);
        ProfileDto dto = new ProfileDto();
        dto.setUsualWorkplace(null);

        boolean result = profileBean.updateProfile(dto, email);
        assertFalse(result);
    }


    @Test
    void updateProfilePhoto_success() {
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");
        ProfileEntity profile = new ProfileEntity();
        user.setProfile(profile);

        boolean result = profileBean.updateProfilePhoto(user, "photo.png");

        assertTrue(result);
        assertEquals("photo.png", profile.getPhotograph());
        verify(profileDao).save(profile);
    }

    @Test
    void updateProfilePhoto_nullUser_returnsFalse() {
        boolean result = profileBean.updateProfilePhoto(null, "photo.png");
        assertFalse(result);
    }

    @Test
    void updateProfilePhoto_noProfile_returnsFalse() {
        UserEntity user = new UserEntity();
        user.setProfile(null);

        boolean result = profileBean.updateProfilePhoto(user, "photo.png");
        assertFalse(result);
    }

    @Test
    void findProfileByUserId_returnsProfile() {
        ProfileEntity profile = new ProfileEntity();
        when(profileDao.findByUserId(1)).thenReturn(profile);

        ProfileEntity result = profileBean.findProfileByUserId(1);

        assertEquals(profile, result);
    }
}
