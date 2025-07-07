package aor.projetofinal.dto;

import java.io.Serializable;

/**
 * FlatEvaluationDto is a lightweight DTO to list evaluations in REST responses.
 * It avoids exposing complex entity or enum types.
 */
public class FlatEvaluationDto implements Serializable {
    private String evaluatedName;
    private String evaluatedEmail;
    private String photograph;
    private String state;          // e.g. "IN_EVALUATION"
    private String grade;          // e.g. "3" or "Contribuição conforme o esperado"
    private String evaluatorName;
    private String cycleEndDate;   // formatted: "2025-08-31 23:59"

    public FlatEvaluationDto() {}

    // Getters and setters
    public String getEvaluatedName() {
        return evaluatedName;
    }

    public void setEvaluatedName(String evaluatedName) {
        this.evaluatedName = evaluatedName;
    }

    public String getEvaluatedEmail() {
        return evaluatedEmail;
    }

    public void setEvaluatedEmail(String evaluatedEmail) {
        this.evaluatedEmail = evaluatedEmail;
    }

    public String getPhotograph() {
        return photograph;
    }

    public void setPhotograph(String photograph) {
        this.photograph = photograph;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public String getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(String cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }
}
