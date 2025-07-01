package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dto.CreateEvaluationDto;
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


    public List<EvaluationEntity> getIncompleteEvaluationsForCycle(EvaluationCycleEntity cycle) {
        return evaluationDao.findIncompleteEvaluationsByCycle(cycle);
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






    public void createEvaluation(CreateEvaluationDto createEvaluationDto,
                                 EvaluationCycleEntity cycle,
                                 UserEntity evaluated,
                                 UserEntity evaluator) {


        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setGrade(GradeEvaluationType.getEnumfromGrade(createEvaluationDto.getGrade()));
        evaluation.setFeedback(createEvaluationDto.getFeedback());
        evaluation.setDate(LocalDateTime.now());
        evaluation.setState(EvaluationStateType.EVALUATED);
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluated(evaluated);
        evaluation.setCycle(cycle);

        evaluationDao.create(evaluation);
    }



    public boolean alreadyEvaluatedAtCurrentCycle(EvaluationCycleEntity cycle, UserEntity evaluated) {
        return evaluationDao.alreadyEvaluatedAtCurrentCycle(cycle, evaluated);
    }






}