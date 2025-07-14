package aor.projetofinal.bean;

import aor.projetofinal.dao.NotificationDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.NotificationDto;
import aor.projetofinal.entity.NotificationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.NotificationEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationBeanTest {

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private NotificationBean notificationBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void countUnreadNotificationsByType_userNotFound_returnsEmptyMap() {
        when(userDao.findById(1)).thenReturn(null);

        Map<NotificationEnum, Integer> result = notificationBean.countUnreadNotificationsByType(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void countUnreadNotificationsByType_userFound_returnsMap() {
        UserEntity user = new UserEntity();
        when(userDao.findById(1)).thenReturn(user);
        when(notificationDao.countUnreadByType(user)).thenReturn(Map.of(NotificationEnum.MESSAGE, 5));

        Map<NotificationEnum, Integer> result = notificationBean.countUnreadNotificationsByType(1);

        assertEquals(1, result.size());
        assertEquals(5, result.get(NotificationEnum.MESSAGE));
    }

    @Test
    void createNotification_validUserAndType_savesNotification() {
        UserEntity user = new UserEntity();
        when(userDao.findById(1)).thenReturn(user);

        boolean result = notificationBean.createNotification(1, "MESSAGE", "Test message");

        assertTrue(result);
        verify(notificationDao).save(any(NotificationEntity.class));
    }

    @Test
    void createNotification_invalidUser_returnsFalse() {
        when(userDao.findById(99)).thenReturn(null);

        boolean result = notificationBean.createNotification(99, "MESSAGE", "Test message");

        assertFalse(result);
        verify(notificationDao, never()).save(any());
    }

    @Test
    void createNotification_invalidType_returnsFalse() {
        UserEntity user = new UserEntity();
        when(userDao.findById(1)).thenReturn(user);

        boolean result = notificationBean.createNotification(1, "INVALID_TYPE", "Test message");

        assertFalse(result);
        verify(notificationDao, never()).save(any());
    }

    @Test
    void getNotificationsForUser_userNotFound_returnsEmptyList() {
        when(userDao.findById(1)).thenReturn(null);

        List<NotificationDto> result = notificationBean.getNotificationsForUser(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }



    @Test
    void getUnreadNonMessageNotificationsForUser_userNotFound_returnsEmptyList() {
        when(userDao.findById(1)).thenReturn(null);

        List<NotificationDto> result = notificationBean.getUnreadNonMessageNotificationsForUser(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void getUnreadNotificationsForUser_userNotFound_returnsEmptyList() {
        when(userDao.findById(1)).thenReturn(null);

        List<NotificationDto> result = notificationBean.getUnreadNotificationsForUser(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }



    @Test
    void markAllMessageNotificationsAsRead_userNotFound_returnsZero() {
        when(userDao.findById(1)).thenReturn(null);

        int result = notificationBean.markAllMessageNotificationsAsRead(1);

        assertEquals(0, result);
    }

    @Test
    void markAllMessageNotificationsAsRead_userFound_callsDao() {
        UserEntity user = new UserEntity();
        when(userDao.findById(1)).thenReturn(user);
        when(notificationDao.markAllMessageNotificationsAsRead(user)).thenReturn(3);

        int result = notificationBean.markAllMessageNotificationsAsRead(1);

        assertEquals(3, result);
        verify(notificationDao).markAllMessageNotificationsAsRead(user);
    }

    @Test
    void markAllNotificationsAsRead_userNotFound_returnsZero() {
        when(userDao.findById(1)).thenReturn(null);

        int result = notificationBean.markAllNotificationsAsRead(1);

        assertEquals(0, result);
    }

    @Test
    void markAllNotificationsAsRead_userFound_callsDao() {
        UserEntity user = new UserEntity();
        when(userDao.findById(1)).thenReturn(user);
        when(notificationDao.markAllAsRead(user)).thenReturn(5);

        int result = notificationBean.markAllNotificationsAsRead(1);

        assertEquals(5, result);
        verify(notificationDao).markAllAsRead(user);
    }

    @Test
    void toDto_nullEntity_returnsNull() {
        NotificationDto dto = notificationBean.toDto(null);
        assertNull(dto);
    }


}
