package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SettingsDao;
import aor.projetofinal.entity.SettingsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettingsBeanTest {

    @Mock
    private SettingsDao settingsDao;

    @InjectMocks
    private SettingsBean settingsBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getConfirmationTokenTimeout_returnsValue() {
        SettingsEntity settings = new SettingsEntity();
        settings.setConfirmationTokenTimeout(30);
        when(settingsDao.getSettings()).thenReturn(settings);

        int timeout = settingsBean.getConfirmationTokenTimeout();

        assertEquals(30, timeout);
        verify(settingsDao, times(1)).getSettings();
    }

    @Test
    void getRecoveryTokenTimeout_returnsValue() {
        SettingsEntity settings = new SettingsEntity();
        settings.setRecoveryTokenTimeout(20);
        when(settingsDao.getSettings()).thenReturn(settings);

        int timeout = settingsBean.getRecoveryTokenTimeout();

        assertEquals(20, timeout);
        verify(settingsDao, times(1)).getSettings();
    }

    @Test
    void getSessionTimeoutMinutes_returnsValue() {
        SettingsEntity settings = new SettingsEntity();
        settings.setSessionTokenTimeout(45);
        when(settingsDao.getSettings()).thenReturn(settings);

        int timeout = settingsBean.getSessionTimeoutMinutes();

        assertEquals(45, timeout);
        verify(settingsDao, times(1)).getSettings();
    }

    @Test
    void getSettings_returnsSettingsEntity() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);

        SettingsEntity result = settingsBean.getSettings();

        assertEquals(settings, result);
        verify(settingsDao, times(1)).getSettings();
    }

    @Test
    void updateConfirmationTokenTimeout_successfulUpdate() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);

        boolean result = settingsBean.updateConfirmationTokenTimeout(15);

        assertTrue(result);
        assertEquals(15, settings.getConfirmationTokenTimeout());
        verify(settingsDao, times(1)).save(settings);
    }

    @Test
    void updateConfirmationTokenTimeout_exceptionReturnsFalse() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);
        doThrow(new RuntimeException("DB error")).when(settingsDao).save(settings);

        boolean result = settingsBean.updateConfirmationTokenTimeout(15);

        assertFalse(result);
        verify(settingsDao, times(1)).save(settings);
    }

    @Test
    void updateRecoveryTokenTimeout_successfulUpdate() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);

        boolean result = settingsBean.updateRecoveryTokenTimeout(10);

        assertTrue(result);
        assertEquals(10, settings.getRecoveryTokenTimeout());
        verify(settingsDao, times(1)).save(settings);
    }

    @Test
    void updateRecoveryTokenTimeout_exceptionReturnsFalse() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);
        doThrow(new RuntimeException("DB error")).when(settingsDao).save(settings);

        boolean result = settingsBean.updateRecoveryTokenTimeout(10);

        assertFalse(result);
        verify(settingsDao, times(1)).save(settings);
    }

    @Test
    void updateSessionTimeoutMinutes_successfulUpdate() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);

        boolean result = settingsBean.updateSessionTimeoutMinutes(25);

        assertTrue(result);
        assertEquals(25, settings.getSessionTokenTimeout());
        verify(settingsDao, times(1)).save(settings);
    }

    @Test
    void updateSessionTimeoutMinutes_exceptionReturnsFalse() {
        SettingsEntity settings = new SettingsEntity();
        when(settingsDao.getSettings()).thenReturn(settings);
        doThrow(new RuntimeException("DB error")).when(settingsDao).save(settings);

        boolean result = settingsBean.updateSessionTimeoutMinutes(25);

        assertFalse(result);
        verify(settingsDao, times(1)).save(settings);
    }
}
