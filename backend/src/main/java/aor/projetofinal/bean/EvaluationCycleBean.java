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

    @Inject
    private NotificationBean notificationBean;

    private static final Logger logger = LogManager.getLogger(EvaluationCycleBean.class);

    /**
     * Retrieves the currently active evaluation cycle, if any.
     *
     * @return The active EvaluationCycleEntity, or null if none is active.
     */
    public EvaluationCycleEntity findActiveCycle() {
        EvaluationCycleEntity cycle = evaluationCycleDao.findActiveCycle();

        if (cycle == null) {
            logger.warn("User: {} | IP: {} - No active evaluation cycle found.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return null;
        }

        logger.info("User: {} | IP: {} - Active evaluation cycle found with ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());

        return cycle;
    }

    /**
     * Closes all evaluations marked as EVALUATED in the currently active evaluation cycle,
     * and attempts to close the cycle itself if all evaluations are successfully closed.
     * Sends notifications upon successful cycle closure.
     */
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
            notifyCycleClosure(cycle);
            logger.info("User: {} | IP: {} - Cycle ID {} has been closed and deactivated.",
                    RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());
        } else {
            logger.warn("User: {} | IP: {} - Not all evaluations are closed. Cycle ID {} remains active.",
                    RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());
        }
    }





    /**
     * Closes a single evaluation and checks if all evaluations in the associated cycle are closed.
     * If all are closed, the cycle is deactivated and its end date is set.
     * Also updates the date of all evaluations in the cycle and sends notifications.
     *
     * @param evaluation The evaluation to be closed.
     */
    public void closeEvaluationAndCheckCycle(EvaluationEntity evaluation) {
        // close the evaluation
        evaluation.setState(EvaluationStateEnum.CLOSED);
        evaluationDao.save(evaluation);

        logger.info("User: {} | IP: {} - Evaluation ID {} closed.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluation.getId());

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
            notifyCycleClosure(cycle);

            logger.info("User: {} | IP: {} - Cycle ID {} closed. All evaluations marked with date {}.",
                    RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId(), LocalDateTime.now());
        }
    }



    /**
     * Creates a new evaluation cycle with the provided end date and generates blank evaluations
     * for all users who have a manager assigned. Also sends notifications and emails to the responsible managers.
     *
     * @param endDate The end date of the new evaluation cycle (received from frontend).
     */
    public void createCycleAndCreateBlankEvaluations(LocalDate endDate) {
        //creating cycle with the end date provided by the admin from the frontend

        EvaluationCycleEntity newCycle = new EvaluationCycleEntity();
        newCycle.setStartDate(LocalDateTime.now());
        //we get a localdate from frontend, which we need to convert to LocalDateTime to store at the DB
        newCycle.setEndDate(endDate.atTime(LocalTime.MAX));
        newCycle.setActive(true);

        evaluationCycleDao.create(newCycle);

        logger.info("User: {} | IP: {} - New evaluation cycle created with end date: {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), endDate);

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

                // create a notification for the manager
                String evaluatedName = user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
                String message = String.format("A new evaluation cycle was created. You are responsible for the evaluation of %s.", evaluatedName);

                notificationBean.createNotification(
                        user.getManager().getId(),
                        "SYSTEM",
                        message
                );
            }
        }

        logger.info("User: {} | IP: {} - Created {} placeholder evaluations for the new cycle.",
                RequestContext.getAuthor(), RequestContext.getIp(), createdCount);

        // Notify managers and admins by email about the new cycle
        emailManagersAndAdminsOfNewCycle(newCycle);
    }


    /**
     * Sends notification emails to all managers, evaluated users, and administrators involved in the specified cycle.
     * Each user receives only one email to prevent duplicates.
     *
     * @param cycle The evaluation cycle that has been closed.
     */
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

        logger.info("User: {} | IP: {} - Unique emails sent to all involved (admins, managers, evaluated). Total: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), sentEmails.size());
    }







    /**
     * Sends an email notification to all managers and administrators informing them of the start of a new evaluation cycle.
     *
     * @param cycle The newly created evaluation cycle.
     */
    private void emailManagersAndAdminsOfNewCycle(EvaluationCycleEntity cycle) {
        List<UserEntity> managers = userDao.findUsersByRole("MANAGER");
        List<UserEntity> admins = userDao.findUsersByRole("ADMIN");

        List<UserEntity> recipients = new ArrayList<>();
        recipients.addAll(managers);
        recipients.addAll(admins);

        for (UserEntity user : recipients) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "Start of a New Evaluation Cycle";
                String body = String.format("""
                    Dear %s,
                    
                    A new evaluation cycle has started, with ending date by %s.
                    
                    Please, access the platform to verify and evaluate processes under your responsibility. 
                    
                    Thank you for your consideration and collaboration,
                    The board.
                    """, user.getEmail(), cycle.getEndDate().toLocalDate());

                EmailUtil.sendEmail(user.getEmail(), subject, body);
            }
        }

        logger.info("User: {} | IP: {} - Admins and managers notified by email about the opening of a new evaluation cycle.",
                RequestContext.getAuthor(), RequestContext.getIp());
    }



    /**
     * Sends system notifications to all users involved in a given evaluation cycle (evaluated users and their evaluators).
     * This should be used when a cycle is officially closed and users are allowed to view their results.
     *
     * The notification is sent:
     * - Once per user (even if they are involved in multiple evaluations)
     * - With the same generic system message
     *
     * The method assumes that the cycle entity provided already contains all evaluation references.
     *
     * @param cycle the evaluation cycle that has been closed
     */
    private void notifyCycleClosure(EvaluationCycleEntity cycle) {
        String message = "Evaluation cycle is over, you can now check results";

        // Avoid duplicate notifications for users who appear in multiple evaluations
        Set<Integer> notifiedUserIds = new HashSet<>();

        for (EvaluationEntity evaluation : cycle.getEvaluations()) {
            UserEntity evaluated = evaluation.getEvaluated();
            UserEntity evaluator = evaluation.getEvaluator();

            if (evaluated != null && notifiedUserIds.add(evaluated.getId())) {
                notificationBean.createNotification(
                        evaluated.getId(),
                        "SYSTEM",
                        message
                );
            }

            if (evaluator != null && notifiedUserIds.add(evaluator.getId())) {
                notificationBean.createNotification(
                        evaluator.getId(),
                        "SYSTEM",
                        message
                );
            }
        }

        logger.info("User: {} | IP: {} - Notifications created for all users in cycle ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), cycle.getId());
    }





}
