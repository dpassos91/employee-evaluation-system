package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.EvaluationDao;
import aor.projetofinal.dto.*;
import aor.projetofinal.entity.EvaluationCycleEntity;
import aor.projetofinal.entity.EvaluationEntity;
import aor.projetofinal.entity.UserEntity;
import aor.projetofinal.entity.enums.EvaluationStateEnum;
import aor.projetofinal.entity.enums.GradeEvaluationEnum;
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
     * Checks whether all evaluations in the active cycle are in EVALUATED state.
     *
     * @return true if all evaluations are EVALUATED, false otherwise.
     */
    public boolean areAllEvaluationsInActiveCycleEvaluated() {
        EvaluationCycleEntity activeCycle = evaluationCycleBean.findActiveCycle();
        if (activeCycle == null) {
            logger.warn("User: {} | IP: {} - No active cycle found during evaluation state check.",
                    RequestContext.getAuthor(), RequestContext.getIp());
            return false;
        }

        List<EvaluationEntity> evaluations = evaluationDao.findAllEvaluationsByCycle(activeCycle);
        boolean allEvaluated = evaluations.stream()
                .allMatch(e -> e.getState() == EvaluationStateEnum.EVALUATED);

        logger.info("User: {} | IP: {} - All evaluations in cycle ID {} evaluated: {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), activeCycle.getId(), allEvaluated);

        return allEvaluated;
    }


    /**
     * Builds an UpdateEvaluationDto object from a given EvaluationEntity.
     * This DTO includes all relevant data (evaluated user, evaluator, grade, feedback, photo, etc.)
     * that should be sent to the frontend when loading an evaluation.
     *
     * @param evaluation The evaluation entity to convert.
     * @return A pre-filled UpdateEvaluationDto with evaluation data.
     */
    public UpdateEvaluationDto buildEvaluationDtoCorrespondingToTheEvaluation(EvaluationEntity evaluation) {
        logger.info("User: {} | IP: {} - Building UpdateEvaluationDto from evaluation ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluation.getId());

        UpdateEvaluationDto dto = new UpdateEvaluationDto();

        // Evaluated user info
        UserEntity evaluated = evaluation.getEvaluated();
        if (evaluated != null) {
            dto.setEvaluatedEmail(evaluated.getEmail());

            if (evaluated.getProfile() != null) {
                dto.setEvaluatedName(evaluated.getProfile().getFirstName() + " " +
                        evaluated.getProfile().getLastName());
                dto.setPhotograph(evaluated.getProfile().getPhotograph());
            }
        }

        // Evaluation content
        if (evaluation.getGrade() != null) {
            dto.setGrade(evaluation.getGrade().getGrade());
        }

        if (evaluation.getFeedback() != null) {
            dto.setFeedback(evaluation.getFeedback());
        }

        // Evaluator info
        UserEntity evaluator = evaluation.getEvaluator();
        if (evaluator != null && evaluator.getProfile() != null) {
            dto.setEvaluatorEmail(evaluator.getEmail());
            dto.setEvaluatorName(evaluator.getProfile().getFirstName() + " " +
                    evaluator.getProfile().getLastName());
        }

        logger.info("User: {} | IP: {} - DTO built successfully from evaluation ID {}.",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluation.getId());

        return dto;
    }












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
                                                                       EvaluationStateEnum state,
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

        for (EvaluationStateEnum type : EvaluationStateEnum.values()) {
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
     * Retrieves paginated evaluation history for the given user, applying optional filters.
     *
     * @param evaluated The evaluated user.
     * @param page Page number.
     * @param grade Optional grade filter.
     * @param cycle Optional cycle number filter.
     * @param cycleEndDate Optional cycle end date filter in "yyyy-MM-dd" format.
     * @return Paginated DTO with evaluations matching the filters.
     */
    public PaginatedEvaluationHistoryDto getFilteredEvaluationHistory(
            UserEntity evaluated, int page, Integer grade, Integer cycle, String cycleEndDate
    ) {
        int pageSize = 10;
        int offsetPage = (page < 1) ? 1 : page;

        logger.info("User: {} | IP: {} - Filtering evaluations for user {} | Page: {} | Grade: {} | Cycle: {} | EndDate: {}",
                RequestContext.getAuthor(), RequestContext.getIp(), evaluated.getEmail(), offsetPage, grade, cycle, cycleEndDate);

        List<EvaluationEntity> results = evaluationDao.findFilteredClosedEvaluations(
                evaluated, offsetPage, pageSize, grade, cycle, cycleEndDate
        );

        long totalCount = evaluationDao.countFilteredClosedEvaluations(evaluated, grade, cycle, cycleEndDate);

        List<FlatEvaluationHistoryDto> dtos = results.stream()
                .map(JavaConversionUtil::convertToFlatHistoryDto)
                .filter(Objects::nonNull)
                .toList();

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        logger.info("User: {} | IP: {} - Found {} evaluations ({} total, {} pages).",
                RequestContext.getAuthor(), RequestContext.getIp(), dtos.size(), totalCount, totalPages);

        return new PaginatedEvaluationHistoryDto(dtos, totalCount, totalPages, offsetPage);
    }





    //method to list all evaluation options for the dropdown menu in the frontend
    public List<EvaluationOptionsDto> listEvaluationOptions() {
        List<EvaluationOptionsDto> list = new ArrayList<>();
        //GradeEvaluationType.values returns all the enum values as an array:
        for (GradeEvaluationEnum evaluation : GradeEvaluationEnum.values()) {
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
        if (evaluation.getState() != EvaluationStateEnum.EVALUATED) {
            logger.warn("Cannot revert evaluation ID {} because it's not in EVALUATED state.", evaluation.getId());
            return false;
        }

        evaluation.setState(EvaluationStateEnum.IN_EVALUATION);
        evaluationDao.save(evaluation);

        logger.info("Evaluation ID {} reverted to IN_EVALUATION.", evaluation.getId());
        return true;
    }





    public void updateEvaluationWithGradeAndFeedback(UpdateEvaluationDto updateEvaluationDto,
                                                     EvaluationEntity evaluation,
                                                     UserEntity evaluator) {


        evaluation.setGrade(GradeEvaluationEnum.getEnumfromGrade(updateEvaluationDto.getGrade()));
        evaluation.setFeedback(updateEvaluationDto.getFeedback());
        evaluation.setDate(LocalDateTime.now());
        evaluation.setEvaluator(evaluator);


        // Update the evaluation state only if feedback was provided
        if (updateEvaluationDto.getFeedback() != null && !updateEvaluationDto.getFeedback().trim().isEmpty()) {
            evaluation.setState(EvaluationStateEnum.EVALUATED);
        } else {
            evaluation.setState(EvaluationStateEnum.IN_EVALUATION);
        }

        evaluationDao.save(evaluation);

        logger.info("Evaluation updated for user {} by {}.",
                evaluation.getEvaluated().getEmail(), evaluator.getEmail());
    }


    public boolean alreadyEvaluatedAtCurrentCycle(EvaluationCycleEntity cycle, UserEntity evaluated) {
        return evaluationDao.alreadyEvaluatedAtCurrentCycle(cycle, evaluated);
    }


}