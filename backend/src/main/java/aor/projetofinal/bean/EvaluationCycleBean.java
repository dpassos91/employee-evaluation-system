package aor.projetofinal.bean;

import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.entity.EvaluationCycleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@ApplicationScoped
public class EvaluationCycleBean implements Serializable {

    @Inject
    private EvaluationCycleDao evaluationCycleDao;

    private static final Logger logger = LogManager.getLogger(EvaluationCycleBean.class);

    public EvaluationCycleEntity findActiveCycle() {
        EvaluationCycleEntity cycle = evaluationCycleDao.findActiveCycle();
        if (cycle == null) {
            return null;
        }
        return cycle;
    }

    public void createCycle(LocalDate endDate) {
        EvaluationCycleEntity newCycle = new EvaluationCycleEntity();
        newCycle.setStartDate(LocalDateTime.now());
        //we get a localdate from frontend, which we need to convert to LocalDateTime to store at the DB
        newCycle.setEndDate(endDate.atTime(LocalTime.MAX));
        newCycle.setActive(true);

        evaluationCycleDao.create(newCycle);
    }








}
