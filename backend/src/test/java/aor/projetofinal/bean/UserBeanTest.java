package aor.projetofinal.bean;

import aor.projetofinal.dao.*;
import aor.projetofinal.dto.LoginUserDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.entity.*;
import aor.projetofinal.entity.RoleEntity;
import aor.projetofinal.entity.enums.UsualWorkPlaceEnum;
import aor.projetofinal.util.JavaConversionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserBeanTest {

    @Mock
    UserDao userDao;

    @Mock
    RoleDao roleDao;

    @Mock
    SessionTokenDao sessionTokenDao;

    @Mock
    ProfileDao profileDao;

    @Mock
    EvaluationDao evaluationDao;

    @Mock
    EvaluationCycleDao evaluationCycleDao;

    @Mock
    JavaConversionUtil javaConversionUtil;

    @Mock
    SettingsBean settingsBean;

    @InjectMocks
    UserBean userBean;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void assignManagerToUser_returnsFalseIfUserOrManagerNull() {
        when(userDao.findByEmail(anyString())).thenReturn(null);

        assertFalse(userBean.assignManagerToUser("user@example.com", "manager@example.com"));
    }

    @Test
    void authorization_returnsTrueWhenValid() {
        UserEntity user = new UserEntity();
        user.setActive(true);

        SessionTokenEntity token = new SessionTokenEntity();
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(sessionTokenDao.findBySessionToken("token")).thenReturn(token);

        boolean authorized = userBean.authorization("token");

        assertTrue(authorized);
    }

    @Test
    void authorization_returnsFalseWhenExpired() {
        UserEntity user = new UserEntity();
        user.setActive(true);

        SessionTokenEntity token = new SessionTokenEntity();
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(10));

        when(sessionTokenDao.findBySessionToken("token")).thenReturn(token);

        boolean authorized = userBean.authorization("token");

        assertFalse(authorized);
    }

    @Test
    void confirmAccount_successfulConfirmation() {
        UserEntity user = new UserEntity();
        user.setConfirmed(false);
        user.setEmail("user@example.com");
        user.setConfirmationTokenExpiry(LocalDateTime.now().plusMinutes(10));

        when(userDao.findByConfirmToken("token")).thenReturn(user);

        boolean confirmed = userBean.confirmAccount("token");

        assertTrue(confirmed);
        assertTrue(user.isConfirmed());
        assertNull(user.getConfirmationToken());
        verify(userDao).save(user);
    }

    @Test
    void confirmAccount_returnsFalseIfAlreadyConfirmed() {
        UserEntity user = new UserEntity();
        user.setConfirmed(true);

        when(userDao.findByConfirmToken("token")).thenReturn(user);

        boolean confirmed = userBean.confirmAccount("token");

        assertFalse(confirmed);
    }


    @Test
    void findUserByEmail_returnsNullIfNotFound() {
        when(userDao.findByEmail("email@example.com")).thenReturn(null);

        UserDto result = userBean.findUserByEmail("email@example.com");

        assertNull(result);
    }

    @Test
    void resetPasswordWithToken_success() {
        UserEntity user = new UserEntity();
        user.setRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(5));
        user.setEmail("email@example.com");

        when(userDao.findByRecoveryToken("token")).thenReturn(user);

        boolean result = userBean.resetPasswordWithToken("token", "newPassword");

        assertTrue(result);
        verify(userDao).save(user);
        assertNull(user.getRecoveryToken());
        assertNull(user.getRecoveryTokenExpiry());
    }

    @Test
    void resetPasswordWithToken_failsIfTokenExpired() {
        UserEntity user = new UserEntity();
        user.setRecoveryTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(userDao.findByRecoveryToken("token")).thenReturn(user);

        boolean result = userBean.resetPasswordWithToken("token", "newPassword");

        assertFalse(result);
    }
}
