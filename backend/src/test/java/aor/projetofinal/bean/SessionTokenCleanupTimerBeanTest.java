package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.SessionTokenDao;
import aor.projetofinal.entity.SessionTokenEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionTokenCleanupTimerBeanTest {

    @Mock
    private SessionTokenDao sessionTokenDao;

    @Mock
    private UserBean userBean;

    @InjectMocks
    private SessionTokenCleanupTimerBean cleanupTimerBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock static RequestContext.getIp() if needed,
        // otherwise it will be null or default value in logs
    }


    @Test
    void cleanupExpiredTokens_noTokens_noForcedLogout() {
        when(sessionTokenDao.findExpiredSessionTokens(any(LocalDateTime.class)))
                .thenReturn(List.of());

        cleanupTimerBean.cleanupExpiredTokens();

        verify(userBean, never()).forcedLogout(any());
        verify(sessionTokenDao, times(1)).findExpiredSessionTokens(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokens_nullList_noForcedLogout() {
        when(sessionTokenDao.findExpiredSessionTokens(any(LocalDateTime.class)))
                .thenReturn(null);

        cleanupTimerBean.cleanupExpiredTokens();

        verify(userBean, never()).forcedLogout(any());
        verify(sessionTokenDao, times(1)).findExpiredSessionTokens(any(LocalDateTime.class));
    }
}
