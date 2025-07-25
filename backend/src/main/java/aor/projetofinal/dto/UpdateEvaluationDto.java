package aor.projetofinal.dto;


import aor.projetofinal.entity.enums.EvaluationStateEnum;

/**
 * DTO used to update an evaluation with grade and feedback information.
 * Includes details about both the evaluated user and the evaluator.
 */
public class UpdateEvaluationDto {
    private int evaluatedId;

    private String state;

    private int grade;                // the frontend should send a number between 1 and 4
    private String feedback;         // manager's feedback on the evaluation
    private String evaluatedEmail;   // evaluated user's email
    private String evaluatedName;

    private String photograph;
    private String evaluatorName;
    private String evaluatorEmail;



    public int getEvaluatedId() {
        return evaluatedId;
    }

    public void setEvaluatedId(int evaluatedId) {
        this.evaluatedId = evaluatedId;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getEvaluatedEmail() { return evaluatedEmail; }
    public void setEvaluatedEmail(String evaluatedEmail) { this.evaluatedEmail = evaluatedEmail; }

    public String getEvaluatedName() { return evaluatedName; }
    public void setEvaluatedName(String evaluatedName) { this.evaluatedName = evaluatedName; }

    public String getPhotograph() { return photograph; }
    public void setPhotograph(String photograph) { this.photograph = photograph; }

    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }

    public String getEvaluatorEmail() { return evaluatorEmail; }
    public void setEvaluatorEmail(String evaluatorEmail) { this.evaluatorEmail = evaluatorEmail; }

}
