package aor.projetofinal.dto;

import java.io.Serializable;

/**
 * FlatEvaluationHistoryDto represents a single closed evaluation record for historical listing.
 * It includes only simple fields: cycle number, evaluation date, grade, and ID for PDF export.
 */
public class FlatEvaluationHistoryDto implements Serializable {

    private Long evaluationId;
    private int cycleNumber;
    private String evaluationDate; // formatted as "yyyy-MM-dd"
    private int grade;

    public FlatEvaluationHistoryDto() {
    }

    public FlatEvaluationHistoryDto(Long evaluationId, int cycleNumber, String evaluationDate, int grade) {
        this.evaluationId = evaluationId;
        this.cycleNumber = cycleNumber;
        this.evaluationDate = evaluationDate;
        this.grade = grade;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(int cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public String getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(String evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }
}
