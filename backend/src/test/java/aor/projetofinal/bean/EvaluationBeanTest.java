package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.*;
import aor.projetofinal.entity.enums.*;
import aor.projetofinal.util.JavaConversionUtil;
import jakarta.ejb.Stateless;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluationBeanTest {

    @Mock
    private EvaluationDao evaluationDao;

    @Mock
    private EvaluationCycleBean evaluationCycleBean;

    @InjectMocks
    private EvaluationBean evaluationBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock static RequestContext methods if needed or use real if safe
        // For example, mock RequestContext.getAuthor() and getIp() if they are static.
    }

    @Test
    void testAreAllEvaluationsInActiveCycleEvaluated_AllTrue() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);

        when(evaluationCycleBean.findActiveCycle()).thenReturn(cycle);

        EvaluationEntity e1 = mock(EvaluationEntity.class);
        when(e1.getState()).thenReturn(EvaluationStateEnum.EVALUATED);

        EvaluationEntity e2 = mock(EvaluationEntity.class);
        when(e2.getState()).thenReturn(EvaluationStateEnum.EVALUATED);

        when(evaluationDao.findAllEvaluationsByCycle(cycle)).thenReturn(List.of(e1, e2));

        boolean result = evaluationBean.areAllEvaluationsInActiveCycleEvaluated();

        assertTrue(result);
        verify(evaluationCycleBean).findActiveCycle();
        verify(evaluationDao).findAllEvaluationsByCycle(cycle);
    }

    @Test
    void testAreAllEvaluationsInActiveCycleEvaluated_SomeNotEvaluated() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);

        when(evaluationCycleBean.findActiveCycle()).thenReturn(cycle);

        EvaluationEntity e1 = mock(EvaluationEntity.class);
        when(e1.getState()).thenReturn(EvaluationStateEnum.EVALUATED);

        EvaluationEntity e2 = mock(EvaluationEntity.class);
        when(e2.getState()).thenReturn(EvaluationStateEnum.IN_EVALUATION);

        when(evaluationDao.findAllEvaluationsByCycle(cycle)).thenReturn(List.of(e1, e2));

        boolean result = evaluationBean.areAllEvaluationsInActiveCycleEvaluated();

        assertFalse(result);
    }

    @Test
    void testBuildEvaluationDtoCorrespondingToTheEvaluation() {
        UserEntity evaluated = new UserEntity();
        evaluated.setId(1);
        evaluated.setEmail("evaluated@example.com");
        evaluated.setProfile(new ProfileEntity());
        evaluated.getProfile().setFirstName("John");
        evaluated.getProfile().setLastName("Doe");
        evaluated.getProfile().setPhotograph("photo.png");

        UserEntity evaluator = new UserEntity();
        evaluator.setEmail("evaluator@example.com");
        evaluator.setProfile(new ProfileEntity());
        evaluator.getProfile().setFirstName("Jane");
        evaluator.getProfile().setLastName("Smith");

        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setId(10L);
        evaluation.setEvaluated(evaluated);
        evaluation.setEvaluator(evaluator);
        evaluation.setGrade(GradeEvaluationEnum.getEnumfromGrade(3));
        evaluation.setFeedback("Good work");

        UpdateEvaluationDto dto = evaluationBean.buildEvaluationDtoCorrespondingToTheEvaluation(evaluation);

        assertEquals("evaluated@example.com", dto.getEvaluatedEmail());
        assertEquals(1, dto.getEvaluatedId());
        assertEquals("John Doe", dto.getEvaluatedName());
        assertEquals("photo.png", dto.getPhotograph());
        assertEquals(3, dto.getGrade());
        assertEquals("Good work", dto.getFeedback());
        assertEquals("evaluator@example.com", dto.getEvaluatorEmail());
        assertEquals("Jane Smith", dto.getEvaluatorName());
    }

    @Test
    void testFindEvaluationById_CallsDao() {
        Long id = 5L;
        EvaluationEntity entity = new EvaluationEntity();
        when(evaluationDao.findById(id)).thenReturn(entity);

        EvaluationEntity result = evaluationBean.findEvaluationById(id);

        assertEquals(entity, result);
        verify(evaluationDao).findById(id);
    }

    @Test
    void testGetIncompleteEvaluationsForCycle() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);
        List<EvaluationEntity> incomplete = new ArrayList<>();
        when(evaluationDao.findIncompleteEvaluationsByCycle(cycle)).thenReturn(incomplete);

        List<EvaluationEntity> result = evaluationBean.getIncompleteEvaluationsForCycle(cycle);

        assertSame(incomplete, result);
        verify(evaluationDao).findIncompleteEvaluationsByCycle(cycle);
    }

    @Test
    void testFindEvaluationByCycleAndUser_Found() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);

        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");

        EvaluationEntity evaluation = new EvaluationEntity();
        when(evaluationDao.findEvaluationByCycleAndUser(cycle, user)).thenReturn(evaluation);

        EvaluationEntity result = evaluationBean.findEvaluationByCycleAndUser(cycle, user);

        assertEquals(evaluation, result);
    }

    @Test
    void testFindEvaluationByCycleAndUser_NotFound() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);

        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");

        when(evaluationDao.findEvaluationByCycleAndUser(cycle, user)).thenReturn(null);

        EvaluationEntity result = evaluationBean.findEvaluationByCycleAndUser(cycle, user);

        assertNull(result);
    }

    @Test
    void testListUsersWithIncompleteEvaluationsFromLastCycle_ActiveCycleNull() {
        when(evaluationCycleBean.findActiveCycle()).thenReturn(null);

        UsersWithIncompleteEvaluationsDto dto = evaluationBean.listUsersWithIncompleteEvaluationsFromLastCycle();

        assertNotNull(dto);
        assertTrue(dto.getUsers() == null || dto.getUsers().isEmpty());
        assertEquals(0, dto.getTotalUsersWithIncompleteEvaluations());
    }

    @Test
    void testListUsersWithIncompleteEvaluationsFromLastCycle_Found() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        cycle.setId(1L);

        when(evaluationCycleBean.findActiveCycle()).thenReturn(cycle);

        UserEntity user1 = new UserEntity();
        user1.setId(1);
        user1.setEmail("u1@example.com");

        UserEntity user2 = new UserEntity();
        user2.setId(2);
        user2.setEmail("u2@example.com");

        EvaluationEntity eval1 = new EvaluationEntity();
        eval1.setEvaluated(user1);
        EvaluationEntity eval2 = new EvaluationEntity();
        eval2.setEvaluated(user2);

        List<EvaluationEntity> incompleteList = List.of(eval1, eval2);
        when(evaluationBean.getIncompleteEvaluationsForCycle(cycle)).thenReturn(incompleteList);

        // Mock JavaConversionUtil.convertUserEntityToUserDto
        try (MockedStatic<JavaConversionUtil> utilities = mockStatic(JavaConversionUtil.class)) {
            utilities.when(() -> JavaConversionUtil.convertUserEntityToUserDto(user1))
                    .thenReturn(new UserDto());
            utilities.when(() -> JavaConversionUtil.convertUserEntityToUserDto(user2))
                    .thenReturn(new UserDto());

            UsersWithIncompleteEvaluationsDto dto = evaluationBean.listUsersWithIncompleteEvaluationsFromLastCycle();

            assertEquals(2, dto.getTotalUsersWithIncompleteEvaluations());
            assertEquals(2, dto.getUsers().size());
        }
    }

    @Test
    void testRevertEvaluationToInEvaluation_Fail() {
        EvaluationEntity eval = new EvaluationEntity();
        eval.setId(1L);
        eval.setState(EvaluationStateEnum.IN_EVALUATION);

        boolean result = evaluationBean.revertEvaluationToInEvaluation(eval);

        assertFalse(result);
        verify(evaluationDao, never()).save(any());
    }

    @Test
    void testUpdateEvaluationWithGradeAndFeedback_WithFeedback() {
        EvaluationEntity eval = new EvaluationEntity();
        UserEntity evaluator = new UserEntity();
        UserEntity evaluated = new UserEntity();
        evaluated.setEmail("eval@example.com");
        eval.setEvaluated(evaluated);

        UpdateEvaluationDto dto = new UpdateEvaluationDto();
        dto.setGrade(3);
        dto.setFeedback("Good");
        dto.setEvaluatedEmail("eval@example.com");

        evaluationBean.updateEvaluationWithGradeAndFeedback(dto, eval, evaluator);

        assertEquals(EvaluationStateEnum.EVALUATED, eval.getState());
        verify(evaluationDao).save(eval);
    }

    @Test
    void testUpdateEvaluationWithGradeAndFeedback_WithoutFeedback() {
        EvaluationEntity eval = new EvaluationEntity();
        UserEntity evaluator = new UserEntity();
        UserEntity evaluated = new UserEntity();
        evaluated.setEmail("eval@example.com");
        eval.setEvaluated(evaluated);

        UpdateEvaluationDto dto = new UpdateEvaluationDto();
        dto.setGrade(3);
        dto.setFeedback("  ");  // blank feedback

        evaluationBean.updateEvaluationWithGradeAndFeedback(dto, eval, evaluator);

        assertEquals(EvaluationStateEnum.IN_EVALUATION, eval.getState());
        verify(evaluationDao).save(eval);
    }

    @Test
    void testAlreadyEvaluatedAtCurrentCycle() {
        EvaluationCycleEntity cycle = new EvaluationCycleEntity();
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");

        when(evaluationDao.alreadyEvaluatedAtCurrentCycle(cycle, user)).thenReturn(true);

        boolean result = evaluationBean.alreadyEvaluatedAtCurrentCycle(cycle, user);

        assertTrue(result);
        verify(evaluationDao).alreadyEvaluatedAtCurrentCycle(cycle, user);
    }
}
