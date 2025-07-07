package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dto.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Stateless
public class EvaluationBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(EvaluationBean.class);

    @Inject
    private EvaluationDao evaluationDao;

    @Inject
    private EvaluationCycleBean evaluationCycleBean;

    /**
     * Returns a paginated list of evaluations matching the provided filters,
     * converted into flat DTOs for list display.
     *
     * @param name      Partial name of evaluated user (nullable)
     * @param state     Evaluation state filter (nullable)
     * @param grade     Grade filter (nullable)
     * @param cycleEnd  Exact cycle end date filter (nullable)
     * @param requester The user making the request (used for access control)
     * @param page      The page number (1-based)
     * @return A PaginatedEvaluationsDto containing results and pagination metadata
     */
    public PaginatedEvaluationsDto findEvaluationsWithFiltersPaginated(String name,
                                                                       EvaluationStateType state,
                                                                       Integer grade,
                                                                       LocalDate cycleEnd,
                                                                       UserEntity requester,
                                                                       int page) {

        int pageSize = 10;

        // 1. Load matching evaluations (paginated)
        List<EvaluationEntity> evaluations = evaluationDao.findEvaluationsWithFiltersPaginated(
                name, state, grade, cycleEnd, requester, page, pageSize
        );

        // 2. Count total records matching filters (for pagination)
        long totalCount = evaluationDao.countEvaluationsWithFilters(
                name, state, grade, cycleEnd, requester
        );

        // 3. Convert to DTOs
        List<FlatEvaluationDto> dtoList = evaluations.stream()
                .map(JavaConversionUtil::convertEvaluationToFlatDto)
                .toList();

        // 4. Calculate total pages
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        return new PaginatedEvaluationsDto(dtoList, totalCount, totalPages, page);
    }


    /**
     * Retrieves all possible evaluation states and their human-readable labels.
     * Intended for use in dropdown filters or UI forms.
     *
     * @return List of EvaluationStateDto objects representing all enum states.
     */
    public List<EvaluationStateDto> getAllEvaluationStates() {
        logger.info("User: {} | IP: {} - Fetching available evaluation states for UI filter.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
        );

        List<EvaluationStateDto> states = new ArrayList<>();

        for (EvaluationStateType type : EvaluationStateType.values()) {
            String label = switch (type) {
                case IN_EVALUATION -> "In Evaluation";
                case EVALUATED     -> "Evaluated";
                case CLOSED        -> "Closed";
            };
            states.add(new EvaluationStateDto(type.name(), label));
        }

        logger.info("User: {} | IP: {} - Loaded {} evaluation states.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                states.size()
        );

        return states;
    }




    /**
     * Returns a paginated history of closed evaluations for the given user.
     * Evaluations are filtered by state CLOSED and cycle inactive, and ordered by date (most recent first).
     *
     * @param evaluated The user whose evaluation history is being requested.
     * @param page      The page number requested (1-based).
     * @return A PaginatedEvaluationHistoryDto containing evaluations and pagination metadata.
     */
    public PaginatedEvaluationHistoryDto getEvaluationHistory(UserEntity evaluated, int page) {
        int pageSize = 10;
        int offsetPage = (page < 1) ? 1 : page;

        List<EvaluationEntity> results = evaluationDao.findClosedByEvaluatedPaginated(evaluated, offsetPage, pageSize);
        long totalCount = evaluationDao.countClosedByEvaluated(evaluated);

        List<FlatEvaluationHistoryDto> dtos = new ArrayList<>();
        for (EvaluationEntity entity : results) {
            FlatEvaluationHistoryDto dto = JavaConversionUtil.convertToFlatHistoryDto(entity);
            if (dto != null) dtos.add(dto);
        }

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        return new PaginatedEvaluationHistoryDto(dtos, totalCount, totalPages, offsetPage);
    }




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


    public boolean revertEvaluationToInEvaluation(EvaluationEntity evaluation) {
        if (evaluation.getState() != EvaluationStateType.EVALUATED) {
            logger.warn("Cannot revert evaluation ID {} because it's not in EVALUATED state.", evaluation.getId());
            return false;
        }

        evaluation.setState(EvaluationStateType.IN_EVALUATION);
        evaluationDao.save(evaluation);

        logger.info("Evaluation ID {} reverted to IN_EVALUATION.", evaluation.getId());
        return true;
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