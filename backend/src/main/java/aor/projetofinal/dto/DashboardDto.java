import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for aggregating and transporting dashboard summary data for the frontend.
 * Includes metrics for standard users, managers, and administrators.
 */
public class DashboardDto {
    // --- User fields ---
    private int activeTrainings;               // Number of active trainings for the user
    private int pendingEvaluations;            // Number of evaluations the user must complete (as evaluated)
    private LocalDateTime lastEvaluationDate;  // Date of last completed evaluation
    private int totalTrainingHours;            // Sum of training hours completed

    // --- Manager fields ---
    private int teamSize;                      // Number of users managed by this manager
    private int teamPendingEvaluations;        // Number of pending evaluations the manager must fill

    // --- Admin fields ---
    private int totalUsers;                    // Total users in the system
    private int totalPendingEvaluations;       // Total pending evaluations in the system


    // --- Getters and Setters ---

    // User
    public int getActiveTrainings() { return activeTrainings; }
    public void setActiveTrainings(int activeTrainings) { this.activeTrainings = activeTrainings; }

    public int getPendingEvaluations() { return pendingEvaluations; }
    public void setPendingEvaluations(int pendingEvaluations) { this.pendingEvaluations = pendingEvaluations; }

    public LocalDateTime getLastEvaluationDate() { return lastEvaluationDate; }
    public void setLastEvaluationDate(LocalDateTime lastEvaluationDate) { this.lastEvaluationDate = lastEvaluationDate; }

    public int getTotalTrainingHours() { return totalTrainingHours; }
    public void setTotalTrainingHours(int totalTrainingHours) { this.totalTrainingHours = totalTrainingHours; }

    // Manager
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }

    public int getTeamPendingEvaluations() { return teamPendingEvaluations; }
    public void setTeamPendingEvaluations(int teamPendingEvaluations) { this.teamPendingEvaluations = teamPendingEvaluations; }

    // Admin
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getTotalPendingEvaluations() { return totalPendingEvaluations; }
    public void setTotalPendingEvaluations(int totalPendingEvaluations) { this.totalPendingEvaluations = totalPendingEvaluations; }
}

