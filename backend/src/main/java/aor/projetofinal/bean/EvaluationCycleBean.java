package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationCycleDao;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateEnum;
import aor.projetofinal.util.EmailUtil;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class EvaluationCycleBean implements Serializable {

    @Inject
    private EvaluationCycleDao evaluationCycleDao;

    @Inject
    private UserDao userDao;

    @Inject
    private EvaluationDao evaluationDao;

    private static final Logger logger = LogManager.getLogger(EvaluationCycleBean.class);

    public EvaluationCycleEntity findActiveCycle() {
        EvaluationCycleEntity cycle = evaluationCycleDao.findActiveCycle();
        if (cycle == null) {
            return null;
        }
        return cycle;
    }

    public void bulkCloseEvaluationsAndCycle() {
        EvaluationCycleEntity cycle = findActiveCycle();
        if (cycle == null) {
            logger.warn("No active cycle found for bulk close.");
            return;
        }

        logger.info("User: {} | IP: {} - Starting bulk close of evaluations for active cycle (ID: {}).",
                RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());



        // close all evaluations in the cycle that are in EVALUATED
        List<EvaluationEntity> evaluationsToClose = evaluationDao.findEvaluatedEvaluationsInActiveCycle();

        for (EvaluationEntity e : evaluationsToClose) {
            e.setState(EvaluationStateEnum.CLOSED);
            e.setDate(LocalDateTime.now());
            evaluationDao.save(e);
        }

        logger.info("User: {} | IP: {} - {} evaluations closed successfully.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluationsToClose.size());


        // checks if every evaluation in the cycle was successfully closed
        List<EvaluationEntity> allEvaluationsInCycle = evaluationDao.findAllEvaluationsByCycle(cycle);

        boolean allClosed = true;
        for (EvaluationEntity e : allEvaluationsInCycle ) {
            if (e.getState() != EvaluationStateEnum.CLOSED) {
                allClosed = false;
                break;
            }
        }

        if (allClosed) {
            cycle.setActive(false);
            cycle.setEndDate(LocalDateTime.now());
            evaluationCycleDao.save(cycle);
            emailManagersAndEvaluatedOfCycleClosure(cycle);
            logger.info("User: {} | IP: {} - Cycle ID {} has been closed and deactivated.",
                    RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());
        } else {
            logger.warn("User: {} | IP: {} - Not all evaluations are closed. Cycle ID {} remains active.",
                    RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());
        }
    }





    public void closeEvaluationAndCheckCycle(EvaluationEntity evaluation) {
        // close the evaluation
        evaluation.setState(EvaluationStateEnum.CLOSED);
        evaluationDao.save(evaluation);
        logger.info("Evaluation ID {} closed.", evaluation.getId());

        EvaluationCycleEntity cycle = evaluation.getCycle();

        // verify whether all other evaluations in the cycle are closed
        boolean allClosed = true;
        for (EvaluationEntity e : cycle.getEvaluations()) {
            if (e.getState() != EvaluationStateEnum.CLOSED) {
                allClosed = false;
                break;
            }
        }
        // if all processes are closed, just close the cycle

        if (allClosed) {
            cycle.setActive(false);
            cycle.setEndDate(LocalDateTime.now());
            evaluationCycleDao.save(cycle);

            // set every evaluation date from that cycle to now
            for (EvaluationEntity e : cycle.getEvaluations()) {
                e.setDate(LocalDateTime.now());
                evaluationDao.save(e);
            }

            emailManagersAndEvaluatedOfCycleClosure(cycle);

            logger.info("Cycle ID {} closed. All evaluations marked with date {}.", cycle.getId(), LocalDateTime.now());
        }


    }


    public void createCycleAndCreateBlankEvaluations(LocalDate endDate) {
        //creating cycle with the end date provided by the admin from the frontend

        EvaluationCycleEntity newCycle = new EvaluationCycleEntity();
        newCycle.setStartDate(LocalDateTime.now());
        //we get a localdate from frontend, which we need to convert to LocalDateTime to store at the DB
        newCycle.setEndDate(endDate.atTime(LocalTime.MAX));
        newCycle.setActive(true);

        evaluationCycleDao.create(newCycle);

        logger.info("New evaluation cycle created with end date: {}", endDate);


        List<UserEntity> usersToEvaluate = userDao.findConfirmedUsersWithManager();

        int createdCount = 0;

        for (UserEntity user : usersToEvaluate) {
            if (user.getManager() != null) {
                EvaluationEntity evaluation = new EvaluationEntity();
                evaluation.setCycle(newCycle);
                evaluation.setEvaluated(user);
                evaluation.setEvaluator(user.getManager());
                evaluation.setState(EvaluationStateEnum.IN_EVALUATION);
                evaluation.setDate(null);
                // grade and feedback also stay at null

                evaluationDao.create(evaluation);
                createdCount++;
            }
        }

        logger.info("Created {} placeholder evaluations for the new cycle.", createdCount);


        // Notify managers and admins by email about the new cycle
        emailManagersAndAdminsOfNewCycle(newCycle);


    }


    private void emailManagersAndEvaluatedOfCycleClosure(EvaluationCycleEntity cycle) {
        Set<String> sentEmails = new HashSet<>(); // validates that any person is only emailed once

        // add emails of managers and evaluated users from the cycle
        for (EvaluationEntity evaluation : cycle.getEvaluations()) {
            if (evaluation.getEvaluator() != null && evaluation.getEvaluator().getEmail() != null) {
                sentEmails.add(evaluation.getEvaluator().getEmail());
            }
            if (evaluation.getEvaluated() != null && evaluation.getEvaluated().getEmail() != null) {
                sentEmails.add(evaluation.getEvaluated().getEmail());
            }
        }

        // add admins' emails
        List<UserEntity> admins = userDao.findUsersByRole("ADMIN");
        for (UserEntity admin : admins) {
            if (admin.getEmail() != null) {
                sentEmails.add(admin.getEmail());
            }
        }

        // send only one email per unique address
        for (String email : sentEmails) {
            if (!email.isEmpty()) {
                String subject = "Evaluation Cycle Closed – Results Available";

                String body = String.format("""
                    Dear %s,
                    
                    The evaluation cycle has been officially closed on %s.
                    
                    You may now access the platform to consult the final evaluations.
                    
                    Best regards,
                    The board.
                    """, email, LocalDateTime.now().toLocalDate());

                EmailUtil.sendEmail(email, subject, body);
            }
        }

        logger.info("Unique emails sent to all involved (admins, managers, evaluated). Total: {}", sentEmails.size());
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
                        Dear %s,
                        
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
