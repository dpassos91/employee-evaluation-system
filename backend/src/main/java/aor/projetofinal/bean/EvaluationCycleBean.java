package aor.projetofinal.bean;

import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.entity.EvaluationCycleEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

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



}
