package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dto.UpdateEvaluationDto;
import aor.projetofinal.dto.EvaluationOptionsDto;
import aor.projetofinal.dto.UserDto;
import aor.projetofinal.dto.UsersWithIncompleteEvaluationsDto;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateType;
import aor.projetofinal.entity.enums.GradeEvaluationType;
import aor.projetofinal.util.JavaConversionUtil;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Stateless
public class EvaluationBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(EvaluationBean.class);

    @Inject
    private EvaluationDao evaluationDao;

    @Inject
    private EvaluationCycleBean evaluationCycleBean;


    //method to list all evaluation options for the dropdown menu in the frontend
    public List<EvaluationOptionsDto> listEvaluationOptions() {
        List<EvaluationOptionsDto> list = new ArrayList<>();
        //GradeEvaluationType.values returns all the enum values as an array:
        for (GradeEvaluationType evaluation : GradeEvaluationType.values()) {
            list.add(new EvaluationOptionsDto(
                    //dto enum name
                    evaluation.name(),
                    //dto grade
                    evaluation.getGrade(),
                    //dto label
                    evaluation.getGrade() + " - " + evaluation.getDescription()
            ));
        }

        return list;
    }

    public EvaluationEntity findEvaluationById(Long id) {
        return evaluationDao.findById(id);
    }


    public List<EvaluationEntity> getIncompleteEvaluationsForCycle(EvaluationCycleEntity cycle) {
        return evaluationDao.findIncompleteEvaluationsByCycle(cycle);
    }


    public EvaluationEntity findEvaluationByCycleAndUser(EvaluationCycleEntity cycle, UserEntity evaluated) {
        EvaluationEntity evaluation = evaluationDao.findEvaluationByCycleAndUser(cycle, evaluated);

        if (evaluation == null) {
            logger.warn("No evaluation found for user {} in cycle ID {}.",
                    evaluated.getEmail(), cycle.getId());
            return null;
        }

        logger.info("Evaluation found for user {} in cycle ID {}.",
                evaluated.getEmail(), cycle.getId());

        return evaluation;
    }


    public UsersWithIncompleteEvaluationsDto listUsersWithIncompleteEvaluationsFromLastCycle() {
        logger.info("User: {} | IP: {} - Listing users with incomplete evaluations from previous cycle.",
                RequestContext.getAuthor(), RequestContext.getIp());

        // get current active cycle
        EvaluationCycleEntity activeCycle = evaluationCycleBean.findActiveCycle();
        if (activeCycle == null) {
            logger.warn("User: {} | IP: {} - No active evaluation cycle found.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return new UsersWithIncompleteEvaluationsDto(); //returns empty DTO
        }

        // Get incomplete evaluations
        List<EvaluationEntity> incompleteEvaluations = getIncompleteEvaluationsForCycle(activeCycle);

        // Extract evaluated users, without duplicates, as it extends the set interface
        Set<UserEntity> uniqueUsers = new HashSet<>();
        for (EvaluationEntity evaluation : incompleteEvaluations) {
            uniqueUsers.add(evaluation.getEvaluated());
        }

        // convert to the appropriate DTO
        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity user : uniqueUsers) {
            userDtos.add(JavaConversionUtil.convertUserEntityToUserDto(user));
        }

        // cretae the DTO to return, with users with incomplete evaluations
        UsersWithIncompleteEvaluationsDto dto = new UsersWithIncompleteEvaluationsDto();
        dto.setUsers(userDtos);
        dto.setTotalUsersWithIncompleteEvaluations(userDtos.size());

        logger.info("User: {} | IP: {} - Found {} users with incomplete evaluations.",
                RequestContext.getAuthor(), RequestContext.getIp(), userDtos.size());

        return dto;
    }


    public void updateEvaluationWithGradeAndFeedback(UpdateEvaluationDto updateEvaluationDto,
                                                     EvaluationEntity evaluation,
                                                     UserEntity evaluator) {


        evaluation.setGrade(GradeEvaluationType.getEnumfromGrade(updateEvaluationDto.getGrade()));
        evaluation.setFeedback(updateEvaluationDto.getFeedback());
        evaluation.setDate(LocalDateTime.now());
        evaluation.setEvaluator(evaluator);


        // Update the evaluation state only if feedback was provided
        if (updateEvaluationDto.getFeedback() != null && !updateEvaluationDto.getFeedback().trim().isEmpty()) {
            evaluation.setState(EvaluationStateType.EVALUATED);
        } else {
            evaluation.setState(EvaluationStateType.IN_EVALUATION);
        }

        evaluationDao.save(evaluation);

        logger.info("Evaluation updated for user {} by {}.",
                evaluation.getEvaluated().getEmail(), evaluator.getEmail());
    }


    public boolean alreadyEvaluatedAtCurrentCycle(EvaluationCycleEntity cycle, UserEntity evaluated) {
        return evaluationDao.alreadyEvaluatedAtCurrentCycle(cycle, evaluated);
    }


}