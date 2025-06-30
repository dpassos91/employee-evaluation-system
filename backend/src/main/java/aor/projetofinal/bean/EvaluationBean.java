package aor.projetofinal.bean;

import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dto.CreateEvaluationDto;
import aor.projetofinal.dto.EvaluationOptionsDto;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateType;
import aor.projetofinal.entity.enums.GradeEvaluationType;

import aor.projetofinal.service.ProfileService;
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


    public void createEvaluation(CreateEvaluationDto createEvaluationDto,
                                 EvaluationCycleEntity cycle,
                                 UserEntity evaluated,
                                 UserEntity evaluator) {


        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setGrade(GradeEvaluationType.getEnumfromGrade(createEvaluationDto.getGrade()));
        evaluation.setFeedback(createEvaluationDto.getFeedback());
        evaluation.setDate(LocalDateTime.now());
        evaluation.setState(EvaluationStateType.CONCLUIDO);
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluated(evaluated);
        evaluation.setCycle(cycle);

        evaluationDao.create(evaluation);
    }



    public boolean alreadyEvaluatedAtCurrentCycle(EvaluationCycleEntity cycle, UserEntity evaluated) {
        return evaluationDao.alreadyEvaluatedAtCurrentCycle(cycle, evaluated);
    }






}