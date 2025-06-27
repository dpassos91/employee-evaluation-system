package aor.projetofinal.bean;

import aor.projetofinal.dto.EvaluationOptionsDto;
import aor.projetofinal.entity.enums.GradeEvaluationType;

import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;

@Stateless
public class EvaluationBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(ProfileBean.class);


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



}