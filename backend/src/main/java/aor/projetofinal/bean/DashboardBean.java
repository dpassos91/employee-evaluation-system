package aor.projetofinal.bean;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import aor.projetofinal.bean.*;
import aor.projetofinal.dto.DashboardDto;
import aor.projetofinal.entity.UserEntity;

/**
 * Bean for aggregating dashboard summary data from multiple sources.
 * Fills the DashboardDto for the authenticated user.
 */
@Stateless
public class DashboardBean {

    @Inject private UserBean userBean;
    @Inject private CourseBean courseBean;
    @Inject private EvaluationBean evaluationBean;

    /**
     * Generates a summary DTO for the dashboard, adapting to the user's role.
     *
     * @param currentUser The authenticated user entity.
     * @return A populated DashboardDto with all relevant summary data.
     */
    public DashboardDto getDashboardForUser(UserEntity currentUser) {
        DashboardDto dto = new DashboardDto();

        String role = currentUser.getRole().getName().toUpperCase();

        // --- User fields ---
        dto.setActiveTrainings(courseBean.countActiveCoursesForUser(currentUser.getId()));
        dto.setPendingEvaluations(evaluationBean.countPendingEvaluationsForUser(currentUser.getId()));
        dto.setLastEvaluationDate(evaluationBean.getLastEvaluationDate(currentUser.getId()));
        dto.setTotalTrainingHours(courseBean.getTotalTrainingHoursForUser(currentUser.getId()));

        // --- Manager fields ---
        if ("MANAGER".equals(role)) {
            dto.setTeamSize(userBean.countUsersManagedBy(currentUser.getId()));
            dto.setTeamPendingEvaluations(evaluationBean.countPendingEvaluationsToFillByManager(currentUser.getId()));
        }

        // --- Admin fields ---
        if ("ADMIN".equals(role)) {
            dto.setTotalUsers(userBean.countAllUsers());
            dto.setTotalPendingEvaluations(evaluationBean.countAllPendingEvaluations());
        }

        return dto;
    }
}

