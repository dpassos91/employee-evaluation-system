package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.entity.*;
import aor.projetofinal.entity.enums.EvaluationStateEnum;

import aor.projetofinal.util.EmailUtil;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluationCycleBeanTest {

    @Mock
    private EvaluationCycleDao evaluationCycleDao;

    @Mock
    private UserDao userDao;

    @Mock
    private EvaluationDao evaluationDao;

    @Mock
    private NotificationBean notificationBean;

    @InjectMocks
    private EvaluationCycleBean evaluationCycleBean;

    @Captor
    private ArgumentCaptor<EvaluationEntity> evaluationCaptor;

    @Captor
    private ArgumentCaptor<EvaluationCycleEntity> cycleCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findActiveCycle_returnsCycle_whenFound() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);
        when(evaluationCycleDao.findActiveCycle()).thenReturn(cycle);

        EvaluationCycleEntity result = evaluationCycleBean.findActiveCycle();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(evaluationCycleDao).findActiveCycle();
    }

    @Test
    void findActiveCycle_returnsNull_whenNoneActive() {
        when(evaluationCycleDao.findActiveCycle()).thenReturn(null);

        EvaluationCycleEntity result = evaluationCycleBean.findActiveCycle();

        assertNull(result);
        verify(evaluationCycleDao).findActiveCycle();
    }


    @Test
    void createCycleAndCreateBlankEvaluations_createsCycleAndEvaluations() {
        LocalDate endDate = LocalDate.now().plusDays(30);

        UserEntity user = new UserEntity();
        UserEntity manager = new UserEntity();
        user.setManager(manager);
        user.setProfile(new ProfileEntity());
        user.getProfile().setFirstName("John");
        user.getProfile().setLastName("Doe");
        manager.setId(2);

        when(userDao.findConfirmedUsersWithManager()).thenReturn(List.of(user));

        evaluationCycleBean.createCycleAndCreateBlankEvaluations(endDate);

        verify(evaluationCycleDao).create(any(EvaluationCycleEntity.class));
        verify(evaluationDao).create(any(EvaluationEntity.class));
        verify(notificationBean).createNotification(eq(manager.getId()), eq("SYSTEM"), anyString());
    }


}
