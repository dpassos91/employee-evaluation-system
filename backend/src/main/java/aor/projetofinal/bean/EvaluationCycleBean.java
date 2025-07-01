package aor.projetofinal.bean;

import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.util.EmailUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EvaluationCycleBean implements Serializable {

    @Inject
    private EvaluationCycleDao evaluationCycleDao;

    @Inject
    private UserDao userDao;

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

        logger.info("New evaluation cycle created with end date: {}", endDate);

        emailManagersAndAdminsOfNewCycle(newCycle);


    }


    private void emailManagersAndAdminsOfNewCycle(EvaluationCycleEntity cycle) {
        List<UserEntity> managers = userDao.findUsersByRole("MANAGER");
        List<UserEntity> admins = userDao.findUsersByRole("ADMIN");

        List<UserEntity> recipients = new ArrayList<>();
        recipients.addAll(managers);
        recipients.addAll(admins);

        for (UserEntity user : recipients) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "Start of a New Evalaution Cycle";
                //Return a formatted string appplying the format method from String class, %s for any type of data
                String body = String.format("""
                    Dear Mr./Ms./Mrs. %s,

                    A new evaluation cycle has started, with ending date by %s.

                    Plese, access the platform to verify and evaluate processes under  your responsibility. 
                   
                    Thank you for ypour consideration and collaboration,
                    The board.
                    """, user.getEmail(), cycle.getEndDate().toLocalDate());

                EmailUtil.sendEmail(user.getEmail(), subject, body);
            }
        }

        logger.info("Admins and managers notified by email about the openning of a new evaluation cycle.");
    }









}
